package th.pd.mail.dao;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import th.pd.mail.darkroom.DbCache;
import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.MailFolder;
import th.pd.mail.fastsync.MailServerAuth;
import th.pd.mail.fastsync.Mailbox;
import th.pd.mail.fastsync.SyncController;

// TODO move the sync stuff into a service
public class FastSyncAccess {

    public static class MailboxSequence {

        private static LinkedList<Mailbox> reload(Context context) {
            Map<String, Mailbox> m = DbCache.getInstance(context)
                    .getMailboxes();

            // filter by known account
            LinkedList<Mailbox> l = new LinkedList<Mailbox>();
            for (Account acc : AccountManager.get(context)
                    .getAccountsByType(Const.ACCOUNT_TYPE)) {
                Mailbox mailbox = m.get(acc.name);
                if (mailbox != null) {
                    l.add(mailbox);
                }
                l.add(mailbox);
            }
            return l;
        }

        private LinkedList<Mailbox> mailboxCache;
        private Mailbox current = null;

        private MailboxSequence(Context context) {
            mailboxCache = reload(context);
            // TODO restore the stored current mailbox
            Mailbox stored = null;
            if (setCurrent(indexOf(stored))) {
                setCurrent(0);
            }
            // TODO save current
        }

        public Mailbox get(int index) {
            if (index >= 0 && index < this.mailboxCache.size()) {
                return this.mailboxCache.get(index);
            }
            return null;
        }

        public Mailbox get(String addr) {
            for (Mailbox mailbox : mailboxCache) {
                if (mailbox.getAddr().equals(addr)) {
                    return mailbox;
                }
            }
            return null;
        }

        public int getCount() {
            if (this.mailboxCache == null) {
                return 0;
            }
            return this.mailboxCache.size();
        }

        public Mailbox getCurrent() {
            if (current == null && !mailboxCache.isEmpty()) {
                current = mailboxCache.get(0);
            }
            return current;
        }

        private int indexOf(Mailbox mailbox) {
            if (this.mailboxCache != null) {
                return this.mailboxCache.indexOf(mailbox);
            }
            return -2;
        }

        public void invalidate(Context context) {
            mailboxCache = reload(context);
        }

        public boolean setCurrent(int index) {
            if (index >= 0 && index < this.mailboxCache.size()) {
                current = this.mailboxCache.get(index);
                return true;
            }
            return false;
        }

        public boolean setCurrent(Mailbox mailbox) {
            return setCurrent(indexOf(mailbox));
        }
    }

    private static MailboxSequence mailboxSequence;

    public static void addMessage(SmtpSyncable syncMessage) {
        SyncController.getInstance().addTask(syncMessage);
    }

    public static Mailbox findCurrentMailbox(Context context) {
        return getMailboxSequence(context).getCurrent();
    }

    public static Mailbox findMailbox(Context context, String addr) {
        return getMailboxSequence(context).get(addr);
    }

    public static MailServerAuth findServerAuth(Context context,
            String addr, String protocol) {
        for (MailServerAuth serverAuth : DbCache.getInstance(context)
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

    public static MailFolder getMailFolder(Context context, int id) {
        //        ContentProviderClient client = context.getContentResolver()
        //                .acquireContentProviderClient(
        //                        Uri.parse(MailFolder.CONTENT_URI));
        // TODO
        return null;
    }

    public static List<MailFolder> getMailFolders(Context context,
            Mailbox mailbox) {
        LinkedList<MailFolder> l = new LinkedList<>();
        // TODO
        return l;
    }

    public static MailServerAuth getServerAuth(Context context,
            String addr, String protocol) {
        for (MailServerAuth serverAuth : DbCache.getInstance(context)
                .getServerAuths()) {
            if (serverAuth.getLogin().equals(addr)
                    && serverAuth.getProtocol().equals(protocol)) {
                return serverAuth;
            }
        }
        return null;
    }
}
