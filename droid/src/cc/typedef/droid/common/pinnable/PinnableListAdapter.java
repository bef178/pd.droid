package cc.typedef.droid.common.pinnable;

import android.widget.BaseAdapter;

public abstract class PinnableListAdapter extends BaseAdapter {

    @Override
    public abstract Pinnable getItem(int position);
}
