package th.pd.mail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.MailProvider;
import th.pd.mail.fastsync.Mailbox;

public class MainActivity extends Activity {
	private void createSyncAccount() {
		Mailbox mailbox = Mailbox.fromQuery(0, null);
		Account account = new Account(mailbox.getAddress(),
				Const.ACCOUNT_TYPE);
		AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		accountManager.addAccountExplicitly(account, null, null);
	}

	private boolean isOutOfBounds(Context context, MotionEvent event) {
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int slop = ViewConfiguration.get(context)
				.getScaledWindowTouchSlop();
		final View decorView = getWindow().getDecorView();
		return (x < -slop) || (y < -slop)
				|| (x > (decorView.getWidth() + slop))
				|| (y > (decorView.getHeight() + slop));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		createSyncAccount();
		requestSyncMailFolder(13);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isOutOfBounds(this, event)) {
			return moveTaskToBack(true);
		}
		return super.onTouchEvent(event);
	}

	private void requestSyncMailFolder(int internalIdOfMailFolder) {
		Uri requestUri = Uri.parse(MailProvider.CONTENT_URI).buildUpon()
				.appendEncodedPath(MailProvider.REQUEST_SYNC_MAIL_FOLDER)
				.appendPath(Integer.toString(internalIdOfMailFolder))
				.build();
		getContentResolver().query(requestUri, null, null, null, null);
	}
}
