package fans.skin.expresstrade.view;

import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v7.widget.*;
import android.view.*;

import com.squareup.otto.*;

import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.utils.*;

public class AppDialogFragment extends DialogFragment {

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private AppActivity activity;

    public boolean isActive = false;
    public HashMap<String, AppLayout> appLayouts = new HashMap<>();
    public Object busEventListener;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    public AppFragment.FragmentName getFragmentName() {
        return AppFragment.FragmentName.NONE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getFragmentName().equals(AppFragment.FragmentName.NONE)) {
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
                AppDialogFragment.this.onEvent(object);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onPause();

        if (activity == null) return;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onDestroy();

        App.eventManager.unregister(busEventListener);
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================


    public void onEvent(OnEventModel event) {
        // layout event
        for (String layoutName : this.appLayouts.keySet())
            this.appLayouts.get(layoutName).onEvent(event);
    }
}
