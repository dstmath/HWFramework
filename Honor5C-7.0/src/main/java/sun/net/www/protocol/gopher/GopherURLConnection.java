package sun.net.www.protocol.gopher;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketPermission;
import java.net.URL;
import java.security.Permission;
import sun.net.www.URLConnection;
import sun.security.util.SecurityConstants;

/* compiled from: Handler */
class GopherURLConnection extends URLConnection {
    Permission permission;

    GopherURLConnection(URL u) {
        super(u);
    }

    public void connect() throws IOException {
    }

    public InputStream getInputStream() throws IOException {
        return new GopherClient(this).openStream(this.url);
    }

    public Permission getPermission() {
        if (this.permission == null) {
            if (this.url.getPort() < 0) {
            }
            this.permission = new SocketPermission(this.url.getHost() + ":" + this.url.getPort(), SecurityConstants.SOCKET_CONNECT_ACTION);
        }
        return this.permission;
    }
}
