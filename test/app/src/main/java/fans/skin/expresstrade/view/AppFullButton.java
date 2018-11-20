package fans.skin.expresstrade.view;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import fans.skin.expresstrade.*;

import static fans.skin.expresstrade.models.OnEventModel.logManager;

public class AppFullButton extends RelativeLayout {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private Context context;
    private LinearLayout rl;
    private TextView tx;
    private ProgressBar pb;
    private ImageView iv;

    private ButtonSize buttonSize;
    private int buttonText;
    private int buttonIcon;

    private int padding = 30;
    private int animTime = 200;

    private Handler handler = new Handler();

    private Boolean isLoaderVisible = false;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public AppFullButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AppButton, 0, 0);

        buttonSize = ButtonSize.BIG;

        try {
            buttonSize = a.getInt(R.styleable.AppButton_sSize, 0) == 0 ? ButtonSize.BIG : ButtonSize.SMALL;

            buttonText = a.getResourceId(R.styleable.AppButton_sText, 0);
            buttonIcon = a.getResourceId(R.styleable.AppButton_sIcon, 0);
        } finally {
            a.recycle();
        }

        initial();
    }

    // =============================================================================================
    // ENUM
    // =============================================================================================

    public enum ButtonSize {
        BIG, SMALL
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    private void initial() {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.block_full_button, this);

        rl = view.findViewById(R.id.rl);
        tx = view.findViewById(R.id.bt);
        pb = view.findViewById(R.id.pb);
        iv = view.findViewById(R.id.iv);

        pb.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        // По умолчанию
        setSize(buttonSize);

        if (buttonText != 0)
            setText(buttonText);

        if (buttonIcon != 0)
            setIcon(buttonIcon);
    }

    public void setText(String string) {
        string = string.replace("OPSKINS", "<b>OPSKINS</b>");

        tx.setText(Html.fromHtml(string));
        tx.setVisibility(VISIBLE);
    }

    public void setText(int res) {
        this.setText(App.context.getResources().getString(res));
    }

    public void setSize(ButtonSize buttonSize) {
        this.buttonSize = buttonSize;

        switch (buttonSize) {
            case BIG:
                padding = (int) (30 * App.display.density);
                rl.setMinimumHeight((int) (50 * App.display.density));
                rl.setPadding(padding, 0, padding, 0);
                break;

            case SMALL:
                padding = (int) (18 * App.display.density);
                rl.getLayoutParams().height = (int) (36 * App.display.density);
                rl.setPadding(padding, 0, padding, 0);
                break;
        }
    }

    public void setColor(int color) {
        this.tx.setTextColor(App.context.getResources().getColor(color));
    }

    public void setIcon(int icon) {
        this.buttonIcon = icon;

        this.iv.setBackgroundResource(icon);
        this.iv.setVisibility(VISIBLE);
    }

    public void setLoaderVisible(boolean isVisible) {
        if (isVisible) {
            if (isLoaderVisible) return;
            isLoaderVisible = true;
            pb.setVisibility(VISIBLE);
            pb.setAlpha(0f);

            if (buttonText != 0) tx.setVisibility(GONE);
            if (buttonIcon != 0) iv.setVisibility(GONE);
        } else {
            if (!isLoaderVisible) return;
            isLoaderVisible = false;

            pb.setAlpha(1f);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pb.setVisibility(GONE);

                    if (buttonText != 0) {
                        tx.setVisibility(VISIBLE);
                        tx.setAlpha(0f);
                        tx.animate().setDuration(animTime).alpha(1f).start();
                    }

                    if (buttonIcon != 0) {
                        iv.setVisibility(GONE);
                        iv.setVisibility(VISIBLE);
                        iv.setAlpha(0f);
                        iv.animate().setDuration(animTime).alpha(1f).start();
                    }
                }
            }, animTime);
        }

        pb.animate().setDuration(animTime).alpha(isVisible ? 1f : 0f).start();
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================
}
