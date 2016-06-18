package th.pd.common.android.stateful;

import th.pd.common.android.R;

/**
 * hook is flexible and achieves kind of multiple inheritance
 */
public class StatefulHook implements Stateful {

    private int[] states = new int[] {
            -R.attr.state_disabled
            -R.attr.state_as_if_focused,
            -R.attr.state_focused,
            -R.attr.state_hovered,
            -R.attr.state_pressed,
            -R.attr.state_checked,
            -R.attr.state_visited
    };

    public void clearStates() {
        for (int state : states) {
            if (state > 0) {
                state = -state;
            }
        }
    }

    /**
     * offer a copy
     */
    public int[] getStates() {
        return this.states.clone();
    }

    @Override
    public void setState(int index, boolean isActive) {
        if (isActive ^ (states[index] >= 0)) {
            states[index] = -states[index];
        }
    }
}
