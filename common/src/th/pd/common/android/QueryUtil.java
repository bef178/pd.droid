package th.pd.common.android;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

public class QueryUtil {

    public static String queryDisplayName(Uri contentUri,
            ContentResolver resolver) {
        String displayName = null;
        Cursor c = querySingle(contentUri, resolver,
                OpenableColumns.DISPLAY_NAME);
        if (c != null) {
            if (c.moveToFirst()) {
                displayName = c.getString(0);
            }
            c.close();
        }
        if (displayName == null) {
            displayName = contentUri.getLastPathSegment();
        }
        return displayName;
    }

    public static String queryMimeType(Uri contentUri,
            ContentResolver resolver) {
        return resolver.getType(contentUri);
    }

    private static Cursor querySingle(Uri contentUri,
            ContentResolver resolver, String columnName) {
        try {
            return resolver.query(contentUri,
                    new String[] {
                        columnName
                    }, null, null, null);
        } catch (SecurityException e) {
            throw e;
        }
    }

    public static int querySize(Uri contentUri, ContentResolver resolver) {
        int size = -1;
        Cursor c = querySingle(contentUri, resolver, OpenableColumns.SIZE);
        if (c != null) {
            if (c.moveToFirst()) {
                size = c.getInt(0);
            }
            c.close();
        }
        if (size == -1) {
            ParcelFileDescriptor fd = null;
            try {
                fd = resolver.openFileDescriptor(contentUri, "r");
                size = (int) fd.getStatSize();
            } catch (FileNotFoundException e) {
                // TODO
            } finally {
                try {
                    if (fd != null) {
                        fd.close();
                    }
                } catch (IOException e) {
                    // TODO
                }
            }
        }
        return size;
    }
}
