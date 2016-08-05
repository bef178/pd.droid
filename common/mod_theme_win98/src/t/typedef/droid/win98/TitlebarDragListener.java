package t.typedef.droid.win98;

import android.graphics.Rect;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import t.typedef.droid.DragListener;
import t.typedef.droid.SystemUiUtil;

/**
 * the finger offset maps to the anchor coordinate<br/>
 * the anchor coordinate is restricted then maps to the layout offset<br/>
 */
class TitlebarDragListener extends DragListener {

    private static final int SNAP_OFFSET = 12;

    private Window mWindow;
    private int[] mDragMargin;
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

    public TitlebarDragListener(Window window, int[] dragMargin) {
        mWindow = window;
        mDragMargin = new int[] {
                0, 0, 0, 0
        };
        setDragMargin(dragMargin);
    }

    private void findAcceptableRectForWindowAnchor(View handleView) {
        // the boundary for the anchor point;
        // for determine whether the window goes beyond the boundary
        View decorView = mWindow.getDecorView();
        mAcceptableAnchorRect.top = -handleView.getTop() + mDragMargin[0];
        mAcceptableAnchorRect.right =
                mScreenSize[0] - handleView.getRight() - mDragMargin[1];
        mAcceptableAnchorRect.bottom =
                mScreenSize[1] - handleView.getBottom() - mDragMargin[2];
        mAcceptableAnchorRect.left = -handleView.getLeft() + mDragMargin[3];
        mAcceptableAnchorRect.offset(decorView.getWidth() / 2,
                decorView.getHeight() / 2);
    }

    private static void findWindowAnchor(View decorView,
            int[] anchorFromTopleft, int[] rawWindowAnchorCoord) {
        // anchor to the center
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

        mLayoutParams = (WindowManager.LayoutParams) mWindow.getDecorView()
                .getLayoutParams();
        mLayoutParams.x += dx;
        mLayoutParams.y += dy;
        mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindow.getWindowManager().updateViewLayout(
                mWindow.getDecorView(), mLayoutParams);

        mRawAnchor[0] += dx;
        mRawAnchor[1] += dy;
    }

    @Override
    public void onDragBegin(View view, int rawX, int rawY) {
        findWindowAnchor(mWindow.getDecorView(), mAnchorFromTopleft,
                mRawAnchor);
        SystemUiUtil.findScreenResolution(
                mWindow.getWindowManager(), mScreenSize);
        findAcceptableRectForWindowAnchor(view);
        mTouchPointFromWindowAnchor[0] = rawX - mRawAnchor[0];
        mTouchPointFromWindowAnchor[1] = rawY - mRawAnchor[1];
    }

    @Override
    public void onDragEnd(View arg0, int arg1, int arg2) {
        // dummy
    }

    public void setDragMargin(int[] dragMargin) {
        if (dragMargin != null) {
            for (int i = 0; i < 4; i++) {
                mDragMargin[i] = dragMargin[i];
            }
        }
    }

    private int snapX(int rawWindowAnchorX) {
        int left = rawWindowAnchorX - mAnchorFromTopleft[0];
        int right = left + mWindow.getDecorView().getWidth();
        if (Math.abs(left) < SNAP_OFFSET) {
            rawWindowAnchorX -= left;
        } else if (Math.abs(mScreenSize[0] - right) < SNAP_OFFSET) {
            rawWindowAnchorX += mScreenSize[0] - right;
        }
        return rawWindowAnchorX;
    }

    private int snapY(int rawWindowAnchorY) {
        int top = rawWindowAnchorY - mAnchorFromTopleft[1];
        int bottom = top + mWindow.getDecorView().getHeight();
        if (Math.abs(top) < SNAP_OFFSET) {
            rawWindowAnchorY -= top;
        } else if (Math.abs(mScreenSize[1] - bottom) < SNAP_OFFSET) {
            rawWindowAnchorY += mScreenSize[1] - bottom;
        }
        return rawWindowAnchorY;
    }
}
