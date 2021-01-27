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
    private static final PlainSocketFactory staticPlainSocketFactory = new PlainSocketFactory();
    protected SchemeRegistry schemeRegistry;

    public DefaultClientConnectionOperator(SchemeRegistry schemes) {
        if (schemes != null) {
            this.schemeRegistry = schemes;
            return;
        }
        throw new IllegalArgumentException("Scheme registry must not be null.");
    }

    @Override // org.apache.http.conn.ClientConnectionOperator
    public OperatedClientConnection createConnection() {
        return new DefaultClientConnection();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00d5, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00d6, code lost:
        r17 = r14;
        r5 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00dd, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00de, code lost:
        r18 = r9;
        r19 = r10;
        r17 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00f0, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00fe, code lost:
        if ((r0 instanceof java.net.ConnectException) != false) goto L_0x0100;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0100, code lost:
        r6 = (java.net.ConnectException) r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0104, code lost:
        r6 = new java.net.ConnectException(r0.getMessage());
        r6.initCause(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0115, code lost:
        throw new org.apache.http.conn.HttpHostConnectException(r22, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0117, code lost:
        r9 = r9 + 1;
        r10 = r5;
        r14 = r17;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00dd A[ExcHandler: ConnectTimeoutException (e org.apache.http.conn.ConnectTimeoutException), Splitter:B:14:0x0047] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x00f0 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x00fc A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0117 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0117 A[SYNTHETIC] */
    @Override // org.apache.http.conn.ClientConnectionOperator
    public void openConnection(OperatedClientConnection conn, HttpHost target, InetAddress local, HttpContext context, HttpParams params) throws IOException {
        LayeredSocketFactory layered_sf;
        SocketFactory plain_sf;
        SocketFactory plain_sf2;
        InetAddress[] addresses;
        SocketException ex;
        ConnectTimeoutException ex2;
        Socket sock;
        if (conn == null) {
            throw new IllegalArgumentException("Connection must not be null.");
        } else if (target == null) {
            throw new IllegalArgumentException("Target host must not be null.");
        } else if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        } else if (!conn.isOpen()) {
            Scheme schm = this.schemeRegistry.getScheme(target.getSchemeName());
            SocketFactory sf = schm.getSocketFactory();
            if (sf instanceof LayeredSocketFactory) {
                plain_sf = staticPlainSocketFactory;
                layered_sf = (LayeredSocketFactory) sf;
            } else {
                plain_sf = sf;
                layered_sf = null;
            }
            InetAddress[] addresses2 = InetAddress.getAllByName(target.getHostName());
            int i = 0;
            while (i < addresses2.length) {
                Socket sock2 = plain_sf.createSocket();
                conn.opening(sock2, target);
                int i2 = 1;
                try {
                    plain_sf2 = plain_sf;
                    int i3 = i;
                    InetAddress[] addresses3 = addresses2;
                    try {
                        Socket connsock = plain_sf.connectSocket(sock2, addresses2[i].getHostAddress(), schm.resolvePort(target.getPort()), local, 0, params);
                        if (sock2 != connsock) {
                            sock = connsock;
                            try {
                                conn.opening(sock, target);
                            } catch (SocketException e) {
                                ex = e;
                                i = i3;
                                addresses = addresses3;
                                i2 = 1;
                            } catch (ConnectTimeoutException e2) {
                                ex2 = e2;
                                i2 = 1;
                                addresses = addresses3;
                                i = i3;
                                if (i != addresses.length - i2) {
                                }
                            }
                        } else {
                            sock = sock2;
                        }
                    } catch (SocketException e3) {
                        ex = e3;
                        i2 = 1;
                        i = i3;
                        addresses = addresses3;
                        if (i == addresses.length - i2) {
                        }
                    } catch (ConnectTimeoutException e4) {
                        ex2 = e4;
                        i2 = 1;
                        addresses = addresses3;
                        i = i3;
                        if (i != addresses.length - i2) {
                        }
                    }
                    try {
                        prepareSocket(sock, context, params);
                        if (layered_sf != null) {
                            i2 = 1;
                            try {
                                Socket layeredsock = layered_sf.createSocket(sock, target.getHostName(), schm.resolvePort(target.getPort()), true);
                                if (layeredsock != sock) {
                                    conn.opening(layeredsock, target);
                                }
                                conn.openCompleted(sf.isSecure(layeredsock), params);
                            } catch (SocketException e5) {
                                ex = e5;
                                i = i3;
                                addresses = addresses3;
                                if (i == addresses.length - i2) {
                                }
                            } catch (ConnectTimeoutException e6) {
                                ex2 = e6;
                                addresses = addresses3;
                                i = i3;
                                if (i != addresses.length - i2) {
                                }
                            }
                        } else {
                            conn.openCompleted(sf.isSecure(sock), params);
                        }
                        return;
                    } catch (SocketException e7) {
                        ex = e7;
                        i2 = 1;
                        i = i3;
                        addresses = addresses3;
                        if (i == addresses.length - i2) {
                        }
                    } catch (ConnectTimeoutException e8) {
                        ex2 = e8;
                        i2 = 1;
                        addresses = addresses3;
                        i = i3;
                        if (i != addresses.length - i2) {
                        }
                    }
                } catch (SocketException e9) {
                    ex = e9;
                    addresses = addresses2;
                    plain_sf2 = plain_sf;
                    if (i == addresses.length - i2) {
                    }
                } catch (ConnectTimeoutException e10) {
                }
            }
        } else {
            throw new IllegalArgumentException("Connection must not be open.");
        }
    }

    @Override // org.apache.http.conn.ClientConnectionOperator
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
                } catch (ConnectException ex) {
                    throw new HttpHostConnectException(target, ex);
                }
            } else {
                throw new IllegalArgumentException("Target scheme (" + schm.getName() + ") must have layered socket factory.");
            }
        } else {
            throw new IllegalArgumentException("Connection must be open.");
        }
    }

    /* access modifiers changed from: protected */
    public void prepareSocket(Socket sock, HttpContext context, HttpParams params) throws IOException {
        sock.setTcpNoDelay(HttpConnectionParams.getTcpNoDelay(params));
        sock.setSoTimeout(HttpConnectionParams.getSoTimeout(params));
        int linger = HttpConnectionParams.getLinger(params);
        if (linger >= 0) {
            sock.setSoLinger(linger > 0, linger);
        }
    }
}
