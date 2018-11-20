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

    // The offer is active, and the recipient can accept it for the exchange of goods
    public static final int STATE_ACTIVE = 2;
    // Recipient accepted the offer and exchange of items
    public static final int STATE_ACCEPTED = 3;
    // Offer expired due to inactivity
    public static final int STATE_EXPIRED = 5;
    // Sender canceled offer
    public static final int STATE_CANCELED = 6;
    // Recipient rejected offer
    public static final int STATE_DECLINED = 7;
    // One of the clauses of the sentence is no longer available, so the offer was automatically canceled.
    public static final int STATE_INVALID_ITEMS = 8;
    // The sales offer was initiated by the VCase site and it is awaiting confirmation. User keys have been deleted, but can be restored later.
    public static final int STATE_PENDING_CASE_OPEN = 9;
    // The sales offer was initiated by the VCase site, and a message appeared about the opening of an error due to internal problems. No items should be exchanged.
    public static final int STATE_EXPIRED_CASE_OPEN = 10;
    // The sales offer was initiated by the VCase site, and we were unable to create items in the blockchain, so user keys were returned.
    public static final int STATE_FAILED_CASE_OPEN = 12;

    public static int MAX_ITEMS = 100; // todo


    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private Handler handler = new Handler();

    // Accepted offers and pending completion of the transaction
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

        // Log in the database of new users of these offers

        HashMap<Long, UserModel.User> users = new HashMap<>();

        for (OfferModel.Offer offer : list) {

            // If this is the opening of the case - skip it. Only real people are interested
            if (offer.is_case_opening) continue;

            UserModel.User recipient = offer.recipient;
            UserModel.User sender = offer.sender;

            recipient.last_time = offer.time_updated;
            sender.last_time = offer.time_updated;

            // Add to user arrays
            if (!users.containsKey(recipient.uid))
                users.put(recipient.uid, recipient);

            if (!users.containsKey(sender.uid))
                users.put(sender.uid, sender);
        }

        App.logManager.debug("We bring in a DB of new users " + users.size());

        // Create users
        for(Map.Entry<Long, UserModel.User> entry : users.entrySet()) {
            Long ops_id = entry.getKey();
            UserModel.User user = entry.getValue();

            // Send to the database
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

    // Creating an offer model
    public void makeOffer(long recipient_ops_id) {
        MakeOfferModel offer = new MakeOfferModel();

        // We save our data (sender)
        offer.sender_ops_id = USER_ID;
        offer.sender_inventory = App.accountModule.inventory;

        // Specify partner ops_id
        offer.recipient_ops_id = recipient_ops_id;
        offer.recipient_inventory = App.usersModule.inventory;

        // Write an offer to create
        makeOffer = offer;
    }

    // Create offer (send already formed offer)
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

    // Get user offers
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

                // User Items
                OfferModel.Offers result = (OfferModel.Offers) response.response;
                List<OfferModel.Offer> list = result.offers;

                // Set Data
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

    // Accept Offer
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

                // If the offer was not sent
                if (response.status != 1) {
                    Toast.makeText(App.context, "Error confirm offer. " + response.status, Toast.LENGTH_SHORT).show();
                    App.eventManager.doEvent(EventType.ON_TRADE_CONFIRM_OFFER_ERROR);
                    return;
                }

                // User Items
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


    // Cancel Offer
    public void reqCancelOffer(Long offer_id) {
        NetworkQuery query = new NetworkQuery();
        query.add(Param.OFFER_ID, offer_id);

        nwManager.req(MethodType.TRADE_CANCEL_OFFER, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                ResponseModel response = (ResponseModel) object;

                // If the offer was not sent
                if (response.status != 1) {
                    Toast.makeText(App.context, "Error cancel offer. " + response.status, Toast.LENGTH_SHORT).show();
                    App.eventManager.doEvent(EventType.ON_TRADE_CANCEL_OFFER_ERROR);
                    return;
                }

                // User Items
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

    // Send offer
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

        // to give
        if (itemsToSend.size() != 0) {
            String sendArrayString = itemsToSend.toString();
            String sendList = sendArrayString.substring(1, sendArrayString.length() - 1);
            query.add(Param.ITEMS_TO_SEND, sendList);

            App.logManager.debug("sendList " + sendList + " CODE " + App.accountModule.get2FA());
        }

        // to get
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

                // If the offer has not been sent
                if (response.status != 1) {
                    Toast.makeText(App.context, "Error send offer. " + response.status, Toast.LENGTH_SHORT).show();
                    App.eventManager.doEvent(EventType.ON_TRADE_SEND_OFFER_ERROR);
                    return;
                }

                // User Items
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
