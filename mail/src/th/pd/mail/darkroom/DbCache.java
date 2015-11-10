package th.pd.mail.darkroom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import th.pd.mail.fastsync.MailServerAuth;
import th.pd.mail.fastsync.Mailbox;

/**
 * a singleton cache that stores Mailbox/MailFolder/MailServerAuth that from db
 */
public class DbCache {

    private static DbCache instance = null;

    /**
     * do not invoke, opens for DAO only
     */
    public static DbCache getInstance(Context context) {
        if (instance == null) {
            instance = new DbCache(context.getApplicationContext());
        }
        return instance;
    }

    public static void invalidate() {
        instance = null;
    }

    private Context appContext;
    private HashMap<String, Mailbox> mailboxCache;
    private List<MailServerAuth> serverAuthCache;

    private DbCache(Context appContext) {
        this.appContext = appContext;
    }

    public Map<String, Mailbox> getMailboxes() {
        if (mailboxCache == null) {
            mailboxCache = new HashMap<>();
            for (Mailbox mailbox : DbHelper.queryMailbox(appContext)) {
                mailboxCache.put(mailbox.getAddr(), mailbox);
            }
        }
        return mailboxCache;
    }

    public List<MailServerAuth> getServerAuths() {
        if (serverAuthCache == null) {
            serverAuthCache = DbHelper.queryServerAuth(appContext);
        }
        return serverAuthCache;
    }
}
