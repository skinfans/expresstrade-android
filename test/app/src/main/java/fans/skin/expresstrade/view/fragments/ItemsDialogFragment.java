package fans.skin.expresstrade.view.fragments;

import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;

import com.squareup.picasso.*;

import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.managers.database.tables.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.containers.*;

import static fans.skin.expresstrade.view.AppButton.*;

public class ItemsDialogFragment extends AppDialogFragment implements OnClickListener {
    // =============================================================================================
    // VIEWERS
    // =============================================================================================

    private LinearLayout ll_keys;
    private LinearLayout ll_items;
    private TextView tv_keys_count;
    private TextView tv_items_count;
    private TextView tv_select_cost;
    private AppButton bt_close_top;

    private TextView tv_title;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private AppSwipeRefreshLayout swipeRefresh;

    private AppRecyclerView recyclerView;

    private ItemsContainer itemsContainer;

    private MakeOfferModel offer;

    private InventoryDialogFragment userInventoryDialog;

    private Handler handler = new Handler();

    private boolean isOpeningInventory;

    private List<ItemModel.Item> items;

    private LinearLayout bt_close;

    private int title;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    public AppFragment.FragmentName getFragmentName() {
        return AppFragment.FragmentName.MAKE_ITEMS_DIALOG;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_dialog_items, container, false);

        swipeRefresh = view.findViewById(R.id.body);
        recyclerView = view.findViewById(R.id.list_items);
        bt_close = view.findViewById(R.id.bt_close);
        bt_close_top = view.findViewById(R.id.bt_close_top);

        ll_keys = view.findViewById(R.id.ll_keys);
        tv_keys_count = view.findViewById(R.id.tv_keys_count);
        ll_items = view.findViewById(R.id.ll_items);
        tv_items_count = view.findViewById(R.id.tv_items_count);
        tv_select_cost = view.findViewById(R.id.tv_select_cost);

        tv_title = view.findViewById(R.id.tv_title);

        bt_close_top.setSize(ButtonSize.SMALL);

        itemsContainer = new ItemsContainer(getContext(), getLoaderManager(), inflater, recyclerView, swipeRefresh);
        itemsContainer.setCurrentFragment(AppFragment.FragmentName.INVENTORY);
        itemsContainer.setOnEventsListener(new ItemsContainer.OnEventsListener() {
            @Override
            public List onLoad() {

                App.logManager.debug("onLoad " + items.size());

                return items;
            }

            @Override
            public void onFinish(AppContainer.LoadStatus data) {
                itemsContainer.doResetSpace();

                switch (data) {
                    case NO_STUFF:
                    case COMPLETED:
                        render();
                        swipeRefresh.setRefreshing(false);
                        swipeRefresh.setEnabled(false);
                        break;
                }
            }

            @Override
            public void onRefresh() {
                App.logManager.debug("onRefresh");
            }

            @Override
            public void onPaging() {

            }

            @Override
            public void onScroll(int y) {
            }

            @Override
            public void onSpace(boolean isSpace) {

            }
        });

        bt_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        bt_close_top.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // Фиксируем размер окна
        swipeRefresh.getLayoutParams().height = App.display.heightPixels / 2;
        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(false);
        itemsContainer.doLoad();

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.MyDialog);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    public void render() {
        if (items == null || items.size() == 0) return;

        int keysCount = App.accountModule.getKeysCount(items);
        int itemsCount = App.accountModule.getItemsCount(items);

        boolean isMineItems = keysCount != 0 || itemsCount != 0;

        tv_select_cost.setText(!isMineItems ? "$0.00" : CommonUtils.getFormattedAmount(App.accountModule.getCostItems(items)));
        tv_select_cost.setTextColor(getResources().getColor(isMineItems ? R.color.gold : R.color.gray));

        tv_items_count.setText(itemsCount + "");
        tv_keys_count.setText(keysCount + "");

        ll_items.setVisibility(itemsCount != 0 ? VISIBLE : GONE);
        ll_keys.setVisibility(keysCount != 0 ? VISIBLE : GONE);

        tv_title.setText(getResources().getString(title));
    }

    // Установить предметы
    public void setItems(List<ItemModel.Item> array) {
        items = array;
    }

    public void setTitle(int string) {
        title = string;
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    @Override
    public void onEvent(OnEventModel event) {
        super.onEvent(event);

        switch ((event.id)) {
        }
    }

    @Override
    public void onClick(View v) {
    }
}
