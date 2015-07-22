package th.pd.fmgr.nav;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import th.pd.common.android.OnActionCallback;
import th.pd.fmgr.R;
import th.pd.fmgr.nav.NavListAdapter;

public class NavFragment extends Fragment {

    private OnActionCallback mCallback = null;
    private NavListAdapter mNavListAdapter = null;

    public NavFragment(OnActionCallback callback) {
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

        mNavListAdapter = new NavListAdapter(getActivity());
        ListView listView = (ListView) getView().findViewById(
                android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(mNavListAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View itemView,
                    int position, long id) {
                Object data = mNavListAdapter.getItem(position);
                if (mCallback != null) {
                    mCallback.onAction(R.id.actionNavItemClicked, data);
                }
            }
        });
    }
}
