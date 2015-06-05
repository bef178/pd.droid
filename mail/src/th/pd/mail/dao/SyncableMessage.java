package th.pd.mail.dao;

/**
 * *have* a message not *be* a message
 */
abstract class SyncableMessage {

	private Message message;

	public void setMessage(Message message) {
		// TODO from/to/cc/bcc should have been corrected when being typed
		// but should not trim user-typed subject and mail body
		message.formalizeMailAddress();
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}
}
