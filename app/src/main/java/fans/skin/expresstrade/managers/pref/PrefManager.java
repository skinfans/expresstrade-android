package fans.skin.expresstrade.managers.pref;

import android.content.*;
import android.os.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.utils.*;

import java.util.*;

public class PrefManager {
    // =============================================================================================
    // CONSTANTS
    // =============================================================================================

    public final static int COUNTER_HISTORY_COUNT = 30; // 30 items in history

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    SharedPreferences pref;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private static final PrefManager ourInstance = new PrefManager();

    private PrefManager() {
    }

    public static PrefManager getInstance() {
        return ourInstance;
    }

    // =============================================================================================
    // ENUM
    // =============================================================================================

    public enum PrefKey {
        ACCOUNT_ACTIVE("acc_active", false),
        ACCOUNT_CLIENT_SECRET("acc_client_secret", false),
        ACCOUNT_CLIENT_BASIC_TOKEN("acc_client_basic_token", true),
        // ***
        ACCOUNT_TOKEN_ACCESS("acc_token_access", false),
        ACCOUNT_TOKEN_REFRESH("acc_token_refresh", false),
        // ***
        ACCOUNT_USER_ID("acc_user_id", false),
        ACCOUNT_STEAM_ID("acc_steam_id", true),
        ACCOUNT_DISPLAY_NAME("acc_display_name", true),
        ACCOUNT_AUTO_ACCEPT_GIFT("acc_auto_accept_gift", true),

        INV_FILTER_DISABLED("inv_filter_disabled", true),

        INV_APP_LAST("inv_app_id", true);


        public final String str;
        public final Boolean isAccountAlias;

        PrefKey(String s, boolean isAccountAlias) {
            this.str = s;

            // Bind to the key the string id of the current user, so that when logging in under a different profile,
            // the settings are different for different profiles
            this.isAccountAlias = isAccountAlias;
        }
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    // ***

    private void set(PrefKey code, String value) {
        String string = code.str;

        if (code.isAccountAlias) {
            long user_id = this.getLong(PrefKey.ACCOUNT_USER_ID);
            if (user_id != 0)
                string = user_id + "_" + string;
        }

        pref = App.context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        pref.edit().putString(string, value).apply();
    }

    public void setString(PrefKey code, String value) {
        this.set(code, value);
    }

    public void setInt(PrefKey code, int value) {
        this.set(code, String.valueOf(value));
    }

    public void setFloat(PrefKey code, float value) {
        this.set(code, String.valueOf(value));
    }

    public void setDouble(PrefKey code, double value) {
        this.set(code, String.valueOf(value));
    }

    public void setLong(PrefKey code, long value) {
        this.set(code, String.valueOf(value));
    }

    public void setBoolean(PrefKey code, boolean value) {
        this.set(code, Boolean.toString(value));
    }

    // ***

    private String get(PrefKey code) {
        String string = code.str;

        if (code.isAccountAlias) {
            long user_id = this.getLong(PrefKey.ACCOUNT_USER_ID);
            if (user_id != 0)
                string = user_id + "_" + string;
        }

        pref = App.context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        return pref.getString(string, "");
    }

    public String getString(PrefKey code) {
        String str = this.get(code);
        return str.equals("") ? null : str;
    }

    public Integer getInteger(PrefKey code) {
        String str = this.get(code);
        return str.equals("") ? null : Integer.parseInt(str);
    }

    public int getInt(PrefKey code) {
        String str = this.get(code);
        return str.equals("") ? 0 : Integer.parseInt(str);
    }

    public float getFloat(PrefKey code) {
        String str = this.get(code);
        return str.equals("") ? 0f : Float.parseFloat(str);
    }

    public double getDouble(PrefKey code) {
        String str = this.get(code);
        return str.equals("") ? 0d : Double.parseDouble(str);
    }

    public long getLong(PrefKey code) {
        String str = this.get(code);
        return str.equals("") ? 0L : Long.parseLong(str);
    }

    public boolean getBoolean(PrefKey code) {
        String str = this.get(code);
        return !str.equals("") && Boolean.parseBoolean(this.get(code));
    }

    // ***

    public void remove(PrefKey code) {
        String string = code.str;

        if (code.isAccountAlias) {
            long user_id = this.getLong(PrefKey.ACCOUNT_USER_ID);
            if (user_id != 0)
                string = user_id + "_" + string;
        }

        pref = App.context.getSharedPreferences("Preferences", 0);
        pref.edit().remove(string).apply();
    }

    // =============================================================================================
    // OTHER
    // =============================================================================================

    // Add time value to parameter
    public void incrementTimeStack(PrefKey prefKey) {
        String timeStack = App.prefManager.getString(prefKey);
        String[] timeStackSplit = timeStack != null ? timeStack.split(",") : new String[]{""};
        List<String> list = new ArrayList<>(Arrays.asList(timeStackSplit));
        list.add(0, System.currentTimeMillis() + "");
        if (list.size() > COUNTER_HISTORY_COUNT)
            list.remove(10);

        // Write the stack with the parameter's string.
        App.prefManager.setString(prefKey, StringUtils.join(list, ","));
    }

    public int getTimeStackCount(PrefKey prefKey) {
        String timeStack = App.prefManager.getString(prefKey);

        if (timeStack == null)
            return 0;

        String[] timeStackSplit = timeStack.split(",");
        return timeStackSplit.length;
    }

    // Get the current number of items in the history and the difference between the last and the "int count" value
    public long getTimeStackDiff(PrefKey prefKey, int index) {
        String timeStack = App.prefManager.getString(prefKey);

        if (timeStack == null)
            return 0;

        String[] timeStackSplit = timeStack.split(",");
        if (timeStackSplit.length == 0)
            return 0;

        return Long.parseLong(timeStackSplit[0]) -
                Long.parseLong(timeStackSplit[Math.min(index - 1, timeStackSplit.length - 1)]);
    }

    public void setDefaults() {
        // TODO
    }
}
