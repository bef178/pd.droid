package th.pd.mail.fastsync;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public class MailProvider extends ContentProvider {
	public static final String CONTENT_URI = "content://" + Const.AUTHORITY;

	// request path goes below
	public static final String REQUEST_SYNC_MAIL_FOLDER = "requestSyncMailFolder";
	public static final int ACTION_SYNC_MAIL_FOLDER = 1 << 11;

	private static final UriMatcher sUriMatcher =
			new UriMatcher(UriMatcher.NO_MATCH);

	static {
		synchronized (sUriMatcher) {
			sUriMatcher.addURI(Const.AUTHORITY,
					REQUEST_SYNC_MAIL_FOLDER + "/#",
					ACTION_SYNC_MAIL_FOLDER);
		}
	}

	@Override
	public boolean onCreate() {
		// TODO
		return false;
	}

	/**
	 * it's the interface for actions and arguments
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO

		switch (sUriMatcher.match(uri)) {
			case ACTION_SYNC_MAIL_FOLDER: {
				int internalIdOfMailFolder = Integer.parseInt(uri
						.getLastPathSegment());
				requestSyncMailFolder(internalIdOfMailFolder);
				break;
			}
			case UriMatcher.NO_MATCH:
				break;
		}
		return null;
	}

	private void requestSyncMailFolder(int internalIdOfMailFolder) {
		ContentProviderClient client = getContext()
				.getContentResolver().acquireContentProviderClient(
						Uri.parse(CONTENT_URI));

		MailFolder mailFolder = MailObjectCache.getInstance().getMailFolder(
				internalIdOfMailFolder, client);
		if (mailFolder == null) {
			return;
		}

		Mailbox mailbox = MailObjectCache.getInstance().getMailbox(
				mailFolder.getInternalIdOfMailbox(), client);
		if (mailbox == null) {
			return;
		}

		Account account = new Account(mailbox.getAddress(),
				Const.ACCOUNT_TYPE);
		Bundle extras = new Bundle();
		ContentResolver.requestSync(account, Const.AUTHORITY, extras);
	}

	@Override
	public String getType(Uri uri) {
		// TODO
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO
		return 0;
	}
}
