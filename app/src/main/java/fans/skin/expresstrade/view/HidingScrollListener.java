package fans.skin.expresstrade.view;

import android.support.v7.widget.*;

import fans.skin.expresstrade.*;

public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {
    private float HIDE_THRESHOLD = 100 * App.display.density;
    private float OFFSET = 150 * App.display.density;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;
    private long distance = 0;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        distance += dy;

        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible && distance > OFFSET) {
            onHide();
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            onShow();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy;
        }
    }

    public void doReset() {
        scrolledDistance = 0;
        controlsVisible = true;
    }

    public abstract void onHide();

    public abstract void onShow();

    public void setDistance(int HIDE_THRESHOLD) {
        this.HIDE_THRESHOLD = HIDE_THRESHOLD;
    }

    public void setOffset(float OFFSET) {
        this.OFFSET = OFFSET;
    }
}
