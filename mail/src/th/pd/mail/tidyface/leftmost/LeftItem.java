package th.pd.mail.tidyface.leftmost;

import th.pd.common.android.pinnable.Pinnable;

public class LeftItem implements Pinnable {

    private String caption = null;
    protected boolean isPinnable = false;

    public LeftItem(String s) {
        this(s, false);
    }

    public LeftItem(String s, boolean isPinnable) {
        this.caption = s;
        this.isPinnable = isPinnable;
    }

    public String getCaption() {
        return caption;
    }

    @Override
    public boolean isPinnable() {
        return isPinnable;
    }
}
