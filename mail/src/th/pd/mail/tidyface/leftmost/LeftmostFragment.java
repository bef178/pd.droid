package th.pd.mail.tidyface.leftmost;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import th.pd.common.android.OnActionCallback;
import th.pd.common.android.pinnable.PinnableList;
import th.pd.mail.R;
import th.pd.mail.tidyface.FakeData;

public class LeftmostFragment extends Fragment {

    private OnActionCallback mCallback = null;

    private LeftmostListAdapter mAdapter = null;

    public LeftmostFragment() {
        this(null);
    }

    public LeftmostFragment(OnActionCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO
        mAdapter = new LeftmostListAdapter(getActivity(),
                FakeData.getLeftmostSampleDataSet(getActivity()));

        PinnableList vList = (PinnableList) getView().findViewById(
                android.R.id.list);
        vList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        vList.setAdapter(mAdapter);
        vList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View itemView,
                    int position, long id) {
                LeftItem item = mAdapter.getItem(position);
                if (itemView.getId() == R.id.typeLeftItem) {
                    Toast.makeText(getActivity(),
                            "caption:" + item.getCaption(),
                            Toast.LENGTH_SHORT).show();
                    if (mCallback != null) {
                        mCallback.onAction(-1, item);
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leftmost, container,
                false);
    }
}
