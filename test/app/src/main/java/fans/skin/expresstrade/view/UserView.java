package fans.skin.expresstrade.view;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.view.AppButton.*;

public class UserView extends LinearLayout implements View.OnTouchListener, View.OnClickListener {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private GestureDetector gd;
    private OnActionListener onActionListener;
    private Context context;

    public View view;
    public TextView tv_name;
    public ImageView iv_avatar;
    public ImageView iv_verified;
    public AppButton bt_select;
    public ImageView iv_star;
    public AppButton bt_change_link;

    public UsersTable data;

    public boolean isFeatured = false;

    // =============================================================================================
    // INTERFACES
    // =============================================================================================

    public interface OnActionListener {
        void onFeatureChanged(boolean isFollow);

        void onSelect(long ops_id);

        void onChangeLink(long ops_id);
    }

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public UserView(Context context) {
        super(context);
        this.context = context;
        initial();
    }

    public UserView(Context context, View inflate) {
        super(context);
        this.context = context;
        this.view = inflate;
        initial();
    }

    public UserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initial();
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    private void initial() {
        if (this.view == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.view = mInflater.inflate(R.layout.item_user, this);
        }

        tv_name = view.findViewById(R.id.tv_name);
        iv_avatar = view.findViewById(R.id.iv_avatar);
        bt_change_link = view.findViewById(R.id.bt_change_link);

        bt_select = view.findViewById(R.id.bt_select);
        bt_select.setSize(ButtonSize.SMALL);
        bt_change_link.setSize(ButtonSize.SMALL);

        iv_star = view.findViewById(R.id.iv_star);
        iv_star.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = !isFeatured;
                setFeaturedState(state);

                if (onActionListener != null)
                    onActionListener.onFeatureChanged(state);
            }
        });

        bt_select.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onActionListener != null)
                    onActionListener.onSelect(data.ops_id);
            }
        });

        bt_change_link.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onActionListener != null)
                    onActionListener.onChangeLink(data.ops_id);
            }
        });

        this.gd = new GestureDetector(context, ql);
        this.view.setOnTouchListener(this);
    }

    public void setOnActionListener(OnActionListener onActionListener) {
        this.onActionListener = onActionListener;
    }

    public void setFeaturedState(boolean state) {
        isFeatured = state;
        iv_star.setImageResource(state ? R.drawable.ic_toggle_favorites_enabled :
                R.drawable.ic_toggle_favorites_disabled);
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        gd.onTouchEvent(event);
        return false;
    }

    GestureDetector.SimpleOnGestureListener ql = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }


}
