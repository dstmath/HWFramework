package org.apache.http.conn.scheme;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

@Deprecated
public final class PlainSocketFactory implements SocketFactory {
    private static final PlainSocketFactory DEFAULT_FACTORY = new PlainSocketFactory();
    private final HostNameResolver nameResolver;

    public static PlainSocketFactory getSocketFactory() {
        return DEFAULT_FACTORY;
    }

    public PlainSocketFactory(HostNameResolver nameResolver2) {
        this.nameResolver = nameResolver2;
    }

    public PlainSocketFactory() {
        this(null);
    }

    @Override // org.apache.http.conn.scheme.SocketFactory
    public Socket createSocket() {
        return new Socket();
    }

    @Override // org.apache.http.conn.scheme.SocketFactory
    public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort, HttpParams params) throws IOException {
        InetSocketAddress remoteAddress;
        if (host == null) {
            throw new IllegalArgumentException("Target host may not be null.");
        } else if (params != null) {
            if (sock == null) {
                sock = createSocket();
            }
            if (localAddress != null || localPort > 0) {
                if (localPort < 0) {
                    localPort = 0;
                }
                sock.bind(new InetSocketAddress(localAddress, localPort));
            }
            int timeout = HttpConnectionParams.getConnectionTimeout(params);
            HostNameResolver hostNameResolver = this.nameResolver;
            if (hostNameResolver != null) {
                remoteAddress = new InetSocketAddress(hostNameResolver.resolve(host), port);
            } else {
                remoteAddress = new InetSocketAddress(host, port);
            }
            try {
                sock.connect(remoteAddress, timeout);
                return sock;
            } catch (SocketTimeoutException e) {
                throw new ConnectTimeoutException("Connect to " + remoteAddress + " timed out");
            }
        } else {
            throw new IllegalArgumentException("Parameters may not be null.");
        }
    }

    @Override // org.apache.http.conn.scheme.SocketFactory
    public final boolean isSecure(Socket sock) throws IllegalArgumentException {
        if (sock == null) {
            throw new IllegalArgumentException("Socket may not be null.");
        } else if (sock.getClass() != Socket.class) {
            throw new IllegalArgumentException("Socket not created by this factory.");
        } else if (!sock.isClosed()) {
            return false;
        } else {
            throw new IllegalArgumentException("Socket is closed.");
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return PlainSocketFactory.class.hashCode();
    }
}
