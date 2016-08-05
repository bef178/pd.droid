package t.typedef.droid.pinnable;

import android.widget.BaseAdapter;

public abstract class PinnableListAdapter extends BaseAdapter {

    @Override
    public abstract Pinnable getItem(int position);
}
