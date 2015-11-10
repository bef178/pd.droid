package th.pd.mail.fastsync;

public class MailServerAuth {

    private String protocol;

    private String host; // server host
    private int port;

    private String user; // login
    private String pass;

    private int flags;

    public int getFlags() {
        return flags;
    }

    public String getHost() {
        return host;
    }

    public String getLogin() {
        return user;
    }

    public String getPin() {
        return pass;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setLogin(String login) {
        user = login;
    }

    public void setPin(String pin) {
        this.pass = pin;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
