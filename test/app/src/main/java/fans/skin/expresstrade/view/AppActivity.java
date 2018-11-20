package fans.skin.expresstrade.view;

import android.os.*;
import android.support.v7.app.*;
import android.view.*;

import com.squareup.otto.*;
import fans.skin.expresstrade.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.utils.*;

import java.util.*;

public class AppActivity extends AppCompatActivity {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    public boolean isActive = false;

    protected Object busEventListener;

    // Список лейаутов. Это дочерние контейнеры активити, жизненый цикл которых, события и прочее
    // зависит от родителя, который передает в него все события
    public HashMap<String, AppLayout> appLayouts = new HashMap<>();

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    public ActivityName getActivityName() {
        return ActivityName.NONE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Скрываем бар навигации
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onCreate();

        // Подписываем родительский класс на события
        // Во всех дочерних активити будут эти события. При надобности можно переопределить
        busEventListener = new Object() {
            @Subscribe
            public void on(final OnEventModel object) {
                AppActivity.this.onEvent(object);
            }

            @Subscribe
            public void on(final ErrorModel object) {
                AppActivity.this.onError(object);
            }
        };

        App.eventManager.register(busEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onDestroy();

        App.eventManager.unregister(busEventListener);
    }

    // =============================================================================================
    // FRAGMENT NAMES
    // =============================================================================================

    public enum ActivityName {
        NONE,
        START_ACTIVITY,
        MAIN_ACTIVITY,
        SETTINGS_ACTIVITY,
        STORE_ACTIVITY,
        OAUTH_ACTIVITY,
        WEB_ACTIVITY;

        public String getCamelCase() {
            return StringUtils.getCamelCase(this.name());
        }
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void addLayout(String layoutName, AppLayout appLayout) {
        this.appLayouts.put(layoutName, appLayout);
    }

    public AppLayout getLayout(String layoutName) {
        return this.appLayouts.get(layoutName);
    }

    public void removeLayout(String layoutName) {
        this.appLayouts.remove(layoutName);
    }

    public void setDisplayStatusbar(boolean enable) {
        if (enable) getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    // Запрос прошел кодом 200/400
    public void onEvent(OnEventModel object) {
        // layout event
        for (String layoutName : this.appLayouts.keySet()) {
            this.appLayouts.get(layoutName).onEvent(object);
        }
    }

    // FIXME: удалить
    // Запрос не прошел и получили ошибку 500 или ошибку соединения
    public void onError(ErrorModel object) {
    }
}
