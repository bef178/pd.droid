package t.typedef.droid;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public abstract class OnScrollReachEndListener implements OnScrollListener {

    private int numOverlook = 0;

    public abstract void onReachEnd();

    @Override
    public void onScroll(AbsListView view, int firstVisible, int numVisibles,
            int numTotal) {
        if (firstVisible + numVisibles + numOverlook >= numTotal) {
            onReachEnd();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // dummy
    }

    public OnScrollReachEndListener setNumOverlook(int n) {
        if (n >= 0) {
            this.numOverlook = n;
        }
        return this;
    }
}
