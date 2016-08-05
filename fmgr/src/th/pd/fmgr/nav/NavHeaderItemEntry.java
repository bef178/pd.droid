package th.pd.fmgr.nav;

import t.typedef.droid.FormatUtil;

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
