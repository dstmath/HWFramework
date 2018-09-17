package org.apache.http.conn.scheme;

import java.util.Locale;
import org.apache.http.util.LangUtils;

@Deprecated
public final class Scheme {
    private final int defaultPort;
    private final boolean layered;
    private final String name;
    private final SocketFactory socketFactory;
    private String stringRep;

    public Scheme(String name, SocketFactory factory, int port) {
        if (name == null) {
            throw new IllegalArgumentException("Scheme name may not be null");
        } else if (factory == null) {
            throw new IllegalArgumentException("Socket factory may not be null");
        } else if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port is invalid: " + port);
        } else {
            this.name = name.toLowerCase(Locale.ENGLISH);
            this.socketFactory = factory;
            this.defaultPort = port;
            this.layered = factory instanceof LayeredSocketFactory;
        }
    }

    public final int getDefaultPort() {
        return this.defaultPort;
    }

    public final SocketFactory getSocketFactory() {
        return this.socketFactory;
    }

    public final String getName() {
        return this.name;
    }

    public final boolean isLayered() {
        return this.layered;
    }

    public final int resolvePort(int port) {
        return (port <= 0 || port > 65535) ? this.defaultPort : port;
    }

    public final String toString() {
        if (this.stringRep == null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.name);
            buffer.append(':');
            buffer.append(Integer.toString(this.defaultPort));
            this.stringRep = buffer.toString();
        }
        return this.stringRep;
    }

    public final boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Scheme)) {
            return false;
        }
        Scheme s = (Scheme) obj;
        if (this.name.equals(s.name) && this.defaultPort == s.defaultPort && this.layered == s.layered) {
            z = this.socketFactory.equals(s.socketFactory);
        }
        return z;
    }

    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(LangUtils.hashCode(LangUtils.hashCode(17, this.defaultPort), this.name), this.layered), this.socketFactory);
    }
}
