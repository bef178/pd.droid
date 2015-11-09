package th.pd.mail.fastsync;

import java.io.IOException;

import th.pd.mail.dao.SmtpSyncable;
import th.pd.mail.dao.Syncable;
import th.pd.mail.fastsync.network.ImapWorker;
import th.pd.mail.fastsync.network.MessengerException;
import th.pd.mail.fastsync.network.SmtpWorker;

public class SyncThread extends Thread {

    private Syncable mSyncMessage = null;

    public SyncThread(String threadName) {
        super(threadName);
    }

    private void doClean() {
        mSyncMessage = null;
        SyncController.getInstance().addResult(this);
    }

    private boolean fetchTask() {
        // TODO move the message to a 'current' set
        mSyncMessage = SyncController.getInstance().getTask();
        return mSyncMessage != null;
    }

    @Override
    public void run() {
        while (true) {
            Syncable syncMessage = waitIfNoMoreTask();

            MailServerAuth serverAuth = syncMessage.getServerAuth();
            if (serverAuth.getProtocol().equals(Const.PROTOCOL_EAS)) {
                // TODO
            } else if (serverAuth.getProtocol().equals(Const.PROTOCOL_IMAP)) {
                try {
                    new ImapWorker().syncMessage(syncMessage);
                    // TODO move the message to 'post-handle' queue
                } catch (IOException | MessengerException e) {
                    // TODO network error/no-connection/...
                    // TODO throw to main thread to decide: save for later or retry or prompt in UI
                    // TODO move the message back to 'waiting' queue
                    e.printStackTrace();
                }
            } else if (serverAuth.getProtocol().equals(Const.PROTOCOL_POP3)) {
                // TODO
            } else if (syncMessage instanceof SmtpSyncable) {
                // TODO send the message and make a result
                try {
                    new SmtpWorker().sendMessage((SmtpSyncable) syncMessage);
                    // TODO move the message to 'post-handle' queue
                } catch (IOException | MessengerException e) {
                    // TODO network error/no-connection/...
                    // TODO throw to main thread to decide: save for later or retry or prompt in UI
                    e.printStackTrace();
                }
            }

            Const.logd(getName() + " task done");
            doClean();
        }
    }

    private Syncable waitIfNoMoreTask() {
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
        return mSyncMessage;
    }

    // would run in main thread
    public void wakeUp() {
        synchronized (this) {
            Const.logd(getName() + " about to wake up");
            notify();
        }
    }
}
