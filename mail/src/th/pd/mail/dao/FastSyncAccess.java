package th.pd.mail.dao;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import th.pd.mail.darkroom.DbHelper;
import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.Mailbox;
import th.pd.mail.fastsync.MailFolder;
import th.pd.mail.fastsync.MailServerAuth;
import th.pd.mail.fastsync.SyncController;

// TODO move the sync stuff into a service
public class FastSyncAccess {

    /**
     * also a cache for Mailbox
     */
    public static class MailboxSequence {

        private static LinkedList<Mailbox> reload(Context context) {
            List<Mailbox> source = DbHelper.getInstance(context)
                    .getMailboxes();

            // filter by known account
            LinkedList<Mailbox> l = new LinkedList<Mailbox>();
            for (Account acc : AccountManager.get(context)
                    .getAccountsByType(Const.ACCOUNT_TYPE)) {
                for (Mailbox mailbox : source) {
                    if (mailbox.getAddr().equals(acc.name)) {
                        l.add(mailbox);
                        break;
                    }
                }
            }
            return l;
        }

        private LinkedList<Mailbox> collection;
        private Mailbox current = null;

        private MailboxSequence(Context context) {
            invalidate(context);
        }

        public void add(Mailbox mailbox) {
            if (get(mailbox.getAddr()) == null) {
                collection.add(mailbox);
                if (collection.size() == 1 || current == null) {
                    current = collection.get(0);
                }
            }
        }

        public Mailbox get(int index) {
            if (index >= 0 && index < this.collection.size()) {
                return this.collection.get(index);
            }
            return null;
        }

        public Mailbox get(String addr) {
            for (Mailbox mailbox : collection) {
                if (mailbox.getAddr().equals(addr)) {
                    return mailbox;
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

        public Mailbox getCurrent() {
            if (current == null && !collection.isEmpty()) {
                current = collection.get(0);
            }
            return current;
        }

        private int indexOf(Mailbox mailbox) {
            if (this.collection != null) {
                return this.collection.indexOf(mailbox);
            }
            return -2;
        }

        // TODO not exact: invalidate does not mean reload
        public void invalidate(Context context) {
            collection = reload(context);
            // TODO restore the stored current mailbox
            Mailbox stored = null;
            if (!setCurrent(indexOf(stored))) {
                setCurrent(0);
            }
            // TODO save current to stored
        }

        public boolean remove(String addr) {
            Iterator<Mailbox> it = collection.iterator();
            while (it.hasNext()) {
                Mailbox mailbox = it.next();
                if (mailbox.getAddr().equals(addr)) {
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

        public boolean setCurrent(Mailbox mailbox) {
            return setCurrent(indexOf(mailbox));
        }
    }

    private static MailboxSequence mailboxSequence;

    public static void add(Context context, Mailbox mailbox) {
        if (DbHelper.getInstance(context).insert(mailbox) != -1) {
            getMailboxSequence(context).add(mailbox);
        }
    }

    public static void add(Context context, MailFolder mailFolder) {
        if (DbHelper.getInstance(context).insert(mailFolder) != -1) {
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

    public static MailFolder findMailFolder(Context context, int id) {
        // XXX can i query directly from db?
        for (MailFolder mailFolder : DbHelper.getInstance(context)
                .getMailFolders()) {
            if (mailFolder.getAutoId() == id) {
                return mailFolder;
            }
        }
        return null;
    }

    public static List<MailFolder> findMailFolders(Context context,
            Mailbox mailbox) {
        List<MailFolder> l = new LinkedList<>();
        for (MailFolder mailFolder : DbHelper.getInstance(context)
                .getMailFolders()) {
            if (mailFolder.getAddr().equals(mailbox.getAddr())) {
                l.add(mailFolder);
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

    public static MailboxSequence getMailboxSequence(Context context) {
        if (mailboxSequence == null) {
            mailboxSequence = new MailboxSequence(context);
        }
        return mailboxSequence;
    }

    public static void remove(Context context, Mailbox mailbox) {
        if (DbHelper.getInstance(context).remove(mailbox) > 0) {
            getMailboxSequence(context).remove(mailbox.getAddr());
        }
    }
}
