package sun.net.www.protocol.http;

import java.net.Authenticator.RequestorType;
import java.net.InetAddress;
import java.net.URL;

public final class HttpCallerInfo {
    public final InetAddress addr;
    public final RequestorType authType;
    public final String host;
    public final int port;
    public final String prompt;
    public final String protocol;
    public final String scheme;
    public final URL url;

    public HttpCallerInfo(HttpCallerInfo old, String scheme) {
        this.url = old.url;
        this.host = old.host;
        this.protocol = old.protocol;
        this.prompt = old.prompt;
        this.port = old.port;
        this.addr = old.addr;
        this.authType = old.authType;
        this.scheme = scheme;
    }

    public HttpCallerInfo(URL url) {
        InetAddress byName;
        this.url = url;
        this.prompt = "";
        this.host = url.getHost();
        int p = url.getPort();
        if (p == -1) {
            this.port = url.getDefaultPort();
        } else {
            this.port = p;
        }
        try {
            byName = InetAddress.getByName(url.getHost());
        } catch (Exception e) {
            byName = null;
        }
        this.addr = byName;
        this.protocol = url.getProtocol();
        this.authType = RequestorType.SERVER;
        this.scheme = "";
    }

    public HttpCallerInfo(URL url, String host, int port) {
        this.url = url;
        this.host = host;
        this.port = port;
        this.prompt = "";
        this.addr = null;
        this.protocol = url.getProtocol();
        this.authType = RequestorType.PROXY;
        this.scheme = "";
    }
}
