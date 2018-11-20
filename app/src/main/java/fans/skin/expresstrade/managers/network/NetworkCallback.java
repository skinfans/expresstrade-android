package fans.skin.expresstrade.managers.network;

import android.os.*;
import android.widget.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.network.NetworkManager.*;
import fans.skin.expresstrade.managers.pref.PrefManager.*;
import fans.skin.expresstrade.models.*;

import static fans.skin.expresstrade.managers.event.EventManager.*;

public abstract class NetworkCallback {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    public String key;
    public MethodType method;

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    // Request fulfilled. Came a successful response.
    public void on200(Object object) {
        PostModel postModel = App.nwManager.get(key);
        if (postModel == null) return;

        App.logManager.debug("Result: 200. " + key + " Method: " + method);

        App.nwManager.setConnectionStatus(true);
        App.nwManager.isError = false;

        // Response received. Remove the request from the stack.
        App.nwManager.remove(key);
    }

    // Request fulfilled. The answer came about the error format request.
    public void on400(ErrorModel error) {
        PostModel postModel = App.nwManager.get(key);
        if (postModel == null) return;

        App.logManager.debug("Result: 400. Key: " + key + " Method: " + method + " Message: " + error.message + " " + error.error_msg);

        App.nwManager.setConnectionStatus(true);
        App.nwManager.isError = false;

        // Answer received. Remove the request from the stack.
        App.nwManager.remove(key);
    }

    // Request fulfilled. The answer came about the user authentication error.
    public void on403(ErrorModel error) {
        /**
         * For methods that require mandatory access_token, override
         * this method (401) and send the corresponding event
         */

        PostModel postModel = App.nwManager.get(key);
        if (postModel == null)
            return;

        App.logManager.debug("Result: 401 " + key + " Method: " + method + " " + error.error);

        App.nwManager.setConnectionStatus(true);
        App.nwManager.isError = false;

        // Perform the standard token recovery procedure for this situation.
        // Set the request execution status
        App.nwManager.setStatus(key, StatusCode.STATUS_403);

        String access_token = App.prefManager.getString(PrefKey.ACCOUNT_TOKEN_ACCESS);
        String refresh_token = App.prefManager.getString(PrefKey.ACCOUNT_TOKEN_REFRESH);

        // Stop the execution of the method if tokens are currently being updated.
        if (access_token != null && App.accountModule.isUpdatingTokens) {
            App.logManager.info("Failed to retry key update request. Tokens are already updated");
            return;
        }

        if (refresh_token == null) {
            App.nwManager.remove(key);
            App.accountModule.setAuthorizeStatus(false);
        } else {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    App.logManager.debug("Need to get a new access token");

                    // Reload access tokens
                    App.accountModule.reqGetRefreshToken();
                }
            }, 1500);
        }
    }

    // The request failed. Server error.
    public void onError(StatusCode status) { // 500, 501, 502, 503, NE, OTHER
        PostModel postModel = App.nwManager.get(key);
        if (postModel == null)
            return;

        App.logManager.debug("service method. do not override. Use onError " + status.name());

    }

    // 500, 501, 502, 503, NE, OTHER
    public void onFailure(StatusCode status) { // service method. do not override. Use onError
        // The method is responsible for reloading the request.

        App.nwManager.isError = false;

        App.logManager.debug("Result: " + status + " " + key + " Method: " + method/* + " Key: " + str*/);

        switch (status) {
            case STATUS_500:
                App.nwManager.setConnectionStatus(false);
                break;
            case STATUS_NONETWORK:
                App.nwManager.setConnectionStatus(false);
                break;
        }

        PostModel request = App.nwManager.get(key);
        if (request == null) return;

        request.status = status;
        request.count++;

        // Maximum number of retries exceeded
        if (!request.getReload() || request.count > NetworkManager.NETWORK_REFRESH_MAX_COUNT) {
            request.networkCallback.onError(request.status);
            App.nwManager.remove(key);
            if (App.nwManager.getCount() == 0) {
                App.nwManager.isError = true;
                App.eventManager.doEvent(EventType.ON_NETWORK_CONN_ERROR);
            }
            return;
        }

        // Send for repeat
        App.nwManager.refreshFromKey(key, NetworkManager.NETWORK_REFRESH_DELAY * App.nwManager.get(key).count);
    }

    // only for upload
    public void onProgressUpdate(int percent) {
    }
}