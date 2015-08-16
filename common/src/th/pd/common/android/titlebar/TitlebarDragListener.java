package th.pd.common.android.titlebar;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import th.pd.common.android.DragListener;

/**
 * the finger offset maps to the anchor coordinate<br/>
 * the anchor coordinate is restricted then maps to the layout offset<br/>
 */
public class TitlebarDragListener extends DragListener {

    private static final int SNAP_OFFSET = 12;

    private Activity mActivity;
    private int[] mMargin;
    private View mDecorView;
    private WindowManager.LayoutParams mLayoutParams;

    private Rect mAcceptableAnchorRect = new Rect();

    /**
     * the window anchor coord in screen<br/>
     * with layoutParams we have<br/>
     * &emsp; rawAnchor.x - layoutParams.x == screenAnchor.x<br/>
     * &emsp; rawAnchor.y - layoutParams.y == screenAnchor.y<br/>
     */
    private int[] mRawAnchor = new int[2];

    /**
     * the anchor coord in window
     */
    private int[] mAnchorFromTopleft = new int[2];

    private int[] mTouchPointFromWindowAnchor = new int[2];

    private int[] mScreenSize = new int[2];

    public TitlebarDragListener(Activity a) {
        this(a, new int[] {
                0,
                0,
                0,
                0
        });
    }

    public TitlebarDragListener(Activity a, int[] margin) {
        mActivity = a;
        mDecorView = a.getWindow().getDecorView();
        mMargin = margin;
    }

    private void findAcceptableRectForWindowAnchor(View handleView) {
        // the boundary for the anchor point;
        // for determine whether the window goes beyoud the boundary
        mAcceptableAnchorRect.top = -handleView.getTop() + mMargin[0];
        mAcceptableAnchorRect.right =
                mScreenSize[0] - handleView.getRight() - mMargin[1];
        mAcceptableAnchorRect.bottom =
                mScreenSize[1] - handleView.getBottom() - mMargin[2];
        mAcceptableAnchorRect.left = -handleView.getLeft() + mMargin[3];
        mAcceptableAnchorRect.offset(mDecorView.getWidth() / 2,
                mDecorView.getHeight() / 2);
    }

    private void findScreenSize(Activity a, int[] screenSize) {
        Display defDisplay = a.getWindowManager().getDefaultDisplay();
        Point p = new Point();
        defDisplay.getSize(p);
        screenSize[0] = p.x;
        screenSize[1] = p.y;
    }

    private void findWindowAnchor(View decorView,
            int[] anchorFromTopleft, int[] rawWindowAnchorCoord) {
        // take the center point as anchor point
        anchorFromTopleft[0] = decorView.getWidth() / 2;
        anchorFromTopleft[1] = decorView.getHeight() / 2;

        decorView.getLocationOnScreen(rawWindowAnchorCoord); // topleft
        rawWindowAnchorCoord[0] += anchorFromTopleft[0];
        rawWindowAnchorCoord[1] += anchorFromTopleft[1];
    }

    @Override
    public void onDrag(View view, int rawX, int rawY) {
        int rawWindowAnchorX = rawX - mTouchPointFromWindowAnchor[0];
        int rawWindowAnchorY = rawY - mTouchPointFromWindowAnchor[1];

        if (rawWindowAnchorX < mAcceptableAnchorRect.left) {
            rawWindowAnchorX = mAcceptableAnchorRect.left;
        } else if (rawWindowAnchorX > mAcceptableAnchorRect.right) {
            rawWindowAnchorX = mAcceptableAnchorRect.right;
        } else {
            rawWindowAnchorX = snapX(rawWindowAnchorX);
        }

        if (rawWindowAnchorY < mAcceptableAnchorRect.top) {
            rawWindowAnchorY = mAcceptableAnchorRect.top;
        } else if (rawWindowAnchorY > mAcceptableAnchorRect.bottom) {
            rawWindowAnchorY = mAcceptableAnchorRect.bottom;
        } else {
            rawWindowAnchorY = snapY(rawWindowAnchorY);
        }

        // the offset after restrict
        int dx = rawWindowAnchorX - mRawAnchor[0];
        int dy = rawWindowAnchorY - mRawAnchor[1];

        mLayoutParams = (WindowManager.LayoutParams) mDecorView
                .getLayoutParams();
        mLayoutParams.x += dx;
        mLayoutParams.y += dy;
        mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mActivity.getWindowManager().updateViewLayout(
                mDecorView, mLayoutParams);

        mRawAnchor[0] += dx;
        mRawAnchor[1] += dy;
    }

    @Override
    public void onDragBegin(View view, int rawX, int rawY) {
        findWindowAnchor(mDecorView, mAnchorFromTopleft,
                mRawAnchor);
        findScreenSize(mActivity, mScreenSize);
        findAcceptableRectForWindowAnchor(view);
        mTouchPointFromWindowAnchor[0] = rawX - mRawAnchor[0];
        mTouchPointFromWindowAnchor[1] = rawY - mRawAnchor[1];
    }

    @Override
    public void onDragEnd(View arg0, int arg1, int arg2) {
        // dummy
    }

    public void setMargin(int[] margin) {
        mMargin = margin;
    }

    private int snapX(int rawWindowAnchorX) {
        int left = rawWindowAnchorX - mAnchorFromTopleft[0];
        int right = left + mDecorView.getWidth();
        if (Math.abs(left) < SNAP_OFFSET) {
            rawWindowAnchorX -= left;
        } else if (Math.abs(mScreenSize[0] - right) < SNAP_OFFSET) {
            rawWindowAnchorX += mScreenSize[0] - right;
        }
        return rawWindowAnchorX;
    }

    private int snapY(int rawWindowAnchorY) {
        int top = rawWindowAnchorY - mAnchorFromTopleft[1];
        int bottom = top + mDecorView.getHeight();
        if (Math.abs(top) < SNAP_OFFSET) {
            rawWindowAnchorY -= top;
        } else if (Math.abs(mScreenSize[1] - bottom) < SNAP_OFFSET) {
            rawWindowAnchorY += mScreenSize[1] - bottom;
        }
        return rawWindowAnchorY;
    }

}
