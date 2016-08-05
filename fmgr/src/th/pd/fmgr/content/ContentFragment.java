package th.pd.fmgr.content;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import t.typedef.droid.OnActionCallback;
import th.pd.fmgr.R;

public class ContentFragment extends Fragment {

    private OnActionCallback mCallback = null;
    private ContentListAdapter mContentListAdapter = null;

    public ContentFragment(OnActionCallback callback) {
        mCallback = callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nav_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContentListAdapter = new ContentListAdapter(getActivity());
        ListView listView = (ListView) getView().findViewById(
                android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(mContentListAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View itemView,
                    int position, long id) {
                Object data = mContentListAdapter.getItem(position);
                if (mCallback != null) {
                    mCallback.onAction(R.id.actionNavItemClicked, data);
                }
            }
        });
    }
}
