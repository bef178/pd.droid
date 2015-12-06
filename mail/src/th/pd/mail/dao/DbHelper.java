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

    public static List<MailAcc> getMailAccs(Context context) {
        return DbHeader.Acc.queryAll(getSqliteDb(context));
    }

    public static List<MailDir> getMailDirs(Context context) {
        return DbHeader.Dir.queryAll(getSqliteDb(context));
    }

    public static List<MailServerAuth> getServerAuths(Context context) {
        return DbHeader.ServerAuth.queryAll(getSqliteDb(context));
    }

    private static SQLiteDatabase getSqliteDb(Context context) {
        if (sSqliteDb == null) {
            SqliteDbHelper helper = new SqliteDbHelper(
                    context.getApplicationContext(), DB_NAME);
            sSqliteDb = helper.getWritableDatabase();
            // TODO fix any possible inconsistent in db
        }
        return sSqliteDb;
    }

    public static long insert(Context context, MailAcc acc) {
        return DbHeader.Acc.insert(getSqliteDb(context), acc);
    }

    public static long insert(Context context, MailDir dir) {
        return DbHeader.Dir.insert(getSqliteDb(context), dir);
    }

    public static long insert(Context context, MailServerAuth auth) {
        return DbHeader.ServerAuth.insert(getSqliteDb(context), auth);
    }

    public static int remove(Context context, MailAcc acc) {
        return DbHeader.Acc.remove(getSqliteDb(context), acc);
    }
}
