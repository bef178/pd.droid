package t.typedef.droid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import t.typedef.droid.mime.MimeTypeUtil;
import t.typedef.io.Util;

public class ImageLoader {

    private static HttpURLConnection getConn(String url)
            throws IOException {
        final String ALLOWED = "@#&=*+-_.,:!?()/~'%";
        url = Uri.encode(url, ALLOWED);
        HttpURLConnection conn = null;
        int numRedirects = 0;
        while (numRedirects++ < 5) {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(20000);
            if (conn.getResponseCode() / 100 == 3) {
                url = conn.getHeaderField("Location");
                conn.disconnect();
            } else {
                break;
            }
        }
        return conn;
    }

    public static InputStream getInputStream(Context context, Uri uri)
            throws IOException {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            String path = (scheme == null)
                    ? uri.toString()
                    : uri.toString().substring(uri.getScheme().length() + 3);
            File file = new File(path);
            String mimeType = MimeTypeUtil.mimeTypeByFile(file);
            if (MimeTypeUtil.isImage(mimeType)) {
                return new FileInputStream(file);
            }
        } else if (scheme.equals("content")) {
            ContentResolver resolver = context.getContentResolver();
            String mimeType = resolver.getType(uri);
            if (MimeTypeUtil.isImage(mimeType)) {
                return resolver.openInputStream(uri);
            }
        } else if (scheme.equals("assets")) {
            String path = uri.toString().substring(scheme.length() + 3);
            File file = new File(path);
            String mimeType = MimeTypeUtil.mimeTypeByFile(file);
            if (MimeTypeUtil.isImage(mimeType)) {
                return context.getAssets().open(path);
            }
        } else if (scheme.equals("drawable")) {
            String path = uri.toString().substring(scheme.length() + 3);
            return context.getResources().openRawResource(
                    Integer.parseInt(path));
        } else if (scheme.equals("http") || scheme.equals("https")) {
            HttpURLConnection conn = getConn(uri.toString());
            InputStream istream = null;
            try {
                istream = conn.getInputStream();
            } catch (IOException e) {
                // consume all data to reuse conn
                Util.consumeDataAndCloseSilently(conn.getErrorStream());
                throw e;
            }
            if (conn.getResponseCode() == 200) {
                return istream;
            } else {
                Util.closeSilently(istream);
                throw new IOException("fail to fetch image: "
                        + conn.getResponseCode() + " " + uri.toString());
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static InputStream getInputStreamFromContact(Context context,
            Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        return ContactsContract.Contacts.openContactPhotoInputStream(
                resolver, uri, true);
    }

    @SuppressWarnings("unused")
    private static InputStream getInputStreamFromVideo(Context context,
            Uri uri) {
        String scheme = uri.getScheme();
        Bitmap thumbnail = null;
        if (scheme == null || scheme.equals("file")) {
            String path = (scheme == null)
                    ? uri.toString()
                    : uri.toString().substring(uri.getScheme().length() + 3);
            File file = new File(path);
            String mimeType = MimeTypeUtil.mimeTypeByFile(file);
            if (MimeTypeUtil.isVideo(mimeType)) {
                thumbnail = ThumbnailUtils.createVideoThumbnail(path,
                        MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);

            }
        } else if (scheme.equals("content")) {
            ContentResolver resolver = context.getContentResolver();
            String mimeType = resolver.getType(uri);
            if (MimeTypeUtil.isVideo(mimeType)) {
                Long id = Long.valueOf(uri.getLastPathSegment());
                thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                        resolver, id, MediaStore.Images.Thumbnails.MINI_KIND,
                        null);
            }
        }

        if (thumbnail == null) {
            return null;
        }

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        thumbnail.compress(CompressFormat.PNG, 30, o);
        return new ByteArrayInputStream(o.toByteArray());
    }

    private static Bitmap load(Context context, String path,
            int outWidth, int outHeight) {
        // sample to avoid OOM
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        double invSampleScale = 1;
        {
            double wScale = 1f * options.outWidth / outWidth;
            double hScale = 1f * options.outHeight / outHeight;
            if (wScale > 2 || hScale > 2) {
                invSampleScale = Math.pow(wScale * hScale, 0.5);
            }
            Log.d(context.getPackageName(),
                    "fp:" + path + "; sample_scale:" + invSampleScale);
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = (int) Math.round(invSampleScale);
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap load(Context context, Uri uri,
            int outWidth, int outHeight) {
        if (uri == null) {
            return null;
        }
        return load(context, uri.getPath(), outWidth, outHeight);
    }
}
