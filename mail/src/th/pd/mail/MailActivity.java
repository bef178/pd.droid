package th.pd.mail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.MailProvider;
import th.pd.mail.fastsync.Mailbox;
import th.pd.mail.tidyface.compose.ComposeActivity;

public class MailActivity extends Activity {

    private void bindViews() {
        findViewById(R.id.btnCompose).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent()
                                .setClass(getApplicationContext(),
                                        ComposeActivity.class));
                    }
                });
    }

    private void createSyncAccount() {
        Mailbox mailbox = Mailbox.fromQuery(0, null);
        Account account = new Account(mailbox.getAddress(),
                Const.ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(account, null, null);
        ContentResolver
                .setSyncAutomatically(account, Const.AUTHORITY, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        bindViews();

        createSyncAccount();
        requestSyncMailFolder(13);
    }

    private void requestSyncMailFolder(int internalIdOfMailFolder) {
        Uri requestUri = Uri.parse(MailProvider.CONTENT_URI).buildUpon()
                .appendEncodedPath(MailProvider.REQUEST_SYNC_MAIL_FOLDER)
                .appendPath(Integer.toString(internalIdOfMailFolder))
                .build();
        getContentResolver().query(requestUri, null, null, null, null);
    }
}
