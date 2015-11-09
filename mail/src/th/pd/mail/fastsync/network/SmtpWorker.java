package th.pd.mail.fastsync.network;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Base64;

import th.pd.mail.dao.Message;
import th.pd.mail.dao.SmtpSyncable;
import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.MailServerAuth;

/**
 * see <strong>Simple Mail Transfer Protocol</strong><br/>
 * &emsp; rfc 821 -> rfc 2821 -> rfc 5321<br/>
 * see <strong>Internet Message Format</strong><br/>
 * &emsp; rfc 822 -> rfc 2822 -> rfc 5322<br/>
 * see <strong>MIME (Multipurpose Internet Mail Extensions)</strong><br/>
 * &emsp; rfc 2045-2047<br/>
 * rfc 2231<br/>
 * rfc 2387<br/>
 * rfc 3461<br/>
 */
public class SmtpWorker {

    private static String getLocalHost(SocketConn conn) {
        String host = "localhost";
        InetAddress addr = conn.getLocalAddress();
        if (addr != null) {
            // rfc 2821 4.1.3
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            if (addr instanceof Inet6Address) {
                sb.append("IPv6:");
            }
            sb.append(addr.getHostAddress());
            sb.append(']');
            host = sb.toString();
        }
        return host;
    }

    private static String getResp(SocketConn conn)
            throws IOException, MessengerException {
        String s = conn.getLine();
        String resp = s;
        while (s.length() > 3 && s.charAt(3) == '-') {
            s = conn.getLine();
            resp += "\n  ";
            resp += s;
        }

        if (!resp.isEmpty()) {
            switch (resp.charAt(0)) {
                case '4': // 4xx
                case '5': // 5xx
                    throw new MessengerException(resp,
                            MessengerException.TYPE_SMTP);
            }
        }
        return resp;
    }

    static void putLine(SocketConn conn, String commandLine)
            throws IOException, MessengerException {
        assert conn != null;
        if (commandLine != null) {
            Const.logd("CMND--- " + commandLine);
            conn.putLine(commandLine);
        }
    }

    private SocketConn mSocketConn = new SocketConn();

    private void conn(SocketAddress remoteAddr, String user, String pass)
            throws IOException, MessengerException {
        connEnd(); // start from a clean state

        Const.logd("CONN--- " + remoteAddr);
        mSocketConn.conn(remoteAddr);

        // consume the server banner and welcome message
        String resp = getResp(mSocketConn);
        Const.logd("RESP--- " + resp);

        resp = onCommandEhlo(getLocalHost(mSocketConn));

        // TODO try tls "STARTTLS"

        if (!user.isEmpty()) {
            if (!pass.isEmpty()) {
                if (resp.matches("(?s).*AUTH.*PLAIN.*")) {
                    onCommandAuthPlain(user, pass);
                } else if (resp.matches("(?s).*AUTH.*LOGIN.*")) {
                    onCommandAuthLogin(user, pass);
                } else {
                    throw new MessengerException(
                            "Unknown authentication mechanism.");
                }
            } else if (true) {
                // TODO above condition: oauth support
                // TODO
            }
        } else {
            // TODO no user
        }
    }

    private void connEnd() {
        Const.logd("CONN CLEAR--- ");
        mSocketConn.connEnd();
    }

    private String onCommand(String command)
            throws IOException, MessengerException {
        putLine(mSocketConn, command);
        return getResp(mSocketConn);
    }

    private void onCommandAuthLogin(String user, String pass)
            throws IOException, MessengerException {
        onCommand("AUTH LOGIN");
        onCommand(Base64.encodeToString(user.getBytes(), Base64.NO_WRAP));
        onCommand(Base64.encodeToString(pass.getBytes(), Base64.NO_WRAP));
    }

    private void onCommandAuthPlain(String user, String pass)
            throws IOException, MessengerException {
        byte[] bytes = Base64.encode(
                ('\000' + user + '\000' + pass).getBytes(), Base64.NO_WRAP);
        onCommand("AUTH PLAIN " + new String(bytes));
    }

    private void onCommandData(Message message)
            throws IOException, MessengerException {
        onCommand("DATA");
        MessageExporter.putMessage(mSocketConn.getBufferedOutputStream(),
                message);
        onCommand(".");
    }

    private String onCommandEhlo(String localHost)
            throws IOException, MessengerException {
        return onCommand("EHLO " + localHost);
    }

    private void onCommandMailFrom(Rfc822Token[] tokens)
            throws IOException, MessengerException {
        onCommand("MAIL FROM: <" + tokens[0].getAddress() + ">");
    }

    private void onCommandQuit()
            throws IOException, MessengerException {
        onCommand("QUIT");
    }

    private void onCommandRcptTo(Rfc822Token[] tokens)
            throws IOException, MessengerException {
        for (Rfc822Token token : tokens) {
            onCommand("RCPT TO: <" + token.getAddress() + ">");
        }
    }

    public void sendMessage(SmtpSyncable syncMessage)
            throws IOException, MessengerException {
        Message message = syncMessage.getMessage();
        Rfc822Token[] from = Rfc822Tokenizer.tokenize(message.getSender());
        Rfc822Token[] to = Rfc822Tokenizer.tokenize(message.getRecipient());
        Rfc822Token[] cc =
                Rfc822Tokenizer.tokenize(message.getCarbonCopy());
        Rfc822Token[] bcc = Rfc822Tokenizer.tokenize(message
                .getBlindCarbonCopy());

        MailServerAuth serverAuth = syncMessage.getServerAuth();
        SocketAddress remoteAddr = new InetSocketAddress(
                serverAuth.getHost(), serverAuth.getPort());

        try {
            conn(remoteAddr, serverAuth.getLogin(), serverAuth.getPin());
            onCommandMailFrom(from);
            onCommandRcptTo(to);
            onCommandRcptTo(cc);
            onCommandRcptTo(bcc);
            onCommandData(message);
            onCommandQuit();
        } finally {
            connEnd();
        }
    }
}
