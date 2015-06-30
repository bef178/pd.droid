package th.mock;

import th.pd.R;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

class MockAdapter extends BaseAdapter {
    static int version = 223;
    Context mContext;
    String[] mData;
    int mVersion;

    public MockAdapter(Context context, String[] data) {
        mContext = context;
        mData = data;
        mVersion = version;
        version += 100;
    }

    @Override
    public int getCount() {
        return mData.length;
    }

    @Override
    public Object getItem(int position) {
        return mData[position];
    }

    @Override
    public long getItemId(int position) {
        return version + position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext,
                    R.layout.mock_filemanager_item_layout, null);
        }
        ((TextView) convertView.findViewById(R.id.text))
                .setText(convertView.getId() + "");
        return convertView;
    }
}

/**
 * It reads file manager but actually is two-column layout and fragment and list
 * view investigation.<br/>
 * <br/>
 * Activity works as view manager while Controller has workflow.
 */
public class MockFileManager extends Activity {

    ListView mListDefLeft;
    ListView mListDefRight;
    ListView mListLeft;
    ListView mListRight;

    private void initializeFragments() {
        FragmentManager fragmentManager = getFragmentManager();

        // nav fragment
        Fragment fragment = new NavFragment();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.navFragment, fragment);
        fragmentTransaction.commitAllowingStateLoss();

        // content fragment
        fragment = new ContentFragment();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.contentFragment, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mock_filemanager_layout);

        initializeFragments();
    }

}
