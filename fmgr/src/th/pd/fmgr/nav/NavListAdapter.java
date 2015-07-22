package th.pd.fmgr.nav;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import th.pd.fmgr.R;

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
        list.add(new NavHeaderItem("Favorite"));
        for (File file : p) {
            list.add(new NavItem().initializeBy(file));
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

        if (navItem instanceof NavHeaderItem) {
            convertView = View.inflate(mContext, R.layout.nav_header_item,
                    null);
        } else if (navItem instanceof NavItem) {
            convertView = View.inflate(mContext, R.layout.nav_item,
                    null);
        }
        View caption = convertView.findViewById(android.R.id.title);
        ((TextView) caption).setText(navItem.getCaption());
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
