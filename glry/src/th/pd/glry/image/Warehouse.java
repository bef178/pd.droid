package th.pd.glry.image;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import cc.typedef.SlidingCache;
import cc.typedef.droid.ImageLoader;
import cc.typedef.droid.common.SystemUiUtil;
import cc.typedef.droid.common.mime.MimeTypeUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.view.WindowManager;

class Warehouse {

    private class PrefetchHandler extends Handler {

        public static final int MSG_PREFETCH = 0;
        public static final int MSG_SKIP_PREFETCH = 1;

        public PrefetchHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SKIP_PREFETCH:
                    removeMessages(MSG_PREFETCH);
                    break;
                case MSG_PREFETCH:
                    int index = msg.arg1;
                    if (Warehouse.this.cache.has(index)) {
                        Bitmap bitmap = ImageLoader.load(
                                Warehouse.this.mContext,
                                (Uri) msg.obj,
                                Warehouse.this.mResolution[0],
                                Warehouse.this.mResolution[1]);
                        // check again since significant time has elapsed
                        if (Warehouse.this.cache.has(index)) {
                            Warehouse.this.cache.set(index, bitmap);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Context mContext;
    private int[] mResolution;
    private HandlerThread mPrefetchThread;
    private PrefetchHandler mPrefetchHandler;

    private List<Uri> data = new LinkedList<Uri>();
    private SlidingCache<Bitmap> cache = new SlidingCache<>(7);
    private int cacheRadius = cache.capacity() / 2;

    public Warehouse(Context context) {
        mContext = context;
        mResolution = SystemUiUtil.findScreenResolution(
                (WindowManager) context.getSystemService(
                        Context.WINDOW_SERVICE), null);

        mPrefetchThread = new HandlerThread(
                context.getPackageName() + ".prefetchThrd",
                Process.THREAD_PRIORITY_BACKGROUND);
        mPrefetchThread.start();
        mPrefetchHandler = new PrefetchHandler(mPrefetchThread.getLooper());
    }

    public boolean add(File file) {
        if (file != null) {
            String mimeType = MimeTypeUtil.mimeTypeByFile(file);
            if (file.isFile() && MimeTypeUtil.isImage(mimeType)) {
                data.add(Uri.fromFile(file));
                return true;
            }
        }
        return false;
    }

    public void add(Uri uri) {
        data.add(uri);
    }

    public void clear() {
        data.clear();
    }

    public Bitmap getBitmap(int index) {
        if (!hasIndex(index)) {
            return null;
        }

        cache.moveTo(index - cacheRadius);
        if (cache.get(index) == null) {
            // TODO show loading
            mPrefetchHandler.sendEmptyMessage(PrefetchHandler.MSG_SKIP_PREFETCH);
            cache.set(index, ImageLoader.load(mContext, getUri(index),
                    mResolution[0], mResolution[1]));
        }
        return cache.get(index);
    }

    public int getCount() {
        return data.size();
    }

    public Uri getUri(int i) {
        if (hasIndex(i)) {
            return data.get(i);
        }
        return null;
    }

    public boolean hasIndex(int i) {
        return i >= 0 && i < getCount();
    }

    public int indexOf(Uri uri) {
        return data.indexOf(uri);
    }

    private void requestPrefetch(int index) {
        Message msg = Message.obtain();
        msg.what = PrefetchHandler.MSG_PREFETCH;
        msg.arg1 = index;
        msg.obj = data.get(index);
        mPrefetchHandler.sendMessage(msg);
    }

    public void schedulePrefetch(int index) {
        for (int i = 1; i <= cacheRadius; ++i) {
            int j = index + i;
            if (hasIndex(j) && cache.has(j) && cache.get(j) == null) {
                requestPrefetch(j);
            }
            j = index - i;
            if (hasIndex(j) && cache.has(j) && cache.get(j) == null) {
                requestPrefetch(j);
            }
        }
    }
}
