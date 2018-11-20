package fans.skin.expresstrade.view;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.widget.*;

public class ConfigurableScrollView extends ScrollView {

    private int scrollOffset = 0;

    public ConfigurableScrollView (final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollOffset (final int scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen (final Rect rect) {
        // adjust by scroll offset
        int scrollDelta = super.computeScrollDeltaToGetChildRectOnScreen(rect);
        int newScrollDelta = (int) Math.signum(scrollDelta) * (scrollDelta + this.scrollOffset);
        return newScrollDelta;
    }
}