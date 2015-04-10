package th.mock;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

public class StateListItemLayout extends FrameLayout {
    static int version = 100;
    int mVersion;

    public StateListItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVersion = version++;
    }

    /**
     * Break the default "selected" call chain.<br/>
     * It is called by parent ListView.
     */
    @Override
    public void setSelected(boolean isSelected) {
        Log.w("th", "StateListItemLayout (" + this.getId()
                + ") setSelected called with " + isSelected);
        super.setSelected(isSelected);
        return;
    }

    /**
     * pick up the "selected" call chain here
     */
    public void setStateSelected(boolean isSelected) {
        super.setSelected(isSelected);
    }

    @Override
    public int getId() {
        return mVersion;
    }
}
