package sun.net.www.protocol.mailto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketPermission;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.Permission;
import sun.net.smtp.SmtpClient;
import sun.net.www.MessageHeader;
import sun.net.www.ParseUtil;
import sun.net.www.URLConnection;
import sun.security.util.SecurityConstants;

public class MailToURLConnection extends URLConnection {
    SmtpClient client;
    private int connectTimeout;
    InputStream is;
    OutputStream os;
    Permission permission;
    private int readTimeout;

    MailToURLConnection(URL u) {
        super(u);
        this.is = null;
        this.os = null;
        this.connectTimeout = -1;
        this.readTimeout = -1;
        MessageHeader props = new MessageHeader();
        props.add("content-type", "text/html");
        setProperties(props);
    }

    String getFromAddress() {
        String str = System.getProperty("user.fromaddr");
        if (str != null) {
            return str;
        }
        str = System.getProperty("user.name");
        if (str == null) {
            return "";
        }
        String host = System.getProperty("mail.host");
        if (host == null) {
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
            }
        }
        return str + "@" + host;
    }

    public void connect() throws IOException {
        this.client = new SmtpClient(this.connectTimeout);
        this.client.setReadTimeout(this.readTimeout);
    }

    public synchronized OutputStream getOutputStream() throws IOException {
        if (this.os != null) {
            return this.os;
        } else if (this.is != null) {
            throw new IOException("Cannot write output after reading input.");
        } else {
            connect();
            String to = ParseUtil.decode(this.url.getPath());
            this.client.from(getFromAddress());
            this.client.to(to);
            this.os = this.client.startMessage();
            return this.os;
        }
    }

    public Permission getPermission() throws IOException {
        if (this.permission == null) {
            connect();
            this.permission = new SocketPermission(this.client.getMailHost() + ":" + 25, SecurityConstants.SOCKET_CONNECT_ACTION);
        }
        return this.permission;
    }

    public void setConnectTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeouts can't be negative");
        }
        this.connectTimeout = timeout;
    }

    public int getConnectTimeout() {
        return this.connectTimeout < 0 ? 0 : this.connectTimeout;
    }

    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeouts can't be negative");
        }
        this.readTimeout = timeout;
    }

    public int getReadTimeout() {
        return this.readTimeout < 0 ? 0 : this.readTimeout;
    }
}
