package fans.skin.expresstrade.managers.database;

import android.content.*;
import android.database.sqlite.*;
import android.util.*;

import com.j256.ormlite.android.apptools.*;
import com.j256.ormlite.support.*;
import com.j256.ormlite.table.*;

import java.sql.*;
import java.util.*;

/**
 * DatabaseHelper is used to manage the creation and updating of the database.
 * This class also provides access to DAO objects (data access objects) used by other classes.
 * DAO - Data Access Objects
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    public HashMap<DatabaseManager.TableName, Object> daos = new HashMap<>();

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public DatabaseHelper(Context context) {
        super(context, DatabaseManager.DATABASE_NAME, null, DatabaseManager.DATABASE_VERSION);
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    // Called when the database is first created. Usually you need to call createTable here to create tables for storing data.
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            // Create new tables
            for (DatabaseManager.TableName table : DatabaseManager.TableName.values())
                TableUtils.createTable(connectionSource, table.clazz);

        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can'fragmentName start database", e);
            throw new RuntimeException(e);
        }
    }

    // Called when the application is updated and has a new (higher) version number. This allows you to manage different data according to the new version number.
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            // Delete old tables before creating new ones.
            for (DatabaseManager.TableName table : DatabaseManager.TableName.values())
                TableUtils.dropTable(connectionSource, table.clazz, true);

            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can'fragmentName drop databases", e);
            throw new RuntimeException(e);
        }
    }

    // Closing a database connection and deleting cached DAO objects
    @Override
    public void close() {
        super.close();
        daos.clear();
    }
}

