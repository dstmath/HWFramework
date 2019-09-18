package org.apache.http;

import java.util.Locale;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.LangUtils;

@Deprecated
public final class HttpHost implements Cloneable {
    public static final String DEFAULT_SCHEME_NAME = "http";
    protected final String hostname;
    protected final String lcHostname;
    protected final int port;
    protected final String schemeName;

    public HttpHost(String hostname2, int port2, String scheme) {
        if (hostname2 != null) {
            this.hostname = hostname2;
            this.lcHostname = hostname2.toLowerCase(Locale.ENGLISH);
            if (scheme != null) {
                this.schemeName = scheme.toLowerCase(Locale.ENGLISH);
            } else {
                this.schemeName = DEFAULT_SCHEME_NAME;
            }
            this.port = port2;
            return;
        }
        throw new IllegalArgumentException("Host name may not be null");
    }

    public HttpHost(String hostname2, int port2) {
        this(hostname2, port2, null);
    }

    public HttpHost(String hostname2) {
        this(hostname2, -1, null);
    }

    public HttpHost(HttpHost httphost) {
        this(httphost.hostname, httphost.port, httphost.schemeName);
    }

    public String getHostName() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    public String getSchemeName() {
        return this.schemeName;
    }

    public String toURI() {
        CharArrayBuffer buffer = new CharArrayBuffer(32);
        buffer.append(this.schemeName);
        buffer.append("://");
        buffer.append(this.hostname);
        if (this.port != -1) {
            buffer.append(':');
            buffer.append(Integer.toString(this.port));
        }
        return buffer.toString();
    }

    public String toHostString() {
        CharArrayBuffer buffer = new CharArrayBuffer(32);
        buffer.append(this.hostname);
        if (this.port != -1) {
            buffer.append(':');
            buffer.append(Integer.toString(this.port));
        }
        return buffer.toString();
    }

    public String toString() {
        return toURI();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HttpHost)) {
            return false;
        }
        HttpHost that = (HttpHost) obj;
        if (this.lcHostname.equals(that.lcHostname) && this.port == that.port && this.schemeName.equals(that.schemeName)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(LangUtils.hashCode(17, (Object) this.lcHostname), this.port), (Object) this.schemeName);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
