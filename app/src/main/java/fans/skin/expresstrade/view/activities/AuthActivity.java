package fans.skin.expresstrade.view.activities;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.view.*;

import static fans.skin.expresstrade.App.URL_OPS_AUTH;
import static fans.skin.expresstrade.App.accountModule;
import static fans.skin.expresstrade.App.logManager;

public class AuthActivity extends AppActivity {

    public AppFullButton button;
    public WebView webView;
    public TextView text_test;
    public ImageView imgLogoWax;
    public LinearLayout llLogos;

    public Boolean isLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        setDisplayStatusbar(false);

        App.authActivity = this;

        final String authUrl = URL_OPS_AUTH + "authorize?client_id=" + App.APP_CODE + "&response_type=code&state=1234567890&duration=permanent&mobile=0&scope=items manage_items trades identity";

        this.button =               findViewById(R.id.button);
        this.imgLogoWax =           findViewById(R.id.img_logo_wax);
        this.llLogos =              findViewById(R.id.ll_logos);

        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onClick(View v) {

                // Show loader
                button.setLoaderVisible(true);

                App.logManager.debug("START WEBVIEW");

                final Dialog dialog = App.dialogViewer.createAuthWebview(App.authActivity);

                // Dialog event
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (isLogged) return;
                        button.setLoaderVisible(false);
                    }
                });

                dialog.setCanceledOnTouchOutside(false);

                webView = dialog.findViewById(R.id.webView);
                webView.loadUrl(authUrl);

                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);

                        App.logManager.debug("onPageStarted");
                    }

                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        super.onReceivedError(view, request, error);
                        App.logManager.debug("onReceivedError");
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        logManager.debug("shouldOverrideUrlLoading");

                        view.loadUrl(url);

                        // Ok, logged in
                        if (url.contains("state") && url.contains("code")) {
                            emptyLoginView();

                            logManager.debug("CODE = " + url);

                            // We get the secret
                            String[] arr = url.split("=");
                            String code = arr[arr.length - 1];
                            code = code.substring(0, code.length() - 1);

                            // We send the code to receive the access token
                            accountModule.reqGetAccessToken(code);
                            dialog.cancel();
                            isLogged = true;
                        }

                        // Mistake
                        if (url.contains("state") && url.contains("error")) {
                            emptyLoginView();
                            dialog.cancel();
                            button.setLoaderVisible(false);
                        }

                        return false;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        App.logManager.debug("onPageFinished " + url);

                        // If authorization through Steam was successful, re-open the application authorization page
                        if ((url.contains("loc=login_migrate") && url.contains("steamcommunity")) ||
                                url.contains("loc=logout"))
                            view.loadUrl(authUrl);
                    }
                });
            }
        });


        // Animations
        llLogos.animate().setStartDelay(500).setDuration((long) (500)).translationY(-50 * App.display.density).start();
        imgLogoWax.animate().setStartDelay(800).alpha(1f).setDuration((long) (500)).start();
        button.animate().setStartDelay(1150).alpha(1f).setDuration((long) (500)).translationY(-20 * App.display.density).start();
    }

    private void emptyLoginView() {
        webView.loadDataWithBaseURL("", "<HTML><BODY style=\"background:black;\"><H3>Test</H3></BODY></HTML>","text/html","utf-8","");
    }

    @Override
    public void onEvent(OnEventModel object) {
        super.onEvent(object);

        switch (object.id) {
            // ON_ACCOUNT_ACCESS_TOKEN_RECEIVED
            case 1231492:
                App.accountModule.reqTradeGetProfile();
                break;

            // ON_TRADE_GET_PROFILE_RECEIVED
            case 7138198:
                Intent intent = new Intent(App.context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

                finish();
                break;
        }
    }
}
