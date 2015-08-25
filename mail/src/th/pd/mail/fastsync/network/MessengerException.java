package th.pd.mail.fastsync.network;

public class MessengerException extends Exception {

    public static final int TYPE_UNSEPCIFIED = 0;
    public static final int TYPE_SMTP = 1;

    private static final long serialVersionUID = -4820996120153268657L;

    public final int type;

    public MessengerException(String msg) {
        this(msg, TYPE_UNSEPCIFIED);
    }

    public MessengerException(String msg, int type) {
        super(msg);
        this.type = type;
    }

    public int isAuthFailed() {
        String msg = getMessage();
        if (msg == null || msg.isEmpty()) {
            return -1;
        }
        if (msg.charAt(1) == '3') {
            // TODO check: x3x or 3xx ?
            return 1;
        } else {
            return 0;
        }
    }
}
