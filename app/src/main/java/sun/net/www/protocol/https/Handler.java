package sun.net.www.protocol.https;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public class Handler extends sun.net.www.protocol.http.Handler {
    protected String proxy;
    protected int proxyPort;

    protected int getDefaultPort() {
        return 443;
    }

    public Handler() {
        this.proxy = null;
        this.proxyPort = -1;
    }

    public Handler(String proxy, int port) {
        this.proxy = proxy;
        this.proxyPort = port;
    }

    protected URLConnection openConnection(URL u) throws IOException {
        return openConnection(u, (Proxy) null);
    }

    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        return new HttpsURLConnectionImpl(u, p, this);
    }
}
