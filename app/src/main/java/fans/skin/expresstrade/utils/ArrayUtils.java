package fans.skin.expresstrade.utils;


import com.google.gson.*;

import java.util.*;

public class ArrayUtils {

    public static <T extends List> T slice(T list, int first, int last) {
        first = Math.min(list.size(), first);
        last = Math.min(list.size(), last);
        return (T) list.subList(first, last);
    }

    public static boolean isEmpty(List list) {
        return list.size() == 1 && "".equals(list.get(0));
    }

    public static <T> List<T> stringToArray(List s, Class<T[]> clazz) {
        // Приходится применять такую форму преобразования при получении данных Gson,
        // из-за того, что компилятор очищает дженерики. То есть пельзя указать параметризированный тип
        return Arrays.asList(new Gson().fromJson(s.toString(), clazz));
    }
}
