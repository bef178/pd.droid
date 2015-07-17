package th.pd.fmgr;

/**
 * hook is flexible and achieves kind of multiple inheritance
 *
 * @author tanghao
 */
public class StatefulHook implements Stateful {

    private int[] states = new int[] {
            -R.attr.itemAsIfFocused,
            -R.attr.itemChecked,
            -R.attr.itemDisabled,
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
    }

    /**
     * offer a copy
     */
    public int[] getStates() {
        return this.states.clone();
    }

    @Override
    public void setState(int index, boolean active) {
        if (active ^ (states[index] >= 0)) {
            states[index] = -states[index];
        }
    }
}
