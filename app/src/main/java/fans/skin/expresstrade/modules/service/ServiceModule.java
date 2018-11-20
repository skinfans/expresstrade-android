package fans.skin.expresstrade.modules.service;

import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v7.app.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.event.EventManager.*;
import fans.skin.expresstrade.managers.network.*;
import fans.skin.expresstrade.managers.network.NetworkQuery.*;
import fans.skin.expresstrade.managers.pref.PrefManager.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.managers.network.NetworkManager.MethodType;
import fans.skin.expresstrade.view.activities.*;
import fans.skin.expresstrade.view.viewers.*;

public class ServiceModule {
    // =============================================================================================
    // CONSTANTS
    // =============================================================================================

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private Handler handler = new Handler();

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private static final ServiceModule ourInstance = new ServiceModule();

    public static ServiceModule getInstance() {
        return ourInstance;
    }

    private ServiceModule() {
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void initial() {
    }

    public void reqDebugPut(String log) {
        String info = "Time: " + System.currentTimeMillis();
        info += "\nOS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        info += "\nOS API Level: " + android.os.Build.VERSION.RELEASE + "(" + android.os.Build.VERSION.SDK_INT + ")";
        info += "\nDevice: " + android.os.Build.DEVICE;
        info += "\nModel (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";
        info += "\nDisplay: Density: " + App.display.density + " Screen: " + App.display.widthPixels + "x" + App.display.heightPixels + "px";

        App.logManager.error(info + '\n' + log);

        NetworkQuery query = new NetworkQuery();
        query.add(Param.LOG, info + '\n' + log);
        query.add(Param.TOKEN, App.APP_TOKEN);
        query.add(Param.VERSION, App.BUILD_VERSION);
        query.add(Param.ACCESS_TOKEN, App.prefManager.getString(PrefKey.ACCOUNT_TOKEN_ACCESS));

        // TODO request
        App.nwManager.req(MethodType.SEND_LOG, query, new NetworkCallback() {
            @Override
            public void on200(Object object) {
                super.on200(object);

            }

            @Override
            public void on403(ErrorModel error) {
//                super.on400(error);
            }

            @Override
            public void on400(ErrorModel error) {
//                super.on400(error);
            }
        });
    }

}
