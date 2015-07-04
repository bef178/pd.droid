package th.pd.mail.fastsync;

import th.pd.mail.dao.MessageForSend;
import th.pd.mail.dao.SyncableMessage;
import th.pd.mail.fastsync.network.AuthFailedException;
import th.pd.mail.fastsync.network.MessengerException;
import th.pd.mail.fastsync.network.SmtpWorker;

public class SyncThread extends Thread {

	private SyncableMessage mSyncMessage = null;

	public SyncThread(String threadName) {
		super(threadName);
	}

	private void doClean() {
		mSyncMessage = null;
		SyncController.getInstance().addResult(this);
	}

	private void doSend(MessageForSend syncMessage) {
		// TODO send the message and make a result
		MailServerAuth serverAuth = syncMessage.getServerAuth();
		if (serverAuth.getProtocol().equals("smtp")) {
			try {
				new SmtpWorker().sendMessage(syncMessage);
			} catch (MessengerException | AuthFailedException e) {
				// TODO
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			waitIfNoTask();

			if (mSyncMessage instanceof MessageForSend) {
				doSend((MessageForSend) mSyncMessage);
			}

			Const.logd(getName() + " task done");
			doClean();
		}
	}

	// would run in main thread
	public void wakeUp() {
		synchronized (this) {
			Const.logd(getName() + " about to wake up");
			notify();
		}
	}

	private boolean fetchTask() {
		mSyncMessage = SyncController.getInstance().getTask();
		return mSyncMessage != null;
	}

	private void waitIfNoTask() {
		try {
			while (!fetchTask()) {
				synchronized (this) {
					Const.logd(getName() + " about to wait");
					// this lock makes enqueue() and wait() atomic
					SyncController.getInstance().putThread(this);
					wait();
				}
			}
		} catch (InterruptedException e) {
			Const.logd(getName() + " interrupted");
		}
	}
}
