package fans.skin.expresstrade.view;

import android.content.*;
import android.content.res.*;
import android.util.*;
import android.widget.*;

import fans.skin.expresstrade.*;

public class SquaredImageView extends android.support.v7.widget.AppCompatImageView {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private boolean byWidth = true;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public SquaredImageView(Context context) {
        super(context);
    }

    public SquaredImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AppButton, 0, 0);

        try {
            byWidth = a.getBoolean(R.styleable.SquaredImageView_sByWidth, true);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = byWidth ? getMeasuredWidth() : getMeasuredHeight();
        setMeasuredDimension(size, size);
    }
}