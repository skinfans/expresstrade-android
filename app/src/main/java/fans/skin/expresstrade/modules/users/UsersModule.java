package fans.skin.expresstrade.modules.users;

import android.os.*;
import android.widget.*;

import com.j256.ormlite.dao.*;
import com.j256.ormlite.stmt.*;

import java.sql.*;
import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.managers.event.EventManager.*;
import fans.skin.expresstrade.managers.network.*;
import fans.skin.expresstrade.managers.pref.*;
import fans.skin.expresstrade.models.*;

import static fans.skin.expresstrade.App.*;
import static fans.skin.expresstrade.managers.network.NetworkManager.*;
import static fans.skin.expresstrade.managers.network.NetworkQuery.*;


public class UsersModule {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private Handler handler = new Handler();

    public InventoryModel inventory;
    public HashMap<Long, UsersTable> users = new HashMap<>();

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private static final UsersModule ourInstance = new UsersModule();

    public static UsersModule getInstance() {
        return ourInstance;
    }

    private UsersModule() {
        inventory = new InventoryModel();
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void initial() {
        loadUsers();
    }

    public void loadUsers() {
        try {
            // get the table object
            Dao<UsersTable, String> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

            // get all the available records in the database
            List<UsersTable> models = usersTable.queryForAll();

            // bring all users to the table
            for(UsersTable user : models)
                users.put(user.ops_id, user);

            App.logManager.debug("Uploaded users from the database" + users.size());

            if (users.size() != 0)
                App.eventManager.doEvent(EventType.ON_DO_USERS_UPDATE_LIST);

        } catch (SQLException e) {
            App.logManager.error("Error loading user list from DB");
            e.printStackTrace();
        }
    }

    // Create user in DB
    public void createUser(UserModel.User user) {
        UsersTable model = new UsersTable();
        model.name = user.display_name;
        model.avatar = user.avatar;
        model.token = user.token;

        if (user.last_time != null && user.last_time != 0)
            model.last_time = user.last_time;

        if (user.id != null && user.id != 0)
            model.ops_id = user.id;

        if (user.uid != null && user.uid != 0)
            model.ops_id = user.uid;

        App.logManager.debug("Create a user in the database");

        try {
            UsersTable el = users.get(model.ops_id);
            if (el != null) {
                model.is_favorite = el.is_favorite;
                if (model.token == null)
                    model.token = el.token;


                // todo ignore since there is no point in creating a user several times
                return;
            }

            // get table object
            Dao<UsersTable, Long> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

            // create stuff
            usersTable.createOrUpdate(model);
            users.put(model.ops_id, model);

            App.eventManager.doEvent(EventType.ON_USERS_DATABASE_SAVED, model);
        } catch (SQLException e) {
            e.printStackTrace();
            App.logManager.error("Error creating user in DB");
        }
    }

    // Create user in DB
    public void createUser(ItemModel.User user) {
        UsersTable model = new UsersTable();
        model.ops_id = user.id;
        model.name = user.username;
        model.avatar = user.avatar;
        model.token = user.token;

        App.logManager.debug("Create a user in the database");

        try {
            UsersTable el = users.get(model.ops_id);
            if (el != null) {
                model.is_favorite = el.is_favorite;
                if (model.token == null)
                    model.token = el.token;

                // todo ignore since there is no point in creating a user several times
                return;
            }

            // Get the table object
            Dao<UsersTable, String> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

            // Create a stuff
            usersTable.createOrUpdate(model);
            users.put(model.ops_id, model);

            App.eventManager.doEvent(EventType.ON_USERS_DATABASE_SAVED, model);
        } catch (SQLException e) {
            e.printStackTrace();
            App.logManager.error("Error creating user in DB");
        }
    }

    // Set favorite status
    public void setUserFeatureState(final Long ops_id, final boolean state) {
        try {
        new Thread(new Runnable() {
            public void run() {

                App.logManager.debug("setUserFeatureState " + ops_id + " " + state);

                try {
                    // get table object
                    Dao<UsersTable, String> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

                    UsersTable user = users.get(ops_id);
                    if (user == null) return;

                    user.is_favorite = state;

                    // create stuff
                    usersTable.update(user);

                    // local update
                    users.put(ops_id, user);

                } catch (SQLException e) {
                    e.printStackTrace();
                    App.logManager.error("Error updating the status of the selected user in the database");
                }

            }
        }).start();
        } catch (Exception e) {
            e.printStackTrace();
            App.logManager.error("Error updating the status of the selected user in the database");
        }
    }

    // set trade-url
    public void setTradeUrl(final Long ops_id, final String token) {
        App.logManager.debug("setTradeUrl " + ops_id + " " + token);
        try {

        new Thread(new Runnable() {
            public void run() {
                try {
                    // get table object
                    Dao<UsersTable, String> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

                    UsersTable user = users.get(ops_id);
                    if (user == null) return;

                    user.token = token;

                    // Create stuff
                    usersTable.update(user);

                    // local update
                    users.put(ops_id, user);

                } catch (SQLException e) {
                    e.printStackTrace();
                    App.logManager.error("Error updating user token in db");
                }
            }
        }).start();
        } catch (Exception e) {
            e.printStackTrace();
            App.logManager.error("Error updating the status of the selected user in the database");
        }
    }

    // =============================================================================================
    // REQUEST METHODS
    // =============================================================================================

    // Get user items
    public void reqSearchUser(final Long ops_id, final String token) {
        App.logManager.debug("reqSearchUser");

        NetworkQuery query = new NetworkQuery();
        query.add(Param.OPS_ID, ops_id);
        query.add(Param.APP_ID, 1);
        query.add(Param.PAGE, 1);
        query.add(Param.PER_PAGE, 1);

        nwManager.req(MethodType.USERS_GET_USER_INVENTORY, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                ResponseModel response = (ResponseModel) object;

                if (response.status != 1) {
                    logManager.error("API returned an error. " + response.message);
                    Toast.makeText(App.context, "No results.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // user items
                ItemModel.Items result = (ItemModel.Items) response.response;
                ItemModel.User user = result.user_data;
                user.token = token;

                // create user
                createUser(user);

                App.eventManager.doEvent(EventType.ON_USERS_SEARCH_USER_RECEIVED, ops_id);
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);
                Toast.makeText(App.context, "No results.", Toast.LENGTH_SHORT).show();
                App.eventManager.doEvent(EventType.ON_USERS_SEARCH_USER_NOT_RECEIVED, ops_id);
            }

            @Override
            public void on403(ErrorModel error) {
                super.on403(error);
                App.eventManager.doEvent(EventType.ON_USERS_SEARCH_USER_NOT_RECEIVED, ops_id);
            }
        });
    }

    // Get user items
    public void reqGetItems(final Long ops_id, final Integer page, final Integer app_id, final List<ItemModel.Item> list) {
        App.logManager.debug("reqGetItems");

        NetworkQuery query = new NetworkQuery();
        query.add(Param.OPS_ID, ops_id);
        query.add(Param.APP_ID, app_id);
        query.add(Param.PAGE, page);
        query.add(Param.PER_PAGE, 500);
        query.add(Param.SORT, 6);

        App.logManager.debug("access_token " + prefManager.getString(PrefManager.PrefKey.ACCOUNT_TOKEN_ACCESS));

        nwManager.req(MethodType.USERS_GET_USER_INVENTORY, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);
                ResponseModel response = (ResponseModel) object;

                if (response.status != 1) {
                    logManager.error("The API returned an error." + response.message);
                    App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_NOT_RECEIVED, ops_id);
                    return;
                }

                // User Items
                ItemModel.Items result = (ItemModel.Items) response.response;
                List<ItemModel.Item> items = result.items;

                // Add an array of items with a common list
                if (list != null)
                    list.addAll(items);

                if (page == 1) {
                    ItemModel.User user = result.user_data;

                    // create user
                    createUser(user);
                }

                // If loaded all pages or only current
                if (list == null || response.current_page == null || response.current_page >= response.total_pages) {

                    // If you have loaded the vgo inventory completely, we will load the stickers
                    if (app_id == 1) {
                        reqGetItems(ops_id, 1, 12, list);
                        return;
                    }

                    // If the stickers are fully loaded, we ship the cats
                    if (app_id == 12) {
                        reqGetItems(ops_id, 1, 7, list);
                        return;
                    }

                    // Set up a local array of objects
                    if (list != null)
                        App.accountModule.setLocalItems(inventory, list);

//                    if (App.tradeModule.makeOffer != null) {
//                        if (App.tradeModule.makeOffer.recipient_ops_id.equals(ops_id)) {
//                            App.tradeModule.makeOffer.recipient_inventory = inventory;
//                        }
//                    }

                    App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_RECEIVED, ops_id);
                } else {
                    App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_PAGE_LOADED, ops_id);

                    // Load items further
                    reqGetItems(ops_id, response.current_page + 1, app_id, list);
                }
            }

            @Override
            public void on400(ErrorModel error) {
                super.on400(error);

                if (error.message.contains("private inventory")) {
                    Toast.makeText(App.context, "User has hidden his inventory.", Toast.LENGTH_SHORT).show();
                    App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_PRIVATE_ERROR, ops_id);
                    return;
                }

                App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_NOT_RECEIVED, ops_id);
            }
        });
    }

    public void emptyTables() {
        try {
            Dao<UsersTable, String> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

            QueryBuilder<UsersTable, String> builder = usersTable.queryBuilder();
            List<UsersTable> itemsDel = builder.query();
            usersTable.delete(itemsDel);

        } catch (SQLException e) {
            App.logManager.error("Error clearing DB table");
            e.printStackTrace();
        }
    }
}