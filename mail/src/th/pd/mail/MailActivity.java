package th.pd.mail;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import th.pd.mail.fastsync.MailProvider;
import th.pd.mail.tidyface.compose.ComposeActivity;
import th.pd.mail.tidyface.leftmost.LeftmostFragment;

public class MailActivity extends Activity {

    private void bindViews() {
        findViewById(R.id.btnCompose).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent().setClass(
                                getApplicationContext(),
                                ComposeActivity.class));
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        bindViews();

        requestSync();

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = new LeftmostFragment(null);
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.vLeftmost, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void requestSync() {
        Uri requestUri = Uri.parse(MailProvider.CONTENT_URI).buildUpon()
                .appendEncodedPath(MailProvider.REQUEST_SYNC_FOLDER)
                .appendPath(Integer.toString(1))
                .build();
        getContentResolver().query(requestUri, null, null, null, null);
    }
}
