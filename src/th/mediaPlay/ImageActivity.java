package th.mediaPlay;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import th.common.Cache;
import th.common.MimeUtil;
import th.common.widget.ImageSwitcher;
import th.mediaPlay.GesturePipeline.Callback;
import th.pd.R;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class ImageActivity extends MediaPlayActivity {

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
    }

    private class UpdateCacheTaskArgument {
        private int pos;
        private Bitmap bitmap;

        void set(int pos, Bitmap bitmap) {
            this.pos = pos;
            this.bitmap = bitmap;
        }
    }

    private Model mModel;
    private int mCurrentPos;
    private ImageSwitcher mImageSwitcher;

    private Cache<Bitmap> mCache;
    private UpdateCacheTaskArgument mUpdateCacheTaskArgument;
    private UpdateCacheTask mUpdateCacheTask;

    private GesturePipeline mGesturePipeline;

    private int mScrolledX;

    private Bitmap createBitmap(int pos) {
        Uri uri = mModel.getData(pos);
        if (uri != null) {
            return BitmapFactory.decodeFile(uri.getPath());
        }
        return null;
    }

    private void fallbackSwitching() {
        if (mScrolledX > 0) {
            mImageSwitcher.doSwitch(getBitmap(mCurrentPos - 1),
                    getBitmap(mCurrentPos),
                    true,
                    1 - getScrolledFraction(mScrolledX));
        } else if (mScrolledX < 0) {
            mImageSwitcher.doSwitch(getBitmap(mCurrentPos + 1),
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
        final float turnPointAnimatedFraction = 0.15f;
        if (offset > 0) {
            mImageSwitcher.doSwitchAndFallback(
                    getBitmap(mCurrentPos), getBitmap(mCurrentPos + 1),
                    true,
                    turnPointAnimatedFraction);
        } else if (offset < 0) {
            mImageSwitcher.doSwitchAndFallback(
                    getBitmap(mCurrentPos), getBitmap(mCurrentPos - 1),
                    false,
                    turnPointAnimatedFraction);
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
        float fraction = 1f * scrolledX / mImageSwitcher.getWidth();
        if (fraction > 1f) {
            fraction = 1f;
        }
        return fraction;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mImageSwitcher.postDelayed(new Runnable() {
            @Override
            public void run() {
                mImageSwitcher.resetImage();
            }
        }, 100);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Uri imageUri = getIntent().getData();
        if (imageUri == null) {
            finish();
        }

        onCreate(savedInstanceState, R.layout.image_main);

        setupModel(imageUri);
        setupSwitcher();
        setupController();

        startInitializeTask();
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
                if (mImageSwitcher.isScaled()) {
                    mImageSwitcher.resetImage();
                } else {
                    // scale to full screen fit
                    Rect imageRect = mImageSwitcher.getImageRect();
                    if (imageRect.width() < mImageSwitcher.getWidth()
                            && imageRect.height() < mImageSwitcher.getHeight()) {
                        float scale = Math.min(1f * mImageSwitcher.getWidth() / imageRect.width(),
                                1f * mImageSwitcher.getHeight() / imageRect.height());
                        mImageSwitcher.doScale(scale);
                    }
                }
                return true;
            }

            @Override
            public boolean onFlingTo(int trend) {
                if (mImageSwitcher.isScaled()) {
                    return false;
                }
                switch (trend) {
                    case 6:
                        switchOrFallback(-1, false);
                        return true;
                    case 4:
                        switchOrFallback(1, false);
                        return true;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public boolean onScaleTo(float scale, int focusX, int focusY) {
                Rect imageRect = mImageSwitcher.getImageRect();
                if (!imageRect.contains(focusX, focusY)
                        || (imageRect.width() <= mImageSwitcher.getWidth()
                        && imageRect.height() <= mImageSwitcher.getHeight())) {
                    // focus on the center
                    mImageSwitcher.doScale(scale);
                } else {
                    mImageSwitcher.doScale(scale, focusX, focusY);
                }
                return true;
            }

            @Override
            public boolean onScrollBy(int[] totalDiff, int[] lastDiff, int trend) {
                if (mImageSwitcher.isScaled()) {
                    Rect imageRect = mImageSwitcher.getImageRect();
                    if (imageRect.width() > mImageSwitcher.getWidth()
                            || imageRect.height() > mImageSwitcher.getHeight()) {
                        imageRect.offset(-lastDiff[0], -lastDiff[1]);
                        if (imageRect.contains(mImageSwitcher.getWidth() / 2,
                                mImageSwitcher.getHeight() / 2)) {
                            mImageSwitcher.doOffset(-lastDiff[0], -lastDiff[1]);
                        }
                    }
                    return true;
                }
                switch (trend) {
                    case 4:
                    case 6:
                        if (totalDiff[0] < 0) {
                            mImageSwitcher.doScroll(
                                    getBitmap(mCurrentPos),
                                    getBitmap(mCurrentPos + 1),
                                    true,
                                    getScrolledFraction(totalDiff[0]));
                        } else if (totalDiff[0] > 0) {
                            mImageSwitcher.doScroll(
                                    getBitmap(mCurrentPos),
                                    getBitmap(mCurrentPos - 1),
                                    false,
                                    getScrolledFraction(totalDiff[0]));
                        }
                        mScrolledX = totalDiff[0];
                        return true;
                }
                return false;
            }

            @Override
            public boolean onTapUp() {
                if (mImageSwitcher.isScaled()) {
                    return false;
                }
                if (getScrolledFraction(mScrolledX) < 0.5f) {
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
                        mImageSwitcher.resetImage();
                        switchOrFallback(1, true);
                    }
                });

        findViewById(R.id.btnPrev).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageSwitcher.resetImage();
                        switchOrFallback(-1, true);
                    }
                });
    }

    private void setupModel(Uri seedUri) {
        mModel = new Model();
        mModel.initializeByUri(seedUri);
        mCurrentPos = mModel.indexOf(seedUri);
    }

    private void setupSwitcher() {
        mImageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        mCache = new Cache<Bitmap>();
        mUpdateCacheTaskArgument = new UpdateCacheTaskArgument();
    }

    private void startInitializeTask() {
        mUpdateCacheTaskArgument.set(mCurrentPos, null);

        new UpdateCacheTask() {
            @Override
            protected Void doInBackground(UpdateCacheTaskArgument... params) {
                UpdateCacheTaskArgument a = params[0];
                a.bitmap = createBitmap(a.pos);
                mCache.roll(a.pos, a.bitmap);
                mCache.set(a.pos, a.bitmap);
                return null;
            }

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
        int pos = mCurrentPos + offset;
        if (!mModel.hasIndex(pos)) {
            return false;
        }

        mImageSwitcher.doneSwitching();

        Bitmap bitmap = getBitmap(pos);
        if (offset == 0) {
            mImageSwitcher.firstLoad(bitmap);
        } else if (offset > 0) {
            mImageSwitcher.doSwitch(getBitmap(mCurrentPos), bitmap,
                    true,
                    getScrolledFraction(mScrolledX));
        } else {
            mImageSwitcher.doSwitch(getBitmap(mCurrentPos), bitmap,
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
        if (!switchBy(offset)) {
            if (triggedByButton) {
                fallbackSwitchingForButton(offset);
            } else {
                fallbackSwitching();
            }
        }
    }

    private void updateCache(int pos, Bitmap bitmap) {
        if (bitmap == null) {
            bitmap = createBitmap(pos);
        }
        mCache.roll(pos, bitmap);
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

class Model {

    private List<Uri> dataList;

    public Model() {
        clear();
    }

    public void clear() {
        if (dataList == null) {
            dataList = new LinkedList<Uri>();
        } else {
            dataList.clear();
        }
    }

    public int getCount() {
        return dataList.size();
    }

    public Uri getData(int i) {
        if (hasIndex(i)) {
            return dataList.get(i);
        }
        return null;
    }

    public boolean hasIndex(int i) {
        return i >= 0 && i < dataList.size();
    }

    public int indexOf(Uri uri) {
        return dataList.indexOf(uri);
    }

    private void initializeByDirectory(File seedDiretory) {
        if (!seedDiretory.exists() || !seedDiretory.isDirectory()) {
            return;
        }

        File[] files = seedDiretory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && MimeUtil.isImage(file)) {
                    dataList.add(Uri.fromFile(file));
                }
            }
        }
    }

    // add all peer files
    private void initializeByFile(File seedFile) {
        if (!seedFile.exists() || !seedFile.isFile()) {
            return;
        }
        File seedDirectory = seedFile.getParentFile();
        if (seedDirectory == null) {
            dataList.add(Uri.fromFile(seedFile));
            return;
        }
        initializeByDirectory(seedDirectory);
    }

    public void initializeByUri(Uri seedUri) {
        clear();

        if (seedUri.isAbsolute()) {
            if (!seedUri.getScheme().equals("file")) {
                dataList.add(seedUri);
                return;
            }
        }

        File seedFile = new File(seedUri.getPath());
        if (seedFile.isFile()) {
            initializeByFile(seedFile);
        } else if (seedFile.isDirectory()) {
            initializeByDirectory(seedFile);
        }
    }
}
