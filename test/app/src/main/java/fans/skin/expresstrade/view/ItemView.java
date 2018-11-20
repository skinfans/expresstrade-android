package fans.skin.expresstrade.view;

import android.annotation.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.models.*;

public class ItemView extends LinearLayout implements View.OnTouchListener {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private GestureDetector gd;
    private OnActionListener onActionListener;

    public Context context;
    public View view;
    public ImageView preview;
    public ImageView preview_min;
    public ImageView preview_svg;
    public ImageView ivLineRarity;
    public TextView tv_price;
    public TextView tv_title;
    public TextView tv_wear;
    public ImageView iv_select;
    public TextView tv_count;
    public RelativeLayout rl_timer;

    public RelativeLayout rl_content;

    public ItemModel.Item data;

    public boolean isSelectState = false;
    public boolean isSelected = false;

    Handler handler = new Handler();

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public ItemView(Context context) {
        super(context);
        this.context = context;
        initial();
    }

    public ItemView(Context context, View inflate) {
        super(context);
        this.context = context;
        this.view = inflate;
        initial();
    }

    public ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initial();
    }

    // =============================================================================================
    // INTERFACES
    // =============================================================================================

    public interface OnActionListener {
        void onOpenDialog();

        void onLike();

        void onOpenProfile();

        void onClick();
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    @SuppressLint("ClickableViewAccessibility")
    private void initial() {
        if (this.view == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.view = mInflater.inflate(R.layout.item_item, this);
        }

        preview = view.findViewById(R.id.preview);
        preview_min = view.findViewById(R.id.preview_min);
        preview_svg = view.findViewById(R.id.preview_svg);

        ivLineRarity = view.findViewById(R.id.iv_line_rarity);
        tv_price = view.findViewById(R.id.tv_price);
        tv_count = view.findViewById(R.id.tv_count);
        tv_title = view.findViewById(R.id.tv_title);
        tv_wear = view.findViewById(R.id.tv_wear);
        iv_select = view.findViewById(R.id.iv_select);
        rl_content = view.findViewById(R.id.rl_content);
        rl_timer = view.findViewById(R.id.rl_timer);

        this.gd = new GestureDetector(context, ql);
        this.view.setOnTouchListener(this);
    }

    // Установить статус выделения
    public void setSelectState(boolean state, boolean isAnim) {
        if (isSelectState == state) return;
        isSelectState = state;

        App.logManager.debug("setSelectState " + state + " isAnim " + isAnim);

        iv_select.animate().alpha(state ? 1f : 0f).setDuration(isAnim ? 200 : 0).start();
    }

    public void setSelectState(boolean state) {
        setSelectState(state, false);
    }


    // Выбрать предмет
    public void setSelection(boolean state, boolean isAnim) {
        if (isSelected == state) return;
        isSelected = state;

        App.logManager.debug("setSelection " + state);

        iv_select.setImageResource(state ? R.drawable.ic_toggle_enabled_blue : R.drawable.ic_toggle_disabled);
        rl_content.setBackgroundColor(getResources().getColor(state ? R.color.bg_content_active : R.color.bg_content));

        if (isAnim)
            setScaleBounceCycle();
    }

    public void setSelection(boolean state) {
        setSelection(state, false);
    }



    public void setOnActionListener(OnActionListener onActionListener) {
        this.onActionListener = onActionListener;
    }

    private void setScaleBounce(boolean state) {
        float scale = 0.92f;
        view.animate().scaleX(state ? scale : 1f).setStartDelay(0).setDuration(80).start();
        view.animate().scaleY(state ? scale : 1f).setStartDelay(0).setDuration(80).start();
    }

    // Анимация выделения предмета
    private void setScaleBounceCycle() {
        setScaleBounce(true);

        handler.postDelayed(new Runnable()
        {
            @Override
            public void run() {
                 setScaleBounce(false);
            }
        }, 100 );
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                return true;

            case MotionEvent.ACTION_DOWN:
                setScaleBounce(true);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setScaleBounce(false);
                break;
        }

        gd.onTouchEvent(event);

        return true;
    }

    GestureDetector.SimpleOnGestureListener ql = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (onActionListener != null)
                onActionListener.onClick();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            if (onActionListener != null)
                onActionListener.onClick();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (onActionListener != null)
                onActionListener.onClick();
        }
    };
}
