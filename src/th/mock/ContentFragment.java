package th.mock;

import th.pd.R;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class ContentFragment extends Fragment {

    public static void show(FragmentManager fragmentManager) {
        Fragment contentFragment = new ContentFragment();
        final FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.contentFragment, contentFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }
}
