package cc.typedef.droid.common;

import android.view.MotionEvent;
import android.view.View;

/**
 * the finger offset maps to the anchor coordinate<br/>
 * the anchor coordinate is restricted then maps to the layout offset<br/>
 */
public abstract class DragListener implements View.OnTouchListener {

    public abstract void onDrag(View view, int rawX, int rawY);

    public abstract void onDragBegin(View view, int rawX, int rawY);

    public abstract void onDragEnd(View view, int rawX, int rawY);

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int rawX = (int) event.getRawX();
        final int rawY = (int) event.getRawY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                onDragBegin(v, rawX, rawY);
                return true;
            case MotionEvent.ACTION_UP:
                onDragEnd(v, rawX, rawY);
                return true;
            case MotionEvent.ACTION_MOVE:
                onDrag(v, rawX, rawY);
                return true;
        }
        return false;
    }
}
