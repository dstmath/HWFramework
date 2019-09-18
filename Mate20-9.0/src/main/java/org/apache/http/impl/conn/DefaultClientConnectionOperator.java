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
        if (schemes != null) {
            this.schemeRegistry = schemes;
            return;
        }
        throw new IllegalArgumentException("Scheme registry must not be null.");
    }

    public OperatedClientConnection createConnection() {
        return new DefaultClientConnection();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00cd, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00ce, code lost:
        r4 = r24;
        r17 = r14;
        r14 = r8;
        r5 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00d5, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00d6, code lost:
        r4 = r24;
        r18 = r9;
        r19 = r10;
        r17 = r14;
        r14 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00e8, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00f6, code lost:
        if ((r0 instanceof java.net.ConnectException) != false) goto L_0x00f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00f8, code lost:
        r6 = (java.net.ConnectException) r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00fc, code lost:
        r6 = new java.net.ConnectException(r0.getMessage());
        r6.initCause(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x010d, code lost:
        throw new org.apache.http.conn.HttpHostConnectException(r3, r6);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00d5 A[ExcHandler: ConnectTimeoutException (e org.apache.http.conn.ConnectTimeoutException), Splitter:B:15:0x0046] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x00e8 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x00f4 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x010f A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x010f A[SYNTHETIC] */
    public void openConnection(OperatedClientConnection conn, HttpHost target, InetAddress local, HttpContext context, HttpParams params) throws IOException {
        SocketFactory plain_sf;
        LayeredSocketFactory layered_sf;
        SocketFactory plain_sf2;
        InetAddress[] addresses;
        InetAddress[] addresses2;
        Socket sock;
        OperatedClientConnection operatedClientConnection = conn;
        HttpHost httpHost = target;
        HttpParams httpParams = params;
        if (operatedClientConnection == null) {
            HttpContext httpContext = context;
            throw new IllegalArgumentException("Connection must not be null.");
        } else if (httpHost == null) {
            HttpContext httpContext2 = context;
            throw new IllegalArgumentException("Target host must not be null.");
        } else if (httpParams == null) {
            HttpContext httpContext3 = context;
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
            SocketFactory plain_sf3 = plain_sf;
            LayeredSocketFactory layered_sf2 = layered_sf;
            InetAddress[] addresses3 = InetAddress.getAllByName(target.getHostName());
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 < addresses3.length) {
                    Socket sock2 = plain_sf3.createSocket();
                    operatedClientConnection.opening(sock2, httpHost);
                    int i3 = 1;
                    try {
                        SocketFactory socketFactory = plain_sf3;
                        plain_sf2 = plain_sf3;
                        Socket sock3 = sock2;
                        int i4 = i2;
                        addresses2 = addresses3;
                        try {
                            Socket connsock = socketFactory.connectSocket(sock2, addresses3[i2].getHostAddress(), schm.resolvePort(target.getPort()), local, 0, httpParams);
                            if (sock3 != connsock) {
                                sock = connsock;
                                try {
                                    operatedClientConnection.opening(sock, httpHost);
                                } catch (SocketException e) {
                                    ex = e;
                                    HttpContext httpContext4 = context;
                                    i2 = i4;
                                    addresses = addresses2;
                                    i3 = 1;
                                } catch (ConnectTimeoutException e2) {
                                    ex = e2;
                                    HttpContext httpContext5 = context;
                                    i3 = 1;
                                    addresses = addresses2;
                                    i2 = i4;
                                    if (i2 != addresses.length - i3) {
                                    }
                                }
                            } else {
                                sock = sock3;
                            }
                        } catch (SocketException e3) {
                            ex = e3;
                            HttpContext httpContext6 = context;
                            i3 = 1;
                            Socket socket = sock3;
                            i2 = i4;
                            addresses = addresses2;
                            if (i2 == addresses.length - i3) {
                            }
                        } catch (ConnectTimeoutException e4) {
                            ex = e4;
                            HttpContext httpContext7 = context;
                            i3 = 1;
                            Socket socket2 = sock3;
                            addresses = addresses2;
                            i2 = i4;
                            if (i2 != addresses.length - i3) {
                            }
                        }
                        try {
                            prepareSocket(sock, context, httpParams);
                            if (layered_sf2 == null) {
                                operatedClientConnection.openCompleted(sf.isSecure(sock), httpParams);
                                break;
                            }
                            i3 = 1;
                            try {
                                Socket layeredsock = layered_sf2.createSocket(sock, target.getHostName(), schm.resolvePort(target.getPort()), true);
                                if (layeredsock != sock) {
                                    operatedClientConnection.opening(layeredsock, httpHost);
                                }
                                operatedClientConnection.openCompleted(sf.isSecure(layeredsock), httpParams);
                            } catch (SocketException e5) {
                                ex = e5;
                                i2 = i4;
                                addresses = addresses2;
                                if (i2 == addresses.length - i3) {
                                }
                            } catch (ConnectTimeoutException e6) {
                                ex = e6;
                                addresses = addresses2;
                                i2 = i4;
                                if (i2 != addresses.length - i3) {
                                }
                            }
                        } catch (SocketException e7) {
                            ex = e7;
                            i3 = 1;
                            i2 = i4;
                            addresses = addresses2;
                            if (i2 == addresses.length - i3) {
                            }
                        } catch (ConnectTimeoutException e8) {
                            ex = e8;
                            i3 = 1;
                            addresses = addresses2;
                            i2 = i4;
                            if (i2 != addresses.length - i3) {
                            }
                        }
                    } catch (SocketException e9) {
                        ex = e9;
                        HttpContext httpContext8 = context;
                        addresses = addresses3;
                        plain_sf2 = plain_sf3;
                        Socket socket3 = sock2;
                        if (i2 == addresses.length - i3) {
                        }
                    } catch (ConnectTimeoutException e10) {
                    }
                } else {
                    HttpContext httpContext9 = context;
                    InetAddress[] inetAddressArr = addresses3;
                    SocketFactory socketFactory2 = plain_sf3;
                    return;
                }
                i = i2 + 1;
                addresses3 = addresses;
                plain_sf3 = plain_sf2;
            }
            InetAddress[] inetAddressArr2 = addresses2;
        } else {
            HttpContext httpContext10 = context;
            throw new IllegalArgumentException("Connection must not be open.");
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
        boolean z = false;
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
