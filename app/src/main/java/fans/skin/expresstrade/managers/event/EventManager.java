package fans.skin.expresstrade.managers.event;


import android.app.admin.*;
import android.os.*;

import com.squareup.otto.*;

import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.network.*;
import fans.skin.expresstrade.managers.network.NetworkManager.*;
import fans.skin.expresstrade.managers.network.NetworkQuery.*;
import fans.skin.expresstrade.models.*;

public class EventManager {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private Bus bus = new Bus();
    private static HashMap<Integer, Integer> map;
    private Handler handler = new Handler();
    private int attempts = 0;

    public boolean isInitialed = false;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private static final EventManager ourInstance = new EventManager();

    public static EventManager getInstance() {
        return ourInstance;
    }

    private EventManager() {
    }


    // =============================================================================================
    // ENUM
    // =============================================================================================


    public enum ErrorCode {
        ERROR_NETWORK_REQUEST_NONETWORK,
        ERROR_NETWORK_REQUEST_500
    }

    public enum EventType {
        ON_DATABASE_PUBLISH_UPDATE(8944215),
        // ***
        ON_ACCOUNT_ACCESS_TOKEN_RECEIVED(6082292),
        ON_ACCOUNT_ACCESS_TOKEN_NOT_RECEIVED(9177472),
        ON_ACCOUNT_REFRESH_TOKEN_RECEIVED(3600276),
        ON_ACCOUNT_REFRESH_TOKEN_NOT_RECEIVED(3148604),
        ON_ACCOUNT_ACTIVE(4049398),
        ON_ACCOUNT_NOT_ACTIVE(6233775),
        ON_ACCOUNT_2FA_CODE_UPDATED(1254294),
        ON_ACCOUNT_2FA_CODE_CANCELED(7276785),
        // ***
        ON_USERS_TOKEN_UPDATED(8378250),
        ON_USERS_SEARCH_USER_RECEIVED(6785892),
        ON_USERS_SEARCH_USER_NOT_RECEIVED(1787474),
        ON_USERS_DATABASE_SAVED(9861662),
        // ***
        ON_NETWORK_CONN_NEW_ATTEMPT(4286895),
        ON_NETWORK_CONN_ACTIVE(6343796),
        ON_NETWORK_CONN_NOT_ACTIVE(3652778),
        ON_NETWORK_CONN_ERROR(8166246),
        // ***
        ON_TRADE_GET_PROFILE_RECEIVED(5553221),
        ON_TRADE_GET_PROFILE_NOT_RECEIVED(1336252),
        ON_TRADE_GET_INVENTORY_PAGE_LOADED(8675000),
        ON_TRADE_GET_INVENTORY_RECEIVED(2659369),
        ON_TRADE_GET_INVENTORY_NOT_RECEIVED(3021254),
        // ***
        ON_TRADE_GET_USER_INVENTORY_PAGE_LOADED(1186076),
        ON_TRADE_GET_USER_INVENTORY_RECEIVED(5804898),
        ON_TRADE_GET_USER_INVENTORY_NOT_RECEIVED(2814098),
        // ***
        ON_TRADE_GET_OFFERS_RECEIVED(5811784),
        ON_TRADE_GET_OFFERS_NOT_RECEIVED(5461496),
        ON_TRADE_GET_INVENTORY_PRIVATE_ERROR(7162904),
        ON_TRADE_MAKE_OFFER_2FA_ACCEPTING(1666823),
        ON_TRADE_SEND_OFFER_COMPLETED(7861526),
        ON_TRADE_SEND_OFFER_CODE_NOT_VALID(1668656),
        ON_TRADE_SEND_OFFER_ERROR(6786342),
        ON_TRADE_SEND_OFFER_USER_TOKEN_NOT_VALID(8946932),

        ON_TRADE_CANCEL_OFFER_COMPLETED(8645823),
        ON_TRADE_CANCEL_OFFER_ERROR(1715257),

        ON_TRADE_CONFIRM_OFFER_COMPLETED(7681925),
        ON_TRADE_CONFIRM_OFFER_ERROR(1547851),
        // ***
        ON_STATE_ITEMS_SELECTION_ENABLED(5475381),
        ON_STATE_ITEMS_SELECTION_DISABLED(2943754),
        ON_STATE_ITEMS_SELECT_CHANGE(2404505),
        // ***
        ON_STATE_KEYS_SELECTION_ENABLED(2002495),
        ON_STATE_KEYS_SELECTION_DISABLED(3079531),
        ON_STATE_KEYS_KEY_SELECTED(2364569),

        ON_STATE_OFFER_UNAVAILABLE(1306223),
        ON_STATE_OFFER_AVAILABLE(3063065),

        ON_EVENTS_INITIALED(7862234),
        ON_DO_USERS_LOAD_LIST(4691082),
        ON_DO_USERS_UPDATE_LIST(7676348),
        ON_DO_USER_CHANGE_LINK(1127644),
        ON_DO_2FA_ACCEPT(6164881),
        ON_DO_CREATE_OFFER(1988149),
        ON_DO_INVENTORY_RELOAD(7857194),
        ON_DO_NAVIGATION_VISIBLE(2982800),
        ON_DO_NAVIGATION_INVISIBLE(7623430),
        ON_DO_PAGING_ENABLED(5110734),
        ON_DO_PAGING_DISABLED(2447329),
        ON_DO_SELECT_FRAME(1914665),
        ON_WINDOW_FOCUS(6363611),
        ON_DO_SELECT_RECIPIENT(5768460),
        ON_DO_CLOSE_USER_INVENTORY(7165468),
        ON_DO_MAKE_OFFER_RENDER(6364961),
        ON_DO_NEW_OFFERS_INDICATOR_VISIBLE(7643782),
        ON_DO_NEW_OFFERS_INDICATOR_INVISIBLE(1166417);

        public Integer id;

        EventType(Integer m) {
            this.id = m;
        }

        public int getId() {
            return map != null ? map.get(id) : id;
        }
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void post(final Object object) {
        handler.post(new Runnable() {
            public void run() {
                bus.post(object);
            }
        });
    }

    // EVENT
    public void doEvent(EventType event_code) {
        App.logManager.info(event_code.name());
        post(new OnEventModel(event_code));
    }

    public void doEvent(EventType event_code, Object object) {
        App.logManager.info(event_code.name());
        post(new OnEventModel(event_code, object));
    }

    // ERROR
    public void doError(EventType error_code) {
        App.logManager.info(error_code.name());
        post(new OnErrorModel(error_code));
    }

    public void doError(EventType error_code, Object object) {
        App.logManager.info(error_code.name());
        post(new OnErrorModel(error_code, object));
    }

    public void getMap() {
        List<Integer> list = new ArrayList<>();
        for (EventType event : EventType.values())
            list.add(event.id);

        String join = list.toString();
        NetworkQuery query = new NetworkQuery();
        query.add(Param.TOKEN, App.APP_TOKEN);
        query.add(Param.IDS, join.substring(1, join.length() - 1));

        attempts++;

        App.nwManager.req(MethodType.SF_GET_MAP, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                ResponseModel response = (ResponseModel) object;
                MapModel model = (MapModel) response.response;

                int i = 0;
                map = new HashMap<>();
                for (EventType event : EventType.values()) {
                    map.put(event.id, model.map.get(i));
                    i++;
                }
                isInitialed = true;
                App.eventManager.doEvent(EventType.ON_EVENTS_INITIALED);
                App.accountModule.initial();
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);
            }

            @Override
            public void on403(ErrorModel error) {
                super.on400(error);
            }

            @Override
            public void onError(StatusCode status) {
                super.onError(status);

                switch (status) {
                    case STATUS_WAITING:
                    case STATUS_NONETWORK:
                        if (attempts >= 3) return;
                        getMap();
                        break;
                }
            }
        });
    }

    public void register(Object object) {
        bus.register(object);
    }

    public void unregister(Object object) {
        if (object == null) return;
        bus.unregister(object);
    }
}
