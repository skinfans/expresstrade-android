package fans.skin.expresstrade.view.containers;

import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.content.Loader;
import android.support.v4.widget.*;
import android.support.v7.widget.*;
import android.view.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.adapters.*;

import java.util.*;

public class UsersContainer {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    public DataContainer dataLoader;

    private UsersAdapter usersAdapter;
    private RecyclerView recyclerView;
    private LoaderManager loaderManager;
    public AppSwipeRefreshLayout swipeRefresh;

    private Long search_ops_id;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public UsersContainer(
            final Context context,
            final LoaderManager loaderManager,
            final LayoutInflater inflater,
            final View recyclerView,
            final View swipeRefresh
    ) {
        this.loaderManager = loaderManager;
        this.recyclerView = (RecyclerView) recyclerView;
        this.swipeRefresh = (AppSwipeRefreshLayout) swipeRefresh;

        usersAdapter = new UsersAdapter(this, context, inflater, new ArrayList<>());
        usersAdapter.updateData(new ArrayList<>());

        LinearLayoutManager lm = new LinearLayoutManager(context);

        this.recyclerView.setLayoutManager(lm);
        this.recyclerView.setAdapter(usersAdapter);

        this.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (dataLoader.onLoaderListener != null)
                    dataLoader.onLoaderListener.onRefresh();
            }
        });

        dataLoader = new DataContainer(context, usersAdapter, swipeRefresh);

        loaderManager.initLoader(0, null, dataLoader);
    }

    // =============================================================================================
    // INTERFACES
    // =============================================================================================

    public interface OnDataEventsListener {
        List onLoad();

        void onFinish(AppContainer.LoadStatus data);

        void onRefresh();
    }

    // =============================================================================================
    // GENERAL METHODS
    // =============================================================================================

    // Receives a list of users from the database
    public void doLoadData() {
        App.logManager.debug("doLoadData " + loaderManager);

        loaderManager.getLoader(0).forceLoad();
    }

    public int getItemsSize() {
        return usersAdapter.getItemCount();
    }

//    public void notifyItems() {
//        usersAdapter.notifyItems();
//    }

    // Set event handler
    public void setOnDataLoaderListener(OnDataEventsListener onDataEventsListener) {
        dataLoader.onLoaderListener = onDataEventsListener;
    }

    public void clearDataLoader() {
        dataLoader.onLoadFinished(null, new ArrayList());
    }

    // Set the current brow we found
    public void setCurrentSearchOpsId(Long ops_id) {
        search_ops_id = ops_id;
    }

    public boolean isUserSearching(Long ops_id) {
        return ops_id.equals(search_ops_id);
    }

    // =============================================================================================
    // CLASS LOADER
    // =============================================================================================

    private static class DataContainer extends AppContainer<List> implements LoaderManager.LoaderCallbacks<List> {
        private UsersAdapter adapter;
        private AppSwipeRefreshLayout swipeRefresh;
        public OnDataEventsListener onLoaderListener;

        public DataContainer(
                Context context,
                UsersAdapter adapter,
                View swipeRefresh
        ) {
            super(context);
            this.swipeRefresh = (AppSwipeRefreshLayout) swipeRefresh;
            this.adapter = adapter;
        }

        // All actions performed in the background
        @Override
        public List loadInBackground() {
            List list = onLoaderListener.onLoad();

            App.logManager.debug("loadInBackground " + list.size());

            return onLoaderListener != null ? list : null;
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

            App.logManager.debug("onLoadFinished " + data.size());

            if (data == null) return;

            // setLoadingStatus(false);
            swipeRefresh.setRefreshing(false);
            swipeRefresh.setEnabled(true);

            App.logManager.debug("onLoadFinished " + data.size());

            adapter.updateData(data);

            App.logManager.debug("onLoadFinished after " + data.size());

            adapter.notifyItems();

            if (onLoaderListener != null) {
                if (loader != null) {
                    if (data.size() == 0) onLoaderListener.onFinish(LoadStatus.NO_STUFF);
                    else onLoaderListener.onFinish(LoadStatus.COMPLETED);
                } else onLoaderListener.onFinish(LoadStatus.NETWORK_ERROR);
            }
        }

        @Override
        public void onLoaderReset(Loader<List> loader) {
        }
    }
}