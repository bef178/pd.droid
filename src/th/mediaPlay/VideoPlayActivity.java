package th.mediaPlay;

import th.pd.R;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

public class VideoPlayActivity extends MediaPlayActivity {

	private static final int STARTING_DELAY = 1000;

	private AudioManager mAudioManager;

	private VideoPlay mPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Uri videoUri = getIntent().getData();
		if (videoUri == null) {
			finish();
		}

		onCreate(savedInstanceState, R.layout.mediaplay_layout);

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		setupPlayer(videoUri);
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
}
