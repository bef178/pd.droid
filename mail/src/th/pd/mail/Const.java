package th.pd.mail;

import android.os.Build;
import android.util.Log;

/**
 * Everything that i don't know where to put collects here.
 */
public class Const {

    static enum SyncTask {
        SYNC_ACC,
        SYNC_SPECIFIC_DIR,
        SYNC_MAIL_FLAGS
    }

    // must sync with const.xml
    public static final String ACCOUNT_TYPE = "th.pd.mail";
    public static final String AUTHORITY = "th.pd.mail.authority";

    public static final String PROTOCOL_EAS = "eas";
    public static final String PROTOCOL_IMAP = "imap";
    public static final String PROTOCOL_POP3 = "pop3";
    public static final String PROTOCOL_SMTP = "smtp";

    public static final String SYNC_FLAGS = "sync-flags";
    public static final int SYNC_RUN_IN_BACKGROUND = 0x01;

    private static final boolean DEBUGGING = true;

    public static void logd(String msg) {
        String tag = null;
        String className = Const.class.getName();
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        for (int i = 0; i < stack.length; ++i) {
            StackTraceElement frame = stack[i];
            if (frame.getClassName().equals(className)) {
                tag = stack[++i].getClassName();
                break;
            }
        }

        if (tag != null) {
            int i = tag.lastIndexOf('$');
            if (i < 0) {
                i = tag.lastIndexOf('.');
            }
            if (i >= 0) {
                tag = tag.substring(++i);
            }
        }

        logd(tag, msg);
    }

    public static void logd(String tag, String msg) {
        Log.d(tag, msg);
        if (!Build.TYPE.equals("user") || DEBUGGING) {
            if (!tag.equals("th")) {
                Log.d("th", msg);
            }
        }
    }
}
