package th.mediaPlay;

import th.mediaPlay.MediaGesturePipeline.Callback;
import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

class MediaGestureListener implements OnGestureListener,
        OnDoubleTapListener, OnScaleGestureListener {

    private Callback mCallback;

    public MediaGestureListener(Callback callback) {
        mCallback = callback;
    }

    private float abs(float f) {
        return f >= 0f ? f : -f;
    }

    public Callback getCallback() {
        return mCallback;
    }

    private int getMoveTrend(float dx, float dy, float vx, float vy) {
        final int THRESHOLD_DISTANCE = 100;
        final int THRESHOLD_VELOCITY = 100;
        if (abs(dx) >= abs(dy)) {
            if (abs(dx) > THRESHOLD_DISTANCE
                    && abs(vx) > THRESHOLD_VELOCITY) {
                if (dx > 0) {
                    return 6;
                } else {
                    return 4;
                }
            }
        } else {
            if (abs(dy) > THRESHOLD_DISTANCE
                    && abs(vy) > THRESHOLD_VELOCITY) {
                if (dy > 0) {
                    return 8;
                } else {
                    return 2;
                }
            }
        }
        return 5;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,
            float velocityX, float velocityY) {
        if (mCallback != null) {
            float dx = e2.getX() - e1.getX();
            float dy = e2.getY() - e1.getY();
            if (mCallback.onFlingTo(
                    getMoveTrend(dx, dy, velocityX, velocityY))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        if (mCallback != null) {
            if (mCallback.onScrollBy(
                    (int) (e2.getRawX() - e1.getRawX()),
                    (int) (e2.getRawY() - e1.getRawY()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }
}

public class MediaGesturePipeline {

    static interface Callback {

        boolean onFlingTo(int trend);

        boolean onScrollBy(int dx, int dy);
    }

    private MediaGestureListener mListener;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    public MediaGesturePipeline(Context context, Callback callback) {
        mListener = new MediaGestureListener(callback);
        mGestureDetector = new GestureDetector(context, mListener);
        mScaleGestureDetector = new ScaleGestureDetector(context, mListener);
    }

    public Callback getCallback() {
        if (mListener != null) {
            return mListener.getCallback();
        }
        return null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        handled = mGestureDetector.onTouchEvent(event) || handled;
        handled = mScaleGestureDetector.onTouchEvent(event) || handled;
        return handled;
    }
}
