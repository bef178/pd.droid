package th.mediaPlay;

import th.pd.R;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ImagePlayActivity extends MediaPlayActivity {

	ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Uri imageUri = getIntent().getData();
		if (imageUri == null) {
			finish();
		}

		onCreate(savedInstanceState, R.layout.imageview_layout);
		mImageView = (ImageView) findViewById(R.id.imageView);
		setupController();
		setupViewer(imageUri);
	}

	private void setupViewer(Uri imageUri) {
		mImageView.setImageURI(imageUri);
	}

	private void setupController() {
		findViewById(R.id.btnNext).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO switch to next image
					}
				});
	}
}
