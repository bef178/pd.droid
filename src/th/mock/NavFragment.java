package th.mock;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import th.pd.R;

public class NavFragment extends Fragment {

    interface ActionListener {

        boolean onNavItemSelected(Object actionArg);
    }

    private ActionListener mActionListener = null;
    private ListView mNavList = null;

    public NavFragment() {
    }

    public NavFragment(ActionListener listener) {
        mActionListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.mock_filemanager_nav_fragment_layout,
                container,
                false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mNavList == null) {
            return;
        }
        mNavList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mNavList.setAdapter(new NavListAdapter(getActivity()));
        mNavList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View itemView,
                    int position, long id) {
                if (parent != mNavList) {
                    return;
                }
                Object data = mNavList.getItemAtPosition(position);
                if (mActionListener != null) {
                    mActionListener.onNavItemSelected(data);
                }
            }
        });
    }
}
