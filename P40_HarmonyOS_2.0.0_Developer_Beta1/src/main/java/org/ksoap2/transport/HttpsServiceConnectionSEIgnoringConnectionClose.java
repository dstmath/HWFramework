package org.ksoap2.transport;

import java.io.IOException;

class HttpsServiceConnectionSEIgnoringConnectionClose extends HttpsServiceConnectionSE {
    public HttpsServiceConnectionSEIgnoringConnectionClose(String host, int port, String file, int timeout) throws IOException {
        super(host, port, file, timeout);
    }

    @Override // org.ksoap2.transport.HttpsServiceConnectionSE, org.ksoap2.transport.ServiceConnection
    public void setRequestProperty(String key, String value) {
        if (!"Connection".equalsIgnoreCase(key) || !"close".equalsIgnoreCase(value)) {
            super.setRequestProperty(key, value);
        }
    }
}
