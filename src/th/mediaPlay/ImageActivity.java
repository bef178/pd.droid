package th.mediaPlay;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import th.pd.Cache;
import th.pd.MimeUtil;
import th.pd.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private MediaGestureDetector mGestureDetector;

    private int mScrolledX;

    private Bitmap createBitmap(int pos) {
        Uri uri = mModel.getData(pos);
        if (uri != null) {
            return BitmapFactory.decodeFile(uri.getPath());
        }
        return null;
    }

    private void fallbackSwitching() {
        mImageSwitcher.fallbackSwitching(getScrolledFraction(mScrolledX));
        mScrolledX = 0;
    }

    // TODO play animation 'forward' and 'backward'
    private void fallbackSwitchingForKey() {
        float fraction = 0.85f;
        mImageSwitcher.fallbackSwitching(fraction);
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
    protected void onCreate(Bundle savedInstanceState) {
        Uri imageUri = getIntent().getData();
        if (imageUri == null) {
            finish();
        }

        onCreate(savedInstanceState, R.layout.image_main);

        setupModel(imageUri);
        setupSwitcher();
        setupController();
        switchBy(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        if ((event.getAction() & MotionEvent.ACTION_UP) == MotionEvent.ACTION_UP) {
            if (getScrolledFraction(mScrolledX) < 0.5f) {
                fallbackSwitching();
            } else {
                if (mScrolledX > 0) {
                    switchOrFallback(-1);
                } else {
                    switchOrFallback(1);
                }
            }
        }
        return false;
    }

    private void setupController() {
        mGestureDetector = new MediaGestureDetector(this,
                new MediaGestureListener.Callback() {
                    @Override
                    public boolean onFlingTo(int trend) {
                        switch (trend) {
                            case 6:
                                switchOrFallback(-1);
                                return true;
                            case 4:
                                switchOrFallback(1);
                                return true;
                            default:
                                break;
                        }
                        return false;
                    }

                    @Override
                    public boolean onScrollBy(int dx, int dy) {
                        if (dx < 0) {
                            if (mScrolledX >= 0) {
                                Bitmap bitmap = getBitmap(mCurrentPos + 1);
                                mImageSwitcher.setComingImage(bitmap, true);
                            }
                        } else if (dx > 0) {
                            if (mScrolledX <= 0) {
                                Bitmap bitmap = getBitmap(mCurrentPos - 1);
                                mImageSwitcher.setComingImage(bitmap, false);
                            }
                        }

                        mImageSwitcher.scrollTo(getScrolledFraction(dx));
                        mScrolledX = dx;

                        return true;
                    }
                });

        findViewById(R.id.btnNext).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchOrFallbackForKey(1);
                    }
                });

        findViewById(R.id.btnPrev).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchOrFallbackForKey(-1);
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

        if (mImageSwitcher.isSwitching()) {
            mImageSwitcher.doneSwitching();
        }

        Bitmap bitmap = getBitmap(pos);
        mImageSwitcher.setComingImage(bitmap, offset >= 0);
        mImageSwitcher.switchTo(getScrolledFraction(mScrolledX));

        mScrolledX = 0;
        mCurrentPos = pos;

        setTitleByUri(mModel.getData(pos));
        setSummary(String.format("%d / %d", pos + 1, mModel.getCount()));

        startUpdateCacheTask(pos, bitmap);

        return true;
    }

    private void switchOrFallback(int offset) {
        if (!switchBy(offset)) {
            fallbackSwitching();
        }
    }

    private void switchOrFallbackForKey(int offset) {
        if (!switchBy(offset)) {
            fallbackSwitchingForKey();
        }
    }

    private void updateCache(int pos, Bitmap bitmap) {
        mCache.update(pos, bitmap);
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
