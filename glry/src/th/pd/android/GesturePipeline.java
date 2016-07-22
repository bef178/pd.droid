package th.pd.android;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

import th.pd.android.ElementalGestureDetector.OnTapListener;
import th.pd.android.GesturePipeline.Callback;

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
    public boolean onFling(MotionEvent first, MotionEvent last,
            float velocityX, float velocityY) {
        if (first.getPointerCount() > 1 || last.getPointerCount() > 1) {
            return false;
        }
        if (mCallback != null) {
            if (mCallback.onFling(
                    (int) (last.getX() - first.getX()),
                    (int) (last.getY() - first.getY()),
                    velocityX, velocityY)) {
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
    public boolean onScroll(MotionEvent first, MotionEvent last,
            float distanceX, float distanceY) {
        if (first.getPointerCount() > 1 || last.getPointerCount() > 1) {
            return false;
        }
        if (mCallback != null) {
            float dxTotal = last.getRawX() - first.getRawX();
            float dyTotal = last.getRawY() - first.getRawY();
            return mCallback.onScrollBy((int) dxTotal, (int) dyTotal,
                    (int) distanceX, (int) distanceY);
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

    public static interface Callback {

        boolean onDoubleTap();

        boolean onFling(int dxTotal, int dyTotal,
                float velocityX, float velocityY);

        boolean onScaleTo(float scale, int focusX, int focusY);

        boolean onScrollBy(int dxTotal, int dyTotal, int dx, int dy);

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
