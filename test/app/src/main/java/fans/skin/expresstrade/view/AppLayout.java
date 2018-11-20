package fans.skin.expresstrade.view;


import android.content.*;
import android.view.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.models.*;

public class AppLayout {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private boolean isFocus = false;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public AppLayout(Context context, LayoutInflater inflater, View view) {
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    protected void onCreate() {
    }

    protected void onPause() {

    }

    protected void onResume() {

    }

    protected void onDestroy() {

    }

    public void onFocus() {
        this.isFocus = true;
    }

    public void onBlur() {
        this.isFocus = false;
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    protected void onEvent(OnEventModel object) {
    }

//    protected void onClick(View v) {
//    }
}
