package sun.net.www.http;

import java.net.URL;

/* compiled from: KeepAliveCache */
class KeepAliveKey {
    private String host;
    private Object obj;
    private int port;
    private String protocol;

    public KeepAliveKey(URL url, Object obj) {
        this.protocol = null;
        this.host = null;
        this.port = 0;
        this.obj = null;
        this.protocol = url.getProtocol();
        this.host = url.getHost();
        this.port = url.getPort();
        this.obj = obj;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof KeepAliveKey)) {
            return false;
        }
        KeepAliveKey kae = (KeepAliveKey) obj;
        if (this.host.equals(kae.host) && this.port == kae.port && this.protocol.equals(kae.protocol) && this.obj == kae.obj) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        String str = this.protocol + this.host + this.port;
        if (this.obj == null) {
            return str.hashCode();
        }
        return str.hashCode() + this.obj.hashCode();
    }
}
