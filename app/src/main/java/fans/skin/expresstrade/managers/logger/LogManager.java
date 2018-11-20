package fans.skin.expresstrade.managers.logger;

import android.text.*;
import android.util.*;

import fans.skin.expresstrade.*;

public class LogManager {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private static final LogManager ourInstance = new LogManager();

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private LogManager() {
      //  GoogleAnalytics.getInstance(App.context).getLogger().setLogLevel(Logger.LogLevel.ERROR);
    }

    public static LogManager getInstance() {
        return ourInstance;
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void info(String msg) {
//        if (1 == 1) return;
        Log.d(App.LOG_TAG_INFO, getLocation() + msg);
    }

    public void debug(String msg) {
//        if (1 == 1) return;
        Log.d(App.LOG_TAG_DEBUG, getLocation() + msg);
    }

    public void error(String msg) {
//        if (1 == 1) return;
        Log.d(App.LOG_TAG_ERROR, getLocation() + msg);
    }

    private static String getLocation() {
        final String className = LogManager.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        boolean found = false;

        for (int i = 0; i < traces.length; i++) {
            StackTraceElement trace = traces[i];

            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        String string = "[" + getClassName(clazz) + "." + trace.getMethodName() + "." + trace.getLineNumber() + "]:";
                        String spacing = " ";

                        for (int c = 0; c < 45 - string.length(); c++) {
                            spacing += " ";
                        }

                        return string + spacing;
                    }
                } else if (trace.getClassName().startsWith(className)) {
                    found = true;
                    continue;
                }
            } catch (ClassNotFoundException e) {
            }
        }

        return "[]: ";
    }

    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }

}
