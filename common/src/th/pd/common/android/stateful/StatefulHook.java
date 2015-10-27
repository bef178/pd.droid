package th.pd.common.android.stateful;

import th.pd.common.android.R;

/**
 * hook is flexible and achieves kind of multiple inheritance
 */
public class StatefulHook implements Stateful {

    private int[] states = new int[] {
            -R.attr.itemAsIfFocused,
            -R.attr.itemChecked,
            -R.attr.itemEnabled,
            -R.attr.itemFocused,
            -R.attr.itemHovered,
            -R.attr.itemPressed,
            -R.attr.itemVisited
    };

    public void clearStates() {
        for (int state : states) {
            if (state > 0) {
                state = -state;
            }
        }
        states[INDEX_ENABLED] = -states[INDEX_ENABLED];
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
