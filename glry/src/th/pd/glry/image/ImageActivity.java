package th.pd.glry.image;

import java.io.File;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import th.pd.common.android.QueryUtil;
import th.pd.common.android.SystemUiUtil;
import th.pd.common.android.mime.MimeTypeUtil;
import th.pd.glry.AbsMediaActivity;
import th.pd.glry.R;
import th.pd.glry.image.GesturePipeline.Callback;

public class ImageActivity extends AbsMediaActivity {

    private class UpdateCacheTask extends
            AsyncTask<UpdateCacheTaskArgument, Void, Void> {

        @Override
        protected Void doInBackground(UpdateCacheTaskArgument... params) {
            UpdateCacheTaskArgument a = params[0];
            updateCache(a.pos, a.bitmap);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mUpdateCacheTask = null;
        }

        private void updateCache(int pos, Bitmap bitmap) {
            if (bitmap == null) {
                bitmap = createBitmap(pos);
            }
            mCache.focus(pos);
            mCache.set(pos, bitmap);
            for (int i = 1; i <= mCache.RADIUS; ++i) {
                if (mCache.get(pos + i) == null) {
                    bitmap = createBitmap(pos + i);
                    mCache.set(pos + i, bitmap);
                }
                if (mCache.get(pos - i) == null) {
                    bitmap = createBitmap(pos - i);
                    mCache.set(pos - i, bitmap);
                }
            }
        }
    }

    private class UpdateCacheTaskArgument {

        private int pos;
        private Bitmap bitmap;

        void set(int pos, Bitmap bitmap) {
            this.pos = pos;
            this.bitmap = bitmap;
        }
    }

    private int[] mResolution = null;

    private Model mModel;
    private int mCurrentPos;

    private ImageDisplay mDisplay;

    private PivotCache<Bitmap> mCache;
    private UpdateCacheTaskArgument mUpdateCacheTaskArgument;
    private UpdateCacheTask mUpdateCacheTask;

    private GesturePipeline mGesturePipeline;

    private int mScrolledX;

    private Bitmap createBitmap(int pos) {
        Uri uri = mModel.getData(pos);
        if (uri != null) {
            // sample to avoid OOM
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(uri.getPath(), options);
            double invSampleScale = 1;
            {
                double wScale = 1f * options.outWidth / mResolution[0];
                double hScale = 1f * options.outHeight / mResolution[1];
                if (wScale > 2 || hScale > 2) {
                    invSampleScale = Math.pow(wScale * hScale, 0.5);
                }
                Log.d(TAG, "pos:" + pos + ";sampleScale:" + invSampleScale);
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = (int) Math.round(invSampleScale);
            return BitmapFactory.decodeFile(uri.getPath(), options);
        }
        return null;
    }

    private void fallbackSwitching() {
        if (mScrolledX > 0) {
            mDisplay.doSwitch(getBitmap(mCurrentPos - 1),
                    getBitmap(mCurrentPos),
                    true,
                    1 - getScrolledFraction(mScrolledX));
        } else if (mScrolledX < 0) {
            mDisplay.doSwitch(getBitmap(mCurrentPos + 1),
                    getBitmap(mCurrentPos),
                    false,
                    1 - getScrolledFraction(mScrolledX));
        }
        mScrolledX = 0;
    }

    /**
     * for key triggered fallback, we don't have scrolledX, so play a
     * forth-and-back animation to tell no more images
     */
    private void fallbackSwitchingForButton(int offset) {
        final float turnPoint = 0.45f;
        if (offset > 0) {
            mDisplay.doSwitch(getBitmap(mCurrentPos),
                    getBitmap(mCurrentPos + 1),
                    true, 0f, turnPoint);
        } else if (offset < 0) {
            mDisplay.doSwitch(getBitmap(mCurrentPos),
                    getBitmap(mCurrentPos - 1),
                    false, 0f, turnPoint);
        }
        mScrolledX = 0;
    }

    private Bitmap getBitmap(int pos) {
        Bitmap bitmap = mCache.get(pos);
        if (bitmap != null) {
            return bitmap;
        } else {
            return createBitmap(pos);
        }
    }

    private float getScrolledFraction(int scrolledX) {
        if (scrolledX < 0) {
            scrolledX = -scrolledX;
        }
        float fraction = 1f * scrolledX / mDisplay.getWidth();
        if (fraction > 1f) {
            fraction = 1f;
        }
        return fraction;
    }

    @Override
    public boolean onAction(int actionId, Object extra) {
        switch (actionId) {
            case R.id.actionNext:
                mDisplay.restore();
                switchOrFallback(1, true);
                return true;
            case R.id.actionPrev:
                mDisplay.restore();
                switchOrFallback(-1, true);
                return true;
            case R.id.actionZoomIn:
                mDisplay.scale(10f / 9);
                return true;
            case R.id.actionZoomOut:
                mDisplay.scale(0.9f);
                return true;
            case R.id.actionZoomReset:
                mDisplay.restore();
                return true;
            case R.id.actionSetImageAs:
                Uri currentUri = mModel.getData(mCurrentPos);
                String mimeType = MimeTypeUtil.mimeTypeByFile(new File(
                        currentUri.getPath()));
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA)
                        .setDataAndType(currentUri, mimeType)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, null));
                return true;
        }
        return super.onAction(actionId, extra);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDisplay.postDelayed(new Runnable() {

            @Override
            public void run() {
                mDisplay.restore();
            }
        }, 100);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Uri imageUri = getIntent().getData();
        if (imageUri == null) {
            finish();
        }

        super.onCreate(savedInstanceState, R.layout.image_main);

        mResolution = new int[2];
        SystemUiUtil.findScreenResolution(getWindowManager(), mResolution);

        mDisplay = (ImageDisplay) findViewById(R.id.image);
        setupModel(imageUri);
        setupController();

        startInitializeTask();
    }

    @Override
    protected boolean onKeyEvent(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
                if (event.isCtrlPressed()
                        && onAction(R.id.actionZoomReset, null)) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_EQUALS:
                if (event.isCtrlPressed()
                        && onAction(R.id.actionZoomIn, null)) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MINUS:
                if (event.isCtrlPressed()
                        && onAction(R.id.actionZoomOut, null)) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return onAction(R.id.actionPrev, null);
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return onAction(R.id.actionNext, null);
            default:
                break;
        }
        return super.onKeyEvent(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGesturePipeline.onTouchEvent(event)) {
            return true;
        }
        return false;
    }

    private void setupController() {

        mGesturePipeline = new GesturePipeline(this, new Callback() {

            @Override
            public boolean onDoubleTap() {
                if (mDisplay.isScaled()) {
                    mDisplay.restore();
                } else {
                    Rect rect = mDisplay.copyFrameRect();
                    if (rect.width() < mDisplay.getWidth()
                            && rect.height() < mDisplay.getHeight()) {
                        // scale to fit full screen
                        float scale = Math.min(
                                1f * mDisplay.getWidth() / rect.width(),
                                1f * mDisplay.getHeight() / rect.height());
                        mDisplay.scale(scale);
                    } else {
                        mDisplay.scaleToDotForDot();
                    }
                }
                return true;
            }

            @Override
            public boolean onFling(int dxTotal, int dyTotal,
                    float velocityX, float velocityY) {
                if (mDisplay.isScaled()) {
                    return false;
                }

                if (Math.abs(dxTotal) > Math.abs(dyTotal)) {
                    if (dxTotal > 0) {
                        switchOrFallback(-1, false);
                    } else {
                        switchOrFallback(1, false);
                    }
                    return true;
                }

                return false;
            }

            @Override
            public boolean onScaleTo(float scale, int focusX, int focusY) {
                Rect rect = mDisplay.copyFrameRect();
                if (!rect.contains(focusX, focusY)
                        || (rect.width() <= mDisplay.getWidth()
                        && rect.height() <= mDisplay.getHeight())) {
                    // focus on the center
                    mDisplay.scale(scale);
                } else {
                    mDisplay.scale(scale, focusX, focusY);
                }
                return true;
            }

            @Override
            public boolean onScrollBy(int dxTotal, int dyTotal,
                    int dx, int dy) {

                if (mDisplay.isScaled()) {
                    Rect rect = mDisplay.copyFrameRect();
                    if (rect.width() > mDisplay.getWidth()
                            || rect.height() > mDisplay.getHeight()) {
                        rect.offset(-dx, -dy);
                        if (rect.contains(mDisplay.getWidth() / 2,
                                mDisplay.getHeight() / 2)) {
                            mDisplay.move(-dx, -dy);
                        }
                    }
                    return true;
                }

                if (Math.abs(dxTotal) > Math.abs(dyTotal)
                        && Math.abs(dxTotal) > 100) {
                    if (dxTotal < 0) {
                        mDisplay.doScroll(
                                getBitmap(mCurrentPos),
                                getBitmap(mCurrentPos + 1),
                                true,
                                getScrolledFraction(dxTotal));
                    } else if (dxTotal > 0) {
                        mDisplay.doScroll(
                                getBitmap(mCurrentPos),
                                getBitmap(mCurrentPos - 1),
                                false,
                                getScrolledFraction(dxTotal));
                    }
                    mScrolledX = dxTotal;
                    return true;
                }

                return false;
            }

            @Override
            public boolean onTapUp() {
                if (mDisplay.isScaled()) {
                    return false;
                }
                if (getScrolledFraction(mScrolledX) < 0.4f) {
                    fallbackSwitching();
                } else {
                    if (mScrolledX > 0) {
                        switchOrFallback(-1, false);
                    } else {
                        switchOrFallback(1, false);
                    }
                }
                return false;
            }
        });

        findViewById(R.id.btnNext).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onAction(R.id.actionNext, null);
                    }
                });

        findViewById(R.id.btnPrev).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onAction(R.id.actionPrev, null);
                    }
                });
    }

    private void setupModel(Uri uri) {
        mModel = new Model();

        if (uri == null) {
            return;
        }

        String path = QueryUtil.getPath(this, uri);
        if (path != null) {
            File f = new File(path);
            uri = Uri.fromFile(f);
            if (f.isFile()) {
                // add all peers
                File[] a = f.getParentFile().listFiles();
                if (a != null) {
                    for (File i : a) {
                        mModel.add(i);
                    }
                }

                // in case no permission etc
                if (mModel.indexOf(uri) == -1) {
                    mModel.add(uri);
                }
            } else if (f.isDirectory()) {
                File[] a = f.listFiles();
                if (a != null) {
                    for (File i : a) {
                        mModel.add(i);
                    }
                }
            }
        } else {
            mModel.add(uri);
        }
        mCurrentPos = mModel.indexOf(uri);

        mCache = new PivotCache<Bitmap>();
        mUpdateCacheTaskArgument = new UpdateCacheTaskArgument();
    }

    private void startInitializeTask() {
        mUpdateCacheTaskArgument.set(mCurrentPos, null);
        new UpdateCacheTask() {

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                switchBy(0);
            }
        }.execute(mUpdateCacheTaskArgument);
    }

    private void startUpdateCacheTask(int pos, Bitmap bitmap) {
        mUpdateCacheTaskArgument.set(pos, bitmap);
        if (mUpdateCacheTask != null) {
            Log.d(TAG, "cancel on-going UpdateCacheTask");
            mUpdateCacheTask.cancel(false);
        }
        mUpdateCacheTask = new UpdateCacheTask();
        mUpdateCacheTask.execute(mUpdateCacheTaskArgument);
    }

    /**
     * switch next/prev item with animation<br/>
     *
     * @param offset
     *            switch to next if positive, to prev if negative
     * @return <code>true</code> if will successfully switch
     */
    private boolean switchBy(int offset) {
        if (mDisplay.isSwitching()) {
            return false;
        }

        int pos = mCurrentPos + offset;
        if (!mModel.hasIndex(pos)) {
            return false;
        }

        Bitmap bitmap = getBitmap(pos);
        if (offset == 0) {
            mDisplay.firstLoad(bitmap);
        } else if (offset > 0) {
            mDisplay.doSwitch(getBitmap(mCurrentPos), bitmap,
                    true,
                    getScrolledFraction(mScrolledX));
        } else {
            mDisplay.doSwitch(getBitmap(mCurrentPos), bitmap,
                    false,
                    getScrolledFraction(mScrolledX));
        }

        mScrolledX = 0;
        mCurrentPos = pos;

        setTitleByUri(mModel.getData(pos));
        setSummary(String.format("%d / %d", pos + 1, mModel.getCount()));

        startUpdateCacheTask(pos, bitmap);

        return true;
    }

    private void switchOrFallback(int offset, boolean triggedByButton) {
        if (!mModel.hasIndex(mCurrentPos + offset)) {
            if (triggedByButton) {
                fallbackSwitchingForButton(offset);
            } else {
                fallbackSwitching();
            }
        } else {
            switchBy(offset);
        }
    }
}
