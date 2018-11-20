package fans.skin.expresstrade.managers.database;

import android.content.*;

import com.j256.ormlite.android.apptools.*;
import com.j256.ormlite.dao.*;
import com.j256.ormlite.table.*;
import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.utils.*;

import java.sql.*;
import java.util.*;

public class DatabaseManager {
    // =============================================================================================
    // CONSTANTS
    // =============================================================================================

    public final static boolean IDS_REPLACE = true;
    public final static boolean IDS_NOT_REPLACE = false;
    public final static boolean IDS_TO_TOP = true;
    public final static boolean IDS_TO_BOTTOM = false;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    protected static final String DATABASE_NAME = "db_expresstrade_skinfans.db";
    protected static final int DATABASE_VERSION = 144;
    private DatabaseHelper helper;
    protected HashMap<TableName, Object> daos = new HashMap<>();

    // =============================================================================================
    // ENUM
    // =============================================================================================

    public enum TableName {
        TABLE_IDS(IdsTable.class),
        TABLE_USERS(UsersTable.class);

        public Class clazz;

        TableName(Class clazz) {
            this.clazz = clazz;
        }
    }

    // Категории IDS
    public enum IdsName {
        TEST
    }

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private static DatabaseManager instance;

    private DatabaseManager() {
    }

    public DatabaseHelper getHelper() {
        return helper;
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void initial(Context context) {
        if (helper == null) helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

    public <T, B> Dao<T, B> getTable(TableName table) throws SQLException {
        if (daos.get(table) == null) daos.put(table, getHelper().getDao(table.clazz));
        return (Dao<T, B>) daos.get(table);
    }

    public void release() {
        if (helper != null) OpenHelperManager.releaseHelper();
    }

    // =============================================================================================
    // OTHER METHODS
    // =============================================================================================

    // Сохраняет список IDs
    public void putIds(IdsName idsName, List<String> list, long index, boolean isReplace, boolean isTop) {
        try {

            App.logManager.debug("Сохраняем IDS");

            Dao<IdsTable, String> idsTableDao = App.databaseManager.getTable(TableName.TABLE_IDS);
            IdsTable idsTable;

            String id = idsName.name() + (index != 0 ? ("_" + index) : "");
            List<String> idsList = new ArrayList<>(); // полученные свайны

            // Удалить ли текущую имеющуюся в базе строку из ids
            if (!isReplace) {
                idsTable = idsTableDao.queryForId(id);
                if (idsTable != null)
                    idsList = new ArrayList<>(Arrays.asList(idsTable.ids.split(" ")));
            }

            App.logManager.debug("Было " + idsList.size());

            for (String cid : list) {
                if (idsList.indexOf(cid) != -1) continue; // Исключаем повторы

                if (isTop) idsList.add(0, cid);
                else idsList.add(cid);
            }

            // Добавляем список в нужную категорию
            idsTable = new IdsTable();
            idsTable.id = id;
            idsTable.ids = StringUtils.join(idsList, " ");
            idsTableDao.createOrUpdate(idsTable);

            App.logManager.debug("Стало " + idsList.size());

            App.logManager.debug("Добавляем в категорию " + idsTable.id);
        } catch (SQLException e) {
            e.printStackTrace();
            App.logManager.error(e.getMessage());
        }
    }

    // Возвращает список ID's раздела
    public List<String> getIds(IdsName idsName, long index, int offset, int limit) {
        List<String> list = new ArrayList<>();

        try {
            Dao<IdsTable, String> idsTableDao = App.databaseManager.getTable(DatabaseManager.TableName.TABLE_IDS);
            String id = idsName.name() + (index != 0 ? ("_" + index) : "");
            IdsTable idsTable = idsTableDao.queryForId(id);
            if (idsTable == null) return list;

            List<String> slice = ArrayUtils.slice(Arrays.asList(idsTable.ids.split(" ")), offset, offset + limit);
            if (!ArrayUtils.isEmpty(slice)) list.addAll(slice);

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            App.logManager.error(e.getMessage());
            return list;
        }
    }
}
