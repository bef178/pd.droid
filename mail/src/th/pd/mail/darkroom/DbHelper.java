package th.pd.mail.darkroom;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.MailServerAuth;
import th.pd.mail.fastsync.Mailbox;

class DbHelper {

    static class SqliteDbHelper extends SQLiteOpenHelper {

        private static final int DB_VERSION = 1;

        public SqliteDbHelper(Context context, String dbName) {
            super(context, dbName, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Const.logd(TAG, "create db");
            DbHeader.Box.createTable(db);
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

    private static synchronized SQLiteDatabase getSqliteDb(Context context) {
        if (sSqliteDb == null) {
            SqliteDbHelper helper = new SqliteDbHelper(context, DB_NAME);
            sSqliteDb = helper.getWritableDatabase();
            // TODO fix any possible inconsistent in db
        }
        return sSqliteDb;
    }

    public static synchronized List<Mailbox> queryMailbox(Context context) {
        return DbHeader.Box.queryAll(getSqliteDb(context));
    }

    public static synchronized List<MailServerAuth> queryServerAuth(
            Context context) {
        return DbHeader.ServerAuth.queryAll(getSqliteDb(context));
    }
}
