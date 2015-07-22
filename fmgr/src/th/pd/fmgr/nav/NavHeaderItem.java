package th.pd.fmgr.nav;

import th.pd.common.android.FormatUtil;

final class NavHeaderItem extends NavItem {

    public NavHeaderItem(CharSequence caption) {
        caption = FormatUtil.toSmallcaps(caption);
        setCaption(caption);
    }
}
