package th.pd.common.android.stateful;

import java.util.Arrays;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Provide api to change View state appearance without modifying the data.<br/>
 * May require the hosted view to break-and-rejoin the state/appearance call
 * chain.
 *
 * @author tanghao
 */
public class ViewStateController {
    public static final int STATE_FLAG_ENABLED = 0x01;
    public static final int STATE_FLAG_HOVERED = 0x02; // transient state
    public static final int STATE_FLAG_PRESSED = 0x04; // transient state
    public static final int STATE_FLAG_FOCUSED = 0x08; // as if it is focused

    /**
     * alias of View.PFLAG_SELECTED
     * checked as radio/checkbox
     */
    public static final int STATE_FLAG_CHECKED = 0x10;

    /**
     * alias of View.PFLAG_ACTIVATED
     * for action mode
     */
    public static final int STATE_FLAG_MARKED = 0x20;

    public static final int STATE_FLAG_VISITED = 0x0100; // same as that in css

    // and more states
    public static final int STATE_FLAG_CUSTOM_02 = 0x0200;
    public static final int STATE_FLAG_CUSTOM_04 = 0x0400;
    public static final int STATE_FLAG_CUSTOM_08 = 0x0800;
    public static final int STATE_FLAG_CUSTOM_10 = 0x1000;
    public static final int STATE_FLAG_CUSTOM_20 = 0x2000;
    public static final int STATE_FLAG_CUSTOM_40 = 0x4000;
    public static final int STATE_FLAG_CUSTOM_80 = 0x8000;

    private int mStateFlags = 0;
    private View mHosted;

    public ViewStateController(View hosted) {
        mHosted = hosted;
    }

    public void addStateFlags(int flags) {
        setStateFlags(mStateFlags | flags);
    }

    public void clearStateFlags(int flags) {
        setStateFlags(mStateFlags & ~flags);
    }

    /**
     * @return *true* if it has *all* of given flags
     */
    protected boolean hasStateFlags(int flags) {
//        assert flags != 0;
        return (mStateFlags & flags) == flags;
    }

    public void setStateFlags(int flags) {
        // do not check because the View state api may change the display state
//        if (flags != mStateFlags) {
        mStateFlags = flags;
        flushAppearance();
//        }
    }

    /**
     * do perform flush action, thus cause invalidate()
     */
    protected void flushAppearance() {
        final int SIZE = 6;
        int[] drawableState = new int[SIZE];
        int i = 0;
        if (hasStateFlags(STATE_FLAG_ENABLED)) {
            drawableState[i++] = android.R.attr.state_enabled;
        }
        if (hasStateFlags(STATE_FLAG_HOVERED)) {
            drawableState[i++] = android.R.attr.state_hovered;
        }
        if (hasStateFlags(STATE_FLAG_PRESSED)) {
            drawableState[i++] = android.R.attr.state_pressed;
        }
        if (hasStateFlags(STATE_FLAG_FOCUSED)) {
            drawableState[i++] = android.R.attr.state_focused;
        }
        if (hasStateFlags(STATE_FLAG_CHECKED)) {
            drawableState[i++] = android.R.attr.state_checked;
        }
        if (hasStateFlags(STATE_FLAG_MARKED)) {
            drawableState[i++] = android.R.attr.state_activated;
        }
        if (i < SIZE) {
            drawableState = Arrays.copyOf(drawableState, i);
        }

        Drawable bgDrawable = mHosted.getBackground();
        if (bgDrawable != null && bgDrawable.isStateful()) {
            bgDrawable.setState(drawableState);
        }
    }
}
