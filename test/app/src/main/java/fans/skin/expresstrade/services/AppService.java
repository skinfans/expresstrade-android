package fans.skin.expresstrade.services;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.annotation.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.view.fragments.*;

import java.util.*;

public class AppService extends Service {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private int intervalTime = 60000;
    private int delayTime = 3000;


    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public void onCreate() {
        super.onCreate();
        App.logManager.info("Сервис запущен");

        Timer autoUpdate = new Timer();

        // Каждые 30 сек
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {

            }
        }, delayTime, 30000);

        // Каждые 5 минут
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {

            }
        }, delayTime, intervalTime * 5);

        // Каждые 15 минут
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {

            }
        }, intervalTime * 15, intervalTime * 15);


        // Каждые 30 минуты
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
            }
        }, intervalTime * 30, intervalTime * 30);

    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
