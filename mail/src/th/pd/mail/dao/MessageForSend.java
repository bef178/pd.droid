package th.pd.mail.dao;

import th.pd.mail.fastsync.MailServerAuth;

public class MessageForSend extends SyncableMessage {

	private MailServerAuth mServerAuth;

	public boolean hasContent() {
		return !getMessage().getContent().isEmpty();
	}

	public boolean hasRecipient() {
		return !getMessage().getRecipient().isEmpty();
	}

	public boolean hasSubject() {
		return !getMessage().getSubject().isEmpty();
	}

	public void setServerAuth(MailServerAuth mailServerAuth) {
		mServerAuth = mailServerAuth;
	}
}
