package th.mediaPlay;

import java.io.File;

import th.pageHeader.PageHeader;
import th.pd.R;
import th.pd.SystemUiUtil;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.VideoView;

public class VideoPlayActivity extends Activity {

	private static final String LOG_TAG = VideoPlayActivity.class.getName();

	private static final int HEADER_DISPLAY_TIMEOUT = 3000;
	private static final int STARTING_DELAY = 1000;

	public static final String INTENT_EXTRA_LOGO = "intent.extra.LOGO";

	private AudioManager mAudioManager;

	private PageHeader mPageHeader;

	private VideoPlay mPlayer;

	private boolean mHasIntentTitle = false;

	private void hideSystemUi() {
		SystemUiUtil.hideSystemUi(mPageHeader.getView().getRootView());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Uri videoUri = getIntent().getData();
		if (videoUri == null) {
			finish();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.mediaplay_layout);

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		setupPageHeader();
		setupPlayer(videoUri);
	}

	@Override
	protected void onPause() {
		mPageHeader.hideImmediately();

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		hideSystemUi();
		mPageHeader.showWithAnim();
		mPageHeader.hideWithDelay(HEADER_DISPLAY_TIMEOUT);
	}

	@Override
	protected void onStart() {
		mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mAudioManager.abandonAudioFocus(null);
	}

	private void setupPageHeader() {
		mPageHeader = new PageHeader(findViewById(R.id.pageHeader));
		mPageHeader.setBackButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				VideoPlayActivity.this.onBackPressed();
			}
		});

		findViewById(R.id.btnToggleHeader).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mPageHeader.isFinallyVisible()) {
							mPageHeader.hideWithAnim();
						} else {
							mPageHeader.showWithAnim();
							mPageHeader.hideWithDelay(HEADER_DISPLAY_TIMEOUT);
						}
					}
				});

		setVideoLogo();
		setVideoTitle();
	}

	private void setupPlayer(Uri videoUri) {
		mPlayer = new VideoPlay((VideoView) findViewById(R.id.videoView));
		mPlayer.setVideoUri(videoUri);
		// TODO play after animation done
		mPlayer.playAfter(STARTING_DELAY);

		findViewById(R.id.btnVideoPlay).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPlayer.togglePlayPause();
					}
				});

		findViewById(R.id.btnVideoStop).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPlayer.stop();
					}
				});
	}

	private void setVideoLogo() {
		Bitmap logo = getIntent().getParcelableExtra(INTENT_EXTRA_LOGO);
		if (logo != null) {
			getActionBar().setLogo(
					new BitmapDrawable(getResources(), logo));
		}
	}

	private void setVideoTitle() {
		String title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
		if (title != null) {
			mPageHeader.setTitle(title);
			mHasIntentTitle = true;
			return;
		}

		setVideoTitleByUri(getIntent().getData());
	}

	/**
	 * @return <code>true</code> iff title do be set by this method
	 */
	private boolean setVideoTitleByUri(Uri uri) {
		if (mHasIntentTitle || uri == null) {
			return false;
		}

		if (uri.isRelative()) {
			// try file scheme
			File f = new File(uri.toString());
			if (f.exists() && f.isFile()) {
				// same as file scheme
				mPageHeader.setTitle(f.getName());
				return true;
			}
			return false;
		}

		String origUriScheme = uri.getScheme();
		if (origUriScheme.equals("content")) {
			setVideoTitleByQuery(uri);
			return true;
		} else if (origUriScheme.equals("file")) {
			mPageHeader.setTitle(new File(uri.toString()).getName());
			return true;
		}
		return false;
	}

	private void setVideoTitleByQuery(Uri contentUri) {
		AsyncQueryHandler handler =
				new AsyncQueryHandler(getContentResolver()) {
					@Override
					protected void onQueryComplete(int token, Object cookie,
							Cursor cursor) {
						try {
							if ((cursor != null) && cursor.moveToFirst()) {
								mPageHeader.setTitle(cursor.getString(0));
							}
						} finally {
							try {
								if (cursor != null) {
									cursor.close();
								}
							} catch (Throwable t) {
								Log.w(LOG_TAG, "fail to close", t);
							}
						}
					}
				};
		handler.startQuery(
				0, null, getIntent().getData(),
				new String[] { OpenableColumns.DISPLAY_NAME }, null,
				null, null);
	}
}
