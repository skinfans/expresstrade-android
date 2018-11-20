package fans.skin.expresstrade.view;

import android.content.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;

public class AppRecyclerView extends RecyclerView {

    private boolean isTouchEnable = true;

    public AppRecyclerView(Context context) {
        super(context);
    }

    public AppRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppRecyclerView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
    }

    public void setTouchEnable(boolean isTouchEnable) {
        this.isTouchEnable = isTouchEnable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return this.isTouchEnable && super.onTouchEvent(e);
    }
}