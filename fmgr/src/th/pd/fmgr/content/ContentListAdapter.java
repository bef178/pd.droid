package th.pd.fmgr.content;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import th.pd.common.android.FormatUtil;
import th.pd.fmgr.R;

public final class ContentListAdapter extends BaseAdapter {

    private static final List<ItemEntry> EMPTY_DATA_SET =
            new ArrayList<ItemEntry>(0);

    private static List<ItemEntry> getDefaultDataSet() {
        List<ItemEntry> list = new ArrayList<ItemEntry>();
        return list;
    }

    private Context mContext;

    private List<ItemEntry> mDataSet;

    public ContentListAdapter(Context context) {
        this(context, getDefaultDataSet());
    }

    public ContentListAdapter(Context context, List<ItemEntry> dataSet) {
        mContext = context;
        mDataSet = dataSet != null ? dataSet : EMPTY_DATA_SET;
    }

    @Override
    public int getCount() {
        return mDataSet.size();
    }

    @Override
    public ItemEntry getItem(int position) {
        if (position >= 0 && position < mDataSet.size()) {
            return mDataSet.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemEntry itemEntry = getItem(position);
        if (itemEntry == null) {
            return null;
        }

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_entry, null);
        }

        TextView tvCaption = (TextView) convertView
                .findViewById(android.R.id.title);
        tvCaption.setText(itemEntry.getCaption());

        TextView tvSize = (TextView) convertView.findViewById(R.id.tvSize);
        tvSize.setText(FormatUtil.formatBytes(itemEntry.getBytes()));

        TextView tvMimeType = (TextView) convertView
                .findViewById(R.id.tvMimeType);
        tvMimeType.setText(itemEntry.getMimeType());

        TextView tvLastModified = (TextView) convertView
                .findViewById(R.id.tvLastModified);
        tvLastModified.setText(FormatUtil.formatTime(itemEntry.getLastModified()));

        return convertView;
    }

    public void setDataSet(List<ItemEntry> dataSet) {
        if (dataSet == null) {
            dataSet = EMPTY_DATA_SET;
        }
        if (dataSet != mDataSet) {
            mDataSet = dataSet;
            notifyDataSetChanged();
        }
    }
}
