package fans.skin.expresstrade.view.fragments;

import android.os.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.viewers.*;

import static fans.skin.expresstrade.view.AppButton.*;

public class InventoryDialogFragment extends AppDialogFragment implements OnClickListener {
    // =============================================================================================
    // VIEWERS
    // =============================================================================================

    private ImageView iv_recipient_avatar;
    private TextView tv_recipient_name;
    private AppButton bt_recipient_view;
    private AppButton bt_close;
    private AppButton bt_partner_selected_view;
    private AppButton bt_mine_selected_view;
    private RelativeLayout rl_main;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private MakeOfferModel offer;
    private FrameLayout container;

    private Long user_ops_id;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_offer_inv, container, false);

        container = view.findViewById(R.id.container);
        rl_main = view.findViewById(R.id.rl_main);

        App.fragmentViewer.addFragmentManager(FragmentViewer.FRAME_POPUPS, getChildFragmentManager());

        InventoryFragment fragment = new InventoryFragment();
        fragment.setUser(user_ops_id);

        App.fragmentViewer.start(
                FragmentViewer.FRAME_POPUPS,
                fragment,
                FragmentViewer.StartType.START_FIRST,
                null
        );

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    // Specify user ops_id to load inventory
    public void setUser(Long ops_id) {
        user_ops_id = ops_id;
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    @Override
    public void onClick(View v) {
    }
}
