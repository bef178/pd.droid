package th.pd.fmgr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

final class NavActionItem extends NavItem {

    protected Uri uri = null;

    public NavActionItem(File file) {
        this.uri = Uri.fromFile(file);
        this.label = file.getName();
    }

    public Uri getUri() {
        return uri;
    }
}

final class NavHeaderItem extends NavItem {

    public NavHeaderItem(String label) {
        this.label = label;
    }
}

class NavItem {

    protected String label = null; // caption for display

    protected String getLabel() {
        return label;
    }
}

public final class NavListAdapter extends BaseAdapter {

    private static final List<NavItem> EMPTY_DATA_SET =
            new ArrayList<NavItem>(0);

    private static List<NavItem> getDefaultDataSet() {
        File[] p = new File[] {
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS),
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES),
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC),
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES),
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS),
        };
        List<NavItem> list = new ArrayList<NavItem>(p.length + 1);
        list.add(new NavHeaderItem("favorite"));
        for (File file : p) {
            list.add(new NavActionItem(file));
        }
        return list;
    }

    private Context mContext;

    private List<NavItem> mDataSet;

    public NavListAdapter(Context context) {
        this(context, getDefaultDataSet());
    }

    public NavListAdapter(Context context, List<NavItem> dataSet) {
        mContext = context;
        mDataSet = dataSet != null ? dataSet : EMPTY_DATA_SET;
    }

    @Override
    public int getCount() {
        return mDataSet.size();
    }

    @Override
    public NavItem getItem(int position) {
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
        NavItem navItem = getItem(position);
        if (navItem == null) {
            return null;
        }

        if (convertView == null) {
            convertView = View.inflate(mContext,
                    R.layout.mock_filemanager_nav_item_layout, null);
        }
        if (navItem instanceof NavHeaderItem) {
            View headerLabel = convertView.findViewById(R.id.headerLabel);
            View actionLabel = convertView.findViewById(R.id.actionLabel);
            headerLabel.setVisibility(View.VISIBLE);
            actionLabel.setVisibility(View.GONE);
            ((TextView) headerLabel).setText(navItem.getLabel());
        } else if (navItem instanceof NavActionItem) {
            View headerLabel = convertView.findViewById(R.id.headerLabel);
            View actionLabel = convertView.findViewById(R.id.actionLabel);
            headerLabel.setVisibility(View.GONE);
            actionLabel.setVisibility(View.VISIBLE);
            ((TextView) actionLabel).setText(navItem.getLabel());
        }

        return convertView;
    }

    public void setDataSet(List<NavItem> dataSet) {
        if (dataSet == null) {
            dataSet = EMPTY_DATA_SET;
        }
        if (dataSet != mDataSet) {
            mDataSet = dataSet;
            notifyDataSetChanged();
        }
    }
}
