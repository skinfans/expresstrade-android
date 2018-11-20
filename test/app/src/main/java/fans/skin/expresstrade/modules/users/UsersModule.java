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
            // Получаем объект таблицы
            Dao<UsersTable, String> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

            // Получаем все имеющиеся записи в БД
            List<UsersTable> models = usersTable.queryForAll();

            // Заносим всех пользователей в таблицу
            for(UsersTable user : models)
                users.put(user.ops_id, user);

            App.logManager.debug("Загружены пользователи из БД " + users.size());

            if (users.size() != 0)
                App.eventManager.doEvent(EventType.ON_DO_USERS_UPDATE_LIST);

        } catch (SQLException e) {
            App.logManager.error("Ошибка загрузки списка юзеров из БД");
            e.printStackTrace();
        }
    }

    // Создать пользователя в БД
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

        App.logManager.debug("Создаем пользователя в БД");

        try {
            UsersTable el = users.get(model.ops_id);
            if (el != null) {
                model.is_favorite = el.is_favorite;
                if (model.token == null)
                    model.token = el.token;


                // todo игнорим, так как нет смысла создавать по несколько раз юзера
                return;
            }

            // Получаем объект таблицы
            Dao<UsersTable, Long> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

            // Создаем запись
            usersTable.createOrUpdate(model);
            users.put(model.ops_id, model);

            App.eventManager.doEvent(EventType.ON_USERS_DATABASE_SAVED, model);
        } catch (SQLException e) {
            e.printStackTrace();
            App.logManager.error("Ошибка создания пользователя в БД");
        }
    }

    // Создать пользователя в БД
    public void createUser(ItemModel.User user) {
        UsersTable model = new UsersTable();
        model.ops_id = user.id;
        model.name = user.username;
        model.avatar = user.avatar;
        model.token = user.token;

        App.logManager.debug("Создаем пользователя в БД");

        try {
            UsersTable el = users.get(model.ops_id);
            if (el != null) {
                model.is_favorite = el.is_favorite;
                if (model.token == null)
                    model.token = el.token;

                // todo игнорим, так как нет смысла создавать по несколько раз юзера
                return;
            }

            // Получаем объект таблицы
            Dao<UsersTable, String> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

            // Создаем запись
            usersTable.createOrUpdate(model);
            users.put(model.ops_id, model);

            App.eventManager.doEvent(EventType.ON_USERS_DATABASE_SAVED, model);
        } catch (SQLException e) {
            e.printStackTrace();
            App.logManager.error("Ошибка создания пользователя в БД");
        }
    }

    // Установить состояние избранного
    public void setUserFeatureState(final Long ops_id, final boolean state) {
        try {
        new Thread(new Runnable() {
            public void run() {

                App.logManager.debug("setUserFeatureState " + ops_id + " " + state);

                try {
                    // Получаем объект таблицы
                    Dao<UsersTable, String> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

                    UsersTable user = users.get(ops_id);
                    if (user == null) return;

                    user.is_favorite = state;

                    // Создаем запись
                    usersTable.update(user);

                    // Обновляем локально
                    users.put(ops_id, user);

                } catch (SQLException e) {
                    e.printStackTrace();
                    App.logManager.error("Ошибка обновления статуса избранного пользователя в БД");
                }

            }
        }).start();
        } catch (Exception e) {
            e.printStackTrace();
            App.logManager.error("Ошибка обновления статуса избранного пользователя в БД");
        }
    }

    // Установить token трейд-юрла пользователя
    public void setTradeUrl(final Long ops_id, final String token) {
        App.logManager.debug("setTradeUrl " + ops_id + " " + token);
        try {

        new Thread(new Runnable() {
            public void run() {
                try {
                    // Получаем объект таблицы
                    Dao<UsersTable, String> usersTable = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_USERS);

                    UsersTable user = users.get(ops_id);
                    if (user == null) return;

                    user.token = token;

                    // Создаем запись
                    usersTable.update(user);

                    // Обновляем локально
                    users.put(ops_id, user);

                } catch (SQLException e) {
                    e.printStackTrace();
                    App.logManager.error("Ошибка обновления token пользовтеля в БД");
                }
            }
        }).start();
        } catch (Exception e) {
            e.printStackTrace();
            App.logManager.error("Ошибка обновления статуса избранного пользователя в БД");
        }
    }

    // =============================================================================================
    // REQUEST METHODS
    // =============================================================================================
    // Получить предметы пользователя
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
                    logManager.error("API вернуло ошибку. " + response.message);
                    Toast.makeText(App.context, "No results.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Предметы пользователя
                ItemModel.Items result = (ItemModel.Items) response.response;
                ItemModel.User user = result.user_data;
                user.token = token;

                // Создать пользователя
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

    // Получить предметы пользователя
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
                    logManager.error("API вернуло ошибку. " + response.message);
                    App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_NOT_RECEIVED, ops_id);
                    return;
                }

                // Предметы пользователя
                ItemModel.Items result = (ItemModel.Items) response.response;
                List<ItemModel.Item> items = result.items;

                // Добавить массив предметов с общий список
                if (list != null)
                    list.addAll(items);

                if (page == 1) {
                    ItemModel.User user = result.user_data;

                    // Создать пользователя
                    createUser(user);
                }

                // Если загрузили все страницы или только текущую
                if (list == null || response.current_page == null || response.current_page >= response.total_pages) {

                    // Если загрузили полностью инвентарь vgo, грузим стикеры
                    if (app_id == 1) {
                        // response.current_page
                        // Загружаем предметы дальше
                        reqGetItems(ops_id, 1, 12, list);
                        return;
                    }

                    // Если загрузили полностью стикеры, грузим котов
                    if (app_id == 12) {
                        // response.current_page
                        // Загружаем предметы дальше
                        reqGetItems(ops_id, 1, 7, list);
                        return;
                    }

                    // Устанавливаем локально массив предметов
                    if (list != null)
                        App.accountModule.setLocalItems(inventory, list);

//                    if (App.tradeModule.makeOffer != null) {
//                        // Если
//                        if (App.tradeModule.makeOffer.recipient_ops_id.equals(ops_id)) {
//                            App.tradeModule.makeOffer.recipient_inventory = inventory;
//                        }
//                    }

                    App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_RECEIVED, ops_id);
                } else {
                    App.eventManager.doEvent(EventType.ON_TRADE_GET_INVENTORY_PAGE_LOADED, ops_id);

                    // Загружаем предметы дальше
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
            App.logManager.error("Ошибка очистки таблицы БД");
            e.printStackTrace();
        }
    }
}