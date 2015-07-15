package th.media;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

import th.media.ElementalGestureDetector.OnTapListener;
import th.media.GesturePipeline.Callback;

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
class GestureListener implements OnGestureListener,
        OnDoubleTapListener, OnScaleGestureListener, OnTapListener {

    private Callback mCallback;

    public GestureListener(Callback callback) {
        mCallback = callback;
    }

    private static float abs(float f) {
        return f >= 0f ? f : -f;
    }

    private static int getMoveTrend(float dx, float dy) {
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

    private static int getMoveTrendWithDistanceCheck(float dx, float dy) {
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

    private static int getMoveTrendWithVelocityCheck(float dx, float dy,
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

    public Callback getCallback() {
        return mCallback;
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
            mCallback.onScaleTo(detector.getScaleFactor(),
                    (int) detector.getFocusX(),
                    (int) detector.getFocusY());
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
            float distanceX, float distanceY) {
        if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
            return false;
        }
        if (mCallback != null) {
            float dx = e2.getRawX() - e1.getRawX();
            float dy = e2.getRawY() - e1.getRawY();
            int trend = getMoveTrendWithDistanceCheck(dx, dy);
            return mCallback.onScrollBy(new int[] {
                    (int) dx, (int) dy
            }, new int[] {
                    (int) distanceX, (int) distanceY
            }, trend);
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

public class GesturePipeline {

    static interface Callback {

        boolean onDoubleTap();

        boolean onFlingTo(int trend);

        boolean onScaleTo(float scale, int focusX, int focusY);

        boolean onScrollBy(int[] totalDiff, int[] lastDiff, int trend);

        boolean onTapUp();
    }

    private GestureListener mListener;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private ElementalGestureDetector mElementalGestureDetector;

    public GesturePipeline(Context context, Callback callback) {
        mListener = new GestureListener(callback);
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
        handled = handled || mGestureDetector.onTouchEvent(event);
        handled = handled || mScaleGestureDetector.onTouchEvent(event);
        handled = mElementalGestureDetector.onTouchEvent(event) || handled; // must run
        return handled;
    }
}
