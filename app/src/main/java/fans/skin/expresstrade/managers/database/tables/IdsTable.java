package fans.skin.expresstrade.managers.database.tables;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "ids")
public class IdsTable {
    @DatabaseField(dataType = DataType.STRING, id = true)
    public String id;

    @DatabaseField(dataType = DataType.STRING)
    public String ids;
}
