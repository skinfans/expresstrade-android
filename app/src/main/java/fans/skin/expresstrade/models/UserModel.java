package fans.skin.expresstrade.models;

import java.util.*;

import fans.skin.expresstrade.managers.event.*;

public class UserModel {
    public User user;
    public User user_data;

    public static class User {
        public Long id;
        public Long uid;
        public Object steam_id;
        public String avatar;
        public String display_name;
        public String token;
        // ***
        public Boolean twofactor_enabled;
        public Boolean allow_twofactor_code_reuse;
        public Boolean inventory_is_private;
        public Boolean auto_accept_gift_trades;
        public Boolean vcase_restricted;
        public Boolean api_key_exists;
        public Boolean verified;
        // ***
        public Long last_time;
        // ***
        public List<ItemModel.Item> items = new ArrayList<>();
    }
}