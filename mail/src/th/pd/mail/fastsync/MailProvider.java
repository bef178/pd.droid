package th.pd.mail.fastsync;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import th.pd.mail.Const;
import th.pd.mail.dao.FastSyncAccess;
import th.pd.mail.dao.MailDir;

public class MailProvider extends ContentProvider {

    public static final String CONTENT_URI = "content://" + Const.AUTHORITY;

    // request path goes below
    public static final String REQUEST_SYNC_DIR = "requestSyncDir";
    public static final int ACTION_SYNC_DIR = 1 << 11;

    private static final UriMatcher sUriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        synchronized (sUriMatcher) {
            sUriMatcher.addURI(Const.AUTHORITY, REQUEST_SYNC_DIR
                    + "/#", ACTION_SYNC_DIR);
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
            case ACTION_SYNC_DIR: {
                int id = Integer.parseInt(uri.getLastPathSegment());
                requestSyncDir(id);
                break;
            }
            case UriMatcher.NO_MATCH:
                break;
        }
        return null;
    }

    private void requestSyncDir(int id) {
        MailDir dir = FastSyncAccess.findMailDir(getContext(), id);
        if (dir == null) {
            return;
        }

        Bundle extras = new Bundle();
        ContentResolver.requestSync(new Account(dir.getAddr(),
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
