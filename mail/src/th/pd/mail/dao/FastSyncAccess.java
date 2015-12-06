package th.pd.mail.dao;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import th.pd.mail.Const;
import th.pd.mail.fastsync.SyncController;

// TODO move the sync stuff into a service
public class FastSyncAccess {

    /**
     * also a cache for MailAcc
     */
    public static class MailAccSequence {

        private static LinkedList<MailAcc> reload(Context context) {
            List<MailAcc> source = DbHelper.getInstance(context)
                    .getMailAccs();

            // filter by known account
            LinkedList<MailAcc> l = new LinkedList<MailAcc>();
            for (Account account : AccountManager.get(context)
                    .getAccountsByType(Const.ACCOUNT_TYPE)) {
                for (MailAcc acc : source) {
                    if (acc.getAddr().equals(account.name)) {
                        l.add(acc);
                        break;
                    }
                }
            }
            return l;
        }

        private LinkedList<MailAcc> collection;
        private MailAcc current = null;

        private MailAccSequence(Context context) {
            invalidate(context);
        }

        public void add(MailAcc acc) {
            if (get(acc.getAddr()) == null) {
                collection.add(acc);
                if (collection.size() == 1 || current == null) {
                    current = collection.get(0);
                }
            }
        }

        public MailAcc get(int index) {
            if (index >= 0 && index < this.collection.size()) {
                return this.collection.get(index);
            }
            return null;
        }

        public MailAcc get(String addr) {
            for (MailAcc acc : collection) {
                if (acc.getAddr().equals(addr)) {
                    return acc;
                }
            }
            return null;
        }

        public int getCount() {
            if (this.collection == null) {
                return 0;
            }
            return this.collection.size();
        }

        public MailAcc getCurrent() {
            if (current == null && !collection.isEmpty()) {
                current = collection.get(0);
            }
            return current;
        }

        private int indexOf(MailAcc acc) {
            if (this.collection != null) {
                return this.collection.indexOf(acc);
            }
            return -2;
        }

        // TODO not exact: invalidate does not mean reload
        public void invalidate(Context context) {
            collection = reload(context);
            // TODO restore the stored current mail account
            MailAcc stored = null;
            if (!setCurrent(indexOf(stored))) {
                setCurrent(0);
            }
            // TODO save current to stored
        }

        public boolean remove(String addr) {
            Iterator<MailAcc> it = collection.iterator();
            while (it.hasNext()) {
                MailAcc acc = it.next();
                if (acc.getAddr().equals(addr)) {
                    it.remove();
                    if (!setCurrent(current)) {
                        setCurrent(0);
                    }
                    // TODO save to stored
                    return true;
                }
            }
            return false;
        }

        public boolean setCurrent(int index) {
            if (index >= 0 && index < this.collection.size()) {
                current = this.collection.get(index);
                return true;
            }
            return false;
        }

        public boolean setCurrent(MailAcc acc) {
            return setCurrent(indexOf(acc));
        }
    }

    private static MailAccSequence mailAccSequence;

    public static void add(Context context, MailAcc acc) {
        if (DbHelper.getInstance(context).insert(acc) != -1) {
            getMailAccSequence(context).add(acc);
        }
    }

    public static void add(Context context, MailDir dir) {
        if (DbHelper.getInstance(context).insert(dir) != -1) {
            // TODO cache
        }
    }

    public static void add(Context context, MailServerAuth serverAuth) {
        if (DbHelper.getInstance(context).insert(serverAuth) != -1) {
            // deal with cache
        }
    }

    public static void enqueueMessage(SmtpSyncable syncMessage) {
        SyncController.getInstance().addTask(syncMessage);
    }

    public static MailDir findMailDir(Context context, int id) {
        // XXX can i query directly from db?
        for (MailDir dir : DbHelper.getInstance(context).getMailDirs()) {
            if (dir.getAutoId() == id) {
                return dir;
            }
        }
        return null;
    }

    public static List<MailDir> findMailDirs(Context context, MailAcc acc) {
        List<MailDir> l = new LinkedList<>();
        for (MailDir dir : DbHelper.getInstance(context).getMailDirs()) {
            if (dir.getAddr().equals(acc.getAddr())) {
                l.add(dir);
            }
        }
        return l;
    }

    public static MailServerAuth findServerAuth(Context context,
            String addr, String protocol) {
        for (MailServerAuth serverAuth : DbHelper.getInstance(context)
                .getServerAuths()) {
            if (serverAuth.getLogin().equals(addr)
                    && serverAuth.getProtocol().equals(protocol)) {
                return serverAuth;
            }
        }
        return null;
    }

    public static MailAccSequence getMailAccSequence(Context context) {
        if (mailAccSequence == null) {
            mailAccSequence = new MailAccSequence(context);
        }
        return mailAccSequence;
    }

    public static void remove(Context context, MailAcc acc) {
        if (DbHelper.getInstance(context).remove(acc) > 0) {
            getMailAccSequence(context).remove(acc.getAddr());
        }
    }
}
