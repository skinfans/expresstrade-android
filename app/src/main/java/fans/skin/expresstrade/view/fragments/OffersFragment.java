package fans.skin.expresstrade.view.fragments;

import android.os.*;
import android.view.*;

import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.event.*;
import fans.skin.expresstrade.managers.event.EventManager.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.containers.*;

import static fans.skin.expresstrade.modules.trade.TradeModule.STATE_ACTIVE;

public class OffersFragment extends AppFragment implements View.OnClickListener {
    // =============================================================================================
    // VIEWERS
    // =============================================================================================

    private OffersContainer offersContainer;
    private AppRecyclerView recyclerView;
    private View swipeRefresh;
    private AppButton bt_offers_new;
    private AppButton bt_history;

    private boolean isNewVisible = true;
    private Handler handler = new Handler();
    private Runnable updateData;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private boolean isAnimItems = true;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public FragmentName getFragmentName() {
        return FragmentName.FRAME_HISTORY;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_offers, container, false);

        recyclerView = view.findViewById(R.id.list_users);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        bt_offers_new = view.findViewById(R.id.bt_offers_new);
        bt_history = view.findViewById(R.id.bt_history);

        bt_offers_new.setSize(AppButton.ButtonSize.SMALL);
        bt_history.setSize(AppButton.ButtonSize.SMALL);

        bt_offers_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNewVisible = true;
                isAnimItems = true;
                offersContainer.doLoadData();
            }
        });

        bt_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNewVisible = false;
                isAnimItems = true;
                offersContainer.doLoadData();
            }
        });

        offersContainer = new OffersContainer(getContext(), getLoaderManager(), inflater, recyclerView, swipeRefresh);
        offersContainer.setFragmentManager(getChildFragmentManager());
        offersContainer.setOnDataLoaderListener(new OffersContainer.OnDataEventsListener() {
            @Override
            public List onLoad() {
                App.logManager.debug("onLoad");

                List<OfferModel.Offer> offers = new ArrayList<>();

                if (App.tradeModule.getActiveCount(App.tradeModule.offers) == 0) {
                    return App.tradeModule.offers;
                }

                for (OfferModel.Offer offer : App.tradeModule.offers) {
                    if(isNewVisible && offer.state != STATE_ACTIVE)
                        continue;

                    if(!isNewVisible && offer.state == STATE_ACTIVE)
                        continue;

                    offers.add(offer);
                }


                return offers;
            }

            @Override
            public void onFinish(AppContainer.LoadStatus data) {
                App.logManager.debug("onFinishData " + data.name());
                isAnimItems = false;

                switch (data) {
                    case NO_STUFF:

                        break;

                    case COMPLETED:

                        break;

                    case NETWORK_ERROR:

                        break;
                }
            }

            @Override
            public void onRefresh() {
                isAnimItems = true;
                loadItems();
                App.logManager.debug("onRefresh");
            }
        });

        loadItems();

        updateData = new Runnable(){
            public void run(){
                App.logManager.debug("Auto load");

                if (isActive)
                    loadItems();

                handler.postDelayed(updateData,15000);
            }
        };

        handler.postDelayed(updateData,15000);



        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void loadItems() {
        App.tradeModule.reqGetOffers(null);
    }

    public void updateVisibleNewButton() {
        boolean isNew = App.tradeModule.getActiveCount(App.tradeModule.offers) != 0;
        bt_offers_new.setVisibility(isNew ? View.VISIBLE : View.GONE);

        App.eventManager.doEvent(isNew ? EventType.ON_DO_NEW_OFFERS_INDICATOR_VISIBLE : EventType.ON_DO_NEW_OFFERS_INDICATOR_INVISIBLE);
    }


    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================


    @Override
    public void onEvent(OnEventModel object) {
        super.onEvent(object);

        AcceptModel model;

        switch ((object.id)) {
            // ON_EVENTS_INITIALED
            case 8979295:
            case 1685548:
            case 1975172:
            case 5858641:
                loadItems();
                break;

            // ON_TRADE_GET_OFFERS_RECEIVED
            case 2953969:
                offersContainer.doLoadData(isAnimItems);
                isAnimItems = false;
                updateVisibleNewButton();
                break;

            // ON_TRADE_GET_OFFERS_NOT_RECEIVED
            case 7120639:
                offersContainer.doLoadData(isAnimItems);
                isAnimItems = false;
                updateVisibleNewButton();
                break;

            // ON_ACCOUNT_2FA_CODE_UPDATED
            case 7715232:
                model = (AcceptModel) object.object;
                if (!model.isAcceptOffer || model.id == null || model.id == 0) return;

                // Выполняем запрос
                App.tradeModule.reqAcceptOffer(model.id, true);

                break;
        }
    }

    @Override
    public void onClick(View v) {
    }
}
