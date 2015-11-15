package th.pd.mail.darkroom;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.MailServerAuth;
import th.pd.mail.fastsync.Mailbox;

public class DbHelper {

    static class SqliteDbHelper extends SQLiteOpenHelper {

        private static final int DB_VERSION = 1;

        public SqliteDbHelper(Context context, String dbName) {
            super(context, dbName, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Const.logd(TAG, "create db");
            DbHeader.Box.createTable(db);
            DbHeader.Folder.createTable(db);
            DbHeader.ServerAuth.createTable(db);
            // TODO other tables
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                int newVersion) {
            // dummy for now
        }
    }

    static final String TAG = DbHelper.class.getName();

    private static final String DB_NAME = "mail.db";
    private static SQLiteDatabase sSqliteDb;
    private static DbHelper sInstance = null;

    /**
     * opens for DAO only
     */
    public static DbHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private static SQLiteDatabase getSqliteDb(Context context) {
        if (sSqliteDb == null) {
            SqliteDbHelper helper = new SqliteDbHelper(context, DB_NAME);
            sSqliteDb = helper.getWritableDatabase();
            // TODO fix any possible inconsistent in db
        }
        return sSqliteDb;
    }

    private Context appContext;

    private DbHelper(Context appContext) {
        this.appContext = appContext;
    }

    public List<Mailbox> getMailboxes() {
        return DbHeader.Box.queryAll(getSqliteDb(appContext));
    }

    public List<MailServerAuth> getServerAuths() {
        return DbHeader.ServerAuth.queryAll(getSqliteDb(appContext));
    }

    public long insert(Mailbox mailbox) {
        return DbHeader.Box.insert(getSqliteDb(appContext), mailbox);
    }

    public long insert(MailServerAuth serverAuth) {
        return DbHeader.ServerAuth.insert(getSqliteDb(appContext),
                serverAuth);
    }

    public int remove(Mailbox mailbox) {
        return DbHeader.Box.remove(getSqliteDb(appContext), mailbox);
    }
}
