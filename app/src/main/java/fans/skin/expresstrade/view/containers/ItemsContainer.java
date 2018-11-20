package fans.skin.expresstrade.view.containers;

import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.content.Loader;
import android.support.v4.widget.*;
import android.support.v7.widget.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.models.*;
import fans.skin.expresstrade.utils.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.adapters.*;

import java.util.*;

public class ItemsContainer extends AppContainer<List> implements LoaderManager.LoaderCallbacks<List> {
    // =============================================================================================
    // VIEWERS
    // =============================================================================================

    public ItemsAdapter itemsAdapter;
    public StaggeredGridLayoutManager layoutManager;
    public RecyclerView recyclerView;
    public AppSwipeRefreshLayout swipeRefresh;
    public AppFragment.FragmentName fragmentName;

    private LoaderManager loaderManager;
    private OnEventsListener onEventsListener;
    private HidingScrollListener hidingScrollListener;

    private View indicator;

    public Animation animFadeIn;
    public Animation animFadeOut;

    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    private List<String> swine_ids_dis = new ArrayList<>();

    private Handler handler = new Handler();
    private int gridSpanCount = 3;

    public InventoryModel inventory;

    public Integer appId = 1;

    public int page = 0;
    public boolean isLoading = false; // If there is a download at the moment
    public boolean isLoadEnd = false; // If the last page was loaded. No more materials

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public ItemsContainer(
            final Context context,
            final LoaderManager loaderManager,
            final LayoutInflater inflater,
            final View recyclerView,
            final View swipeRefresh
    ) {
        super(context);
        this.loaderManager = loaderManager;
        this.recyclerView = (RecyclerView) recyclerView;
        this.swipeRefresh = (AppSwipeRefreshLayout) swipeRefresh;

        loaderManager.initLoader(0, null, this);

        animFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);

        layoutManager = new StaggeredGridLayoutManager(gridSpanCount, StaggeredGridLayoutManager.VERTICAL);

        itemsAdapter = new ItemsAdapter(this, getContext(), inflater, layoutManager, new ArrayList<>());
        itemsAdapter.updateData(new ArrayList<>());
        itemsAdapter.setSpanCount(gridSpanCount);

        hidingScrollListener = new HidingScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onHide() {
                App.logManager.debug("SCROLL ON HIDE");
                onEventsListener.onSpace(true);
            }

            @Override
            public void onShow() {
                onEventsListener.onSpace(false);
            }
        };

        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(layoutManager);

        this.recyclerView.setAdapter(itemsAdapter);

        this.recyclerView.addOnScrollListener(hidingScrollListener);
        this.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (onEventsListener != null) {
                    itemsAdapter.lastPosition = -1;
                    swine_ids_dis.clear();

                    onEventsListener.onRefresh();
                }
            }
        });
    }

    // =============================================================================================
    // INTERFACES
    // =============================================================================================

    public interface OnEventsListener {
        List onLoad();

        void onFinish(LoadStatus data);

        void onRefresh();

        void onPaging();

        void onScroll(int y);

        void onSpace(boolean isSpace);
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    // The main loader method is responsible for loading content
    public void doLoad() {
        setLoadingStatus(true);
        loaderManager.getLoader(0).forceLoad();
    }

    // Set event handler
    public void setOnEventsListener(OnEventsListener onEventsListener) {
        this.onEventsListener = onEventsListener;
    }

    public OnEventsListener getOnEventsListener() {
        return onEventsListener;
    }

    // Set download status
    public void setLoadingStatus(boolean isLoading) {
        App.logManager.debug("setLoadingStatus " + isLoading);
        this.isLoading = isLoading;
        this.swipeRefresh.setEnabled(!isLoading);
    }

    // Set the view of the dynamic content loading indicator
    public void setIndicatorView(View view) {
        this.indicator = view;
    }

    public void setContainerPaddingTop(float y) {
        float density = App.display.density;
        swipeRefresh.setProgressViewOffset(false, (int) ((y - 50) * density), (int) ((y + 50) * density));
        recyclerView.setPadding((int) (6 * density), (int) (y * density), 0, 0);
    }

    public void setCurrentFragment(AppFragment.FragmentName fragmentName) {
        this.fragmentName = fragmentName;
    }

    // Specify inventory model
    public void setInventoryModel(InventoryModel model) {
        inventory = model;
    }

    // Get the number of all loader items
    public int getItemCount() {
        return itemsAdapter.getItemCount();
    }

    public boolean checkScrollPositionTop() {
        return layoutManager.findFirstVisibleItemPositions(null)[0] == 0;
    }

    // Get object by item ID
    public ItemModel.Item getItemById(String swine_id) {
        for (Object el : itemsAdapter.data) {
            ItemModel.Item item = (ItemModel.Item) el;
            if (item.id.equals(swine_id)) return item;
        }
        return null;
    }

    // Get the view item by its ID
    public View getViewById(String swine_id) {
        return recyclerView.findViewWithTag(swine_id);
    }

    public void doResetSpace() {
        // Reset the variables, because when you swipe and move to another fragment in
        // (MainFragment), the preview unfolds into a compact view. when going back
        // to the current snippet, hidingScrollListener thinks the content is already stretched
        hidingScrollListener.doReset();
    }

    public void doLoadingCancel() {
        page--;
        setLoadingStatus(false);
        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(true);
    }

    // =============================================================================================
    // EVENT METHODS
    // =============================================================================================

    // All actions performed in the background
    @Override
    public List loadInBackground() {
        App.logManager.debug("loadInBackground");
        if (!isLoading) return null;
        List list = onEventsListener != null ? onEventsListener.onLoad() : null;

        return list;
    }

    // Create loader
    @Override
    public Loader<List> onCreateLoader(int id, Bundle args) {
        App.logManager.debug("onCreateLoader");
        return this;
    }

    // Loader worked and returns value
    @Override
    public void onLoadFinished(Loader<List> loader, List data) {
        // loader equals null only when we run the method manually in the absence of the Internet

        App.logManager.debug("onLoadFinished " + isLoading);
        if ((!isLoading && loader != null) || data == null) return;

        setLoadingStatus(false);
        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(true);
        isLoadEnd = data.size() == 0;

        if (loader == null || page == 0) {
            App.logManager.debug("Update items");
            itemsAdapter.updateData(data);
            itemsAdapter.notifyDataSetChanged();
        } else {
            App.logManager.debug("Add items");
            itemsAdapter.pushData(data);
            itemsAdapter.notifyItemRangeInserted(itemsAdapter.getItemCount(), data.size());
        }

        if (onEventsListener != null) {
            if (loader != null) {
                if (page == 0 && data.size() == 0) onEventsListener.onFinish(LoadStatus.NO_STUFF);
                else onEventsListener.onFinish(LoadStatus.COMPLETED);
            } else onEventsListener.onFinish(LoadStatus.NETWORK_ERROR);
        }

        if (loader != null && !isLoadEnd && page == 0) recyclerView.startAnimation(animFadeIn);
    }

    @Override
    public void onLoaderReset(Loader<List> loader) {

    }
}