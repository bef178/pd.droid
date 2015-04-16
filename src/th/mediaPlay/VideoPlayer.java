
package th.mediaPlay;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.widget.VideoView;

/**
 * an adapter of VideoView
 *
 * @author tanghao
 */
public class VideoPlayer {

    /**
     * don't change status when polling
     */
    class StatusPoller implements Runnable {
        private static final int DELAY = 50;

        private static final int PURPOSE_FLAG_EXPECT_PLAYING = 0x01;
        // enable seek and preivew when "stopped"
        private static final int PURPOSE_FLAG_GO_PAUSED = 0x02;

        private Handler hander;
        private int purpose = 0;

        public StatusPoller(Handler handler) {
            this.hander = handler;
        }

        public void addPurpose(int purpose) {
            this.purpose |= purpose;
        }

        private void clearPurpose(int purpose) {
            this.purpose &= ~purpose;
        }

        private boolean hasPurpose(int purpose) {
            return (this.purpose & purpose) != 0;
        }

        public void launchPoll() {
            hander.removeCallbacks(this);
            if (purpose != 0) {
                hander.postDelayed(this, DELAY);
            }
        }

        @Override
        public void run() {
            if (mVideoView.isPlaying()) {
                mStatus = STATE_PLAYING;
            }
            switch (mStatus) {
                case STATE_PREPARING:
                    break;
                case STATE_PLAYING:
                    if (hasPurpose(PURPOSE_FLAG_EXPECT_PLAYING)) {
                        clearPurpose(PURPOSE_FLAG_EXPECT_PLAYING);
                    }
                    if (hasPurpose(PURPOSE_FLAG_GO_PAUSED)) {
                        clearPurpose(PURPOSE_FLAG_GO_PAUSED);
                        pause();
                        mVideoView.seekTo(0);
                    }
                    break;
                case STATE_PAUSED:
                    break;
                case STATE_STOPPED:
                    if (hasPurpose(PURPOSE_FLAG_GO_PAUSED)) {
                        setVideoUri(mVideoUri);
                        startVideo(0);

                    }
                    break;
            }
            launchPoll();
        }

        public void run(int purpose) {
            addPurpose(purpose);
            run();
        }
    }

    private static final int STATE_STOPPED = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSED = 3;

    private VideoView mVideoView;
    private Uri mVideoUri;

    private Handler mHandler;

    private int mStatus = STATE_STOPPED;

    private StatusPoller mStatusPoller;

    public VideoPlayer(VideoView videoView) {
        mVideoView = videoView;
        mVideoView.setOnClickListener(null);
        mVideoView.setOnErrorListener(null);
        mVideoView.setOnPreparedListener(null);
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
        mVideoView.setOnTouchListener(null);

        mHandler = new Handler(videoView.getContext().getMainLooper());

        mStatusPoller = new StatusPoller(mHandler);
    }

    public void pause() {
        mVideoView.pause();
        mStatus = STATE_PAUSED;
    }

    /**
     * could be resume or restart
     */
    public void play() {
        if (mVideoUri == null) {
            return;
        }

        requestStopMusic();
        mVideoView.requestFocus(); // so it supports hot keys

        switch (mStatus) {
            case STATE_STOPPED:
                if (mVideoUri.isAbsolute()) {
                    String scheme = mVideoUri.getScheme();
                    if (scheme.equals("http") || scheme.equals("rtsp")) {
                        // callback: prepare-start
                    }
                }
                setVideoUri(mVideoUri);
                startVideo(0);
                mStatus = STATE_PLAYING;
                break;
            case STATE_PAUSED:
                startVideo(0);
                mStatus = STATE_PLAYING;
                break;
            default:
                break;
        }
    }

    public void play(Uri videoUri) {
        if (videoUri != mVideoUri) {
            setVideoUri(videoUri);
        }
        play();
    }

    public void playDelayed(int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                play();
            }
        }, delay);
    }

    private void requestStopMusic() {
        Intent intent = new Intent("com.android.music.musicservicecommand");
        intent.putExtra("command", "pause");
        mVideoView.getContext().sendBroadcast(intent);
    }

    public void setVideoUri(Uri videoUri) {
        mVideoUri = videoUri;
        mVideoView.setVideoURI(mVideoUri);
    }

    private void startVideo(int bookmark) {
        if (bookmark > 0) {
            mVideoView.seekTo(bookmark);
            // callback progress-update current, total
        }

        mVideoView.start();
        mStatus = STATE_PREPARING;
        mStatusPoller.run(StatusPoller.PURPOSE_FLAG_EXPECT_PLAYING);
        // callback load-start
        // callback prepare-start
    }

    public void stop() {
        if (mStatus != STATE_STOPPED) {
            mVideoView.stopPlayback();
        }
        mStatus = STATE_STOPPED;
        mStatusPoller.run(StatusPoller.PURPOSE_FLAG_GO_PAUSED);
    }

    public void togglePlayPause() {
        if (mVideoView.isPlaying()) {
            pause();
        } else {
            play();
        }
    }
}
