package org.apache.http.impl.conn;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class DefaultClientConnectionOperator implements ClientConnectionOperator {
    public static final String SOCK_SEND_BUFF = "http.socket.send-buffer";
    private static final PlainSocketFactory staticPlainSocketFactory = new PlainSocketFactory();
    protected SchemeRegistry schemeRegistry;

    public DefaultClientConnectionOperator(SchemeRegistry schemes) {
        if (schemes == null) {
            throw new IllegalArgumentException("Scheme registry must not be null.");
        }
        this.schemeRegistry = schemes;
    }

    public OperatedClientConnection createConnection() {
        return new DefaultClientConnection();
    }

    public void openConnection(OperatedClientConnection conn, HttpHost target, InetAddress local, HttpContext context, HttpParams params) throws IOException {
        if (conn == null) {
            throw new IllegalArgumentException("Connection must not be null.");
        } else if (target == null) {
            throw new IllegalArgumentException("Target host must not be null.");
        } else if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        } else if (conn.isOpen()) {
            throw new IllegalArgumentException("Connection must not be open.");
        } else {
            SocketFactory plain_sf;
            LayeredSocketFactory layered_sf;
            Scheme schm = this.schemeRegistry.getScheme(target.getSchemeName());
            SocketFactory sf = schm.getSocketFactory();
            if (sf instanceof LayeredSocketFactory) {
                plain_sf = staticPlainSocketFactory;
                layered_sf = (LayeredSocketFactory) sf;
            } else {
                plain_sf = sf;
                layered_sf = null;
            }
            InetAddress[] addresses = InetAddress.getAllByName(target.getHostName());
            int i = 0;
            while (i < addresses.length) {
                Socket sock = plain_sf.createSocket();
                conn.opening(sock, target);
                try {
                    Socket connsock = plain_sf.connectSocket(sock, addresses[i].getHostAddress(), schm.resolvePort(target.getPort()), local, 0, params);
                    if (sock != connsock) {
                        sock = connsock;
                        conn.opening(connsock, target);
                    }
                    prepareSocket(sock, context, params);
                    if (layered_sf != null) {
                        Socket layeredsock = layered_sf.createSocket(sock, target.getHostName(), schm.resolvePort(target.getPort()), true);
                        if (layeredsock != sock) {
                            conn.opening(layeredsock, target);
                        }
                        conn.openCompleted(sf.isSecure(layeredsock), params);
                        return;
                    }
                    conn.openCompleted(sf.isSecure(sock), params);
                    return;
                } catch (SocketException ex) {
                    if (i == addresses.length - 1) {
                        ConnectException cause;
                        if (ex instanceof ConnectException) {
                            cause = (ConnectException) ex;
                        } else {
                            cause = new ConnectException(ex.getMessage());
                            cause.initCause(ex);
                        }
                        throw new HttpHostConnectException(target, cause);
                    }
                    i++;
                } catch (ConnectTimeoutException ex2) {
                    if (i == addresses.length - 1) {
                        throw ex2;
                    }
                    i++;
                }
            }
        }
    }

    public void updateSecureConnection(OperatedClientConnection conn, HttpHost target, HttpContext context, HttpParams params) throws IOException {
        if (conn == null) {
            throw new IllegalArgumentException("Connection must not be null.");
        } else if (target == null) {
            throw new IllegalArgumentException("Target host must not be null.");
        } else if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        } else if (conn.isOpen()) {
            Scheme schm = this.schemeRegistry.getScheme(target.getSchemeName());
            if (schm.getSocketFactory() instanceof LayeredSocketFactory) {
                LayeredSocketFactory lsf = (LayeredSocketFactory) schm.getSocketFactory();
                try {
                    Socket sock = lsf.createSocket(conn.getSocket(), target.getHostName(), schm.resolvePort(target.getPort()), true);
                    prepareSocket(sock, context, params);
                    conn.update(sock, target, lsf.isSecure(sock), params);
                    return;
                } catch (ConnectException ex) {
                    throw new HttpHostConnectException(target, ex);
                }
            }
            throw new IllegalArgumentException("Target scheme (" + schm.getName() + ") must have layered socket factory.");
        } else {
            throw new IllegalArgumentException("Connection must be open.");
        }
    }

    protected void prepareSocket(Socket sock, HttpContext context, HttpParams params) throws IOException {
        boolean z = false;
        sock.setTcpNoDelay(HttpConnectionParams.getTcpNoDelay(params));
        sock.setSoTimeout(HttpConnectionParams.getSoTimeout(params));
        int sendBufSize = params.getIntParameter(SOCK_SEND_BUFF, 0);
        if (sendBufSize > 0) {
            sock.setSendBufferSize(sendBufSize);
        }
        int linger = HttpConnectionParams.getLinger(params);
        if (linger >= 0) {
            if (linger > 0) {
                z = true;
            }
            sock.setSoLinger(z, linger);
        }
    }
}
