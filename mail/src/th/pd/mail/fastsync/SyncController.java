package th.pd.mail.fastsync;

import java.util.concurrent.LinkedBlockingQueue;

import th.pd.mail.dao.SmtpSyncable;
import th.pd.mail.dao.Syncable;

/**
 * a dispatcher to manage queues, tasks and threads<br/>
 * we don't use handler/post if want to take the control of priority and number
 * of threads
 */
public class SyncController {

    private static final int NUM_THREADS = 3;

    private static SyncController controller = null;

    public static SyncController getInstance() {
        if (controller == null) {
            controller = new SyncController();
        }
        return controller;
    }

    ////////////////////////////////////////////////////////

    // TODO 3-priority waiting queues
    // TODO work-in-progress set
    // TODO done queue
    private final LinkedBlockingQueue<Syncable> mTaskQueue =
            new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<SyncThread> mThreadPool =
            new LinkedBlockingQueue<>();

    private SyncController() {
        setupThreads(NUM_THREADS);
    }

    // would run in SyncThread
    void addResult(SyncThread syncThread) {
        // TODO parse/handle/save the result
        // TODO tell UI
    }

    // would run in main thread
    public void addTask(SmtpSyncable syncMessage) {
        // TODO if wip set has such message then return
        mTaskQueue.offer(syncMessage);
        SyncThread t = getThread();
        if (t != null) {
            t.wakeUp();
        }
    }

    // would run in SyncThread
    Syncable getTask() {
        return mTaskQueue.poll();
    }

    private SyncThread getThread() {
        return mThreadPool.poll();
    }

    // SyncThread may invoke
    void putThread(SyncThread syncThread) {
        mThreadPool.offer(syncThread);
    }

    private void setupThreads(int numThreads) {
        for (int i = 0; i < numThreads; ++i) {
            SyncThread syncThread = new SyncThread("syncd-#" + i);
            syncThread.start();
        }
    }
}
