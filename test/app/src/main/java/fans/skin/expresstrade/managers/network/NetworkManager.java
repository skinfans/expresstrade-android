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

    public boolean isConnection = true; // Есть ли подключение к интернету
    public boolean isError = false; // Есть ли подключение к интернету

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
        // Создаем уникальный ключ запроса
        String key = method.isUnique ? method.name() : UUID.randomUUID().toString().substring(0, 8);

        App.logManager.info("Отправляем запрос: " + key + " " + type + " " + method);

        if (networkCallback == null) {
            networkCallback = new NetworkCallback() {
            };
        }

        if (query != null) {
            // Указываем версию API при обращении
            query.add(Param.API_VERSION, App.API_VERSION);
        }

        // Указываем значения запроса
        networkCallback.key = key;
        networkCallback.method = method;

        if (requests.get(key) != null) {
            App.logManager.info("Запрос не может быть выполнен повторно. Отмена.");
            return null;
        }

        PostModel postModel = new PostModel(type, method, query, networkCallback);
        // Добавляем запрос в стек
        requests.put(key, postModel);

        switch (type) {
            case POST_REQUEST:
                networkRetrofit.request(method, query, networkCallback);
                break;
        }

        // Если перед этим запросом все запросы провалились и статус соединения false - говорим о новой попытке соединения
        if (!isConnection) {
            App.eventManager.doEvent(EventManager.EventType.ON_NETWORK_CONN_NEW_ATTEMPT);
        }

        return postModel;
    }

    // Удаление запроса из стека
    public void remove(String key) {
        requests.remove(key);
        App.logManager.info("Запрос удален: " + key);
    }

    // Обходим массив запросов и удаляем те, статус которых соответствует переменной status
    public void removeFromStatus(StatusCode status) {
        try {
            for (Map.Entry<String, PostModel> entry : requests.entrySet()) {
                String key = entry.getKey();
                PostModel request = entry.getValue();

                // Выбираем нужный тип для повтора запроса
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

    // Обходим массив запросов и повторяем те, статус которых соответствует переменной status
    public void refreshFromStatus(StatusCode status) {
        for (Map.Entry<String, PostModel> entry : requests.entrySet()) {
            String key = entry.getKey();
            PostModel request = entry.getValue();

            App.logManager.debug("На повтор после восстановления ключа доступа: " + key);

            // Выбираем нужный тип для повтора запроса
            if (request.status.equals(status) && request.count < NETWORK_REFRESH_MAX_COUNT)
                refreshFromKey(key, 0);
        }
    }

    // Данный метод повторяет запрос по ключу в случае наличия
    public void refreshFromKey(final String key, int delay) {
        final PostModel request = requests.get(key);
        if (request == null) return;

        Timer autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                // Проверяем наличие ключа
                if (requests.get(key) == null) {
                    App.logManager.error("Повтор отменен. Ключ запроса " + key + " не существут.");
                    return;
                }

                switch (request.type) {
                    case POST_REQUEST:
                        if (request.query == null) return;

                        // Обновляем значение access_token в запросе до последней версии.
                        // Если шанс, что токен мог измениться
                        if (request.query.get(Param.ACCESS_TOKEN) != null)
                            request.query.add(Param.ACCESS_TOKEN, App.prefManager.getString(PrefKey.ACCOUNT_TOKEN_ACCESS));

                        networkRetrofit.request(request.method, request.query, request.networkCallback);
                        App.logManager.info("Повторяем запрос: " + key + " HTTP " + request.method);
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

    // Установить активность интернет-соединения
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