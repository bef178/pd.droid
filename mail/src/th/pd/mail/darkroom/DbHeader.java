package th.pd.mail.darkroom;

import static th.pd.mail.darkroom.DbHelper.TAG;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.MailFolder;
import th.pd.mail.fastsync.MailServerAuth;
import th.pd.mail.fastsync.Mailbox;

final class DbHeader {

    /**
     * fall through in index order
     */
    public static class Box {

        public static final String TABLE = "mailbox";
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

        private static Mailbox fromCursor(Cursor c) {
            return new Mailbox(c.getString(c.getColumnIndex(COLUMN_ADDR)),
                    c.getString(c.getColumnIndex(COLUMN_CAPTION)));
        }

        static long insert(SQLiteDatabase db, Mailbox mailbox) {
            synchronized (LOCK) {
                return db.insert(TABLE, null, toContentValues(mailbox));
            }
        }

        /**
         * @return list of [addr, caption]
         */
        static List<Mailbox> queryAll(SQLiteDatabase db) {
            Cursor c;
            synchronized (LOCK) {
                c = db.query(TABLE, null,
                        null, null, null, null, null);
            }
            List<Mailbox> l = new LinkedList<>();
            while (c.moveToNext()) {
                l.add(fromCursor(c));
            }
            c.close();
            return l;
        }

        static int remove(SQLiteDatabase db, Mailbox mailbox) {
            return db.delete(TABLE, COLUMN_ADDR + "=?", new String[] {
                    mailbox.getAddr()
            });
        }

        private static ContentValues toContentValues(Mailbox mailbox) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ADDR, mailbox.getAddr());
            cv.put(COLUMN_CAPTION, mailbox.getCaption());
            return cv;
        }
    }

    public static class Folder {

        public static final String TABLE = "mail_folder";
        public static final String COLUMN_ADDR = Box.COLUMN_ADDR;
        public static final String COLUMN_CAPTION = Box.COLUMN_CAPTION;
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

        private static MailFolder fromCursor(Cursor c) {
            MailFolder mailFolder = new MailFolder();
            mailFolder.setAutoId(c.getInt(
                    c.getColumnIndex(COLUMN_AUTO_ID)));
            mailFolder.setAddr(c.getString(
                    c.getColumnIndex(COLUMN_ADDR)));
            mailFolder.setCaption(c.getString(
                    c.getColumnIndex(COLUMN_CAPTION)));
            mailFolder.setPath(c.getString(
                    c.getColumnIndex(COLUMN_PATH)));
            mailFolder.setLastSync(c.getLong(
                    c.getColumnIndex(COLUMN_LAST_SYNC)));
            mailFolder.setSyncStatus(c.getInt(
                    c.getColumnIndex(COLUMN_SYNC_STATUS)));
            mailFolder.setFlags(c.getInt(
                    c.getColumnIndex(COLUMN_FLAGS)));
            return mailFolder;
        }

        static void insert(SQLiteDatabase db,
                MailFolder mailFolder) {
            synchronized (LOCK) {
                db.insert(TABLE, null, toContentValues(mailFolder));
            }
        }

        static List<MailFolder> queryAll(SQLiteDatabase db) {
            Cursor c;
            synchronized (LOCK) {
                c = db.query(TABLE, null,
                        null, null, null, null, COLUMN_AUTO_ID);
            }
            ArrayList<MailFolder> a = new ArrayList<>(
                    c.getColumnCount());
            while (c.moveToNext()) {
                a.add(fromCursor(c));
            }
            c.close();
            return a;
        }

        private static ContentValues toContentValues(MailFolder mailFolder) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_AUTO_ID, mailFolder.getAutoId());
            cv.put(COLUMN_ADDR, mailFolder.getAddr());
            cv.put(COLUMN_CAPTION, mailFolder.getCaption());
            cv.put(COLUMN_PATH, mailFolder.getPath());
            cv.put(COLUMN_LAST_SYNC, mailFolder.getLastSync());
            cv.put(COLUMN_SYNC_STATUS, mailFolder.getSyncStatus());
            cv.put(COLUMN_FLAGS, mailFolder.getFlags());
            return cv;
        }
    }

    public static class ServerAuth {

        public static final String TABLE = "server_auth";
        public static final String COLUMN_PROTOCOL = "protocol";
        public static final String COLUMN_HOST = "host";
        public static final String COLUMN_PORT = "port";
        public static final String COLUMN_USER = "user";
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
                    .append(COLUMN_USER).append(" TEXT,")
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
            serverAuth.setProtocol(c.getString(
                    c.getColumnIndex(COLUMN_PROTOCOL)));
            serverAuth.setHost(c.getString(
                    c.getColumnIndex(COLUMN_HOST)));
            serverAuth.setPort(c.getInt(
                    c.getColumnIndex(COLUMN_PORT)));
            serverAuth.setLogin(c.getString(
                    c.getColumnIndex(COLUMN_USER)));
            serverAuth.setPin(c.getString(
                    c.getColumnIndex(COLUMN_PASS)));
            serverAuth.setFlags(c.getInt(
                    c.getColumnIndex(COLUMN_FLAGS)));
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

        private static ContentValues toContentValues(
                MailServerAuth serverAuth) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_PROTOCOL, serverAuth.getProtocol());
            cv.put(COLUMN_HOST, serverAuth.getHost());
            cv.put(COLUMN_PORT, serverAuth.getPort());
            cv.put(COLUMN_USER, serverAuth.getLogin());
            cv.put(COLUMN_PASS, serverAuth.getPin());
            cv.put(COLUMN_FLAGS, serverAuth.getFlags());
            return cv;
        }
    }

    static final String COLUMN_AUTO_ID = "auto_id";
    private static final Object LOCK = new Object();
}
