package th.pd.common.android.stateful;

import th.pd.common.android.R;

public interface Stateful {

    public static final int INDEX_AS_IF_FOCUSED = R.styleable.Stateful_itemAsIfFocused;
    public static final int INDEX_CHECKED = R.styleable.Stateful_itemChecked;
    public static final int INDEX_ENABLED = R.styleable.Stateful_itemEnabled;
    public static final int INDEX_FOCUSED = R.styleable.Stateful_itemFocused;
    public static final int INDEX_HOVERED = R.styleable.Stateful_itemHovered;
    public static final int INDEX_PRESSED = R.styleable.Stateful_itemPressed;
    public static final int INDEX_VISITED = R.styleable.Stateful_itemVisited;

    /**
     * primary/secondary/default is a kind of buttons, not a state
     */
    static final int STATE_INDEX_PRIMARY = 99;

    public void setState(int index, boolean active);
}