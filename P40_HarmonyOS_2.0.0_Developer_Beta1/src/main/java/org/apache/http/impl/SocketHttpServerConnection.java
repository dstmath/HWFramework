package org.apache.http.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import org.apache.http.HttpInetConnection;
import org.apache.http.impl.io.SocketInputBuffer;
import org.apache.http.impl.io.SocketOutputBuffer;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

@Deprecated
public class SocketHttpServerConnection extends AbstractHttpServerConnection implements HttpInetConnection {
    private volatile boolean open;
    private Socket socket = null;

    /* access modifiers changed from: protected */
    public void assertNotOpen() {
        if (this.open) {
            throw new IllegalStateException("Connection is already open");
        }
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.AbstractHttpServerConnection
    public void assertOpen() {
        if (!this.open) {
            throw new IllegalStateException("Connection is not open");
        }
    }

    /* access modifiers changed from: protected */
    public SessionInputBuffer createHttpDataReceiver(Socket socket2, int buffersize, HttpParams params) throws IOException {
        return new SocketInputBuffer(socket2, buffersize, params);
    }

    /* access modifiers changed from: protected */
    public SessionOutputBuffer createHttpDataTransmitter(Socket socket2, int buffersize, HttpParams params) throws IOException {
        return new SocketOutputBuffer(socket2, buffersize, params);
    }

    /* access modifiers changed from: protected */
    public void bind(Socket socket2, HttpParams params) throws IOException {
        if (socket2 == null) {
            throw new IllegalArgumentException("Socket may not be null");
        } else if (params != null) {
            this.socket = socket2;
            int buffersize = HttpConnectionParams.getSocketBufferSize(params);
            init(createHttpDataReceiver(socket2, buffersize, params), createHttpDataTransmitter(socket2, buffersize, params), params);
            this.open = true;
        } else {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
    }

    /* access modifiers changed from: protected */
    public Socket getSocket() {
        return this.socket;
    }

    @Override // org.apache.http.HttpConnection
    public boolean isOpen() {
        return this.open;
    }

    @Override // org.apache.http.HttpInetConnection
    public InetAddress getLocalAddress() {
        Socket socket2 = this.socket;
        if (socket2 != null) {
            return socket2.getLocalAddress();
        }
        return null;
    }

    @Override // org.apache.http.HttpInetConnection
    public int getLocalPort() {
        Socket socket2 = this.socket;
        if (socket2 != null) {
            return socket2.getLocalPort();
        }
        return -1;
    }

    @Override // org.apache.http.HttpInetConnection
    public InetAddress getRemoteAddress() {
        Socket socket2 = this.socket;
        if (socket2 != null) {
            return socket2.getInetAddress();
        }
        return null;
    }

    @Override // org.apache.http.HttpInetConnection
    public int getRemotePort() {
        Socket socket2 = this.socket;
        if (socket2 != null) {
            return socket2.getPort();
        }
        return -1;
    }

    @Override // org.apache.http.HttpConnection
    public void setSocketTimeout(int timeout) {
        assertOpen();
        Socket socket2 = this.socket;
        if (socket2 != null) {
            try {
                socket2.setSoTimeout(timeout);
            } catch (SocketException e) {
            }
        }
    }

    @Override // org.apache.http.HttpConnection
    public int getSocketTimeout() {
        Socket socket2 = this.socket;
        if (socket2 == null) {
            return -1;
        }
        try {
            return socket2.getSoTimeout();
        } catch (SocketException e) {
            return -1;
        }
    }

    @Override // org.apache.http.HttpConnection
    public void shutdown() throws IOException {
        this.open = false;
        Socket tmpsocket = this.socket;
        if (tmpsocket != null) {
            tmpsocket.close();
        }
    }

    @Override // org.apache.http.HttpConnection
    public void close() throws IOException {
        if (this.open) {
            this.open = false;
            doFlush();
            try {
                this.socket.shutdownOutput();
            } catch (IOException e) {
            }
            try {
                this.socket.shutdownInput();
            } catch (IOException e2) {
            }
            this.socket.close();
        }
    }
}
