package org.apache.http.conn;

import java.net.ConnectException;
import org.apache.http.HttpHost;

@Deprecated
public class HttpHostConnectException extends ConnectException {
    private static final long serialVersionUID = -3194482710275220224L;
    private final HttpHost host;

    public HttpHostConnectException(HttpHost host, ConnectException cause) {
        super("Connection to " + host + " refused");
        this.host = host;
        initCause(cause);
    }

    public HttpHost getHost() {
        return this.host;
    }
}
