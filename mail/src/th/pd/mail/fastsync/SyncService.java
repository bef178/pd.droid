package th.pd.mail.fastsync;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * main entrance of the sync service
 */
public class SyncService extends Service {
	private static class ThreadedSyncAdapter extends
			AbstractThreadedSyncAdapter {
		public ThreadedSyncAdapter(Context context) {
			super(context, true);
		}

		@Override
		public void onPerformSync(Account account, Bundle extras,
				String authority, ContentProviderClient client,
				SyncResult syncResult) {
			logi("performSync--begin--");

			Mailbox mailbox = MailObjectCache.getInstance().getMailbox(
					account.name, client);
			if (mailbox == null) {
				logi("performSync--end---- with null mailbox");
				return;
			}

			// TODO multi-thread and extract extras
			for (int internalIdOfMailFolder : findInternalIdOfMailFolder(extras)) {
				MailFolder mailFolder = MailObjectCache.getInstance()
						.getMailFolder(internalIdOfMailFolder, client);
				if (mailFolder == null) {
					continue;
				}
				performSync(getContext(), mailbox, mailFolder, extras,
						syncResult);
			}
			logi("performSync--end---- done");
		}
	}

	private static ThreadedSyncAdapter sSyncAdapter;
	private static Object sSyncAdapterLock = new Object();

	private static int[] findInternalIdOfMailFolder(Bundle extras) {
		// TODO
		return new int[] {
				13
		};
	}

	private static void logi(String message) {
		final String LOG_TAG = SyncService.class.getSimpleName();
		Log.i(LOG_TAG, message);
		Log.w("th", message);
	}

	private static void performSync(Context context, Mailbox mailbox,
			MailFolder mailFolder, Bundle extras, SyncResult syncResult) {
		if (mailbox == null || mailFolder == null) {
			return;
		}

		// TODO should not sync local only mail folder

		Uri uri = Uri.parse(MailFolder.CONTENT_URI + "/"
				+ mailFolder.getInternalId());
		ContentValues values = new ContentValues();
		values.put(Const.SYNC_FLAGS, Const.SYNC_RUN_IN_BACKGROUND);

		// to inform it's syncing
		context.getContentResolver().update(uri, values, null, null);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return sSyncAdapter.getSyncAdapterBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		synchronized (sSyncAdapterLock) {
			if (sSyncAdapter == null) {
				sSyncAdapter = new ThreadedSyncAdapter(
						getApplicationContext());
			}
		}
	}
}
