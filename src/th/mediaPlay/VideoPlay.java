package th.mediaPlay;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.widget.VideoView;

/**
 * an adapter of VideoView
 *
 * @author tanghao
 *
 */
public class VideoPlay {

	private VideoView mVideoView;
	private Uri mVideoUri;
	private boolean mStopped = false;

	private Handler mHandler;

	public VideoPlay(VideoView videoView) {
		mVideoView = videoView;
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mStopped = true;
			}
		});
		mHandler = new Handler(videoView.getContext().getMainLooper());
	}

	public void setVideoUri(Uri videoUri) {
		mVideoUri = videoUri;
		mVideoView.setVideoURI(mVideoUri);
	}

	public void pause() {
		mVideoView.pause();
	}

	public void play(Uri videoUri) {
		setVideoUri(videoUri);
		play();
	}

	public void togglePlayPause() {
		if (mVideoView.isPlaying()) {
			pause();
		} else {
			play();
		}
	}

	public void play() {
		if (mStopped) {
			mStopped = false;
			setVideoUri(mVideoUri);
		}
		mVideoView.start();
	}

	public void playAfter(int delay) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				play();
			}
		}, delay);
	}

	public void stop() {
		mStopped = true;
		mVideoView.stopPlayback();
		// TODO make screen display the first frame
	}
}
