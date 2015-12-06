package th.pd.mail.dao;

import static th.pd.mail.dao.DbHelper.TAG;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import th.pd.mail.Const;

final class DbHeader {

    /**
     * fall through in index order
     */
    static class Acc {

        public static final String TABLE = "mail_acc";
        public static final String COLUMN_ADDR = "addr";
        public static final String COLUMN_CAPTION = "caption";

        static void createTable(SQLiteDatabase db) {
            String sql = new StringBuilder()
                    .append("CREATE TABLE ").append(TABLE)
                    .append(" (")
                    .append(COLUMN_ADDR).append(" TEXT PRIMARY KEY,")
                    .append(COLUMN_CAPTION).append(" TEXT")
                    .append(");")
                    .toString();
            Const.logd(TAG, "db run: " + sql);
            synchronized (LOCK) {
                db.execSQL(sql);
            }
        }

        private static MailAcc fromCursor(Cursor c) {
            return new MailAcc(getCursorString(c, COLUMN_ADDR),
                    getCursorString(c, COLUMN_CAPTION));
        }

        static long insert(SQLiteDatabase db, MailAcc acc) {
            synchronized (LOCK) {
                return db.insert(TABLE, null, toContentValues(acc));
            }
        }

        /**
         * @return list of [addr, caption]
         */
        static List<MailAcc> queryAll(SQLiteDatabase db) {
            Cursor c;
            synchronized (LOCK) {
                c = db.query(TABLE, null,
                        null, null, null, null, null);
            }
            List<MailAcc> l = new LinkedList<>();
            while (c.moveToNext()) {
                l.add(fromCursor(c));
            }
            c.close();
            return l;
        }

        static int remove(SQLiteDatabase db, MailAcc acc) {
            return db.delete(TABLE, COLUMN_ADDR + "=?", new String[] {
                    acc.getAddr()
            });
        }

        private static ContentValues toContentValues(MailAcc acc) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ADDR, acc.getAddr());
            cv.put(COLUMN_CAPTION, acc.getCaption());
            return cv;
        }
    }

    static class Dir {

        public static final String TABLE = "mail_dir";
        public static final String COLUMN_ADDR = Acc.COLUMN_ADDR;
        public static final String COLUMN_CAPTION = "caption";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_LAST_SYNC = "last_sync";
        public static final String COLUMN_SYNC_STATUS = "sync_status";
        public static final String COLUMN_FLAGS = "flags";

        static void createTable(SQLiteDatabase db) {
            String sql = new StringBuilder()
                    .append("CREATE TABLE ").append(TABLE)
                    .append(" (")
                    .append(COLUMN_AUTO_ID)
                    .append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append(COLUMN_ADDR).append(" TEXT,")
                    .append(COLUMN_CAPTION).append(" TXET,")
                    .append(COLUMN_PATH).append(" INTEGER,")
                    .append(COLUMN_LAST_SYNC).append(" LONG,")
                    .append(COLUMN_SYNC_STATUS).append(" INTEGER,")
                    .append(COLUMN_FLAGS).append(" INTEGER")
                    .append(");")
                    .toString();
            Const.logd(TAG, "db run: " + sql);
            synchronized (LOCK) {
                db.execSQL(sql);
            }
        }

        private static MailDir fromCursor(Cursor c) {
            MailDir dir = new MailDir();
            dir.setAutoId(
                    getCursorInt(c, COLUMN_AUTO_ID));
            dir.setAddr(
                    getCursorString(c, COLUMN_ADDR));
            dir.setCaption(
                    getCursorString(c, COLUMN_CAPTION));
            dir.setPath(
                    getCursorString(c, COLUMN_PATH));
            dir.setLastSync(
                    getCursorLong(c, COLUMN_LAST_SYNC));
            dir.setSyncStatus(
                    getCursorInt(c, COLUMN_SYNC_STATUS));
            dir.setFlags(
                    getCursorInt(c, COLUMN_FLAGS));
            return dir;
        }

        static long insert(SQLiteDatabase db, MailDir dir) {
            synchronized (LOCK) {
                return db.insert(TABLE, null, toContentValues(dir));
            }
        }

        static List<MailDir> queryAll(SQLiteDatabase db) {
            Cursor c;
            synchronized (LOCK) {
                c = db.query(TABLE, null,
                        null, null, null, null, COLUMN_AUTO_ID);
            }
            ArrayList<MailDir> a = new ArrayList<>(
                    c.getColumnCount());
            while (c.moveToNext()) {
                a.add(fromCursor(c));
            }
            c.close();
            return a;
        }

        private static ContentValues toContentValues(MailDir dir) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_AUTO_ID, dir.getAutoId());
            cv.put(COLUMN_ADDR, dir.getAddr());
            cv.put(COLUMN_CAPTION, dir.getCaption());
            cv.put(COLUMN_PATH, dir.getPath());
            cv.put(COLUMN_LAST_SYNC, dir.getLastSync());
            cv.put(COLUMN_SYNC_STATUS, dir.getSyncStatus());
            cv.put(COLUMN_FLAGS, dir.getFlags());
            return cv;
        }
    }

    static class Ent {
        // TODO
    }

    static class ServerAuth {

        public static final String TABLE = "server_auth";
        public static final String COLUMN_PROTOCOL = "protocol";
        public static final String COLUMN_HOST = "host";
        public static final String COLUMN_PORT = "port";
        public static final String COLUMN_ADDR = Acc.COLUMN_ADDR;
        public static final String COLUMN_PASS = "pass";
        public static final String COLUMN_FLAGS = "flags";

        static void createTable(SQLiteDatabase db) {
            String sql = new StringBuilder()
                    .append("CREATE TABLE ").append(TABLE)
                    .append(" (")
                    .append(COLUMN_AUTO_ID)
                    .append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append(COLUMN_PROTOCOL).append(" TEXT,")
                    .append(COLUMN_HOST).append(" TXET,")
                    .append(COLUMN_PORT).append(" INTEGER,")
                    .append(COLUMN_ADDR).append(" TEXT,")
                    .append(COLUMN_PASS).append(" TEXT,")
                    .append(COLUMN_FLAGS).append(" INTEGER")
                    .append(");")
                    .toString();
            Const.logd(TAG, "db run: " + sql);
            synchronized (LOCK) {
                db.execSQL(sql);
            }
        }

        private static MailServerAuth fromCursor(Cursor c) {
            MailServerAuth serverAuth = new MailServerAuth();
            serverAuth.setProtocol(
                    getCursorString(c, COLUMN_PROTOCOL));
            serverAuth.setHost(
                    getCursorString(c, COLUMN_HOST));
            serverAuth.setPort(
                    getCursorInt(c, COLUMN_PORT));
            serverAuth.setLogin(
                    getCursorString(c, COLUMN_ADDR));
            serverAuth.setPin(
                    getCursorString(c, COLUMN_PASS));
            serverAuth.setFlags(
                    getCursorInt(c, COLUMN_FLAGS));
            return serverAuth;
        }

        static long insert(SQLiteDatabase db,
                MailServerAuth serverAuth) {
            synchronized (LOCK) {
                return db.insert(TABLE, null, toContentValues(serverAuth));
            }
        }

        static List<MailServerAuth> queryAll(SQLiteDatabase db) {
            Cursor c;
            synchronized (LOCK) {
                c = db.query(TABLE, null,
                        null, null, null, null, COLUMN_AUTO_ID);
            }
            ArrayList<MailServerAuth> a = new ArrayList<>(
                    c.getColumnCount());
            while (c.moveToNext()) {
                a.add(fromCursor(c));
            }
            c.close();
            return a;
        }

        private static ContentValues toContentValues(MailServerAuth auth) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_PROTOCOL, auth.getProtocol());
            cv.put(COLUMN_HOST, auth.getHost());
            cv.put(COLUMN_PORT, auth.getPort());
            cv.put(COLUMN_ADDR, auth.getLogin());
            cv.put(COLUMN_PASS, auth.getPin());
            cv.put(COLUMN_FLAGS, auth.getFlags());
            return cv;
        }
    }

    static final String COLUMN_AUTO_ID = "auto_id";

    private static final Object LOCK = new Object();

    public static int getCursorInt(Cursor c, String columnName) {
        return c.getInt(c.getColumnIndex(columnName));
    }

    public static String getCursorString(Cursor c, String columnName) {
        return c.getString(c.getColumnIndex(columnName));
    }

    public static long getCursorLong(Cursor c, String columnName) {
        return c.getLong(c.getColumnIndex(columnName));
    }
}
