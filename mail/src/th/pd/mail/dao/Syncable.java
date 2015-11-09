package th.pd.mail.dao;

import th.pd.mail.fastsync.MailServerAuth;

/**
 * *have* a message not *be* a message
 */
public abstract class Syncable {

    private Message message;
    protected MailServerAuth serverAuth;

    @Override
    public abstract boolean equals(Object o);

    public Message getMessage() {
        return message;
    }

    public MailServerAuth getServerAuth() {
        return serverAuth;
    }

    public void setMessage(Message message) {
        // TODO from/to/cc/bcc should have been corrected when being typed
        // and should not trim user-typed subject and mail body
        message.formalizeMailAddress();
        this.message = message;
    }

    public abstract void setServerAuth(MailServerAuth serverAuth);
}
