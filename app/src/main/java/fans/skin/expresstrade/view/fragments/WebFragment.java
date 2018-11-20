package fans.skin.expresstrade.view.fragments; 

import android.annotation.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.event.*;
import fans.skin.expresstrade.managers.event.EventManager.*;
import fans.skin.expresstrade.view.*;

public class WebFragment extends AppFragment {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private WebView webView;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private AppFullButton bt_close;

    private boolean isComplete = false;

    private String url;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public FragmentName getFragmentName() {
        return FragmentName.WEB;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_web, container, false);
        Intent intent = App.mainActivity.getIntent();

        webView = view.findViewById(R.id.webView);
        bt_close = view.findViewById(R.id.bt_close);
        progressBar = view.findViewById(R.id.progressBar);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isComplete = true;

//                webView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                App.logManager.debug("onPageStarted");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                App.logManager.debug("onPageFinished");
                progressBar.setVisibility(View.GONE);

                if (isComplete) {
//                    webView.setVisibility(View.VISIBLE);
                } else {
//                    appCap.setCapVisible(R.drawable.img_cap_problems, R.string.profile_cap_title, R.string.cap_error_conn_descr);
//                    webView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                isComplete = false;
                App.logManager.debug("onReceivedError");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.contains("inapp")) {
                    Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browser);
                    return true;
                }

                view.loadUrl(url);
                return true;
            }
        });

        if (url != null)
            doLoadPage(url);

        bt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.mainActivity.popFragment(true);
            }
        });

        App.eventManager.doEvent(EventType.ON_DO_NAVIGATION_INVISIBLE);
        App.eventManager.doEvent(EventType.ON_DO_PAGING_DISABLED);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        App.eventManager.doEvent(EventType.ON_DO_NAVIGATION_VISIBLE);
        App.eventManager.doEvent(EventType.ON_DO_PAGING_ENABLED);
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void setUrl(String url) {
        this.url = url;
    }

    public void doLoadPage(String url) {
        webView.loadUrl(App.URL_WEB + url);
    }

    @Override
    protected boolean isPagingPages() {
        return false;
    }
}
