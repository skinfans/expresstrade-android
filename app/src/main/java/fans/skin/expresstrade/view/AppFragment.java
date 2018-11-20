package fans.skin.expresstrade.view;

import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v7.widget.*;
import android.view.*;

import com.squareup.otto.*;
import fans.skin.expresstrade.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.utils.*;

import java.util.*;

public class AppFragment extends Fragment {

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private AppActivity activity;
    public Toolbar toolbar;

    public boolean isFocus = false; // Если фрагмент виден пользователю на этой странице
    public boolean isVisible = false; // Если фрагмент есть видимый (последний) на странице
    public boolean isActive = false;

    public HashMap<String, AppLayout> appLayouts = new HashMap<>();

    public Object busEventListener;

    // Only for MainActivity
    public int pageIndex = -1;

    // =============================================================================================
    // FRAGMENT NAMES
    // =============================================================================================

    public enum FragmentName {
        NONE,
        FRAME_INVENTORY,
        FRAME_HISTORY,
        FRAME_USERS,
        INVENTORY,
        WEB,
        MAKE_ITEMS_DIALOG,
        MAKE_OFFER_DIALOG;

        public String getCamelCase() {
            return StringUtils.getCamelCase(this.name());
        }
    }

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    public FragmentName getFragmentName() {
        return FragmentName.NONE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getFragmentName().equals(FragmentName.NONE)) {
            new Exception("Not override to fragment name " + getFragmentName());
            return;
        }

        if (this.activity == null && getActivity() != null)
            this.activity = (AppActivity) getActivity();

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onCreate();

        // Подписываем на события
        busEventListener = new Object() {
            @Subscribe
            public void on(final OnEventModel object) {
                AppFragment.this.onEvent(object);
            }
        };
        App.eventManager.register(busEventListener);

    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onResume();

        if (activity == null) return;
        switch (activity.getActivityName()) {
//            case MAIN_ACTIVITY:
//                break;

            default:
                if (isLast())
                    onFocus();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onPause();

        if (activity == null) return;
        switch (activity.getActivityName()) {
//            case MAIN_ACTIVITY:
//                break;

            default:
                if (isLast())
                    onBlur();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onDestroy();

        App.eventManager.unregister(busEventListener);
    }


    // Вызывается, когда фрагмент становится видимым, то есть в стеке фрагментов он находится вверху
    public void onVisible() {
        isVisible = true;

        if (activity == null) return;
        switch (activity.getActivityName()) {
//            case MAIN_ACTIVITY:
//                break;

            default:
                onFocus();
                break;
        }
    }

    public void onInvisible() {
        isVisible = false;

        if (activity == null) return;
        switch (activity.getActivityName()) {
//            case MAIN_ACTIVITY:
//                break;

            default:
                onBlur();
                break;
        }
    }

    // Вызывается при onVisible и учитывается условие, что i контейнера совпадает с i ViewPager
    public void onFocus() {
        if (isFocus) return;
        isFocus = true;

        App.logManager.debug("onFocus " + getFragmentName());

        // layout event
        for (String layoutName : this.appLayouts.keySet()) {
            this.appLayouts.get(layoutName).onFocus();
        }

        AppActivity activity = (AppActivity) getActivity();
        if (activity == null) return;

        switch (activity.getActivityName()) {
//            case MAIN_ACTIVITY:
//                break;

            default:
                activity.setDisplayStatusbar(isDisplayStatusbar());
                break;
        }
    }

    public void onBlur() {
        isFocus = false;

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onBlur();
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public boolean isLast() {
        boolean isLast = true;
        AppFragment appFragment = App.fragmentViewer.getLast(pageIndex);
        if (appFragment != null && !appFragment.getFragmentName().equals(getFragmentName()))
            isLast = false;
        return isLast;
    }

    // Добавить layout
    public void addLayout(String layoutName, AppLayout appLayout) {
        this.appLayouts.put(layoutName, appLayout);
    }

    // Удалить layout
    public void removeLayout(String layoutName) {
        this.appLayouts.remove(layoutName);
    }

    // fixme спорный метод
    public void setActivity(AppActivity activity) {
        this.activity = activity;
    }

    public void setMenuItemVisible(int res, boolean isVisible) {
        if (toolbar == null) return;

        Menu menu = toolbar.getMenu();
        if (menu == null) return;

        MenuItem item = menu.findItem(res);
        if (item == null) return;

        item.setVisible(isVisible);
    }

    public MenuItem getMenuItem(int res) {
        if (toolbar == null) return null;

        Menu menu = toolbar.getMenu();
        if (menu == null) return null;

        return menu.findItem(res);
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================


    public void onEvent(OnEventModel event) {
        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onEvent(event);
    }

    public boolean onBackPressed() {
        return true; // Разрашение на закрытие этого фрагмента
    }

    protected boolean isDisplayStatusbar() {
        return true;
    }

    protected boolean isDisplayNotify() {
        return true;
    }

    protected boolean isPagingPages() {
        return true;
    }
}
