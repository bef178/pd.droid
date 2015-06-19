package th.media;

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
    static interface Callback {
        public static final int ACTION_PLAY = 1;
        public static final int ACTION_PAUSE = 2;
        public static final int ACTION_UPDATE_PROGRESS = 3;
        public static final int ACTION_UPDATE_PROGRESS_TOTAL = 4;

        public boolean onAction(int actionId, int extra);
        public boolean onError(MediaPlayer mp, int what, int extra);
    }

    /**
     * don't change status when polling
     */
    class StatusPoller implements Runnable {
        private static final int DELAY = 50;

        // switch state and update progress
        private static final int PURPOSE_FLAG_EXPECT_PLAYING = 0x01;
        // enable seek and preivew when "stopped"
        private static final int PURPOSE_FLAG_GO_PAUSED = 0x02;
        // update total progress
        private static final int PURPOSE_FLAG_EXPECT_DURATION = 0x04;

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
        public synchronized void run() {
            if (mVideoView.isPlaying()) {
                if (mStatus != STATE_PLAYING) {
                    mCallback.onAction(Callback.ACTION_PLAY, 0);
                }
                mStatus = STATE_PLAYING;
            }
            switch (mStatus) {
                case STATE_PREPARING:
                    break;
                case STATE_PLAYING:
                    if (hasPurpose(PURPOSE_FLAG_EXPECT_PLAYING)) {
                        mCallback.onAction(Callback.ACTION_UPDATE_PROGRESS,
                                mVideoView.getCurrentPosition());
                    }
                    if (hasPurpose(PURPOSE_FLAG_GO_PAUSED)) {
                        clearPurpose(PURPOSE_FLAG_GO_PAUSED);
                        pause();
                        seekTo(0);
                    }
                    if (hasPurpose(PURPOSE_FLAG_EXPECT_DURATION)) {
                        clearPurpose(PURPOSE_FLAG_EXPECT_DURATION);
                        mCallback.onAction(Callback.ACTION_UPDATE_PROGRESS_TOTAL,
                                mVideoView.getDuration());
                    }
                    break;
                case STATE_PAUSED:
                    clearPurpose(PURPOSE_FLAG_EXPECT_PLAYING);
                    break;
                case STATE_STOPPED:
                    clearPurpose(PURPOSE_FLAG_EXPECT_PLAYING);
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

    private Callback mCallback;

    private VideoView mVideoView;
    private Uri mVideoUri;

    private Handler mHandler;

    private int mStatus = STATE_STOPPED;

    private StatusPoller mStatusPoller;

    private int mBookmark = -1;

    public VideoPlayer(VideoView videoView, Callback callback) {
        mCallback = callback;
        mVideoView = videoView;
        mVideoView.setOnClickListener(null);
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return mCallback.onError(mp, what, extra);
            }
        });
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

    public void onPause() {
        pause();
        mBookmark = mVideoView.getCurrentPosition();
    }

    public void onResume() {
        if (mBookmark >= 0) {
            seekTo(mBookmark);
            mBookmark = -1;
        }
    }

    public void pause() {
        mVideoView.pause();
        mStatus = STATE_PAUSED;
        mCallback.onAction(Callback.ACTION_PAUSE, 0);
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
                break;
            case STATE_PAUSED:
                startVideo(0);
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

    public void seekTo(int bookmark) {
        mVideoView.seekTo(bookmark);
        mCallback.onAction(Callback.ACTION_UPDATE_PROGRESS, bookmark);
    }

    public void setVideoUri(Uri videoUri) {
        mVideoUri = videoUri;
        mVideoView.setVideoURI(mVideoUri);
        mStatusPoller.addPurpose(StatusPoller.PURPOSE_FLAG_EXPECT_DURATION);
    }

    private void startVideo(int bookmark) {
        if (bookmark > 0) {
            seekTo(bookmark);
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
