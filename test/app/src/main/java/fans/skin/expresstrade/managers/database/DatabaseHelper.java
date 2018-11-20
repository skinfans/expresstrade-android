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
 * DatabaseHelper используется для управления созданием и обновлением базы данных.
 * Этот класс также обеспечивает доступ к объектам DAO (объектам доступа к данным), используемые другими классами.
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

    // Вызывается, когда база данных создается впервые. Обычно нужно вызвать createTable здесь, чтобы создать таблицы для хранения данных.
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            // Создаем новые таблицы
            for (DatabaseManager.TableName table : DatabaseManager.TableName.values())
                TableUtils.createTable(connectionSource, table.clazz);

        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can'fragmentName start database", e);
            throw new RuntimeException(e);
        }
    }

    // Вызывается, когда приложение обновляется и имеет новый (высший) номер версии. Это позволяет управлять различными данными в соответствии с новым номером версии.
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            // Удаляем старые таблицы перед созданием новых
            for (DatabaseManager.TableName table : DatabaseManager.TableName.values())
                TableUtils.dropTable(connectionSource, table.clazz, true);

            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can'fragmentName drop databases", e);
            throw new RuntimeException(e);
        }
    }

    // Закрытие соединения с БД и удаление кэшированных объектов DAO
    @Override
    public void close() {
        super.close();
        daos.clear();
    }
}

