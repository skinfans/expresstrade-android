package fans.skin.expresstrade.utils;

import java.util.*;

public class StringUtils {
    public static String join(List<String> list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first)
                first = false;
            else
                sb.append(conjunction);
            sb.append(item);
        }
        return sb.toString();
    }

    public static String join(String[] array, String conjunction) {
        String str = "";
        for (int i = 0; i < array.length; i++) {
            str += array[i];
            if (i != array.length - 1) str += conjunction;
        }
        return str;
    }

    public static Integer[] splitInt(String ids, String regular) {
        if (ids == null || ids.equals("") || ids.equals(" ")) return new Integer[0];

        String[] strArray = ids.split(regular);
        Integer[] intArray = new Integer[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        return intArray;
    }

    public static Long[] splitLong(String ids, String regular) {
        if (ids == null || ids.equals("") || ids.equals(" ")) return new Long[0];

        String[] strArray = ids.split(regular);
        Long[] intArray = new Long[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Long.parseLong(strArray[i]);
        }
        return intArray;
    }

    public static String getCamelCase(String str) {
        if (str.equals("")) return "";
        str = str.toLowerCase();
        str = str.replace("-", "/");
        str = str.replace("_", "/");
        str = str.replace(" ", "/");
        String[] strings = str.split("/");
        String res = "";

        for (String el : strings) {
            if (el.equals("")) continue;
            res += String.valueOf(el.charAt(0)).toUpperCase() + el.substring(1);
        }


        return res;
    }

    public static String truncate(String str, int len) {
        if (str.length() > len) {
            return str.substring(0, len) + "..";
        } else {
            return str;
        }
    }
}
