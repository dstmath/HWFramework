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
    private static final PlainSocketFactory staticPlainSocketFactory = null;
    protected SchemeRegistry schemeRegistry;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.http.impl.conn.DefaultClientConnectionOperator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.http.impl.conn.DefaultClientConnectionOperator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.conn.DefaultClientConnectionOperator.<clinit>():void");
    }

    public DefaultClientConnectionOperator(SchemeRegistry schemes) {
        if (schemes == null) {
            throw new IllegalArgumentException("Scheme registry must not be null.");
        }
        this.schemeRegistry = schemes;
    }

    public OperatedClientConnection createConnection() {
        return new DefaultClientConnection();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            LayeredSocketFactory layeredSocketFactory;
            Scheme schm = this.schemeRegistry.getScheme(target.getSchemeName());
            SocketFactory sf = schm.getSocketFactory();
            if (sf instanceof LayeredSocketFactory) {
                plain_sf = staticPlainSocketFactory;
                layeredSocketFactory = (LayeredSocketFactory) sf;
            } else {
                plain_sf = sf;
                layeredSocketFactory = null;
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
                    if (layeredSocketFactory != null) {
                        Socket layeredsock = layeredSocketFactory.createSocket(sock, target.getHostName(), schm.resolvePort(target.getPort()), true);
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
                } catch (ConnectTimeoutException ex2) {
                    if (i == addresses.length - 1) {
                        throw ex2;
                    }
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
