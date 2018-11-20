package fans.skin.expresstrade.view.containers;

import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.content.Loader;
import android.support.v4.widget.*;
import android.support.v7.widget.*;
import android.view.*;

import java.util.*;

import fans.skin.expresstrade.*;
import fans.skin.expresstrade.view.*;
import fans.skin.expresstrade.view.adapters.*;

public class OffersContainer {
    // =============================================================================================
    // VARIABLES
    // =============================================================================================

    public DataContainer dataLoader;

    private OffersAdapter offersAdapter;
    private RecyclerView recyclerView;
    private LoaderManager loaderManager;
    public AppSwipeRefreshLayout swipeRefresh;
    public FragmentManager fragmentManager;

    public boolean isAnim = true;

    // =============================================================================================
    // CONSTRUCTOR
    // =============================================================================================

    public OffersContainer(
            final Context context,
            final LoaderManager loaderManager,
            final LayoutInflater inflater,
            final View recyclerView,
            final View swipeRefresh
    ) {
        this.loaderManager = loaderManager;
        this.recyclerView = (RecyclerView) recyclerView;
        this.swipeRefresh = (AppSwipeRefreshLayout) swipeRefresh;

        offersAdapter = new OffersAdapter(this, context, inflater, new ArrayList<>());
        offersAdapter.updateData(new ArrayList<>());

        LinearLayoutManager lm = new LinearLayoutManager(context);

        this.recyclerView.setLayoutManager(lm);
        this.recyclerView.setAdapter(offersAdapter);

        this.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (dataLoader.onLoaderListener != null) {
                    offersAdapter.lastPosition = -1;
                    offersAdapter.lastAnimTime = 0;
                    dataLoader.onLoaderListener.onRefresh();
                }
            }
        });

        dataLoader = new DataContainer(context, offersAdapter, swipeRefresh);

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

    // Получает из БД список юзеров
    public void doLoadData(boolean isAnim) {
        App.logManager.debug("doLoadData " + isAnim);

        this.isAnim = isAnim;

        if (isAnim) {
            offersAdapter.lastPosition = -1;
            offersAdapter.lastAnimTime = 0;
        }

        loaderManager.getLoader(0).forceLoad();
    }

    public void doLoadData() {
        doLoadData(true);
    }

    public int getItemsSize() {
        return offersAdapter.getItemCount();
    }


    // Устанвить обработчик события
    public void setOnDataLoaderListener(OnDataEventsListener onDataEventsListener) {
        dataLoader.onLoaderListener = onDataEventsListener;
    }

    public void clearDataLoader() {
        dataLoader.onLoadFinished(null, new ArrayList());
    }

    public void setFragmentManager(FragmentManager fm) {
        fragmentManager = fm;
    }

    // =============================================================================================
    // CLASS LOADER
    // =============================================================================================

    private static class DataContainer extends AppContainer<List> implements LoaderManager.LoaderCallbacks<List> {
        private OffersAdapter adapter;
        private AppSwipeRefreshLayout swipeRefresh;
        public OnDataEventsListener onLoaderListener;

        public DataContainer(
                Context context,
                OffersAdapter adapter,
                View swipeRefresh
        ) {
            super(context);
            this.swipeRefresh = (AppSwipeRefreshLayout) swipeRefresh;
            this.adapter = adapter;
        }

        // Все действия выполняемые в бэкграунде
        @Override
        public List loadInBackground() {
            List list = onLoaderListener.onLoad();

            App.logManager.debug("loadInBackground " + list.size());

            return onLoaderListener != null ? list : null;
        }

        // Создание лоадера
        @Override
        public Loader<List> onCreateLoader(int id, Bundle args) {
            App.logManager.debug("onCreateLoader");
            return this;
        }

        // Лоадер отработал и возвращает значение
        @Override
        public void onLoadFinished(Loader<List> loader, List data) {
            // loader равняется null только тогда, когда мы выполняем метод вручную при отсутствии интернета

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