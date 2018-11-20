package fans.skin.expresstrade.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fans.skin.expresstrade.App;
import fans.skin.expresstrade.R;
import fans.skin.expresstrade.view.viewers.FragmentViewer;
import fans.skin.expresstrade.view.AppFragment;

public class FrameInventoryFragment extends AppFragment implements View.OnClickListener {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public FragmentName getFragmentName() {
        return FragmentName.FRAME_INVENTORY;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_frame_content, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.fragmentViewer.addFragmentManager(FragmentViewer.FRAME_INVENTORY, getChildFragmentManager());

        InventoryFragment fragment = new InventoryFragment();
//        fragment.setUser(2126295);

        App.fragmentViewer.start(
                FragmentViewer.FRAME_INVENTORY,
                fragment,
                FragmentViewer.StartType.START_FIRST,
                null
        );

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
