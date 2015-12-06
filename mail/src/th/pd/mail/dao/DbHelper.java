package th.pd.mail.dao;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import th.pd.mail.Const;

final class DbHelper {

    static class SqliteDbHelper extends SQLiteOpenHelper {

        private static final int DB_VERSION = 1;

        public SqliteDbHelper(Context context, String dbName) {
            super(context, dbName, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Const.logd(TAG, "create db");
            DbHeader.Acc.createTable(db);
            DbHeader.Dir.createTable(db);
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

    public List<MailAcc> getMailAccs() {
        return DbHeader.Acc.queryAll(getSqliteDb(appContext));
    }

    public List<MailDir> getMailDirs() {
        return DbHeader.Dir.queryAll(getSqliteDb(appContext));
    }

    public List<MailServerAuth> getServerAuths() {
        return DbHeader.ServerAuth.queryAll(getSqliteDb(appContext));
    }

    public long insert(MailAcc acc) {
        return DbHeader.Acc.insert(getSqliteDb(appContext), acc);
    }

    public long insert(MailDir dir) {
        return DbHeader.Dir.insert(getSqliteDb(appContext), dir);
    }

    public long insert(MailServerAuth auth) {
        return DbHeader.ServerAuth.insert(getSqliteDb(appContext), auth);
    }

    public int remove(MailAcc acc) {
        return DbHeader.Acc.remove(getSqliteDb(appContext), acc);
    }
}
