package th.pd.mail.dao;

import th.pd.mail.fastsync.MailServerAuth;

public class MessageForSend extends SyncableMessage {
	private MailServerAuth serverAuth;

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
	public void setMessage(Message message) {
		super.setMessage(message);
		parse();
	}

	private void parse() {
		// TODO parse message so set attributes needed for send
	}

	public MailServerAuth getServerAuth() {
		return serverAuth;
	}

	public void setServerAuth(MailServerAuth serverAuth) {
		this.serverAuth = serverAuth;
	}
}
