package fans.skin.expresstrade.utils;

import android.content.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.view.activities.*;

import java.io.*;

public class TryMe implements Thread.UncaughtExceptionHandler {

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    Thread.UncaughtExceptionHandler oldHandler;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public TryMe() {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    // =============================================================================================
    // METHODS
    // =============================================================================================

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        DebugActivity.debugError = sw.toString();

        Intent intent = new Intent(App.context, DebugActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        App.context.startActivity(intent);

        App.serviceModule.reqDebugPut(sw.toString());

        if (oldHandler != null)
            oldHandler.uncaughtException(thread, throwable);
    }
}