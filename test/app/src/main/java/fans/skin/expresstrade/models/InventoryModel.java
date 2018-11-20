package fans.skin.expresstrade.models;

import java.util.*;

public class InventoryModel {
    // Type
    public Long ops_id =                                0l;

    // Data
    public HashMap<Long, ItemModel.Item> items =        new HashMap<>();
    public HashMap<Long, ItemModel.Item> keys =         new HashMap<>();
    public Integer itemsCount =                         0;
    public Integer keysCount =                          0;
    public Integer keyPrice =                           0;

    // Selection
    public List<Long> itemsSelected =                   new ArrayList<>();
    public Integer keysSelected =                       0;
}
