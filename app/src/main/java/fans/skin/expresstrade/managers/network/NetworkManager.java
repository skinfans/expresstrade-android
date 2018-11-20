package fans.skin.expresstrade.managers.network;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.event.*;
import fans.skin.expresstrade.managers.network.NetworkQuery.*;
import fans.skin.expresstrade.managers.pref.PrefManager.*;
import fans.skin.expresstrade.models.*;

import java.io.*;
import java.util.*;

public class NetworkManager {
    // =============================================================================================
    // CONSTANTS
    // =============================================================================================

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private NetworkRetrofit networkRetrofit;
    private HashMap<String, PostModel> requests;

    public boolean isConnection = true; // Is there an internet connection
    public boolean isError = false;

    public final static Integer NETWORK_REFRESH_MAX_COUNT = 3;
    public final static Integer NETWORK_REFRESH_DELAY = 2000; // ms

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private static final NetworkManager ourInstance = new NetworkManager();

    public static NetworkManager getInstance() {
        return ourInstance;
    }

    private NetworkManager() {
        networkRetrofit = new NetworkRetrofit();
        requests = new HashMap<>();
    }

    public enum PostType {
        POST_REQUEST, POST_UPLOAD, POST_SOCKET
    }

    public enum StatusCode {
        STATUS_WAITING, STATUS_200, STATUS_400, STATUS_403, STATUS_500, STATUS_NONETWORK, STATUS_UNKNOWN
    }

    public enum MethodType {
        ACCOUNT_ACCESS_TOKEN("access_token", false),
        ACCOUNT_REFRESH_TOKEN("account_token", false),
        ACCOUNT_GET_PROFILE("GetProfile", false),
        ACCOUNT_GET_INVENTORY("GetInventory", false),
        // ***
        USERS_GET_USER_INVENTORY("GetUserInventory", false),
        TRADE_GET_OFFERS("GetOffers", false),
        TRADE_SEND_OFFER("SendOffer", true),
        TRADE_CANCEL_OFFER("CancelOffer", false),
        TRADE_ACCEPT_OFFER("AcceptOffer", false),
        ACCOUNT_UPDATE_PROFILE("UpdateProfile", false),
        SF_GET_MAP("GetMap", false),
        CHECK_IN("CheckIn", true),
        SEND_LOG("SendLog", false);

        public final String name;
        public final boolean isUnique;

        MethodType(String m, boolean isUnique) {
            this.name = m;
            this.isUnique = isUnique;
        }
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public PostModel req(MethodType method, NetworkQuery query, NetworkCallback networkCallback) {
        return this.post(PostType.POST_REQUEST, method, query, networkCallback);
    }

    public PostModel upload(MethodType method, NetworkQuery query, NetworkCallback networkCallback) {
        return this.post(PostType.POST_UPLOAD, method, query, networkCallback);
    }

    private PostModel post(PostType type, MethodType method, NetworkQuery query, NetworkCallback networkCallback) {
        // Create a unique query key
        String key = method.isUnique ? method.name() : UUID.randomUUID().toString().substring(0, 8);

        App.logManager.info("Send request: " + key + " " + type + " " + method);

        if (networkCallback == null) {
            networkCallback = new NetworkCallback() {
            };
        }

        if (query != null) {
            // Specify API version when accessing
            query.add(Param.API_VERSION, App.API_VERSION);
        }

        // Specify the values of the request
        networkCallback.key = key;
        networkCallback.method = method;

        if (requests.get(key) != null) {
            App.logManager.info("The request cannot be executed again. Cancel.");
            return null;
        }

        PostModel postModel = new PostModel(type, method, query, networkCallback);
        // Add a request to the stack
        requests.put(key, postModel);

        switch (type) {
            case POST_REQUEST:
                networkRetrofit.request(method, query, networkCallback);
                break;
        }

        // If before this request all requests failed and the connection status is false - we are talking about a new connection attempt
        if (!isConnection) {
            App.eventManager.doEvent(EventManager.EventType.ON_NETWORK_CONN_NEW_ATTEMPT);
        }

        return postModel;
    }

    // Deleting a request from the stack
    public void remove(String key) {
        requests.remove(key);
        App.logManager.info("Request is deleted: " + key);
    }

    // We go around the array of requests and delete those whose status corresponds to the status variable
    public void removeFromStatus(StatusCode status) {
        try {
            for (Map.Entry<String, PostModel> entry : requests.entrySet()) {
                String key = entry.getKey();
                PostModel request = entry.getValue();

                // Choose the type you want to repeat the request
                if (request.status.equals(status) && request.count < NETWORK_REFRESH_MAX_COUNT)
                    remove(key);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            App.logManager.debug(sw.toString());
        }
    }

    // We go around the array of requests and repeat those whose status corresponds to the status variable
    public void refreshFromStatus(StatusCode status) {
        for (Map.Entry<String, PostModel> entry : requests.entrySet()) {
            String key = entry.getKey();
            PostModel request = entry.getValue();

            App.logManager.debug("To repeat after the restoration of the access key: " + key);

            // Choose the type you want to repeat the request
            if (request.status.equals(status) && request.count < NETWORK_REFRESH_MAX_COUNT)
                refreshFromKey(key, 0);
        }
    }

    // This method repeats the query by key in the case of
    public void refreshFromKey(final String key, int delay) {
        final PostModel request = requests.get(key);
        if (request == null) return;

        Timer autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                // We check the availability of the key
                if (requests.get(key) == null) {
                    App.logManager.error("Repeat canceled. Request key " + key + " does not exist.");
                    return;
                }

                switch (request.type) {
                    case POST_REQUEST:
                        if (request.query == null) return;

                        // Update the access token value in the request to the latest version.
                        // If the chance that the token could change
                        if (request.query.get(Param.ACCESS_TOKEN) != null)
                            request.query.add(Param.ACCESS_TOKEN, App.prefManager.getString(PrefKey.ACCOUNT_TOKEN_ACCESS));

                        networkRetrofit.request(request.method, request.query, request.networkCallback);
                        App.logManager.info("Repeat request: " + key + " HTTP " + request.method);
                        break;

                    case POST_UPLOAD:
                        break;
                }
            }
        }, delay);
    }

    public void setStatus(String key, StatusCode status) {
        if (requests.get(key) == null) {
            App.logManager.error("error");
        }
        requests.get(key).status = status;
    }

    // Set Internet connection activity
    public void setConnectionStatus(boolean status) {
        if (isConnection == status) return;

        App.logManager.debug("setConnectionStatus " + status);

        isConnection = status;
        App.eventManager.doEvent(isConnection ? EventManager.EventType.ON_NETWORK_CONN_ACTIVE : EventManager.EventType.ON_NETWORK_CONN_NOT_ACTIVE);

    }

    public PostModel get(String key) {
        return requests.get(key);
    }

    public int getCount() {
        return requests.size();
    }

}