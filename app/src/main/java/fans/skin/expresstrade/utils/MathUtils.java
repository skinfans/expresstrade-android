package fans.skin.expresstrade.utils;

import java.math.*;

public class MathUtils {

    public static double round(double number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        double tmp = number * pow;
        return (double) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
    }

    public static float lerp(float a, float b, float k) {
        k = k < 0 ? 0 : k;
        k = k > 1 ? 1 : k;
        return a + (b - a) * k;
    }

    // ***

    public static String reduceLengthFormat(double n, int iteration) {
        char[] c = new char[]{'k', 'm', 'b', 't'};

        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) % 10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
        return (d < 1000 ? //this determines the class, i.e. 'k', 'm' etc
                ((d > 99.9 || isRound || (!isRound && d > 9.99) ? //this decides whether to trim the decimals
                        (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + c[iteration])
                : reduceLengthFormat(d, iteration + 1));
    }

    public static String reduceDistanceFormat(long n) {
        if (n / 1000f < 1f) {
            return String.valueOf(n + "m");
        } else {
            if (n < 10000) {
                return new BigDecimal(n / 1000f).setScale(1, RoundingMode.HALF_UP).floatValue() + "km";
            } else {
                return Math.round(n / 1000f) + "km";
            }
        }
    }

    // ***

    public static String spaceFormat(long number) {
        return String.format("%,d", number);
    }

}
