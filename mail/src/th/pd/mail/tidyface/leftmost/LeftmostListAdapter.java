package th.pd.mail.tidyface.leftmost;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import th.pd.common.android.pinnable.PinnableListAdapter;
import th.pd.mail.R;
import th.pd.mail.fastsync.Mailbox;

public final class LeftmostListAdapter extends PinnableListAdapter {

    private static final List<LeftItem> EMPTY_DATA_SET =
            new ArrayList<LeftItem>(0);

    private Context mContext;

    private List<LeftItem> mDataSet;

    public LeftmostListAdapter(Context context, List<LeftItem> dataSet) {
        mContext = context;
        setData(dataSet);
    }

    @Override
    public int getCount() {
        return mDataSet.size();
    }

    @Override
    public LeftItem getItem(int position) {
        if (position >= 0 && position < mDataSet.size()) {
            return mDataSet.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LeftItem item = getItem(position);
        if (item == null) {
            return null;
        }

        if (item instanceof LeftAccounts) {
            if (convertView == null
                    || convertView.getId() != R.id.typeLeftAccount) {
                convertView = View.inflate(mContext, R.layout.left_account,
                        null);
            }
            Mailbox mailbox = ((LeftAccounts) item).getCurrent();
            if (mailbox != null) {
                ((TextView) convertView.findViewById(android.R.id.text1))
                        .setText(mailbox.getFriendlyCaption());
                ((TextView) convertView.findViewById(android.R.id.text2))
                        .setText(mailbox.getAddr());
                // TODO set avatar
            } else {
                ((TextView) convertView.findViewById(android.R.id.text1))
                        .setText(null);
                ((TextView) convertView.findViewById(android.R.id.text2))
                        .setText(null);
            }
            return convertView;
        }

        if (item instanceof LeftHeader) {
            if (convertView == null
                    || convertView.getId() != R.id.typeLeftHeader) {
                convertView = View.inflate(mContext, R.layout.left_header,
                        null);
            }
        } else if (item instanceof LeftItem) {
            if (convertView == null
                    || convertView.getId() != R.id.typeLeftItem) {
                convertView = View.inflate(mContext, R.layout.left_item,
                        null);
            }
        }

        ((TextView) convertView.findViewById(android.R.id.title))
                .setText(item.getCaption());

        return convertView;
    }

    public void setData(List<LeftItem> dataSet) {
        if (dataSet == null || dataSet.isEmpty()) {
            dataSet = EMPTY_DATA_SET;
        }
        if (dataSet != mDataSet) {
            mDataSet = dataSet;
            notifyDataSetChanged();
        }
    }
}
