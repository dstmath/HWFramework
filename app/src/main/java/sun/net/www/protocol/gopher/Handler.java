package sun.net.www.protocol.gopher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import sun.net.www.protocol.http.HttpURLConnection;

public class Handler extends URLStreamHandler {
    protected int getDefaultPort() {
        return 70;
    }

    public URLConnection openConnection(URL u) throws IOException {
        return openConnection(u, null);
    }

    public URLConnection openConnection(URL u, Proxy p) throws IOException {
        if (p == null && GopherClient.getUseGopherProxy()) {
            String host = GopherClient.getGopherProxyHost();
            if (host != null) {
                p = new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(host, GopherClient.getGopherProxyPort()));
            }
        }
        if (p != null) {
            return new HttpURLConnection(u, p);
        }
        return new GopherURLConnection(u);
    }
}
