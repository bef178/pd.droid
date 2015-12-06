package th.pd.mail.tidyface.leftmost;

import android.content.Context;

import th.pd.mail.dao.FastSyncAccess;
import th.pd.mail.fastsync.Mailbox;

public class LeftAccounts extends LeftItem {

    private FastSyncAccess.MailboxSequence mAccMgr;

    public LeftAccounts(Context context) {
        super(null);
        mAccMgr = FastSyncAccess.getMailboxSequence(context);
    }

    public int getCount() {
        return mAccMgr.getCount();
    }

    @Override
    public String getCaption() {
        throw new UnsupportedOperationException("not applicable");
    }

    public Mailbox getCurrent() {
        return mAccMgr.getCurrent();
    }
}
