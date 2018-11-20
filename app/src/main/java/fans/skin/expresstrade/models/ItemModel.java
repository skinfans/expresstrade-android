package fans.skin.expresstrade.models;

import java.util.*;

public class ItemModel {
    public static class Item {
        public Long id;
        public Integer sku;
        public Float wear;
        public Long pattern_index;
        public String name;
        public String market_name;
        public Object image;
        public String color;
        public String type;
        public Integer internal_app_id;
        public int suggested_price;
        public int suggested_price_floor;
        public Integer wear_tier_index;
        public Long trade_hold_expires = null;
        public Integer count;
    }

    public static class User {
        public Long id;
        public Object steam_id;
        public String avatar;
        public String username;
        public String token;
    }

    public static class Items {
        public User user_data = new User();
        public List<Item> items = new ArrayList<>();
        public Integer total;
    }
}