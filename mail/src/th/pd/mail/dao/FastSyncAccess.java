package th.pd.mail.dao;

import th.pd.mail.fastsync.MailObjectCache;
import th.pd.mail.fastsync.SyncController;

// TODO move the sync stuff into a service
public class FastSyncAccess {

	public static void addMessageForSend(MessageForSend syncMessage) {
		// TODO find send queue and enqueue
		SyncController.getInstance().addTask(syncMessage);
	}

	public static void awakeWorkerIfNecessary() {
		// TODO
	}

	public static String getCurrentUser() {
		// TODO
		return MailObjectCache.getInstance().getMailServerAuth(1, null).getLogin();
	}
}
