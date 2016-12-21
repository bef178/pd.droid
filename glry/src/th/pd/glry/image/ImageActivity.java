package th.pd.glry.image;

import java.io.File;

import cc.typedef.droid.common.GesturePipeline;
import cc.typedef.droid.common.GesturePipeline.Callback;
import cc.typedef.droid.common.QueryUtil;
import cc.typedef.droid.common.mime.MimeTypeUtil;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import th.pd.glry.AbsMediaActivity;
import th.pd.glry.R;

public class ImageActivity extends AbsMediaActivity {

    private static final float FALLBACK_POINT = 0.45f;

    private Warehouse mModel;
    private int mCurrentPos;

    private ImageDisplay mDisplay;

    private GesturePipeline mGesturePipeline;

    private int mScrolledX;

    /**
     * play a reverse-scroll animation
     */
    private void fallbackSwitching() {
        boolean isForward = mScrolledX < 0;
        mDisplay.doFallback(mModel.getBitmap(mCurrentPos),
                mModel.getBitmap(mCurrentPos + (isForward ? 1 : -1)),
                isForward,
                getScrolledFraction(mScrolledX),
                null);
        mScrolledX = 0;
    }

    private float getScrolledFraction(int scrolledX) {
        float fraction = 1f * Math.abs(scrolledX) / mDisplay.getWidth();
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
                Uri currentUri = mModel.getUri(mCurrentPos);
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

        mDisplay = (ImageDisplay) findViewById(R.id.image);
        setupModel(imageUri);
        setupController();

        // first load
        if (mModel.hasIndex(mCurrentPos)) {
            setTitleByUri(mModel.getUri(mCurrentPos));
            setSummary(String.format("%d / %d",
                    mCurrentPos + 1,
                    mModel.getCount()));

            mDisplay.firstLoad(mModel.getBitmap(mCurrentPos));
            mScrolledX = 0;
        }
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
        return mGesturePipeline.onTouchEvent(event);
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
                                mModel.getBitmap(mCurrentPos),
                                mModel.getBitmap(mCurrentPos + 1),
                                true,
                                getScrolledFraction(dxTotal));
                    } else if (dxTotal > 0) {
                        mDisplay.doScroll(
                                mModel.getBitmap(mCurrentPos),
                                mModel.getBitmap(mCurrentPos - 1),
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
        mModel = new Warehouse(this);

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
    }

    private void switchOrFallback(int offset, boolean keyTriggered) {
        boolean isForward = offset > 0;
        final int pos = mCurrentPos + offset;
        if (!mModel.hasIndex(pos)) {
            // fallback
            if (keyTriggered) {
                // for key triggered fallback, we don't have scrolledX, so play a
                // forth-and-back animation to tell no more images
                Bitmap bitmap = mModel.getBitmap(pos);
                mDisplay.doSwitch(mModel.getBitmap(mCurrentPos), bitmap,
                        isForward,
                        0f, FALLBACK_POINT, null);
                mScrolledX = 0;
            } else {
                fallbackSwitching();
            }
        } else {
            // switch
            if (mDisplay.isSwitching()) {
                return;
            }

            Bitmap bitmap = mModel.getBitmap(pos);
            mDisplay.doSwitch(mModel.getBitmap(mCurrentPos), bitmap,
                    isForward,
                    getScrolledFraction(mScrolledX), new Runnable() {

                        @Override
                        public void run() {
                            setTitleByUri(mModel.getUri(pos));
                            setSummary(String.format("%d / %d", pos + 1,
                                    mModel.getCount()));
                            mCurrentPos = pos;
                        }
                    });
            mScrolledX = 0;
        }
    }
}
