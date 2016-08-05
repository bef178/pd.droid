package t.typedef.droid;

import android.graphics.Point;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

public abstract class DoubleClickListener implements View.OnTouchListener {

    private static final int ACCEPTABLE_DEVIATION = 10;
    private static final int ACCEPTABLE_TIMEOUT = 250;

    private long mLastActionTimestamp = -1;
    private Point mFirstTouchPoint = new Point();
    private int mDoubleClickStep = 0;

    public abstract void onDoubleClick(View view);

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                mLastActionTimestamp = SystemClock.elapsedRealtime();
                return true;
            case MotionEvent.ACTION_UP:
                onTouchUp(view, event);
                mLastActionTimestamp = SystemClock.elapsedRealtime();
                return true;
        }
        mLastActionTimestamp = SystemClock.elapsedRealtime();
        return false;
    }

    private void onTouchDown(MotionEvent event) {
        if (mDoubleClickStep == 0) {
            mFirstTouchPoint.x = (int) event.getRawX();
            mFirstTouchPoint.y = (int) event.getRawY();
            mDoubleClickStep = 1;
            return;
        }

        if (!testTimestamp()) {
            mDoubleClickStep = 1;
            return;
        }

        if (mDoubleClickStep == 2
                && testPosition((int) event.getRawX(),
                        (int) event.getRawY())) {
            mDoubleClickStep = 3;
        } else {
            mDoubleClickStep = 1;
        }
    }

    private void onTouchUp(View view, MotionEvent event) {
        if (!testTimestamp()
                || !testPosition((int) event.getRawX(),
                        (int) event.getRawY())) {
            mDoubleClickStep = 0;
            return;
        }

        ++mDoubleClickStep;
        if (mDoubleClickStep == 4) {
            mDoubleClickStep = 0;

            // trigger double click
            onDoubleClick(view);
        }
    }

    private boolean testPosition(int rawX, int rawY) {
        return rawX - mFirstTouchPoint.x <= ACCEPTABLE_DEVIATION
                && rawY - mFirstTouchPoint.y <= ACCEPTABLE_DEVIATION;
    }

    private boolean testTimestamp() {
        return SystemClock.elapsedRealtime() - mLastActionTimestamp <= ACCEPTABLE_TIMEOUT;
    }
}
