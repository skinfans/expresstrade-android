package fans.skin.expresstrade.modules.account;

import android.content.*;
import android.os.*;
import android.support.v7.util.*;
import android.util.Base64;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.managers.event.*;
import fans.skin.expresstrade.managers.event.EventManager.EventType;
import fans.skin.expresstrade.managers.network.*;
import fans.skin.expresstrade.managers.pref.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.view.activities.*;

import java.util.*;

import static fans.skin.expresstrade.App.APP_CODE;
import static fans.skin.expresstrade.App.logManager;
import static fans.skin.expresstrade.App.nwManager;
import static fans.skin.expresstrade.App.prefManager;
import static fans.skin.expresstrade.managers.event.EventManager.*;
import static fans.skin.expresstrade.managers.network.NetworkManager.*;
import static fans.skin.expresstrade.managers.network.NetworkQuery.*;
import static fans.skin.expresstrade.managers.pref.PrefManager.*;


public class AccountModule {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    public boolean isAuthorized = false;
    public boolean isUpdatingTokens = false;
    public boolean isDeactivated = false;


    private Handler handler = new Handler();
    private String clientSecret; // 32 length 
    public InventoryModel inventory;

    private Long twofaCode = null;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private static final AccountModule ourInstance = new AccountModule();

    public static AccountModule getInstance() {
        return ourInstance;
    }

    private AccountModule() {
        inventory = new InventoryModel();
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void initial() {
        // Set the authorization status
        setAuthorizeStatus(prefManager.getBoolean(PrefKey.ACCOUNT_ACTIVE));

        // Create a secret if there is none
        clientSecret = prefManager.getString(PrefKey.ACCOUNT_CLIENT_SECRET);
        if(clientSecret == null) {
            clientSecret = CommonUtils.getHash(32);
            prefManager.setString(PrefKey.ACCOUNT_CLIENT_SECRET, clientSecret);
        }

        Long ops_id = App.prefManager.getLong(PrefManager.PrefKey.ACCOUNT_USER_ID);
        if (ops_id != 0) App.USER_ID = ops_id;

        if (!App.eventManager.isInitialed) {
            App.eventManager.getMap();
            return;
        }

        // Install Auto Accept GIFT
        // setDefaultOptions();

        // Perform user initialization function
        App.usersModule.initial();

        // CheckIn
        reqCheckIn();
    }

    public Long get2FA() {
        return twofaCode;
    }

    public void set2FA(Long code) {
        twofaCode = code;
    }

    // Get items from all pages
    public void loadItems(InventoryModel model, Integer app_id) {
        if (!App.eventManager.isInitialed) return;

        // Request to receive our items or
        // Request for receiving other items

        if (model.ops_id == 0)
            App.accountModule.reqGetItems(1, app_id, new ArrayList<ItemModel.Item>());
        else
            App.usersModule.reqGetItems(model.ops_id, 1, app_id, new ArrayList<ItemModel.Item>());
    }

    public List<ItemModel.Item> findItems(InventoryModel model, Integer app_id) {
        return findItems(model, app_id, new ArrayList<String>());
    }

    public List<ItemModel.Item> findItems(InventoryModel inv, Integer app_id, List<String> rarityExceptions) {
        List<ItemModel.Item> list = new ArrayList<>();


        // Log all users to table
        for(Map.Entry<Long, ItemModel.Item> entry : inv.items.entrySet()) {
            ItemModel.Item item = entry.getValue();

            // Ignore key rendering
            if (item.sku == 1)
                continue;

            // If the item is not the current application
            if (!item.internal_app_id.equals(app_id))
                continue;

            String rarity = CommonUtils.getRarity(item.color);

            // If there is a margin in the exception
            if (rarityExceptions.contains(rarity))
                continue;

            list.add(item);
        }

        // Sort the array by price
        Collections.sort(list, new Comparator<ItemModel.Item>() {
            @Override
            public int compare(ItemModel.Item o1, ItemModel.Item o2) {
                return o2.suggested_price - o1.suggested_price;
            }
        });

        return list;
    }

    public void emptySelectInventory(InventoryModel inv) {
        inv.keysSelected = 0;
        inv.itemsSelected = new ArrayList<>();
    }

    // Install items locally
    public void setLocalItems(InventoryModel inv, List<ItemModel.Item> items) {
        // clear all
        inv.items.clear();
        inv.keys.clear();
        inv.keysCount = 0;
        inv.itemsCount = 0;
        inv.keyPrice = 0;
//        inv.keysSelected = 0;
//        inv.itemsSelected = new ArrayList<>();

        // Items
        for (ItemModel.Item item : items) {
            if (inv.items.containsKey(item.id) || inv.keys.containsKey(item.id))
                continue;

            // Determine the number of keys and the number of items
            if (item.sku == 1) {
                inv.keys.put(item.id, item);
                inv.keysCount++;
                inv.keyPrice = item.suggested_price;
            } else {
                inv.items.put(item.id, item);
                inv.itemsCount++;
            }
        }

        // Clear all products that are no longer there.
        clearMissingSelectedItems(inv);
    }

    // Get the total cost of selected items
    public int getCostSelectedInventory(InventoryModel inv) {
        int sum = 0;

        for (Long id : inv.itemsSelected) {
            ItemModel.Item item = inv.items.get(id);
            if (item == null) continue;

            sum += item.suggested_price;
        }

        if (inv.keysSelected != 0)
            sum += (inv.keysSelected * inv.keyPrice);

        return sum;
    }

    // Get the total value of the array of items, taking into account the prescribed number
    public int getCostItems(List<ItemModel.Item> items) {
        int[] sum = {0, 0}; // {keys, items}

        for(ItemModel.Item item : items) {
            if (item.count == null)
                item.count = 1;

            if (item.sku == 1)
                sum[0] += item.count * item.suggested_price;
            else
                sum[1] += item.suggested_price;
        }

        return sum[0] + sum[1];
    }

    // Get the number of keys
    public int getKeysCount(List<ItemModel.Item> items) {
        int count = 0;

        for(ItemModel.Item item : items)
            if (item.sku == 1)
                count += item.count;

        return count;
    }

    // Get the items count
    public int getItemsCount(List<ItemModel.Item> items) {
        int count = 0;

        for(ItemModel.Item item : items)
            if (item.sku != 1)
                count++;

        return count;
    }

    // Get key object
    public ItemModel.Item getKeyItem(HashMap<Long, ItemModel.Item> keys) {
        for(Map.Entry<Long, ItemModel.Item> entry : keys.entrySet()) {
            ItemModel.Item item = entry.getValue();
            if (item.sku != 1) continue;
            return entry.getValue();
        }
        return null;
    }

    // Get key object
    public ItemModel.Item getKeyItem(List<ItemModel.Item> items) {
        for(ItemModel.Item item : items) {
            if (item.sku != 1) continue;
            return item;
        }
        return null;
    }

    // Get the required number of id's keys
    public List<Long> getKeyIds(int count, HashMap<Long, ItemModel.Item> keys) {
        List<Long> ids = new ArrayList<>();
        int i = 0;
        for(Map.Entry<Long, ItemModel.Item> entry : keys.entrySet()) {
            if (i >= count)
                return ids;

            ItemModel.Item item = entry.getValue();
            if (item.sku != 1)
                continue;

            ids.add(item.id);
            i++;
        }
        return ids;
    }

    // Get the IDS array of all selected items + keys
    public List<Long> getTradeIds(InventoryModel inv) {
        List<Long> ids = new ArrayList<>();

        if (inv.keysSelected == 0 && inv.itemsSelected.size() == 0)
            return ids;

        if (inv.keysSelected != 0) {
            List<Long> keysIds = getKeyIds(inv.keysSelected, inv.keys);
            ids.addAll(keysIds);
        }

        if (inv.itemsSelected.size() != 0)
            ids.addAll(inv.itemsSelected);

        return ids;
    }

    public void clearMissingSelectedItems(InventoryModel inv) {
        List<Long> list = new ArrayList<>();

        // If there is an item with an ID in the inventory, then add it to the array of selected items
        for(Long id : inv.itemsSelected)
            if (inv.items.containsKey(id))
                list.add(id);

        inv.itemsSelected = list;
    }

    // Combine key objects in 1, specifying the number
    public List<ItemModel.Item> getKeysSplit(List<ItemModel.Item> items) {
        List<ItemModel.Item> result = new ArrayList<>();

        // Sort the array by price
        Collections.sort(items, new Comparator<ItemModel.Item>() {
            @Override
            public int compare(ItemModel.Item o1, ItemModel.Item o2) {
                return o2.suggested_price - o1.suggested_price;
            }
        });

        // Get the keys object
        ItemModel.Item key = getKeyItem(items);
        if (key == null) return items;

        ItemModel.Item model = new ItemModel.Item();
        model.sku = 1;
        model.count = 1;
        model.suggested_price = key.suggested_price;
        model.name = key.name;
        model.color = key.color;
        model.id = key.id;
        model.image = key.image;
        model.type = key.type;

        int keysCount = 0;

        for(ItemModel.Item item : items) {
            if (item.sku != 1) {
                result.add(item);
                continue;
            }

            // Increment And Delete Object
            keysCount++;
        }

        model.count = keysCount;
        result.add(0, model);

        return result;
    }

    // =============================================================================================
    // REQUEST METHODS
    // =============================================================================================

    // Get profile data
    public void reqTradeGetProfile() {
        NetworkQuery query = new NetworkQuery();
        query.add(Param.WITH_EXTRA, 1);

        nwManager.req(MethodType.ACCOUNT_GET_PROFILE, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                ResponseModel response = (ResponseModel) object;
                UserModel model = (UserModel) response.response;

                logManager.debug("TEST GET PROFILE RESULT: " +  model.user.display_name);

                App.prefManager.setLong(PrefKey.ACCOUNT_USER_ID, model.user.id);
                App.prefManager.setString(PrefKey.ACCOUNT_DISPLAY_NAME, model.user.display_name);
                App.prefManager.setBoolean(PrefKey.ACCOUNT_AUTO_ACCEPT_GIFT, model.user.auto_accept_gift_trades);

                try {
                    App.prefManager.setLong(PrefKey.ACCOUNT_STEAM_ID, Long.parseLong((String) model.user.steam_id));
                } catch(NumberFormatException ex) {
                    App.logManager.error("No steamID");
                }

                App.eventManager.doEvent(EventType.ON_TRADE_GET_PROFILE_RECEIVED);

                // Write user_id to the global variable for quick access.
                App.USER_ID = model.user.id;
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);
                App.eventManager.doEvent(EventType.ON_TRADE_GET_PROFILE_NOT_RECEIVED);
            }
        });
    }

    // Get user items
    public void reqGetItems(final Integer page, final Integer app_id, final List<ItemModel.Item> list) {
        App.logManager.debug("reqGetItems");

        NetworkQuery query;
        query = new NetworkQuery();
        query.add(Param.APP_ID, app_id);
        query.add(Param.PAGE, page);
        query.add(Param.PER_PAGE, 500);
        query.add(Param.SORT, 6);

        App.logManager.debug("access_token " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_TOKEN_ACCESS));

        nwManager.req(MethodType.ACCOUNT_GET_INVENTORY, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                ResponseModel response = (ResponseModel) object;

                if (response.status != 1) {
                    logManager.error("API returned error. " + response.message);
                    return;
                }

                // User items
                ItemModel.Items result = (ItemModel.Items) response.response;
                List<ItemModel.Item> items = result.items;

                // Add an array of items with a common list
                list.addAll(items);

                // If you have downloaded all the pages or only the current
                if (response.current_page == null || response.current_page >= response.total_pages) {

                    // If you have downloaded a full vgo inventory, we will ship stickers
                    if (app_id == 1) {
                        reqGetItems(1, 12, list);
                        return;
                    }

                    // If you downloaded the stickers completely, we ship the cats
                    if (app_id == 12) {
                        reqGetItems(1, 7, list);
                        return;
                    }

                    // Set up a local array of objects
                    setLocalItems(inventory, list);

                    App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_RECEIVED, 0L);
                    App.eventManager.doEvent(EventType.ON_DO_PAGING_ENABLED);
                } else {
                    App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_PAGE_LOADED, 0L);

                    reqGetItems(page + 1, app_id, list);
                }
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);

                App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_NOT_RECEIVED, 0);
            }
        });
    }

    // =============================================================================================
    // AUTH METHODS
    // =============================================================================================

    // Get access token
    public void reqGetAccessToken(String clientCode) {
        clientSecret = prefManager.getString(PrefKey.ACCOUNT_CLIENT_SECRET);

        byte[] bytes = (APP_CODE + ":" + clientSecret).getBytes();
        String token = Base64.encodeToString(bytes, Base64.NO_WRAP);

        prefManager.setString(PrefKey.ACCOUNT_CLIENT_BASIC_TOKEN, token);

        NetworkQuery query = new NetworkQuery();
        query.add(Param.AUTH_GRAND_TYPE, "authorization_code");
        query.add(Param.AUTH_CLIENT_CODE, clientCode);

        nwManager.req(MethodType.ACCOUNT_ACCESS_TOKEN, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                AuthorizeModel model = (AuthorizeModel) object;

                setTokens(model.access_token, model.refresh_token);

                setAuthorizeStatus(true);

                App.eventManager.doEvent(EventType.ON_ACCOUNT_ACCESS_TOKEN_RECEIVED);
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);
                App.eventManager.doEvent(EventType.ON_ACCOUNT_ACCESS_TOKEN_NOT_RECEIVED);
            }
        });
    }

    // Get a new access token based on a reload token
    public void reqGetRefreshToken() {
        String refreshToken = prefManager.getString(PrefKey.ACCOUNT_TOKEN_REFRESH);

        NetworkQuery query = new NetworkQuery();
        query.add(Param.AUTH_GRAND_TYPE, "refresh_token");
        query.add(Param.REFRESH_TOKEN, refreshToken);
        query.add(Param.DURATION, "permanent");

        // Tokens are updated
        isUpdatingTokens = true;



        nwManager.req(MethodType.ACCOUNT_REFRESH_TOKEN, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                AuthorizeModel model = (AuthorizeModel) object;

                // Set the token
                setTokens(model.access_token, null);

                // Repeat all requests with 403 error
                App.nwManager.refreshFromStatus(StatusCode.STATUS_403);
                App.eventManager.doEvent(EventType.ON_ACCOUNT_REFRESH_TOKEN_RECEIVED);

                isUpdatingTokens = false;
            }

            @Override
            public void on403(ErrorModel error) {
                super.on400(error);
                isUpdatingTokens = false;
                App.accountModule.setAuthorizeStatus(false);
                App.eventManager.doEvent(EventType.ON_ACCOUNT_REFRESH_TOKEN_NOT_RECEIVED);
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);
                isUpdatingTokens = false;
                App.accountModule.setAuthorizeStatus(false);
                App.eventManager.doEvent(EventType.ON_ACCOUNT_REFRESH_TOKEN_NOT_RECEIVED);
            }
        });
    }

    public void reqCheckIn() {
        NetworkQuery query = new NetworkQuery();
        query.add(Param.TOKEN, App.APP_TOKEN);
        query.add(Param.VERSION, App.BUILD_VERSION);

        nwManager.req(MethodType.CHECK_IN, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);

                CheckInModel model = (CheckInModel) object;
                CheckInModel.Notify notify = model.notify;

                if (notify != null)
                    App.dialogViewer.setNotifyDialog(App.mainActivity, notify);
            }

            @Override
            public void on403(ErrorModel error) {
                super.on400(error);
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);

            }
        });
    }


    // =============================================================================================
    // OTHER METHODS
    // =============================================================================================

    public void setTokens(String access_token, String refresh_token) {
        if (access_token != null)
            App.prefManager.setString(PrefKey.ACCOUNT_TOKEN_ACCESS, access_token);
        if (refresh_token != null)
            App.prefManager.setString(PrefKey.ACCOUNT_TOKEN_REFRESH, refresh_token);

        App.logManager.info("Tokens installed: AT:" + access_token + " RT:" + refresh_token);
    }

    // Set profile authorization activity
    public void setAuthorizeStatus(boolean status) {
        if (isAuthorized == status) return;

        App.logManager.debug("setAuthorizeStatus");

        isAuthorized = status;
        prefManager.setBoolean(PrefKey.ACCOUNT_ACTIVE, status);
        App.eventManager.doEvent(status ? EventType.ON_ACCOUNT_ACTIVE : EventType.ON_ACCOUNT_NOT_ACTIVE);

        // clear the data
        if (status) {
            App.prefManager.setBoolean(PrefKey.ACCOUNT_ACTIVE, true);
        } else {
            clearAccountInfo();

            // Open authorization if at least one activity is active.
            if ((App.mainActivity != null && App.mainActivity.isActive)) {

                Intent intent = new Intent(App.context, StartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                App.context.startActivity(intent);
            }

            // Close all activations
            if (App.mainActivity != null)
                App.mainActivity.finish();
        }
    }

    private void clearAccountInfo() {
        prefManager.remove(PrefKey.ACCOUNT_TOKEN_ACCESS);
        prefManager.remove(PrefKey.ACCOUNT_TOKEN_REFRESH);

        // Clear the tables
//        App.usersModule.emptyTables();
    }
}
