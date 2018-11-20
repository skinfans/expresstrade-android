package fans.skin.expresstrade.view.activities;

import android.content.*;
import android.os.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.AppFragment.*;
import fans.skin.expresstrade.view.fragments.*;
import fans.skin.expresstrade.view.viewers.*;

import static fans.skin.expresstrade.managers.event.EventManager.*;

public class MainActivity extends AppActivity implements AppViewPager.OnPageChangeListener, View.OnClickListener {
    // =============================================================================================
    // VIEWERS
    // =============================================================================================

    public AppPagerAdapter appPagerAdapter;
    public AppViewPager viewPager;

    public ImageView slider;
    public Integer sliderWidth;

    public RelativeLayout nav_users;
    public RelativeLayout nav_inventory;
    public RelativeLayout nav_offers;
    public RelativeLayout navigation;
    public EditText et_accept_input;
    private RelativeLayout block_accept;

    private AppFullButton bt_accept;
    private AppFullButton bt_accept_close;
    private AppButton bt_accept_back;
    private ImageView iv_accept_code_error;
    private ImageView iv_accept_code_complete;
    private TextView tv_accept_message;

    private ImageView bt_menu_web;
    private ImageView bt_menu_logout;
    private ImageView iv_indicator_new_offers;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    public Integer defaultFrame = FragmentViewer.FRAME_INVENTORY;
    private Handler handler = new Handler();
    private AcceptModel acceptModel;
    private boolean isAcceptConfirmProcess = false;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDisplayStatusbar(true);

        App.mainActivity = this;

        viewPager = findViewById(R.id.pager);
        slider = findViewById(R.id.slider);

        nav_users = findViewById(R.id.nav_users);
        nav_inventory = findViewById(R.id.nav_inventory);
        nav_offers = findViewById(R.id.nav_offers);

        navigation = findViewById(R.id.navigation);
        et_accept_input = findViewById(R.id.et_accept_input);

        bt_menu_web = findViewById(R.id.bt_menu_web);
        bt_menu_logout = findViewById(R.id.bt_menu_logout);
        iv_indicator_new_offers = findViewById(R.id.iv_indicator_new_offers);

        bt_menu_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AppFragment fragment = App.fragmentViewer.getLast(viewPager.getCurrentItem());
                if (fragment.getFragmentName() == FragmentName.WEB)
                    return;

                WebFragment webFragment = new WebFragment();
                webFragment.setUrl("/");

                App.fragmentViewer.start(
                        viewPager.getCurrentItem(),
                        webFragment,
                        FragmentViewer.StartType.START_PUSH,
                        FragmentViewer.AnimType.ANIM_SCALE
                );
            }
        });

        bt_menu_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.accountModule.setAuthorizeStatus(false);
            }
        });

        bt_accept = findViewById(R.id.bt_accept);
        block_accept = findViewById(R.id.block_accept);
        iv_accept_code_error = findViewById(R.id.iv_accept_code_error);
        iv_accept_code_complete = findViewById(R.id.iv_accept_code_complete);
        tv_accept_message = findViewById(R.id.tv_accept_message);
        bt_accept_close = findViewById(R.id.bt_accept_close);


        bt_accept_back = findViewById(R.id.bt_accept_back);
        bt_accept_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAcceptConfirmProcess || acceptModel == null) return;
                App.eventManager.doEvent(EventType.ON_ACCOUNT_2FA_CODE_CANCELED, acceptModel);
                doCloseAccept();
            }
        });

        bt_accept_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCloseAccept();
            }
        });

        bt_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAcceptConfirm();
            }
        });


        nav_users.setOnClickListener(this);
        nav_inventory.setOnClickListener(this);
        nav_offers.setOnClickListener(this);

        appPagerAdapter = new AppPagerAdapter(getSupportFragmentManager());

        // Frame
        appPagerAdapter.addFragment(new FrameUsersFragment());
        appPagerAdapter.addFragment(new FrameInventoryFragment());
        appPagerAdapter.addFragment(new FrameOffersFragment());

        viewPager.setAdapter(appPagerAdapter); // Устанавливаем адаптер
        viewPager.setOffscreenPageLimit(3);

        sliderWidth = App.display.widthPixels / 3;
        slider.getLayoutParams().width = sliderWidth;

        // Устанавливаем обработку событий
        viewPager.setOnPageChangeListener(this);

        // Устанавливаем текущий экран
        viewPager.setCurrentItem(defaultFrame);
        setSliderIndex(defaultFrame, 0);

        setPagingEnable(false);
    }

    // Оболочка для удаления фрагментов
    public boolean popFragment(int i, boolean anyway) {
        return App.fragmentViewer.popLast(i, anyway);
    }

    public boolean popFragment(boolean anyway) {
        App.logManager.debug("close " + viewPager.getCurrentItem());
        return popFragment(viewPager.getCurrentItem(), anyway);
    }

    // Включить/Выключить скрол viewPager
    public void setPagingEnable(boolean paging) {
        App.logManager.debug("setPagingEnable " + paging);
        viewPager.setPagingEnabled(paging);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Делаем CheckIn
        App.accountModule.reqCheckIn();
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    // Установить позицию скрола
    private void setSliderIndex(int index, int time) {
        slider.animate().translationX(index * sliderWidth).setDuration(time);
    }

    // Открыть окно открытия
    public void doOpenAccept(AcceptModel model) {
        acceptModel = model;

        block_accept.setVisibility(View.VISIBLE);
        tv_accept_message.setText(App.context.getResources().getString(R.string.enter_code));
        bt_accept.setVisibility(View.VISIBLE);
        bt_accept_close.setVisibility(View.GONE);
        doAcceptKeyboardVisible(et_accept_input, true);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                block_accept.animate().alpha(1f).translationY(0).setDuration(400).start();
            }
        }, 30);
    }

    // Закрыть окно открытия
    public void doCloseAccept() {
        acceptModel = null;
        block_accept.animate().alpha(0f).translationY(40 * App.display.density).setDuration(200).start();

        bt_accept.setLoaderVisible(false);
        et_accept_input.setFocusableInTouchMode(true);
        et_accept_input.setFocusable(true);
        et_accept_input.setText("");
        iv_accept_code_error.setAlpha(0f);
        iv_accept_code_complete.setAlpha(0f);
        et_accept_input.animate().alpha(1f).setDuration(150).start();
        bt_accept_back.animate().alpha(1f).setDuration(150).start();
        doAcceptKeyboardVisible(et_accept_input, false);
        isAcceptConfirmProcess = false;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                block_accept.setVisibility(View.GONE);
                App.eventManager.doEvent(EventType.ON_DO_INVENTORY_RELOAD);
            }
        }, 210);
    }

    public void doAcceptKeyboardVisible(EditText et, boolean state) {
        if (!state) {
            InputMethodManager imm = (InputMethodManager) App.context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } else {
            et.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // Подтвердить обмен
    private void doAcceptConfirm() {
        if (et_accept_input.getText().toString().equals("")) return;

        App.accountModule.set2FA(Long.parseLong(et_accept_input.getText().toString()));
        App.eventManager.doEvent(EventType.ON_ACCOUNT_2FA_CODE_UPDATED, acceptModel);

        bt_accept.setLoaderVisible(true);
        et_accept_input.setFocusable(false);
        iv_accept_code_error.setAlpha(0f);
        iv_accept_code_complete.setAlpha(0f);
        et_accept_input.animate().alpha(0.4f).setDuration(250).start();
        bt_accept_back.animate().alpha(0.4f).setDuration(250).start();
        isAcceptConfirmProcess = true;
    }

    private void doAcceptCompleted() {
        if (!isAcceptConfirmProcess) return;

        bt_accept.setLoaderVisible(false);
        iv_accept_code_error.setAlpha(0f);
        et_accept_input.animate().alpha(1f).setDuration(250).start();
        bt_accept_back.animate().alpha(1f).setDuration(250).start();
        bt_accept.setVisibility(View.GONE);
        bt_accept_close.setVisibility(View.VISIBLE);
        isAcceptConfirmProcess = false;

        iv_accept_code_complete.animate().alpha(1f).setDuration(250).start();
        et_accept_input.animate().alpha(0f).setDuration(250).start();
        tv_accept_message.setText(App.context.getResources().getString(R.string.enter_code_offer_sent));
        doAcceptKeyboardVisible(et_accept_input, false);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doCloseAccept();
            }
        }, 3000);
    }

    private void doAcceptNotValid() {
        if (!isAcceptConfirmProcess) return;

        bt_accept.setLoaderVisible(false);
        et_accept_input.animate().alpha(0f).setDuration(250).start();
        bt_accept_back.animate().alpha(1f).setDuration(250).start();
        isAcceptConfirmProcess = false;

        iv_accept_code_error.animate().alpha(1f).setDuration(250).start();
        tv_accept_message.setText(App.context.getResources().getString(R.string.enter_code_invalid));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                et_accept_input.animate().alpha(1f).setDuration(250).start();
                iv_accept_code_error.animate().alpha(0f).setDuration(250).start();
                et_accept_input.setFocusableInTouchMode(true);
                et_accept_input.setFocusable(true);
                et_accept_input.setText("");
                et_accept_input.requestFocus();
                tv_accept_message.setText(App.context.getResources().getString(R.string.enter_code));
            }
        }, 1800);
    }

    // =============================================================================================
    // EVENTS
    // =============================================================================================


    @Override
    public void onClick(View v) {
        if (!viewPager.isPagingEnabled) return;

        switch (v.getTag().toString()) {
            case "nav_users":
                viewPager.setCurrentItem(FragmentViewer.FRAME_USERS);
                break;
            case "nav_inventory":
                viewPager.setCurrentItem(FragmentViewer.FRAME_INVENTORY);
                break;
            case "nav_offers":
                viewPager.setCurrentItem(FragmentViewer.FRAME_OFFERS);
                break;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        setSliderIndex(i, 200);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onEvent(OnEventModel object) {
        super.onEvent(object);


        switch ((object.id)) {
            // ON_DO_NAVIGATION_VISIBLE
            case 4317996:
                navigation.setVisibility(View.VISIBLE);
                break;

            // ON_DO_NAVIGATION_INVISIBLE
            case 4012810:
                navigation.setVisibility(View.GONE);
                break;

            // ON_DO_PAGING_ENABLED
            case 1314880:
                setPagingEnable(true);
                break;

            // ON_DO_PAGING_DISABLED
            case 6646842:
                setPagingEnable(false);
                break;

            // ON_DO_SELECT_FRAME
            case 7648262:
                Integer index = (Integer) object.object;
                viewPager.setCurrentItem(index);
                break;

            // ON_DO_2FA_ACCEPT
            case 8828856:
                doOpenAccept((AcceptModel) object.object);
                break;

            // ON_TRADE_SEND_OFFER_COMPLETED
            case 1975172:
            // ON_TRADE_CONFIRM_OFFER_COMPLETED
            case 5858641:
                doAcceptCompleted();
                break;

            // ON_TRADE_SEND_OFFER_CODE_NOT_VALID
            case 9617241:
            // ON_TRADE_SEND_OFFER_ERROR
            case 6162457:
            // ON_TRADE_CONFIRM_OFFER_ERROR
            case 6719581:
                doAcceptNotValid();
                break;

            // ON_TRADE_SEND_OFFER_USER_TOKEN_NOT_VALID
            case 8518241:
                break;

            // ON_DO_NEW_OFFERS_INDICATOR_VISIBLE:
            case 9981935:
                iv_indicator_new_offers.setVisibility(View.VISIBLE);
                break;

            // ON_DO_NEW_OFFERS_INDICATOR_INVISIBLE:
            case 6781262:
                iv_indicator_new_offers.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            App.eventManager.doEvent(EventType.ON_WINDOW_FOCUS);
        }
    }

    @Override
    public void onBackPressed() {
        if (popFragment(false)) super.onBackPressed();
    }
}
