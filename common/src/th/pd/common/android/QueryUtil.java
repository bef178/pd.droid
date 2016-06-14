package th.pd.common.android;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

public class QueryUtil {

    public static String getPath(Context context, Uri uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        if (DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents"
                    .equals(uri.getAuthority())) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] a = docId.split(":");
                String type = a[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + a[1];
                }
                // TODO
                return null;
            }

            if ("com.android.providers.downloads.documents"
                    .equals(uri.getAuthority())) {
                String docId = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                return queryData(context, contentUri, null, null);
            }

            if ("com.android.providers.media.documents"
                    .equals(uri.getAuthority())) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] a = docId.split(":");
                Uri contentUri = null;
                if ("image".equals(a[0])) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(a[0])) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(a[0])) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }

                return queryData(context, contentUri, "_id=?",
                        new String[] {
                            a[1]
                        });
            }
        }

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return queryData(context, uri, null, null);
        }

        return null;
    }

    private static String queryData(Context context, Uri uri,
            String selection, String[] selectionArgs) {

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[] {
                    "_data"
            }, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(
                        cursor.getColumnIndexOrThrow("_data"));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

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
