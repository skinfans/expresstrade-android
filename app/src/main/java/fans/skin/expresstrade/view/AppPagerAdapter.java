package fans.skin.expresstrade.view;

import android.support.v4.app.*;

import fans.skin.expresstrade.view.*;

import java.util.*;

public class AppPagerAdapter extends FragmentStatePagerAdapter {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private ArrayList<AppFragment> fragments;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    // Modify class constructor
    public AppPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void addFragment(AppFragment fragment) {
        fragments.add(fragment);
        notifyDataSetChanged();
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    @Override
    public AppFragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}