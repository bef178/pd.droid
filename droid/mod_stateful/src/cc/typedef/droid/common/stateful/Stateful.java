package cc.typedef.droid.common.stateful;

import cc.typedef.droid.common.R;

public interface Stateful {

    public static final int INDEX_DISABLED = R.styleable.stateful_state_disabled;
    public static final int INDEX_AS_IF_FOCUSED = R.styleable.stateful_state_as_if_focused;
    public static final int INDEX_FOCUSED = R.styleable.stateful_state_focused;
    public static final int INDEX_HOVERED = R.styleable.stateful_state_hovered;
    public static final int INDEX_PRESSED = R.styleable.stateful_state_pressed;
    public static final int INDEX_CHECKED = R.styleable.stateful_state_checked;
    public static final int INDEX_VISITED = R.styleable.stateful_state_visited;

    /**
     * primary/secondary/default each is a kind of buttons, not a state
     */
    static final int STATE_INDEX_PRIMARY = 99;

    public void setState(int index, boolean active);
}
