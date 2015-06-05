package th.pd.mail.dao;

public class MessageForSend extends SyncableMessage {

	public boolean hasContent() {
		return !getMessage().getContent().isEmpty();
	}

	public boolean hasRecipient() {
		return !getMessage().getRecipient().isEmpty();
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
}
