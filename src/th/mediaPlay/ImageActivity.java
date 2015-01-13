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
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class ImageActivity extends MediaPlayActivity {

    Model mModel;
    int mCurrentPos;
    ImageSwitcher mImageSwitcher;
    Cache<Bitmap> mCache;
    MediaGestureDetector mGestureDetector;

    private Bitmap createBitmap(int pos) {
        Uri uri = mModel.getData(pos);
        if (uri != null) {
            return BitmapFactory.decodeFile(uri.getPath());
        }
        return null;
    }

    private Bitmap getBitmap(int pos) {
        Bitmap bitmap = mCache.get(pos);
        if (bitmap != null) {
            return bitmap;
        } else {
            return createBitmap(pos);
        }
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private void setupController() {
        mGestureDetector = new MediaGestureDetector(this,
                new MediaGestureListener.Callback() {
                    @Override
                    public boolean onFlingTo(int trend) {
                        switch (trend) {
                            case 6:
                                switchBy(-1);
                                return true;
                            case 4:
                                switchBy(1);
                                return true;
                            default:
                                break;
                        }
                        return false;
                    }
                });

        findViewById(R.id.btnNext).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchBy(1);
                    }
                });

        findViewById(R.id.btnPrev).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchBy(-1);
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
        switchBy(0);
    }

    /**
     * @param offset
     *            switch to next if positive, to prev if negative
     */
    private void switchBy(int offset) {
        int pos = mCurrentPos + offset;
        if (mModel.hasIndex(pos)) {
            Bitmap nextBitmap = getBitmap(pos);
            mImageSwitcher.switchTo(nextBitmap, offset >= 0);

            mCurrentPos = pos;

            setTitleByUri(mModel.getData(pos));
            setSummary(String.format("%d / %d", pos + 1, mModel.getCount()));

            updateCache(pos, nextBitmap);
        } else {
            // TODO prompt user the request is out of range
        }
    }

    private void updateCache(int pos, Bitmap bitmap) {
        // TODO should be in another thread
        mCache.update(pos, bitmap);
        for (int i = 1; i <= Cache.RADIUS; ++i) {
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
