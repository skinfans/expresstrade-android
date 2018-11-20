package fans.skin.expresstrade.view;


import android.content.*;
import android.support.v4.widget.*;
import android.util.*;

import fans.skin.expresstrade.*;

public class AppSwipeRefreshLayout extends SwipeRefreshLayout {
    public AppSwipeRefreshLayout(Context context) {
        super(context);
        initial(context);
    }

    public AppSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial(context);
    }

    private void initial(Context context) {
        setColorSchemeColors(
                context.getResources().getColor(R.color.accent),
                context.getResources().getColor(R.color.green),
                context.getResources().getColor(R.color.red)
        );
    }
}
