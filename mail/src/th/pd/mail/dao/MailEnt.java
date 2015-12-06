package th.pd.mail.dao;

/**
 * message object from/to db; i.e. a mail itself
 */
public class MailEnt {

    private int autoId;
    private String messageId; // from server
    private String mailAddr;
    private String mailPath;

    private int lastSync; // timestamp in milliseconds

    private String caption; // sender/receiver display name, depends on its mailAcc
    private String subject;
    private String[] from;
    private String[] to;
    private String[] cc;
    private String[] bcc;
    private String[] replyTo;
    private String summary; // very limit stripped content

    private String attachmentId;

    private int remoteId;
    private int remoteSync;

    private int flags; // visible; loaded; read; marked;
}
