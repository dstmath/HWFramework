package org.apache.http.cookie;

import java.util.Locale;

@Deprecated
public final class CookieOrigin {
    private final String host;
    private final String path;
    private final int port;
    private final boolean secure;

    public CookieOrigin(String host2, int port2, String path2, boolean secure2) {
        if (host2 == null) {
            throw new IllegalArgumentException("Host of origin may not be null");
        } else if (host2.trim().length() == 0) {
            throw new IllegalArgumentException("Host of origin may not be blank");
        } else if (port2 < 0) {
            throw new IllegalArgumentException("Invalid port: " + port2);
        } else if (path2 != null) {
            this.host = host2.toLowerCase(Locale.ENGLISH);
            this.port = port2;
            if (path2.trim().length() != 0) {
                this.path = path2;
            } else {
                this.path = "/";
            }
            this.secure = secure2;
        } else {
            throw new IllegalArgumentException("Path of origin may not be null.");
        }
    }

    public String getHost() {
        return this.host;
    }

    public String getPath() {
        return this.path;
    }

    public int getPort() {
        return this.port;
    }

    public boolean isSecure() {
        return this.secure;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (this.secure) {
            buffer.append("(secure)");
        }
        buffer.append(this.host);
        buffer.append(':');
        buffer.append(Integer.toString(this.port));
        buffer.append(this.path);
        buffer.append(']');
        return buffer.toString();
    }
}
