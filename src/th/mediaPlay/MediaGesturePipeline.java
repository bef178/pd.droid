package th.mediaPlay;

import th.mediaPlay.ElementalGestureDetector.OnTapListener;
import th.mediaPlay.MediaGesturePipeline.Callback;
import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

class ElementalGestureDetector {

    interface OnTapListener {

        boolean onTapDown(MotionEvent event);

        boolean onTapUp(MotionEvent event);
    }

    private OnTapListener mListener;
    private boolean isDown;

    public ElementalGestureDetector(OnTapListener listener) {
        mListener = listener;
    }

    public boolean isDown() {
        return this.isDown;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean isStatusChanged = false;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isStatusChanged = setState(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_DOWN:
                isStatusChanged = setState(false);
                break;
        }
        if (isStatusChanged && mListener != null) {
            if (this.isDown) {
                return mListener.onTapDown(event);
            } else {
                return mListener.onTapUp(event);
            }
        }
        return false;
    }

    private boolean setState(boolean isDown) {
        if (isDown == this.isDown) {
            return false;
        }
        this.isDown = isDown;
        return true;
    }
}

/**
 * all-in-one motion listener, to filter/reclassify the gesture
 */
class MediaGestureListener implements OnGestureListener,
        OnDoubleTapListener, OnScaleGestureListener, OnTapListener {

    private Callback mCallback;

    private float mScaleFactor;

    public MediaGestureListener(Callback callback) {
        mCallback = callback;
    }

    private float abs(float f) {
        return f >= 0f ? f : -f;
    }

    public Callback getCallback() {
        return mCallback;
    }

    private int getMoveTrend(float dx, float dy) {
        if (abs(dx) >= abs(dy)) {
            if (dx >= 0) {
                return 6;
            } else {
                return 4;
            }
        } else {
            if (dy >= 0) {
                return 8;
            } else {
                return 4;
            }
        }
    }

    private int getMoveTrendWithDistanceCheck(float dx, float dy) {
        final int THRESHOLD_DISTANCE = 100;
        int trend = getMoveTrend(dx, dy);
        switch (trend) {
            case 4:
            case 6:
                if (abs(dx) >= THRESHOLD_DISTANCE) {
                    return trend;
                }
                break;
            case 2:
            case 8:
                if (abs(dy) >= THRESHOLD_DISTANCE) {
                    return trend;
                }
                break;
        }
        return 5;
    }

    private int getMoveTrendWithVelocityCheck(float dx, float dy,
            float vx, float vy) {
        final int THRESHOLD_VELOCITY = 100;
        int trend = getMoveTrend(dx, dy);
        switch (trend) {
            case 4:
            case 6:
                if (abs(vx) >= THRESHOLD_VELOCITY) {
                    return trend;
                }
                break;
            case 2:
            case 8:
                if (abs(vy) >= THRESHOLD_VELOCITY) {
                    return trend;
                }
                break;
        }
        return 5;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return mCallback != null && mCallback.onDoubleTap();
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,
            float velocityX, float velocityY) {
        if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
            return false;
        }

        resetStatus();
        if (mCallback != null) {
            float dx = e2.getX() - e1.getX();
            float dy = e2.getY() - e1.getY();
            if (mCallback.onFlingTo(
                    getMoveTrendWithVelocityCheck(dx, dy,
                            velocityX, velocityY))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (mCallback != null) {
            mScaleFactor *= detector.getScaleFactor();
            mCallback.onScaleTo(mScaleFactor);
        }
        return true;
    }

    private void resetStatus() {
        mScaleFactor = 1f;
        if (mCallback != null) {
            mCallback.onScaleTo(mScaleFactor);
        }
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mScaleFactor = 1f;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
            return false;
        }
        resetStatus();
        if (mCallback != null) {
            int dx = (int) (e2.getRawX() - e1.getRawX());
            int trend = getMoveTrendWithDistanceCheck(
                    dx, e2.getRawY() - e1.getRawY());
            switch (trend) {
                case 6:
                case 4:
                    if (mCallback.onScrollBy(dx)) {
                        return true;
                    }
            }
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onTapDown(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTapUp(MotionEvent event) {
        if (mCallback != null) {
            return mCallback.onTapUp();
        }
        return false;
    }
}

public class MediaGesturePipeline {

    static interface Callback {

        boolean onDoubleTap();

        boolean onFlingTo(int trend);

        boolean onScaleTo(float scale);

        boolean onScrollBy(int dx);

        boolean onTapUp();
    }

    private MediaGestureListener mListener;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private ElementalGestureDetector mElementalGestureDetector;

    public MediaGesturePipeline(Context context, Callback callback) {
        mListener = new MediaGestureListener(callback);
        mGestureDetector = new GestureDetector(context, mListener);
        mScaleGestureDetector = new ScaleGestureDetector(context, mListener);
        mElementalGestureDetector = new ElementalGestureDetector(mListener);
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
        handled = mElementalGestureDetector.onTouchEvent(event) || handled;
        return handled;
    }
}
