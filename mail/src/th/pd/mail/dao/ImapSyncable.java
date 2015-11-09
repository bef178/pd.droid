package th.pd.mail.dao;

import th.pd.mail.fastsync.MailServerAuth;

/**
 * for IMAP, it cares command rather than the message itself<br/>
 */
public class ImapSyncable extends Syncable {

    // generic sync
    public static final int REQUEST_SYNC = 0;

    // takes 0.message 1.path
    public static final int REQUEST_CREATE_MESSAGE = 1;

    // takes 1.path
    public static final int REQUEST_CREATE_FOLDER = 2;

    // takes 0.messageId 1.targetPath
    public static final int REQUEST_MOVE_MESSAGE = 3;

    // takes 1.path
    public static final int REQUEST_SYNC_FOLDER = 4;

    public int request = -1;

    public String path = "";

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ImapSyncable) {
            // TODO
        }
        return false;
    }

    @Override
    public void setServerAuth(MailServerAuth serverAuth) {
        // TODO Auto-generated method stub

    }
}
