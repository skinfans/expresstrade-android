package fans.skin.expresstrade.view;

import android.content.*;
import android.support.v4.content.AsyncTaskLoader;

public class AppContainer<T> extends AsyncTaskLoader<T> {
    public AppContainer(Context context) {
        super(context);
    }

    public enum LoadStatus {
        COMPLETED,
        NETWORK_ERROR,
        NO_STUFF
    }

    @Override
    public T loadInBackground() {
        return null;
    }
}
