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

    // Запрос выполнился. Пришел успешный ответ.
    public void on200(Object object) {
        PostModel postModel = App.nwManager.get(key);
        if (postModel == null) return;

        App.logManager.debug("Получен ответ: 200. " + key + " Method: " + method);

        App.nwManager.setConnectionStatus(true);
        App.nwManager.isError = false;

        // Ответ получен. Удаляем запрос из стека.
        App.nwManager.remove(key);
    }

    // Запрос выполнился. Пришел ответ об ошибке формата запроса.
    public void on400(ErrorModel error) {
        PostModel postModel = App.nwManager.get(key);
        if (postModel == null) return;

        App.logManager.debug("Получен ответ: 400. Key: " + key + " Method: " + method + " Message: " + error.message + " " + error.error_msg);

        App.nwManager.setConnectionStatus(true);
        App.nwManager.isError = false;

        // Ответ получен. Удаляем запрос из стека.
        App.nwManager.remove(key);
    }

    // Запрос выполнился. Пришел ответ об ошибке авторизации пользователя.
    public void on403(ErrorModel error) {
        /**
         * Для методов, которые требуют обязательный access_token, переопределять этот метод (401)
         * и отправлять соответствующее событие
         */

        PostModel postModel = App.nwManager.get(key);
        if (postModel == null)
            return;

        App.logManager.debug("Получен ответ: 401 " + key + " Method: " + method + " " + error.error);

        App.nwManager.setConnectionStatus(true);
        App.nwManager.isError = false;

        // Выполняем стандартную процедуру восстановления токена для этой ситуации.

        // Устанавливаем статус выполнения запроса
        App.nwManager.setStatus(key, StatusCode.STATUS_403);

        String access_token = App.prefManager.getString(PrefKey.ACCOUNT_TOKEN_ACCESS);
        String refresh_token = App.prefManager.getString(PrefKey.ACCOUNT_TOKEN_REFRESH);

        // Останавливаем выполнение метода, если на данный момент происходит обновление токенов
        if (access_token != null && App.accountModule.isUpdatingTokens) {
            App.logManager.info("Ошибка повторной попытки запроса на обновление ключей. На данный момент уже происхоит обновление токенов");
            return;
        }

        if (refresh_token == null) {
            App.nwManager.remove(key);
            App.accountModule.setAuthorizeStatus(false);
        } else {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    App.logManager.debug("Нужно получить новый токен доступа");

                    // Перезагрузить токены доступа
                    App.accountModule.reqGetRefreshToken();
                }
            }, 1500);
        }
    }

    // Запрос не выполнился. Ошибка сервера.
    public void onError(StatusCode status) { // 500, 501, 502, 503, NE, OTHER
        PostModel postModel = App.nwManager.get(key);
        if (postModel == null)
            return;

        App.logManager.debug("После всех мучительных повторений, вызываем коллбек " + status.name());

    }

    // 500, 501, 502, 503, NE, OTHER
    public void onFailure(StatusCode status) { // служебный метод. не оверрайдить. Использовать onError
        // Метод отвечает за перезагрузку запроса

        App.nwManager.isError = false;

        App.logManager.debug("Получен ответ: " + status + " " + key + " Method: " + method/* + " Key: " + str*/);

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

        // Превышено максимальное количество повторов запроса
        if (!request.getReload() || request.count > NetworkManager.NETWORK_REFRESH_MAX_COUNT) {
            request.networkCallback.onError(request.status);
            App.nwManager.remove(key);
            if (App.nwManager.getCount() == 0) {
                App.nwManager.isError = true;
                App.eventManager.doEvent(EventType.ON_NETWORK_CONN_ERROR);
            }
            return;
        }

        // Оправляем на повтор
        App.nwManager.refreshFromKey(key, NetworkManager.NETWORK_REFRESH_DELAY * App.nwManager.get(key).count);
    }

    // Только для upload
    public void onProgressUpdate(int percent) {
    }
}