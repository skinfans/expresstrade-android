package fans.skin.expresstrade.managers.network;

import java.io.*;
import java.util.*;

public class NetworkQuery {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private HashMap<String, String> query;
    private File file;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public NetworkQuery() {
        query = new HashMap<>();
    }

    // =============================================================================================
    // ENUM
    // =============================================================================================

    public enum Fields {
        USER("test");

        public final String fields;

        Fields(String a) {
            fields = a;
        }
    }

    public enum Param {
        AUTH_GRAND_TYPE("grant_type"),
        AUTH_CLIENT_CODE("code"),

        API_VERSION("api_version"),
        ACCESS_TOKEN("access_token"),
        REFRESH_TOKEN("refresh_token"),
        WITH_EXTRA("with_extra"),
        DURATION("duration"),
        APP_ID("app_id"),
        PAGE("page"),
        PER_PAGE("per_page"),
        SEARCH("search"),
        OPS_ID("uid"),
        STATE("state"),
        SORT("sort"),
        TWOFACTOR_CODE("twofactor_code"),
        UID("uid"),
        LOG("log"),
        TOKEN("token"),
        VERSION("version"),
        OFFER_ID("offer_id"),
        ITEMS_TO_SEND("items_to_send"),
        ITEMS_TO_RECEIVE("items_to_receive"),
        MESSAGE("message"),
        AUTO_ACCEPT_GIFTS("auto_accept_gift_trades"),
        IDS("ids");

        public final String name;

        Param(String a) {
            name = a;
        }
    }

    public void add(Param param, String value) {
        query.put(param.name, value);
    }

    public void add(Param param, int value) {
        this.add(param, value + "");
    }

    public void add(Param param, long value) {
        this.add(param, value + "");
    }

    public void add(Param param, double value) {
        this.add(param, value + "");
    }

    public void add(Param param, boolean value) {
        this.add(param, value + "");
    }


    public void add(Param param, float value) {
        this.add(param, value + "");
    }

    public void add(Param param, Fields... value) {
        String fields = "";
        for (int i = 0; i < value.length; i++) {
            fields += value[i].fields;
            if (i != value.length - 1)
                fields += ",";
        }
        this.add(param, fields);
    }

    public HashMap<String, String> get() {
        return query;
    }

    public String get(Param param) {
        return query.get(param.name);
    }

    public HashMap<String, String> getQuery() {
        return query;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }
}
