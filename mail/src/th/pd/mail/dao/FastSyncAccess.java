package th.pd.mail.dao;

import th.pd.mail.fastsync.MailObjectCache;
import th.pd.mail.fastsync.MailServerAuth;

public class FastSyncAccess {

	public static void addMessageForSend(MessageForSend syncMessage) {
		// TODO find send queue and enqueue
	}

	public static void awakeWorkerIfNecessary() {
		// TODO
	}

	public static MailServerAuth getCurrentServerAuthForSend() {
		return MailObjectCache.getInstance().getMailServerAuth(0, null);
	}
}
