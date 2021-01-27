package org.apache.http.impl;

import java.io.IOException;
import java.net.Socket;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

@Deprecated
public class DefaultHttpClientConnection extends SocketHttpClientConnection {
    @Override // org.apache.http.impl.SocketHttpClientConnection
    public void bind(Socket socket, HttpParams params) throws IOException {
        if (socket == null) {
            throw new IllegalArgumentException("Socket may not be null");
        } else if (params != null) {
            assertNotOpen();
            socket.setTcpNoDelay(HttpConnectionParams.getTcpNoDelay(params));
            socket.setSoTimeout(HttpConnectionParams.getSoTimeout(params));
            int linger = HttpConnectionParams.getLinger(params);
            if (linger >= 0) {
                socket.setSoLinger(linger > 0, linger);
            }
            super.bind(socket, params);
        } else {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        if (isOpen()) {
            buffer.append(getRemotePort());
        } else {
            buffer.append("closed");
        }
        buffer.append("]");
        return buffer.toString();
    }
}
