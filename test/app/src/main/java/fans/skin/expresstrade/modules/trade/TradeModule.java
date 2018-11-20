package fans.skin.expresstrade.modules.trade;

import android.os.*;
import android.widget.*;

import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.managers.event.EventManager.*;
import fans.skin.expresstrade.managers.network.*;
import fans.skin.expresstrade.models.*;

import static fans.skin.expresstrade.App.*;
import static fans.skin.expresstrade.managers.network.NetworkManager.*;
import static fans.skin.expresstrade.managers.network.NetworkQuery.*;


public class TradeModule {
    // =============================================================================================
    // CONSTANTS
    // =============================================================================================

    // Предложение активно, и получатель может принять его для обмена товарами
    public static final int STATE_ACTIVE = 2;
    // Получатель принял предложение и обмен предметами
    public static final int STATE_ACCEPTED = 3;
    // Предложение истекло по причине неактивности
    public static final int STATE_EXPIRED = 5;
    // Отправитель отменил предложение
    public static final int STATE_CANCELED = 6;
    // Получатель отклонил предложение
    public static final int STATE_DECLINED = 7;
    // Один из пунктов предложения больше недоступен, поэтому предложение было отменено автоматически
    public static final int STATE_INVALID_ITEMS = 8;
    // Торговое предложение было инициировано сайтом VCase, и оно ожидает подтверждения. Ключи пользователя были удалены, но позже могут быть восстановлены.
    public static final int STATE_PENDING_CASE_OPEN = 9;
    // Торговое предложение было инициировано сайтом VCase, и возникло сообщение об открытии ошибки из-за внутренних проблем. Никакие предметы не должны быть обменены.
    public static final int STATE_EXPIRED_CASE_OPEN = 10;
    // Торговое предложение было инициировано сайтом VCase, и нам не удалось создать элементы в блок-цепочке, поэтому ключи пользователя были возвращены.
    public static final int STATE_FAILED_CASE_OPEN = 12;

    public static int MAX_ITEMS = 100; // todo


    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private Handler handler = new Handler();

    // Принятые офферы и в ожидании завершения сделки
    public List<OfferModel.Offer> offers = new ArrayList<>();

    public MakeOfferModel makeOffer;


    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private static final TradeModule ourInstance = new TradeModule();

    public static TradeModule getInstance() {
        return ourInstance;
    }

    private TradeModule() {
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void initial() {

    }

    private void setData (List<OfferModel.Offer> list) {
        offers = list;

        // Заносим в БД новых пользователей этих офферов

        HashMap<Long, UserModel.User> users = new HashMap<>();

        for (OfferModel.Offer offer : list) {

            // Если это открытие дела - пропускаем. Интересуют только реальные люди
            if (offer.is_case_opening) continue;

            UserModel.User recipient = offer.recipient;
            UserModel.User sender = offer.sender;

            recipient.last_time = offer.time_updated;
            sender.last_time = offer.time_updated;

            // Добавляем в массивы пользователей
            if (!users.containsKey(recipient.uid))
                users.put(recipient.uid, recipient);

            if (!users.containsKey(sender.uid))
                users.put(sender.uid, sender);
        }

        App.logManager.debug("Заносим в БД новых пользователей " + users.size());

        // Создаем пользователей
        for(Map.Entry<Long, UserModel.User> entry : users.entrySet()) {
            Long ops_id = entry.getKey();
            UserModel.User user = entry.getValue();

            // Отправляем в БД
            App.usersModule.createUser(user);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                App.eventManager.doEvent(EventType.ON_DO_USERS_LOAD_LIST);
            }
        }, 500);
    }

    public int getActiveCount(List<OfferModel.Offer> offers) {
        int count = 0;

        for(OfferModel.Offer offer : offers)
            if (offer.state == STATE_ACTIVE)
                count++;

        return count;
    }

    // Создание модели оффера
    public void makeOffer(long recipient_ops_id) {
        MakeOfferModel offer = new MakeOfferModel();

        // Сохраняем наши данные (отправителя)
        offer.sender_ops_id = USER_ID;
        offer.sender_inventory = App.accountModule.inventory;

        // Указываем ops_id партнера
        offer.recipient_ops_id = recipient_ops_id;
        offer.recipient_inventory = App.usersModule.inventory;

        // Записываем оффер для создания
        makeOffer = offer;
    }

    // Создать оффер (отправить уже сформированных оффер)
    public void sendOffer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                App.eventManager.doEvent(EventType.ON_TRADE_SEND_OFFER_CODE_NOT_VALID);
            }
        }, 2000);
    }

    // =============================================================================================
    // REQUEST METHODS
    // =============================================================================================

    public void reqGetOffers(Integer code) {
        if (!App.eventManager.isInitialed) return;
        reqGetOffers(null, code);
    }

    // Получить офферы пользователя
    public void reqGetOffers(String sort, final Integer code) {
        if (!App.eventManager.isInitialed) return;

        NetworkQuery query = new NetworkQuery();
        query.add(Param.APP_ID, 1);
        query.add(Param.PAGE, 1);
        query.add(Param.PER_PAGE, 100);
        query.add(Param.SORT, "modified");

        if (sort != null) query.add(Param.STATE, sort);

        nwManager.req(MethodType.TRADE_GET_OFFERS, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                ResponseModel response = (ResponseModel) object;

                // Предметы пользователя
                OfferModel.Offers result = (OfferModel.Offers) response.response;
                List<OfferModel.Offer> list = result.offers;

                // Установить данные
                setData(list);

                App.eventManager.doEvent(EventType.ON_TRADE_GET_OFFERS_RECEIVED, code);
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);

                App.eventManager.doEvent(EventType.ON_TRADE_GET_OFFERS_NOT_RECEIVED, code);
            }
        });
    }

    // Принять оффер
    public void reqAcceptOffer(Long offer_id, boolean is2FA) {
        NetworkQuery query = new NetworkQuery();
        query.add(Param.OFFER_ID, offer_id);

        if (is2FA)
            query.add(Param.TWOFACTOR_CODE, App.accountModule.get2FA());

        nwManager.req(MethodType.TRADE_ACCEPT_OFFER, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                ResponseModel response = (ResponseModel) object;

                // Если оффер не был отправлен
                if (response.status != 1) {
                    Toast.makeText(App.context, "Error confirm offer. " + response.status, Toast.LENGTH_SHORT).show();
                    App.eventManager.doEvent(EventType.ON_TRADE_CONFIRM_OFFER_ERROR);
                    return;
                }

                // Предметы пользователя
                OfferModel.Trade result = (OfferModel.Trade) response.response;
                OfferModel.Offer offer = result.offer;

                App.eventManager.doEvent(EventType.ON_TRADE_CONFIRM_OFFER_COMPLETED);

                Toast.makeText(App.context, "Offer confirmed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);
                App.eventManager.doEvent(EventType.ON_TRADE_CONFIRM_OFFER_ERROR);

                Toast.makeText(App.context, "Error confirm offer. " + error.message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Отменить оффер
    public void reqCancelOffer(Long offer_id) {
        NetworkQuery query = new NetworkQuery();
        query.add(Param.OFFER_ID, offer_id);

        nwManager.req(MethodType.TRADE_CANCEL_OFFER, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                ResponseModel response = (ResponseModel) object;

                // Если оффер не был отправлен
                if (response.status != 1) {
                    Toast.makeText(App.context, "Error cancel offer. " + response.status, Toast.LENGTH_SHORT).show();
                    App.eventManager.doEvent(EventType.ON_TRADE_CANCEL_OFFER_ERROR);
                    return;
                }

                // Предметы пользователя
                OfferModel.Trade result = (OfferModel.Trade) response.response;
                OfferModel.Offer offer = result.offer;

                App.eventManager.doEvent(EventType.ON_TRADE_CANCEL_OFFER_COMPLETED);

                Toast.makeText(App.context, "Offer canceled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);
                App.eventManager.doEvent(EventType.ON_TRADE_CANCEL_OFFER_ERROR);

                Toast.makeText(App.context, "Error cancel offer. " + error.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Отправить оффер
    public void reqSendOffer() {
        if (makeOffer == null) {
            Toast.makeText(App.context, "Offer not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        final Long ops_id = makeOffer.recipient_ops_id;
        UsersTable user = App.usersModule.users.get(ops_id);
        if (user == null) {
            Toast.makeText(App.context, "User not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        NetworkQuery query = new NetworkQuery();
        query.add(Param.TWOFACTOR_CODE, App.accountModule.get2FA());
        query.add(Param.UID, makeOffer.recipient_ops_id);
        query.add(Param.TOKEN, user.token);
        query.add(Param.MESSAGE, "Sent from Android app ExpressTrade Skinfans.");

        List<Long> itemsToSend = App.accountModule.getTradeIds(makeOffer.sender_inventory);
        List<Long> itemsToReceive = App.accountModule.getTradeIds(makeOffer.recipient_inventory);

        // Предметы, отправляемые нами
        if (itemsToSend.size() != 0) {
            String sendArrayString = itemsToSend.toString();
            String sendList = sendArrayString.substring(1, sendArrayString.length() - 1);
            query.add(Param.ITEMS_TO_SEND, sendList);

            App.logManager.debug("sendList " + sendList + " CODE " + App.accountModule.get2FA());
        }

        // Предметы, отправляемые нам
        if (itemsToReceive.size() != 0) {
            String receiveArrayString = itemsToReceive.toString();
            String receiveList = receiveArrayString.substring(1, receiveArrayString.length() - 1);
            query.add(Param.ITEMS_TO_RECEIVE, receiveList);

            App.logManager.debug("receiveList " + receiveList + " CODE " + App.accountModule.get2FA());
        }

        nwManager.req(MethodType.TRADE_SEND_OFFER, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                ResponseModel response = (ResponseModel) object;

                // Если оффер не был отправлен
                if (response.status != 1) {
                    Toast.makeText(App.context, "Error send offer. " + response.status, Toast.LENGTH_SHORT).show();
                    App.eventManager.doEvent(EventType.ON_TRADE_SEND_OFFER_ERROR);
                    return;
                }

                // Предметы пользователя
                OfferModel.Trade result = (OfferModel.Trade) response.response;

                OfferModel.Offer offer = result.offer;

                App.logManager.debug("TEST RESULT SENT" + offer.state);

                App.eventManager.doEvent(EventType.ON_TRADE_SEND_OFFER_COMPLETED);
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);
                App.eventManager.doEvent(EventType.ON_TRADE_SEND_OFFER_CODE_NOT_VALID);

                if (error.message.contains("token")) {
                    App.eventManager.doEvent(EventType.ON_DO_USER_CHANGE_LINK, ops_id);
                    App.eventManager.doEvent(EventType.ON_TRADE_SEND_OFFER_USER_TOKEN_NOT_VALID);

                    App.usersModule.setTradeUrl(ops_id, null);
                    return;
                }

                if (error.message.contains("the same parameters")) {
                    App.eventManager.doEvent(EventType.ON_TRADE_SEND_OFFER_USER_TOKEN_NOT_VALID);
                    Toast.makeText(App.context, "This offer already exists.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(App.context, "Error send offer. Please update your inventory and try again. " + error.status, Toast.LENGTH_SHORT).show();


                /*
                {
                    "status": 1,
                    "time": 1539871753,
                    "response": {
                        "offer": {

                        }
                    }
                }
                {
                    "status": 122,
                    "time": 1539873111,
                    "message": "Invalid two-factor code."
                }

                {
                    "status": 302,
                    "time": 1539716000,
                    "message": "Offer receiver information is incorrect (uid + token or trade_url)."
                }

                {
                    "status": 122,
                    "time": 1539871724,
                    "message": "Invalid two-factor code."
                }

                {
                    "status": 312,
                    "time": 1539871898,
                    "message": "You have provided items (5243293) that do not belong to you (5552193) in items_to_send"
                }
                */
            }
        });
    }
}
