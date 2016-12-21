package th.pd.fmgr.nav;

import cc.typedef.droid.common.FormatUtil;

final class NavHeaderItemEntry extends NavItemEntry {

    public NavHeaderItemEntry by(CharSequence caption) {
        setCaption(caption);
        return this;
    }

    @Override
    public CharSequence getCaption() {
        return FormatUtil.toSmallcaps(super.getCaption());
    }
}
