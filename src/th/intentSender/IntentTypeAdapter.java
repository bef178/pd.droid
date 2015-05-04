package th.intentSender;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import th.common.MimeUtil;
import th.pd.R;

import java.util.List;
import java.util.Map;

public class IntentTypeAdapter extends BaseAdapter {
	Context mContext;
	List<Map.Entry<String, Integer>> mModel;

	public IntentTypeAdapter(Context context) {
		mContext = context;
		mModel = MimeUtil.mimeTypeSortedList();
	}

	@Override
	public int getCount() {
		return mModel.size();
	}

	/**
	 * return IntentTypeItem
	 *
	 * @Override
	 */
	@Override
	public Object getItem(int position) {
		Map.Entry<String, Integer> entry = mModel.get(position);
		return new IntentTypeItem(entry.getKey(), entry.getValue());
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.icontext_item, parent, false);
		}
		((TextView) convertView.findViewById(R.id.text)).setText(
				((IntentTypeItem) getItem(position)).type);
		((ImageView) convertView.findViewById(R.id.icon)).setImageResource(
				((IntentTypeItem) getItem(position)).iconResId);
		return convertView;
	}
}

class IntentTypeItem implements Comparable<IntentTypeItem> {
	String type;
	int iconResId;

	public IntentTypeItem(String type, int iconResId) {
		this.type = type;
		this.iconResId = iconResId;
	}

	@Override
	public int compareTo(IntentTypeItem another) {
		return type.compareTo(another.type);
	}
}
