package org.apache.http.conn;

import java.net.ConnectException;
import org.apache.http.HttpHost;

@Deprecated
public class HttpHostConnectException extends ConnectException {
    private static final long serialVersionUID = -3194482710275220224L;
    private final HttpHost host;

    public HttpHostConnectException(HttpHost host2, ConnectException cause) {
        super("Connection to " + host2 + " refused");
        this.host = host2;
        initCause(cause);
    }

    public HttpHost getHost() {
        return this.host;
    }
}
