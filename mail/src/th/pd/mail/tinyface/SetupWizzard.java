package th.pd.mail.tinyface;

import android.app.Activity;
import android.os.Bundle;

import th.pd.mail.Const;

public class SetupWizzard extends Activity {

    private static final String TAG = SetupWizzard.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FakeData.add(this);
        Const.logd(TAG + ": starts");
        setResult(RESULT_OK);
        finish();
    }
}
