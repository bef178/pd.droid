package th.pd.mail.fastsync;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import th.pd.mail.dao.FastSyncAccess;

public class MailProvider extends ContentProvider {

    public static final String CONTENT_URI = "content://" + Const.AUTHORITY;

    // request path goes below
    public static final String REQUEST_SYNC_FOLDER = "requestSyncFolder";
    public static final int ACTION_SYNC_FOLDER = 1 << 11;

    private static final UriMatcher sUriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        synchronized (sUriMatcher) {
            sUriMatcher.addURI(Const.AUTHORITY, REQUEST_SYNC_FOLDER
                    + "/#", ACTION_SYNC_FOLDER);
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
            case ACTION_SYNC_FOLDER: {
                int id = Integer.parseInt(uri.getLastPathSegment());
                requestSyncFolder(id);
                break;
            }
            case UriMatcher.NO_MATCH:
                break;
        }
        return null;
    }

    private void requestSyncFolder(int id) {
        MailFolder mailFolder = FastSyncAccess.findMailFolder(getContext(),
                id);
        if (mailFolder == null) {
            return;
        }

        Bundle extras = new Bundle();
        ContentResolver.requestSync(new Account(mailFolder.getAddr(),
                Const.ACCOUNT_TYPE), Const.AUTHORITY, extras);
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
