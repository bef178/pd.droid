package th.pd.fmgr;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import cc.typedef.droid.common.OnActionCallback;
import th.pd.fmgr.content.ContentFragment;
import th.pd.fmgr.nav.NavFragment;

/**
 * It reads file manager but actually is two-column layout and fragment and list
 * view investigation.<br/>
 * <br/>
 * Activity works as view manager while Controller identifies workflow.
 */
public class FileManager extends Activity implements OnActionCallback {

    private void initializeFragments() {
        FragmentManager fragmentManager = getFragmentManager();

        Fragment fragment = new NavFragment(null);
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.navFragment, fragment);
        fragmentTransaction.commitAllowingStateLoss();

        fragment = new ContentFragment(null);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.contentFragment, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initializeFragments();
    }

    @Override
    public boolean onAction(int arg0, Object arg1) {
        // TODO Auto-generated method stub
        return false;
    }

}
