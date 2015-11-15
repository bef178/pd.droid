package th.pd.mail.tinyface;

import android.accounts.Account;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import th.pd.mail.R;
import th.pd.mail.dao.FastSyncAccess;
import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.Mailbox;
import th.pd.mail.tidyface.compose.ComposeActivity;
import th.pd.mail.tidyface.leftmost.LeftmostFragment;

public class MailMain extends Activity {

    private final int SETUP_WIZZARD_REQ_CODE = 7749;

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
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETUP_WIZZARD_REQ_CODE) {
            Const.logd("req code" + resultCode);
            switch (resultCode) {
                case RESULT_OK:
                    onCreateContinued();
                    // TODO trigger a sync immediately
                    return;
            }
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mailbox mailbox = FastSyncAccess.getMailboxSequence(this)
                .getCurrent();
        if (mailbox == null) {
            startActivityForResult(new Intent()
                    .setClass(this, SetupWizzard.class),
                    SETUP_WIZZARD_REQ_CODE);
            return;
        }

        onCreateContinued();
    }

    private void onCreateContinued() {
        setContentView(R.layout.activity_main);
        bindViews();

        requestSync(FastSyncAccess.getMailboxSequence(this).getCurrent());

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = new LeftmostFragment();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.vLeftmost, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void requestSync(Mailbox mailbox) {
        Account account = new Account(mailbox.getAddr(),
                Const.ACCOUNT_TYPE);
        Bundle extras = new Bundle();
        ContentResolver.requestSync(account, Const.AUTHORITY, extras);
    }
}
