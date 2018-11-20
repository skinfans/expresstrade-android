package fans.skin.expresstrade.models;

import fans.skin.expresstrade.managers.network.*;
import fans.skin.expresstrade.managers.network.NetworkManager.*;

public class PostModel {
    public StatusCode status;
    public NetworkManager.PostType type;
    public MethodType method;
    public NetworkQuery query;
    public NetworkCallback networkCallback;
    public long time;
    public int count;
    private boolean isReload;

    public PostModel(
            NetworkManager.PostType type,
            MethodType method,
            NetworkQuery query,
            NetworkCallback networkCallback
    ) {
        this.status = StatusCode.STATUS_WAITING;
        this.type = type;
        this.method = method;
        this.query = query;
        this.networkCallback = networkCallback;
        this.count = 1;
        this.time = System.currentTimeMillis();
        this.isReload = true;
    }

    // ***

    public void setReload(boolean isReload) {
        this.isReload = isReload;
    }

    public boolean getReload() {
        return this.isReload;
    }
}
