package fans.skin.expresstrade;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;

import fans.skin.expresstrade.managers.database.*;
import fans.skin.expresstrade.managers.event.*;
import fans.skin.expresstrade.managers.logger.*;
import fans.skin.expresstrade.managers.network.*;
import fans.skin.expresstrade.managers.pref.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.modules.account.*;
import fans.skin.expresstrade.modules.service.*;
import fans.skin.expresstrade.modules.trade.*;
import fans.skin.expresstrade.modules.users.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.managers.pref.PrefManager.PrefKey;
import fans.skin.expresstrade.view.activities.*;
import fans.skin.expresstrade.view.viewers.*;

public class App extends Application {
    // =============================================================================================
    // CONSTANTS
    // =============================================================================================

    private Handler handler = new Handler();

    public final static int BUILD_VERSION = 7;
    public final static String BUILD_VERSION_NAME = "1.0.7";

    public final static String API_VERSION = "1.0";

    public final static String APP_CODE = "{YOUR_APP_CODE}";
    public final static String APP_TOKEN = "{YOUR_APP_TOKEN}";

    public final static String LOG_TAG_INFO = "LOG_TAG_INF";
    public final static String LOG_TAG_DEBUG = "LOG_TAG_DEB";
    public final static String LOG_TAG_ERROR = "LOG_TAG_ERR";

    public final static String URL_OPS_AUTH = "https://oauth.opskins.com/v1/";
    public final static String URL_TRADE_AUTH = "https://api-trade.opskins.com/";

    public final static String URL_WEB = "http://skin.fans/expresstrade/inapp";

    public static Long USER_ID;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    public static Context context;

    public static AuthActivity authActivity;
    public static MainActivity mainActivity;
    public static StartActivity startActivity;
    public static DebugActivity debugActivity;

    public static DisplayMetrics display;

    // =============================================================================================
    // SINGLETONS
    // =============================================================================================

    public final static DatabaseManager databaseManager = DatabaseManager.getInstance();
    public final static EventManager eventManager = EventManager.getInstance();
    public final static LogManager logManager = LogManager.getInstance();
    public final static PrefManager prefManager = PrefManager.getInstance();
    public final static NetworkManager nwManager = NetworkManager.getInstance();

    public final static ServiceModule serviceModule = ServiceModule.getInstance();
    public final static AccountModule accountModule = AccountModule.getInstance();
    public final static UsersModule usersModule = UsersModule.getInstance();
    public final static TradeModule tradeModule = TradeModule.getInstance();

    public final static FragmentViewer fragmentViewer = FragmentViewer.getInstance();
    public final static DialogViewer dialogViewer = DialogViewer.getInstance();

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler(new TryMe());

        super.onCreate();

        // Записываем контент текущего приложения
        context = getApplicationContext();
        display = context.getResources().getDisplayMetrics();


        // Инициализируем БД
        App.databaseManager.initial(App.context);

        // Инициализируем секрет пользователя
        App.accountModule.initial();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
