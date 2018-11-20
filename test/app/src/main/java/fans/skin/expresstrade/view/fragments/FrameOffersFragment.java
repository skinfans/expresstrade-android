package fans.skin.expresstrade.view.fragments;

import android.os.*;
import android.view.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.viewers.*;

public class FrameOffersFragment extends AppFragment implements View.OnClickListener {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public FragmentName getFragmentName() {
        return FragmentName.FRAME_HISTORY;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_frame_content, container, false);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.fragmentViewer.addFragmentManager(FragmentViewer.FRAME_OFFERS, getChildFragmentManager());

        App.fragmentViewer.start(
                FragmentViewer.FRAME_OFFERS,
                new OffersFragment(),
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
