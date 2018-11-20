package fans.skin.expresstrade.view;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.view.AppButton.*;

public class OfferView extends LinearLayout implements View.OnTouchListener, View.OnClickListener {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private GestureDetector gd;
    private OnActionListener onActionListener;
    private Context context;

    public View view;
    public TextView tv_title;
    public ImageView ic_verified;
    public ImageView ic_vgosite;
    public LinearLayout ll_state;
    public TextView tv_state_value;
    public LinearLayout ll_buttons;
    public AppButton bt_accept;
    public AppButton bt_cancel;
    public TextView tv_give_total;
    public TextView tv_get_total;

    public ImageView iv_arrow_give;
    public ImageView iv_arrow_get;

    public ImageView iv_give_eye;
    public TextView iv_give_name;
    public ImageView iv_get_eye;
    public TextView iv_get_name;

    public LinearLayout ll_give;
    public LinearLayout ll_get;

    public LinearLayout ll_give_info;
    public LinearLayout ll_get_info;
    public LinearLayout ll_get_cases;
    public TextView tv_get_cases_count;

    public OfferModel.Offer data;

    public boolean isFeatured = false;

    // =============================================================================================
    // INTERFACES
    // =============================================================================================

    public interface OnActionListener {
        void onViewGiveItems();
        void onViewGetItems();
        void onCancel();
        void onAccept();
    }

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public OfferView(Context context) {
        super(context);
        this.context = context;
        initial();
    }

    public OfferView(Context context, View inflate) {
        super(context);
        this.context = context;
        this.view = inflate;
        initial();
    }

    public OfferView(Context context, AttributeSet attrs) {
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
            this.view = mInflater.inflate(R.layout.item_offer, this);
        }

        tv_title = view.findViewById(R.id.tv_title);
        ic_verified = view.findViewById(R.id.ic_verified);
        ic_vgosite = view.findViewById(R.id.ic_vgosite);
        ll_state = view.findViewById(R.id.ll_state);
        tv_state_value = view.findViewById(R.id.tv_state_value);
        ll_buttons = view.findViewById(R.id.ll_buttons);
        bt_accept = view.findViewById(R.id.bt_accept);
        bt_cancel = view.findViewById(R.id.bt_cancel);

        tv_give_total = view.findViewById(R.id.tv_give_total);
        tv_get_total = view.findViewById(R.id.tv_get_total);

        iv_arrow_give = view.findViewById(R.id.iv_arrow_send);
        iv_arrow_get = view.findViewById(R.id.iv_arrow_get);

        iv_give_eye = view.findViewById(R.id.iv_give_eye);
        iv_give_name = view.findViewById(R.id.iv_give_name);
        iv_get_eye = view.findViewById(R.id.iv_get_eye);
        iv_get_name = view.findViewById(R.id.iv_get_name);

        ll_give = view.findViewById(R.id.ll_give);
        ll_get = view.findViewById(R.id.ll_get);

        ll_give_info = view.findViewById(R.id.ll_give_info);
        ll_get_info = view.findViewById(R.id.ll_get_info);
        ll_get_cases = view.findViewById(R.id.ll_get_cases);
        tv_get_cases_count = view.findViewById(R.id.tv_get_cases_count);

        bt_accept.setSize(ButtonSize.SMALL);
        bt_cancel.setSize(ButtonSize.SQUARE);

        bt_accept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onActionListener != null)
                    onActionListener.onAccept();
            }
        });

        bt_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onActionListener != null)
                    onActionListener.onCancel();
            }
        });


        ll_give.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onActionListener != null)
                    onActionListener.onViewGiveItems();
            }
        });

        ll_get.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onActionListener != null)
                    onActionListener.onViewGetItems();
            }
        });

        this.gd = new GestureDetector(context, ql);
        this.view.setOnTouchListener(this);
    }

    public void setOnActionListener(OnActionListener onActionListener) {
        this.onActionListener = onActionListener;
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
