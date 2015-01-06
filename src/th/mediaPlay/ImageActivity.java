package th.mediaPlay;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import th.pd.R;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ImageActivity extends MediaPlayActivity {

	ImageView mImageView;
	Model mModel;
	int mCurrentPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Uri imageUri = getIntent().getData();
		if (imageUri == null) {
			finish();
		}

		onCreate(savedInstanceState, R.layout.image_main);
		mImageView = (ImageView) findViewById(R.id.imageView);

		setupModel(imageUri);
		setupView();
		setupController();
	}

	private void setupController() {
		findViewById(R.id.btnNext).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						switchOffset(1);
					}
				});

		findViewById(R.id.btnPrev).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						switchOffset(-1);
					}
				});
	}

	private void setupModel(Uri seedUri) {
		mModel = new Model();
		mModel.initializeByUri(seedUri);
		mCurrentPos = mModel.indexOf(seedUri);
	}

	private void setupView() {
		// TODO add animation
		Uri uri = mModel.getData(mCurrentPos);
		if (uri != null) {
			mImageView.setImageURI(uri);
			setTitleByUri(uri);
			setSummary(String.format("%d / %d", mCurrentPos + 1,
					mModel.getCount()));
		}
	}

	private void switchOffset(int offset) {
		if (mModel.hasIndex(mCurrentPos + offset)) {
			mCurrentPos += offset;
			setupView();
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

	public boolean hasIndex(int i) {
		return i >= 0 && i < dataList.size();
	}
}
