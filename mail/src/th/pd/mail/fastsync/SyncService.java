package th.pd.mail.fastsync;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;

import th.pd.mail.dao.FastSyncAccess;

/**
 * main entrance of the sync service
 */
public class SyncService extends Service {

    private static class ThreadedSyncAdapter extends
            AbstractThreadedSyncAdapter {

        public ThreadedSyncAdapter(Context context) {
            super(context, true);
        }

        @Override
        public void onPerformSync(Account account, Bundle extras,
                String authority, ContentProviderClient client,
                SyncResult syncResult) {
            Const.logd("performSync--begin--");

            Mailbox mailbox = FastSyncAccess.getMailboxSequence(
                    getContext()).get(account.name);
            if (mailbox == null) {
                Const.logd("performSync--end---- with null mailbox");
                return;
            }

            // TODO sync mailbox

            // TODO multi-thread and extract extras
            for (MailFolder mailFolder : FastSyncAccess.findMailFolders(
                    getContext(), mailbox)) {
                syncFolder(getContext(), mailFolder, extras, syncResult);
            }
            Const.logd("performSync--end---- done");
        }
    }

    private static ThreadedSyncAdapter sSyncAdapter;
    private static Object sSyncAdapterLock = new Object();

    private static void syncFolder(Context context, MailFolder mailFolder,
            Bundle extras, SyncResult syncResult) {
        if (mailFolder == null) {
            return;
        }

        // TODO should not sync local only mail folder

        // what to sync depends on the extras param

        // TODO to inform it's syncing
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new ThreadedSyncAdapter(
                        getApplicationContext());
            }
        }
    }
}
