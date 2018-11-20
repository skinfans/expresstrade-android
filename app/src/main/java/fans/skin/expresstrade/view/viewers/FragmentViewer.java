package fans.skin.expresstrade.view.viewers;

import android.os.*;
import android.support.v4.app.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.view.*;

import java.io.*;
import java.util.*;

public class FragmentViewer {
    // =============================================================================================
    // CONSTANTS
    // =============================================================================================

    public final static int FRAME_USERS = 0;
    public final static int FRAME_INVENTORY = 1;
    public final static int FRAME_OFFERS = 2;
    public final static int FRAME_POPUPS = 3;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private final int container = R.id.container;
    private HashMap<Integer, FragmentManager> fragmentManagers = new HashMap<>();
    private int currentFramentManager = 0;

    private Handler handler = new Handler();

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    private static final FragmentViewer ourInstance = new FragmentViewer();

    public static FragmentViewer getInstance() {
        return ourInstance;
    }

    private FragmentViewer() {
    }

    // =============================================================================================
    // ENUM
    // =============================================================================================

    public enum StartType {
        START_PUSH, // add on top, hiding the previous one
        START_FIRST // add to the very beginning by deleting the previous ones
    }

    public enum AnimType {
        ANIM_SLIDE(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left),
        ANIM_SCALE(R.anim.scale_in, R.anim.fade_out, R.anim.fade_in, R.anim.scale_out);

        public int enter, exit, popEnter, popExit;

        AnimType(int enter, int exit, int popEnter, int popExit) {
            this.enter = enter;
            this.exit = exit;
            this.popEnter = popEnter;
            this.popExit = popExit;
        }
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public FragmentManager getFragmentManager(int id) {
        return this.fragmentManagers.get(id);
    }

    public void addFragmentManager(int fmKey, FragmentManager fragmentManager) {
        this.fragmentManagers.put(fmKey, fragmentManager);
    }

    public void removeFragmentManager(int fmKey) {
        this.fragmentManagers.remove(fmKey);
    }

    public void setCurrentFragmentManager(int i) {
        this.currentFramentManager = i;
        // bypass all managers and set blur except current
    }

    // ***

    // Run fragment
    public void start(final int id, final AppFragment fragment, final StartType startType, final AnimType animType) {
        final FragmentManager fragmentManager = getFragmentManager(id);
        if (fragmentManager == null || fragment == null) return;

        fragment.pageIndex = id;
        FragmentTransaction fTrans = fragmentManager.beginTransaction();
        AppFragment currentFragment = getLast(id);

        App.logManager.error("Create fragment " + id + " " + fragment.getFragmentName());

        try {
            switch (startType) {
                case START_PUSH:
                    if (animType != null)
                        fTrans.setCustomAnimations(animType.enter, animType.exit, animType.popEnter, animType.popExit);

                    if (currentFragment != null) {
                        setVisible(currentFragment, false);
                        fTrans.hide(currentFragment);
                    }

                    fTrans.add(container, fragment, fragment.getFragmentName().getCamelCase());
                    fTrans.addToBackStack(fragment.getFragmentName().getCamelCase());

                    fTrans.commit(); // commit in ui stream
                    setVisible(fragment, true);
                    break;

                case START_FIRST:
                    List<Fragment> fragments = fragmentManager.getFragments();
                    if (fragments != null)
                        for (int i = 0; i < fragments.size(); i++)
                            if (fragments.get(i) != null) {
                                setVisible((AppFragment) fragments.get(i), false);
                                fTrans.remove(fragments.get(i));
                            }

                    fTrans.add(container, fragment, fragment.getFragmentName().getCamelCase());
                    fTrans.commit();

                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            } catch (IllegalStateException ignored) {
                                // There's no way to avoid getting this if saveInstanceState has already been called.
                            }
                        }
                    });

                    setVisible(fragment, true);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
//            App.serviceModule.reqDebugPut("TRY CATCH " + sw.toString());
            App.logManager.error(sw.toString());
        }
    }

    // Close the last fragment
    public boolean popLast(int id, boolean anyway) {
        AppFragment fragment = getLast(id);
        if (fragment == null) return true;

        // if the fragment does not allow to close it, but ignore the answer, if anyway
        if (!anyway && !fragment.onBackPressed())
            return false;

        // Clear
        final FragmentManager fragmentManager = getFragmentManager(id);

        if (fragmentManager.getBackStackEntryCount() > 0) {
            setVisible(fragment, false);
            setVisible(getByIndex(id, getCount(id) - 2), true);

            handler.post(new Runnable() {
                public void run() {
                    try {
                        fragmentManager.popBackStack();
                    } catch (Exception ignored) {
                    }
                }
            });
        }

        // In response, can the application be closed if necessary due to the absence of fragments
        return fragmentManager.getBackStackEntryCount() == 0;
    }

    // ***

    // Set Focus Status
    public void setFocus(int id, boolean isFocus) {
        AppFragment fragment = getLast(id);
        if (fragment == null) return;
        if (isFocus) fragment.onFocus();
        else fragment.onBlur();
    }

    // Set visibility status. Called ONLY from FragmentViewer
    public void setVisible(AppFragment fragment, boolean isVisible) {
        if (fragment == null) return;
        if (isVisible) fragment.onVisible();
        else fragment.onInvisible();
    }

    // ***

    public AppFragment getLast(int id) {
        FragmentManager fragmentManager = getFragmentManager(id);
        if (fragmentManager == null) return null;
        return (AppFragment) fragmentManager.findFragmentById(container); // returns the last fragment
    }

    public AppFragment getByIndex(int id, int index) {
        if (index < 0) return null;
        FragmentManager fragmentManager = getFragmentManager(id);
        if (fragmentManager == null) return null;
        List<Fragment> fragments = fragmentManager.getFragments();

        int count = 0;
        for (int i = 0; i < fragments.size(); i++) {
            if (fragments.get(i) != null) {
                count++;
                if (index == count - 1) return (AppFragment) fragments.get(i);
            }
        }
        return null;
    }

    public AppFragment getByTag(int id, String fragment) {
        FragmentManager fragmentManager = getFragmentManager(id);
        if (fragmentManager == null) return null;
        return (AppFragment) fragmentManager.findFragmentByTag(fragment);
    }

    // ***

    public boolean showByTag(int id, String fragment) {
        FragmentManager fragmentManager = getFragmentManager(id);
        if (fragmentManager == null) return false;
        AppFragment appFragment = getByTag(id, fragment);
        if (appFragment == null) return false;
        FragmentTransaction fTrans = fragmentManager.beginTransaction();
        fTrans.show(appFragment);
        fTrans.commit();
        return true;
    }


    public boolean hideByTag(int id, String fragment) {
        FragmentManager fragmentManager = getFragmentManager(id);
        if (fragmentManager == null) return false;
        AppFragment appFragment = getByTag(id, fragment);
        if (appFragment == null) return false;
        FragmentTransaction fTrans = fragmentManager.beginTransaction();
        fTrans.hide(appFragment);
        fTrans.commit();
        return true;
    }

    public int getCount(int id) {
        FragmentManager fragmentManager = getFragmentManager(id);
        if (fragmentManager == null) return -1;
        List<Fragment> fragments = fragmentManager.getFragments();

        int count = 0;

        if (fragments != null)
            for (int i = 0; i < fragments.size(); i++)
                if (fragments.get(i) != null) count++;

        return count;
    }
}
