package th.pd.mail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.MailProvider;
import th.pd.mail.fastsync.Mailbox;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
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

	private void createSyncAccount() {
		Mailbox mailbox = Mailbox.fromQuery(0, null);
		Account account = new Account(mailbox.getAddress(),
				Const.ACCOUNT_TYPE);
		AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		accountManager.addAccountExplicitly(account, null, null);
	}
}
