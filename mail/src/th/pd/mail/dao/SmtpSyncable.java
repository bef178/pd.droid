package th.pd.mail.dao;

import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.MailServerAuth;

public class SmtpSyncable extends Syncable {

    @Override
    public boolean equals(Object o) {
        if (o instanceof SmtpSyncable) {
            // TODO
        }
        return false;
    }

    public boolean hasContent() {
        return !getMessage().getContent().isEmpty();
    }

    public boolean hasRecipient() {
        return !getMessage().getRecipient().isEmpty();
    }

    public boolean hasSender() {
        return !getMessage().getSender().isEmpty();
    }

    public boolean hasSubject() {
        return !getMessage().getSubject().isEmpty();
    }

    @Override
    public void setServerAuth(MailServerAuth serverAuth) {
        if (Const.PROTOCOL_SMTP.equals(serverAuth.getProtocol())) {
            this.serverAuth = serverAuth;
            return;
        }
        throw new IllegalArgumentException("expect smtp server auth");
    }
}
