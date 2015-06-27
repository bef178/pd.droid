package th.intentSender;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import th.pd.R;

public class IntentActionAdapter extends BaseAdapter {
	public static final String ACTION_MANAGE_ROOT = "android.provider.action.MANAGE_ROOT";

	Context mContext;
	List<IntentActionItem> mModel;

	public IntentActionAdapter(Context context) {
		mContext = context;
		initModel();
	}

	private void initModel() {
		mModel = new ArrayList<IntentActionItem>(4);
		mModel.add(new IntentActionItem(Intent.ACTION_VIEW,
				"view"));
		mModel.add(new IntentActionItem(Intent.ACTION_GET_CONTENT,
				"get content"));
		mModel.add(new IntentActionItem(Intent.ACTION_OPEN_DOCUMENT,
				"open document"));
		mModel.add(new IntentActionItem(Intent.ACTION_CREATE_DOCUMENT,
				"create document"));
		mModel.add(new IntentActionItem(ACTION_MANAGE_ROOT,
				"manage root"));
	}

	@Override
	public int getCount() {
		return mModel.size();
	}

	@Override
	public Object getItem(int position) {
		return mModel.get(position);
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
			convertView.findViewById(R.id.icon).setVisibility(View.GONE);
		}
		((TextView) convertView.findViewById(R.id.text)).setText(
				((IntentActionItem) getItem(position)).caption);
		return convertView;
	}
}

class IntentActionItem {
	String action;
	String caption;

	public IntentActionItem(String action, String caption) {
		this.action = action;
		this.caption = caption;
	}
}
