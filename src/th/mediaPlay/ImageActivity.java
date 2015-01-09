package th.mediaPlay;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import th.pd.Cache;
import th.pd.R;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class ImageActivity extends MediaPlayActivity {

	Model mModel;
	int mCurrentPos;
	ImageSwitcher mImageSwitcher;
	Cache<View> mCache;
	MediaGestureDetector mGestureDetector;

	private View createImageView(int pos) {
		ImageView imageView = (ImageView) View.inflate(
				this, R.layout.image_item, null);
		Uri uri = mModel.getData(pos);
		if (uri != null) {
			imageView.setImageURI(uri);
		} else {
			imageView.setImageResource(android.R.color.holo_green_dark);
		}
		return imageView;
	}

	private View getImageView(int pos) {
		View view = mCache.get(pos);
		if (view != null) {
			return view;
		} else {
			return createImageView(pos);
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

		mCache = new Cache<View>();

		switchBy(0);
	}

	/**
	 * @param offset
	 *            switch to next if positive, to prev if negative
	 */
	private void switchBy(int offset) {
		Animation animEnter = null;
		Animation animLeave = null;
		if (offset > 0) {
			animEnter = AnimationUtils.loadAnimation(this,
					R.anim.enter_scale_from_center);
			animLeave = AnimationUtils.loadAnimation(this,
					R.anim.leave_trans_to_left);
		} else if (offset < 0) {
			animEnter = AnimationUtils.loadAnimation(this,
					R.anim.enter_trans_from_left);
			animLeave = AnimationUtils.loadAnimation(this,
					R.anim.leave_scale_to_center);
		}

		int pos = mCurrentPos + offset;
		if (mModel.hasIndex(pos)) {
			View nextView = getImageView(pos);
			mImageSwitcher.switchTo(nextView, animEnter, animLeave);
			mCurrentPos = pos;
			setTitleByUri(mModel.getData(pos));
			setSummary(String.format("%d / %d", pos + 1, mModel.getCount()));
			updateCache(pos, nextView);
		}
	}

	private void updateCache(int pos, View view) {
		// TODO this should be in another thread
		mCache.update(pos, view);
		for (int i = 1; i <= Cache.RADIUS; ++i) {
			if (mCache.get(pos + i) == null) {
				view = createImageView(pos + i);
				mCache.set(pos + i, view);
			}
			if (mCache.get(pos - i) == null) {
				view = createImageView(pos - i);
				mCache.set(pos - i, view);
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
				if (file.isFile()) {
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
