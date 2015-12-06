package th.pd.mail.tidyface.leftmost;

import android.content.Context;

import th.pd.mail.dao.FastSyncAccess;
import th.pd.mail.dao.MailAcc;

public class LeftAccounts extends LeftItem {

    private FastSyncAccess.MailAccSequence mAccMgr;

    public LeftAccounts(Context context) {
        super(null);
        mAccMgr = FastSyncAccess.getMailAccSequence(context);
    }

    public int getCount() {
        return mAccMgr.getCount();
    }

    @Override
    public String getCaption() {
        throw new UnsupportedOperationException("not applicable");
    }

    public MailAcc getCurrent() {
        return mAccMgr.getCurrent();
    }
}
