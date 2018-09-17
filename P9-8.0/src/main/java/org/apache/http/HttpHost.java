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

    public HttpHost(String hostname, int port, String scheme) {
        if (hostname == null) {
            throw new IllegalArgumentException("Host name may not be null");
        }
        this.hostname = hostname;
        this.lcHostname = hostname.toLowerCase(Locale.ENGLISH);
        if (scheme != null) {
            this.schemeName = scheme.toLowerCase(Locale.ENGLISH);
        } else {
            this.schemeName = DEFAULT_SCHEME_NAME;
        }
        this.port = port;
    }

    public HttpHost(String hostname, int port) {
        this(hostname, port, null);
    }

    public HttpHost(String hostname) {
        this(hostname, -1, null);
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
        if (this.lcHostname.equals(that.lcHostname) && this.port == that.port) {
            z = this.schemeName.equals(that.schemeName);
        }
        return z;
    }

    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(LangUtils.hashCode(17, this.lcHostname), this.port), this.schemeName);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
