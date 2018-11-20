package fans.skin.expresstrade.view;

import android.content.*;
import android.support.v4.view.*;
import android.util.*;
import android.view.*;

/**
 * Includes scroll stop functions.
 */

public class AppViewPager extends ViewPager {

    public boolean isPagingEnabled = true;

    public AppViewPager(Context context) {
        super(context);
    }

    public AppViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return this.isPagingEnabled && super.onTouchEvent(event);
        } catch (IllegalArgumentException ignored) {
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            return this.isPagingEnabled && super.onInterceptTouchEvent(event);
        } catch (IllegalArgumentException ignored) {
        }
        return false;
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }
}
