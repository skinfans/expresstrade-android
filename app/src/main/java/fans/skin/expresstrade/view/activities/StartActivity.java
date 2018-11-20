package fans.skin.expresstrade.view.activities;

import android.content.*;
import android.os.*;
import android.view.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.view.*;

import static fans.skin.expresstrade.App.logManager;

/**
 * Отвечает за запуск активити по умолчанию.
 */
public class StartActivity extends AppActivity {
    // =============================================================================================
    // LIFECYCLE
    // =============================================================================================

    public ActivityName getActivityName() {
        return ActivityName.START_ACTIVITY;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);

        // Выбираем активити и запускаем
        boolean isAuthorize = App.accountModule.isAuthorized;

        if (!isAuthorize) {
//        if (true) {
            Intent intent = new Intent(App.context, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(App.context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }

        App.startActivity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
