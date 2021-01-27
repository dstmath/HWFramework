package org.ksoap2.transport;

import java.io.IOException;
import java.net.Proxy;

public class HttpsTransportSE extends HttpTransportSE {
    static final String PROTOCOL = "https";
    private static final String PROTOCOL_FULL = "https://";
    private HttpsServiceConnectionSE connection;
    protected final String file;
    protected final String host;
    protected final int port;

    public HttpsTransportSE(String host2, int port2, String file2, int timeout) {
        super(PROTOCOL_FULL + host2 + ":" + port2 + file2, timeout);
        this.host = host2;
        this.port = port2;
        this.file = file2;
    }

    public HttpsTransportSE(Proxy proxy, String host2, int port2, String file2, int timeout) {
        super(proxy, PROTOCOL_FULL + host2 + ":" + port2 + file2);
        this.host = host2;
        this.port = port2;
        this.file = file2;
        this.timeout = timeout;
    }

    @Override // org.ksoap2.transport.HttpTransportSE, org.ksoap2.transport.Transport
    public ServiceConnection getServiceConnection() throws IOException {
        HttpsServiceConnectionSE httpsServiceConnectionSE = this.connection;
        if (httpsServiceConnectionSE != null) {
            return httpsServiceConnectionSE;
        }
        this.connection = new HttpsServiceConnectionSE(this.proxy, this.host, this.port, this.file, this.timeout);
        return this.connection;
    }
}
