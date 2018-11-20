package fans.skin.expresstrade.utils;

import java.util.*;

public class CommonUtils {

    // Получить рандомный hash
    public static String getHash(Integer len) {
        String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random RANDOM = new Random();

        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }

        return sb.toString();
    }

    public static String getRarity(String color) {
        switch (color.toLowerCase()) {
            case "#ffd700":
                return "gold";
            case "#4b69ff":
                return "blue";
            case "#d32ee6":
                return "pink";
            case "#eb4b4b":
                return "red";
            case "#8847ff":
                return "purple";
        }
        return "null";
    };

    public static String getFormattedAmount(int amount) {
        int cents = amount % 100;
        int dollars = (amount - cents) / 100;

        String camount;
        if (cents <= 9) {
            camount = 0 + "" + cents;
        } else {
            camount = "" + cents;
        }

        return "$" + dollars + "." + camount;
    }

    public static String getItemName(String s) {
        if (s == null || !s.contains(" | ")) return s;
        String[] title = s.split(" | ");
        return title[0];
    }

    public static String getItemDescr(String s) {
        if (s == null || !s.contains(" | ")) return s;
        String[] title = s.split(" | ");
        return title.length == 2 ? title[1] : title[0];
    }

    public static String getItemWear(String s) {
        String[] title = s.split("\\(");
        if (title.length <= 1) return s;
        String wear = title[1];
        return wear.substring(0, wear.length() -1);
    }

    public static String getAvatar(String url) {
        if (url.equals("/images/opskins-logo-avatar.png"))
            return "https://opskins.com/images/opskins-logo-avatar.png";

        if (url.indexOf("steamcommunity") != -1)
            url = url.replace(".jpg", "_full.jpg");

        return url;
    }

}
