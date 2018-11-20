package fans.skin.expresstrade.managers.database.tables;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "users")
public class UsersTable {
    @DatabaseField(dataType = DataType.LONG, id = true)
    public long ops_id;

    @DatabaseField(dataType = DataType.STRING)
    public String name;

    @DatabaseField(dataType = DataType.STRING)
    public String token;

    @DatabaseField(dataType = DataType.STRING)
    public String avatar;

    @DatabaseField(dataType = DataType.BOOLEAN)
    public boolean is_favorite;

    @DatabaseField(dataType = DataType.LONG)
    public long last_time;
}
