package org.apache.http.conn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

@Deprecated
public final class MultihomePlainSocketFactory implements SocketFactory {
    private static final MultihomePlainSocketFactory DEFAULT_FACTORY = new MultihomePlainSocketFactory();

    public static MultihomePlainSocketFactory getSocketFactory() {
        return DEFAULT_FACTORY;
    }

    private MultihomePlainSocketFactory() {
    }

    public Socket createSocket() {
        return new Socket();
    }

    public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort, HttpParams params) throws IOException {
        if (host == null) {
            throw new IllegalArgumentException("Target host may not be null.");
        } else if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null.");
        } else {
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
            InetAddress[] inetadrs = InetAddress.getAllByName(host);
            List<InetAddress> addresses = new ArrayList(inetadrs.length);
            addresses.addAll(Arrays.asList(inetadrs));
            Collections.shuffle(addresses);
            IOException lastEx = null;
            for (InetAddress address : addresses) {
                try {
                    sock.connect(new InetSocketAddress(address, port), timeout);
                    break;
                } catch (SocketTimeoutException ex) {
                    throw ex;
                } catch (IOException ex2) {
                    sock = new Socket();
                    lastEx = ex2;
                }
            }
            if (lastEx == null) {
                return sock;
            }
            throw lastEx;
        }
    }

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

    public boolean equals(Object obj) {
        return obj == this;
    }

    public int hashCode() {
        return PlainSocketFactory.class.hashCode();
    }
}
