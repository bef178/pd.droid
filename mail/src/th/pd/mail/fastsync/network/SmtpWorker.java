package th.pd.mail.fastsync.network;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.net.ssl.SSLException;

import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Base64;

import th.pd.mail.dao.Message;
import th.pd.mail.dao.MessageForSend;
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

    private SocketConn mSocketConn = new SocketConn();

    private void conn(SocketAddress remoteAddr, String user, String pass)
            throws AuthFailedException, MessengerException, IOException {
        connEnd(); // start from a clean state

        try {
            Const.logd("CONN--- " + remoteAddr);
            mSocketConn.conn(remoteAddr);

            // consume the server banner and welcome message
            String resp = getResp();
            Const.logd("RESP--- " + resp);

            resp = onCommandEhlo(getLocalHost());

            // TODO try tls "STARTTLS"

            if (!user.isEmpty()) {
                if (!pass.isEmpty()) {
                    if (resp.matches("(?s).*AUTH.*PLAIN.*")) {
                        onCommandAuthPlain(user, pass);
                    } else if (resp.matches("(?s).*AUTH.*LOGIN.*")) {
                        onCommandAuthLogin(user, pass);
                    } else {
                        String s1 =
                                "No valid authentication mechanism found.";
                        String s2 =
                                "Authentication is required but the server did not support it.";
                        throw new MessengerException(s1 + " " + s2);
                    }
                } else if (true) {
                    // TODO above condition: oauth support
                    // TODO
                }
            } else {
                // TODO no user
            }
        } catch (SSLException e) {
            // TODO
            throw new MessengerException(e.toString());
        }
    }

    private void connEnd() {
        Const.logd("CONN CLEAR--- ");
        mSocketConn.connEnd();
    }

    private String getLocalHost() {
        String host = "localhost";
        InetAddress addr = mSocketConn.getLocalAddress();
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

    private String getResp() throws IOException {
        String s = mSocketConn.getLine();
        String resp = s;
        while (s.length() > 3 && s.charAt(3) == '-') {
            s = mSocketConn.getLine();
            resp += "\n  ";
            resp += s;
        }

        if (!resp.isEmpty()) {
            switch (resp.charAt(0)) {
                case '4':
                case '5':
                    // TODO error, throw exception
                    break;
            }
        }
        return resp;
    }

    private String onCommand(String command) throws IOException,
            MessengerException {
        if (command != null) {
            Const.logd("CMND--- " + command);
            mSocketConn.putLine(command);
        }
        String resp = getResp();
        Const.logd("RESP--- " + resp);
        if (!resp.isEmpty()) {
            switch (resp.charAt(0)) {
                case '4':
                case '5':
                    throw new MessengerException(resp);
            }
        }
        return resp;
    }

    private void onCommandAuthLogin(String user, String pass)
            throws IOException, MessengerException, AuthFailedException {
        try {
            onCommand("AUTH LOGIN");
            onCommand(Base64
                    .encodeToString(user.getBytes(), Base64.NO_WRAP));
            onCommand(Base64
                    .encodeToString(pass.getBytes(), Base64.NO_WRAP));
        } catch (MessengerException e) {
            String msg = e.getMessage();
            if (!msg.isEmpty() && msg.charAt(1) == '3') {
                throw new AuthFailedException(msg);
            } else {
                throw e;
            }
        }
    }

    private void onCommandAuthPlain(String user, String pass)
            throws IOException, MessengerException, AuthFailedException {
        byte[] bytes = Base64.encode(
                ('\000' + user + '\000' + pass).getBytes(), Base64.NO_WRAP);
        try {
            onCommand("AUTH PLAIN " + new String(bytes));
        } catch (MessengerException e) {
            String msg = e.getMessage();
            if (!msg.isEmpty() && msg.charAt(1) == '3') {
                throw new AuthFailedException(msg);
            } else {
                throw e;
            }
        }
    }

    private void onCommandData(Message message) throws IOException,
            MessengerException {
        onCommand("DATA");
        MessageExporter.putMessage(mSocketConn.getBufferedOutputStream(),
                message);
        onCommand(".");
    }

    private String onCommandEhlo(String localHost) throws IOException,
            MessengerException {
        return onCommand("EHLO " + localHost);
    }

    private void onCommandMailFrom(Rfc822Token[] tokens)
            throws IOException,
            MessengerException {
        onCommand("MAIL FROM: <" + tokens[0].getAddress() + ">");
    }

    private void onCommandQuit() throws IOException, MessengerException {
        onCommand("QUIT");
    }

    private void onCommandRcptTo(Rfc822Token[] tokens) throws IOException,
            MessengerException {
        for (Rfc822Token token : tokens) {
            onCommand("RCPT TO: <" + token.getAddress() + ">");
        }
    }

    public void sendMessage(MessageForSend syncMessage)
            throws MessengerException, AuthFailedException {

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

        // TODO test network

        try {
            conn(remoteAddr, serverAuth.getLogin(), serverAuth.getPin());
            onCommandMailFrom(from);
            onCommandRcptTo(to);
            onCommandRcptTo(cc);
            onCommandRcptTo(bcc);
            onCommandData(message);
            onCommandQuit();
        } catch (IOException e) {
            throw new MessengerException(e.toString());
        } finally {
            connEnd();
        }
    }
}
