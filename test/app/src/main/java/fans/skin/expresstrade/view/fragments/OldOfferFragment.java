package fans.skin.expresstrade.view.fragments;

import android.os.*;
import android.view.*;
import android.widget.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.view.*;

public class OldOfferFragment extends AppFragment implements View.OnClickListener {
    // =============================================================================================
    // VIEWERS
    // =============================================================================================

    TextView tv_partner;
    TextView tv_total_cost;

    AppButton bt_close;
    AppButton bt_set_message;
    AppButton bt_remove;

    AppRecyclerView list_items;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_offers, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    @Override
    public void onClick(View v) {
    }
}
