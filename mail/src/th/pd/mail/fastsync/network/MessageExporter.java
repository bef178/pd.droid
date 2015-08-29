package th.pd.mail.fastsync.network;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.SystemClock;
import android.util.Base64;
import android.util.Base64OutputStream;

import th.pd.common.android.FormatUtil;
import th.pd.mail.MailApp;
import th.pd.mail.dao.Message;
import th.pd.mail.dao.Message.Attachment;

class MessageExporter {

    /**
     * main text also forms Part
     */
    private static class Part extends Message.Attachment {

        private Part mixed;

        private Part alternative;

        private String text;

        public Part() {
            // dummy
        }

        public Part(Attachment attachment) {
            this.file = attachment.file;
            this.mimeType = attachment.mimeType;
        }

        private String getNameOfAttachment() {
            if (isCalendarEvent()) {
                return "meeting.ics";
            } else if (!isMainText()) {
                return file.getName();
            }
            return null;
        }

        private long getSizeOfAttachment() {
            if (!isMainText() && !isCalendarEvent() && file != null) {
                return file.length();
            }
            return -1;
        }

        public boolean isCalendarEvent() {
            if (!"text/calendar".equals(mimeType)) {
                return false;
            }
            if (file != null) {
                return "meeting.ics".equals(text);
            } else {
                return text != null && !text.isEmpty();
            }
        }

        /**
         * part is either main text or attachment
         */
        public boolean isMainText() {
            return file == null
                    && ("text/plain".equals(mimeType) || "text/html"
                            .equals(mimeType));
        }

    }

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    // rfc 5322 2.1.1
    private static final int MAX_LINE_LENGTH = 78;

    private static Part formMessagePart(Message message)
            throws UnsupportedEncodingException {

        // message text
        Part head = new Part();
        // TODO text/plain or text/html ?
        head.mimeType = "text/plain";
        head.text = message.getContent();

        // TODO also form an html Part

        Part alternative = head;
        Part mixed = head;

        for (Message.Attachment attachment : message.attachments) {
            Part p = new Part(attachment);
            // treat the first ics("text/calendar") as an alternative of the mail content
            if (p.isCalendarEvent() && !alternative.isCalendarEvent()) {
                alternative.alternative = p;
                alternative = p;
            } else {
                mixed.mixed = p;
                mixed = p;
            }
        }

        return head;
    }

    private static String generateBoundary() {
        return new StringBuilder().append(getPackageName()).append('-')
                .append(getVersionCode()).append('-')
                .append(System.currentTimeMillis()).append('-')
                .append(SystemClock.elapsedRealtimeNanos()).toString();
    }

    private static final String getAppName() {
        return MailApp.appName;
    }

    private static final String getPackageName() {
        return MailApp.packageName;
    }

    private static byte[] getUtf8Bytes(String s)
            throws UnsupportedEncodingException {
        return s.getBytes("UTF-8");
    }

    private static final String getVersionCode() {
        return Integer.toHexString(MailApp.versionCode);
    }

    /**
     * *this* byte will not be checked, allows -1 as start
     */
    private static int nextUtf8(byte[] bytes, int start) {
        if (start < -1) {
            return -1;
        }
        while (++start < bytes.length && ((bytes[start] & 0xC0) == 0x80)) {
            // dummy
        }
        return (start < bytes.length) ? start : -1;
    }

    /**
     * taking start position means it being header and obeying {@link #MAX_LINE_LENGTH}<br/>
     * <br/>
     * encapsulate with "<code> =?UTF-8?B?</code>" and "<code>?=</code>"<br/>
     */
    private static void putBase64Encoded(OutputStream ostream,
            String s, int start) throws IOException {
        // rfc 2047 section 2. Syntax of encoded-words
        // encoded-word = "=?" charset "?" encoding "?" encoded-text "?="
        final byte[] prefixBytes = getUtf8Bytes("=?UTF-8?B?");
        final byte[] suffixBytes = getUtf8Bytes("?=");
        final int maxLength =
                MAX_LINE_LENGTH - prefixBytes.length - suffixBytes.length;

        // we would decode each encoded-word respectively and then join them
        // always use base64 rather than quoted-printable: very few people read the raw mail

        byte[] utf8Bytes = getUtf8Bytes(s);
        int capacity = (maxLength - start) * 3 / 4;
        int i = 0;
        int j = 0;
        while (i < utf8Bytes.length) {
            while (true) {
                // make sure bytes from a single utf8 graph are in the same line
                int next = nextUtf8(utf8Bytes, j);
                if (next < 0) {
                    // write the rest bytes
                    j = utf8Bytes.length;
                    break;
                }
                if (next - i > capacity) {
                    // enough for this line
                    break;
                }
                j = next;
            }
            if (j > i) {
                if (i != 0) {
                    putNewLine(ostream);
                    ostream.write(' ');
                }
                ostream.write(prefixBytes);
                ostream.write(Base64.encode(utf8Bytes, i, j - i,
                        Base64.NO_WRAP));
                ostream.write(suffixBytes);
            }

            capacity = (maxLength - 1) * 3 / 4; // SP
            i = j;
        }
    }

    private static void putBoundary(OutputStream ostream, String boundary)
            throws UnsupportedEncodingException, IOException {
        ostream.write('-');
        ostream.write('-');
        putString(ostream, boundary);
        putNewLine(ostream);
    }

    private static void putBoundaryEnd(OutputStream ostream, String boundary)
            throws UnsupportedEncodingException, IOException {
        ostream.write('-');
        ostream.write('-');
        putString(ostream, boundary);
        ostream.write('-');
        ostream.write('-');
        putNewLine(ostream);
    }

    private static void putHeader(OutputStream ostream, String key,
            String value) throws IOException {
        if (shouldEncode(key, value)) {
            putHeaderEncoded(ostream, key, value);
        } else {
            putString(ostream, key);
            ostream.write(':');
            ostream.write(' ');
            putString(ostream, value);
            putNewLine(ostream);
        }
    }

    private static void putHeaderContentDisposition(OutputStream ostream,
            String filename, long size) throws IOException {
        putString(ostream, "Content-Disposition: attachment;");
        putNewLine(ostream);
        if (filename != null && !filename.isEmpty()) {
            putString(ostream, " filename*=utf-8''");
            putUrlEncoded(ostream, getUtf8Bytes(filename));
            putNewLine(ostream);
        }
        if (size >= 0) {
            putString(ostream, " size=");
            putString(ostream, Long.toString(size));
            putNewLine(ostream);
        }
    }

    private static void putHeaderContentTransferEncoding(
            OutputStream ostream) throws IOException {
        putString(ostream, "Content-Transfer-Encoding: base64");
        putNewLine(ostream);
    }

    private static void putHeaderContentType(OutputStream ostream,
            String mimeType, String charset, String name, String boundary)
            throws IOException {
        putString(ostream, "Content-Type: ");
        putString(ostream, mimeType);
        if (charset != null || name != null || boundary != null) {
            ostream.write(';');
        }
        if (charset != null && !charset.isEmpty()) {
            putString(ostream, " charset=");
            putString(ostream, charset);
        }
        if (name != null && !name.isEmpty()) {
            putNewLine(ostream);
            putString(ostream, " name=\"");
            putBase64Encoded(ostream, name, 7);
            ostream.write('"');
        }
        if (boundary != null && !boundary.isEmpty()) {
            putNewLine(ostream);
            putString(ostream, " boundary=\"");
            putString(ostream, boundary);
            ostream.write('"');
        }
        putNewLine(ostream);
    }

    private static void putHeaderEncoded(OutputStream ostream, String key,
            String value) throws IOException {
        final byte[] keyBytes = getUtf8Bytes(key);
        ostream.write(keyBytes);
        ostream.write(':');
        ostream.write(' ');
        putBase64Encoded(ostream, value, keyBytes.length + 2);
        putNewLine(ostream);
    }

    static void putMessage(OutputStream ostream, Message message)
            throws IOException {

        // rfc 822 4.1
        // "Return-Path"
        // "Received"
        putHeader(ostream, "Date", DATE_FORMAT.format(new Date()));
        putHeader(ostream, "From", message.getSender());
        putHeader(ostream, "Subject", message.getSubject());
        // "Sender",
        // "To"
        // "Cc"
        // "Bcc": SMTP should not send BCC header but EAS must do
        // "Reply-To"
        putHeader(ostream, "MIME-Version", "1.0");
        // "References"
        // "In-Reply-To"
        putHeader(ostream, "X-Mailer", getAppName());

        Part head = formMessagePart(message);
        putMessagePartForMixed(ostream, head);
        ostream.flush();
    }

    /**
     * export message part it self, ignoring the linkage
     */
    private static void putMessagePart(OutputStream ostream, Part part)
            throws IOException {
        if (part.isMainText()) {
            putHeaderContentType(ostream, part.mimeType, "utf-8", null,
                    null);
            putHeaderContentTransferEncoding(ostream);
            putNewLine(ostream);

            ostream.write(Base64.encode(getUtf8Bytes(part.text),
                    Base64.CRLF));
            putNewLine(ostream);
        } else if (part.isCalendarEvent()) {
            // TODO
        } else {
            putHeaderContentDisposition(ostream,
                    part.getNameOfAttachment(), part.getSizeOfAttachment());
            putHeaderContentType(ostream, part.mimeType, null,
                    part.getNameOfAttachment(), null);
            putHeaderContentTransferEncoding(ostream);
            putNewLine(ostream);

            Base64OutputStream os =
                    new Base64OutputStream(ostream, Base64.CRLF
                            | Base64.NO_CLOSE);
            FileInputStream is = new FileInputStream(part.file);
            byte[] buffer = new byte[4096];
            int n = -1;
            while ((n = is.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
            is.close();
            os.flush();
            os.close();
            putNewLine(ostream);
        }
        ostream.flush();
    }

    private static void putMessagePartForAlternative(OutputStream ostream,
            Part part) throws IOException {

        if (part.alternative == null) {
            putMessagePart(ostream, part);
            return;
        }

        String boundary = generateBoundary();
        putHeaderContentType(ostream, "multipart/alternative", null, null,
                boundary);
        for (Part p = part; p != null; p = p.alternative) {
            putBoundary(ostream, boundary);
            putMessagePart(ostream, p);
        }
        putBoundaryEnd(ostream, boundary);
    }

    private static void putMessagePartForMixed(OutputStream ostream,
            Part part) throws IOException {

        if (part.mixed == null) {
            putMessagePartForAlternative(ostream, part);
            return;
        }

        String boundary = generateBoundary();

        putHeaderContentType(ostream, "multipart/mixed", null, null,
                boundary);
        putNewLine(ostream);
        for (Part p = part; p != null; p = p.mixed) {
            putBoundary(ostream, boundary);
            putMessagePartForAlternative(ostream, p);
        }
        putBoundaryEnd(ostream, boundary);
    }

    private static void putNewLine(OutputStream ostream) throws IOException {
        ostream.write('\r');
        ostream.write('\n');
    }

    private static void putString(OutputStream ostream, String s)
            throws IOException {
        // each short string translates to byte array
        // cost much, but won't be the bottleneck
        ostream.write(s.getBytes("UTF-8"));
    }

    private static void putUrlEncoded(OutputStream ostream, byte[] a)
            throws IOException {
        for (byte b : a) {
            if (FormatUtil.Url.shouldEncode(b)) {
                ostream.write('%');
                ostream.write(FormatUtil.Url.getHi(b));
                ostream.write(FormatUtil.Url.getLo(b));
            } else {
                ostream.write(b);
            }
        }
    }

    /**
     * a simple analysis that return <code>true</code> iff the bytes are in one
     * line and all alphanum
     */
    private static boolean shouldEncode(String key, String value) {
        if (key == null || value == null) {
            return false;
        }

        // key should not be encoded
        int taken = key.length() + 2;
        if (taken + value.length() > MAX_LINE_LENGTH) {
            return true;
        }

        if (value.length() > 1 && value.charAt(0) == '='
                && value.charAt(1) == '?') {
            // the literal is as if the encoded format
            return true;
        }

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch < 0x20 || ch >= 0x7F) {
                return true;
            }
        }
        return false;
    }
}
