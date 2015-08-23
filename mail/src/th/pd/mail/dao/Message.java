package th.pd.mail.dao;

import java.io.File;
import java.util.LinkedList;

import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;

/**
 * basic mail message containing all info that human normally cares
 */
public class Message {

    public static class Attachment {

        /**
         * file to attach
         */
        public File file = null;

        public String mimeType = null;
    }

    private static String formalizeMailAddress(String addr) {
        Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(addr);
        if (tokens.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(tokens[0].toString());
        for (int i = 1; i < tokens.length; ++i) {
            sb.append("; ").append(tokens[i]);
        }
        return sb.toString();
    }

    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String content;

    public final LinkedList<Attachment> attachments = new LinkedList<>();

    public void formalizeMailAddress() {
        to = formalizeMailAddress(to);
        cc = formalizeMailAddress(cc);
        bcc = formalizeMailAddress(bcc);
    }

    public String getBlindCarbonCopy() {
        return bcc;
    }

    public String getCarbonCopy() {
        return cc;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public String getRecipient() {
        return to;
    }

    public String getSender() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public void setBlindCarbonCopy(String blindCarbonCopy) {
        this.bcc = blindCarbonCopy;
    }

    public void setCarbonCopy(String carbonCopy) {
        this.cc = carbonCopy;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRecipient(String recipient) {
        this.to = recipient;
    }

    public void setSender(String from) {
        this.from = from;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
