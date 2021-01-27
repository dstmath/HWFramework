package org.ksoap2.transport;

import java.io.IOException;

public class KeepAliveHttpsTransportSE extends HttpsTransportSE {
    public KeepAliveHttpsTransportSE(String host, int port, String file, int timeout) {
        super(host, port, file, timeout);
    }

    @Override // org.ksoap2.transport.HttpsTransportSE, org.ksoap2.transport.HttpTransportSE, org.ksoap2.transport.Transport
    public ServiceConnection getServiceConnection() throws IOException {
        ServiceConnection serviceConnection = new HttpsServiceConnectionSEIgnoringConnectionClose(this.host, this.port, this.file, this.timeout);
        serviceConnection.setRequestProperty("Connection", "keep-alive");
        return serviceConnection;
    }
}
