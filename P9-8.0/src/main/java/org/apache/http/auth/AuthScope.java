package org.apache.http.auth;

import java.util.Locale;
import org.apache.http.util.LangUtils;

@Deprecated
public class AuthScope {
    public static final AuthScope ANY = new AuthScope(ANY_HOST, -1, ANY_REALM, ANY_SCHEME);
    public static final String ANY_HOST = null;
    public static final int ANY_PORT = -1;
    public static final String ANY_REALM = null;
    public static final String ANY_SCHEME = null;
    private final String host;
    private final int port;
    private final String realm;
    private final String scheme;

    public AuthScope(String host, int port, String realm, String scheme) {
        this.host = host == null ? ANY_HOST : host.toLowerCase(Locale.ENGLISH);
        if (port < 0) {
            port = -1;
        }
        this.port = port;
        if (realm == null) {
            realm = ANY_REALM;
        }
        this.realm = realm;
        this.scheme = scheme == null ? ANY_SCHEME : scheme.toUpperCase(Locale.ENGLISH);
    }

    public AuthScope(String host, int port, String realm) {
        this(host, port, realm, ANY_SCHEME);
    }

    public AuthScope(String host, int port) {
        this(host, port, ANY_REALM, ANY_SCHEME);
    }

    public AuthScope(AuthScope authscope) {
        if (authscope == null) {
            throw new IllegalArgumentException("Scope may not be null");
        }
        this.host = authscope.getHost();
        this.port = authscope.getPort();
        this.realm = authscope.getRealm();
        this.scheme = authscope.getScheme();
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getRealm() {
        return this.realm;
    }

    public String getScheme() {
        return this.scheme;
    }

    public int match(AuthScope that) {
        int factor = 0;
        if (LangUtils.equals(this.scheme, that.scheme)) {
            factor = 1;
        } else if (!(this.scheme == ANY_SCHEME || that.scheme == ANY_SCHEME)) {
            return -1;
        }
        if (LangUtils.equals(this.realm, that.realm)) {
            factor += 2;
        } else if (!(this.realm == ANY_REALM || that.realm == ANY_REALM)) {
            return -1;
        }
        if (this.port == that.port) {
            factor += 4;
        } else if (!(this.port == -1 || that.port == -1)) {
            return -1;
        }
        if (LangUtils.equals(this.host, that.host)) {
            factor += 8;
        } else if (this.host == ANY_HOST || that.host == ANY_HOST) {
            return factor;
        } else {
            return -1;
        }
        return factor;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof AuthScope)) {
            return super.equals(o);
        }
        AuthScope that = (AuthScope) o;
        if (LangUtils.equals(this.host, that.host) && this.port == that.port && LangUtils.equals(this.realm, that.realm)) {
            z = LangUtils.equals(this.scheme, that.scheme);
        }
        return z;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (this.scheme != null) {
            buffer.append(this.scheme.toUpperCase(Locale.ENGLISH));
            buffer.append(' ');
        }
        if (this.realm != null) {
            buffer.append('\'');
            buffer.append(this.realm);
            buffer.append('\'');
        } else {
            buffer.append("<any realm>");
        }
        if (this.host != null) {
            buffer.append('@');
            buffer.append(this.host);
            if (this.port >= 0) {
                buffer.append(':');
                buffer.append(this.port);
            }
        }
        return buffer.toString();
    }

    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(LangUtils.hashCode(LangUtils.hashCode(17, this.host), this.port), this.realm), this.scheme);
    }
}
