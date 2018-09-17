package org.apache.http.cookie;

import java.util.Locale;

@Deprecated
public final class CookieOrigin {
    private final String host;
    private final String path;
    private final int port;
    private final boolean secure;

    public CookieOrigin(String host, int port, String path, boolean secure) {
        if (host == null) {
            throw new IllegalArgumentException("Host of origin may not be null");
        } else if (host.trim().length() == 0) {
            throw new IllegalArgumentException("Host of origin may not be blank");
        } else if (port < 0) {
            throw new IllegalArgumentException("Invalid port: " + port);
        } else if (path == null) {
            throw new IllegalArgumentException("Path of origin may not be null.");
        } else {
            this.host = host.toLowerCase(Locale.ENGLISH);
            this.port = port;
            if (path.trim().length() != 0) {
                this.path = path;
            } else {
                this.path = "/";
            }
            this.secure = secure;
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
