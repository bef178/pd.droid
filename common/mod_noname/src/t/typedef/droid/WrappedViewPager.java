package t.typedef.droid;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class WrappedViewPager extends ViewPager {

    private float x0;
    private float y0;

    private boolean mClaimed = false;

    public WrappedViewPager(Context context) {
        super(context);
    }

    public WrappedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                x0 = ev.getRawX();
                y0 = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mClaimed) {
                    if (shouldClaimEvent(x0, y0, ev.getRawX(), ev.getRawY())) {
                        super.requestDisallowInterceptTouchEvent(true);
                        mClaimed = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mClaimed = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    protected boolean shouldClaimEvent(float x0, float y0,
            float x1, float y1) {
        float dx = Math.abs(x1 - x0);
        float dy = Math.abs(y1 - y0);
        return dy < 10 && dx / dy >= 1.732;
    }
}
