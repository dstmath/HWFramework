package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeCrypto.SSLHandshakeCallbacks;
import com.android.org.conscrypt.SSLParametersImpl.AliasChooser;
import com.android.org.conscrypt.SSLParametersImpl.PSKCallbacks;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

public class OpenSSLSocketImpl extends SSLSocket implements SSLHandshakeCallbacks, AliasChooser, PSKCallbacks {
    private static final boolean DBG_STATE = false;
    private static final int STATE_CLOSED = 5;
    private static final int STATE_HANDSHAKE_COMPLETED = 2;
    private static final int STATE_HANDSHAKE_STARTED = 1;
    private static final int STATE_NEW = 0;
    private static final int STATE_READY = 4;
    private static final int STATE_READY_HANDSHAKE_CUT_THROUGH = 3;
    private final boolean autoClose;
    private OpenSSLKey channelIdPrivateKey;
    private final Object guard;
    private AbstractOpenSSLSession handshakeSession;
    private int handshakeTimeoutMilliseconds;
    private SSLInputStream is;
    private ArrayList<HandshakeCompletedListener> listeners;
    private SSLOutputStream os;
    private String peerHostname;
    private final int peerPort;
    private int readTimeoutMilliseconds;
    private final Socket socket;
    private long sslNativePointer;
    private final SSLParametersImpl sslParameters;
    private AbstractOpenSSLSession sslSession;
    private int state;
    private final Object stateLock;
    private int writeTimeoutMilliseconds;

    private class SSLInputStream extends InputStream {
        private final Object readLock = new Object();

        SSLInputStream() {
        }

        public int read() throws IOException {
            byte[] buffer = new byte[1];
            if (read(buffer, 0, 1) != -1) {
                return buffer[0] & 255;
            }
            return -1;
        }

        public int read(byte[] buf, int offset, int byteCount) throws IOException {
            Platform.blockGuardOnNetwork();
            OpenSSLSocketImpl.this.checkOpen();
            ArrayUtils.checkOffsetAndCount(buf.length, offset, byteCount);
            if (byteCount == 0) {
                return 0;
            }
            int SSL_read;
            synchronized (this.readLock) {
                synchronized (OpenSSLSocketImpl.this.stateLock) {
                    if (OpenSSLSocketImpl.this.state == 5) {
                        throw new SocketException("socket is closed");
                    }
                }
                SSL_read = NativeCrypto.SSL_read(OpenSSLSocketImpl.this.sslNativePointer, Platform.getFileDescriptor(OpenSSLSocketImpl.this.socket), OpenSSLSocketImpl.this, buf, offset, byteCount, OpenSSLSocketImpl.this.getSoTimeout());
            }
            return SSL_read;
        }

        public void awaitPendingOps() {
            synchronized (this.readLock) {
            }
        }
    }

    private class SSLOutputStream extends OutputStream {
        private final Object writeLock = new Object();

        SSLOutputStream() {
        }

        public void write(int oneByte) throws IOException {
            write(new byte[]{(byte) (oneByte & 255)});
        }

        public void write(byte[] buf, int offset, int byteCount) throws IOException {
            Platform.blockGuardOnNetwork();
            OpenSSLSocketImpl.this.checkOpen();
            ArrayUtils.checkOffsetAndCount(buf.length, offset, byteCount);
            if (byteCount != 0) {
                synchronized (this.writeLock) {
                    synchronized (OpenSSLSocketImpl.this.stateLock) {
                        if (OpenSSLSocketImpl.this.state == 5) {
                            throw new SocketException("socket is closed");
                        }
                    }
                    NativeCrypto.SSL_write(OpenSSLSocketImpl.this.sslNativePointer, Platform.getFileDescriptor(OpenSSLSocketImpl.this.socket), OpenSSLSocketImpl.this, buf, offset, byteCount, OpenSSLSocketImpl.this.writeTimeoutMilliseconds);
                }
            }
        }

        public void awaitPendingOps() {
            synchronized (this.writeLock) {
            }
        }
    }

    protected OpenSSLSocketImpl(SSLParametersImpl sslParameters) throws IOException {
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = this;
        this.peerHostname = null;
        this.peerPort = -1;
        this.autoClose = DBG_STATE;
        this.sslParameters = sslParameters;
    }

    protected OpenSSLSocketImpl(String hostname, int port, SSLParametersImpl sslParameters) throws IOException {
        super(hostname, port);
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = this;
        this.peerHostname = hostname;
        this.peerPort = port;
        this.autoClose = DBG_STATE;
        this.sslParameters = sslParameters;
    }

    protected OpenSSLSocketImpl(InetAddress address, int port, SSLParametersImpl sslParameters) throws IOException {
        super(address, port);
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = this;
        this.peerHostname = null;
        this.peerPort = -1;
        this.autoClose = DBG_STATE;
        this.sslParameters = sslParameters;
    }

    protected OpenSSLSocketImpl(String hostname, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        super(hostname, port, clientAddress, clientPort);
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = this;
        this.peerHostname = hostname;
        this.peerPort = port;
        this.autoClose = DBG_STATE;
        this.sslParameters = sslParameters;
    }

    protected OpenSSLSocketImpl(InetAddress address, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        super(address, port, clientAddress, clientPort);
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = this;
        this.peerHostname = null;
        this.peerPort = -1;
        this.autoClose = DBG_STATE;
        this.sslParameters = sslParameters;
    }

    protected OpenSSLSocketImpl(Socket socket, String hostname, int port, boolean autoClose, SSLParametersImpl sslParameters) throws IOException {
        this.stateLock = new Object();
        this.state = 0;
        this.guard = Platform.closeGuardGet();
        this.readTimeoutMilliseconds = 0;
        this.writeTimeoutMilliseconds = 0;
        this.handshakeTimeoutMilliseconds = -1;
        this.socket = socket;
        this.peerHostname = hostname;
        this.peerPort = port;
        this.autoClose = autoClose;
        this.sslParameters = sslParameters;
    }

    public void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (this.peerHostname == null && (endpoint instanceof InetSocketAddress)) {
            this.peerHostname = Platform.getHostStringFromInetSocketAddress((InetSocketAddress) endpoint);
        }
        super.connect(endpoint, timeout);
    }

    private void checkOpen() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
    }

    /* JADX WARNING: Missing block: B:7:0x0014, code:
            r2 = r25.sslParameters.getUseClientMode();
            r25.sslNativePointer = 0;
            r19 = true;
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            r25.sslNativePointer = com.android.org.conscrypt.NativeCrypto.SSL_new(r25.sslParameters.getSessionContext().sslCtxNativePointer);
            com.android.org.conscrypt.Platform.closeGuardOpen(r25.guard, "close");
            r15 = getEnableSessionCreation();
     */
    /* JADX WARNING: Missing block: B:10:0x0046, code:
            if (r15 != false) goto L_0x004f;
     */
    /* JADX WARNING: Missing block: B:11:0x0048, code:
            com.android.org.conscrypt.NativeCrypto.SSL_set_session_creation_enabled(r25.sslNativePointer, r15);
     */
    /* JADX WARNING: Missing block: B:12:0x004f, code:
            com.android.org.conscrypt.NativeCrypto.SSL_accept_renegotiations(r25.sslNativePointer);
     */
    /* JADX WARNING: Missing block: B:13:0x0056, code:
            if (r2 == false) goto L_0x0106;
     */
    /* JADX WARNING: Missing block: B:14:0x0058, code:
            com.android.org.conscrypt.NativeCrypto.SSL_set_connect_state(r25.sslNativePointer);
            com.android.org.conscrypt.NativeCrypto.SSL_enable_ocsp_stapling(r25.sslNativePointer);
     */
    /* JADX WARNING: Missing block: B:15:0x0072, code:
            if (r25.sslParameters.isCTVerificationEnabled(getHostname()) == false) goto L_0x007b;
     */
    /* JADX WARNING: Missing block: B:16:0x0074, code:
            com.android.org.conscrypt.NativeCrypto.SSL_enable_signed_cert_timestamps(r25.sslNativePointer);
     */
    /* JADX WARNING: Missing block: B:17:0x007b, code:
            r23 = r25.sslParameters.getSessionToReuse(r25.sslNativePointer, getHostnameOrIP(), getPort());
            r25.sslParameters.setSSLParameters(r25.sslNativePointer, r25, r25, getHostname());
            r25.sslParameters.setCertificateValidation(r25.sslNativePointer);
            r25.sslParameters.setTlsChannelId(r25.sslNativePointer, r25.channelIdPrivateKey);
            r20 = getSoTimeout();
            r21 = getSoWriteTimeout();
     */
    /* JADX WARNING: Missing block: B:18:0x00c8, code:
            if (r25.handshakeTimeoutMilliseconds < 0) goto L_0x00dc;
     */
    /* JADX WARNING: Missing block: B:19:0x00ca, code:
            setSoTimeout(r25.handshakeTimeoutMilliseconds);
            setSoWriteTimeout(r25.handshakeTimeoutMilliseconds);
     */
    /* JADX WARNING: Missing block: B:20:0x00dc, code:
            r3 = r25.stateLock;
     */
    /* JADX WARNING: Missing block: B:21:0x00e0, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:25:0x00e6, code:
            if (r25.state != 5) goto L_0x014e;
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:28:0x00e9, code:
            if (1 == null) goto L_0x0100;
     */
    /* JADX WARNING: Missing block: B:29:0x00eb, code:
            r6 = r25.stateLock;
     */
    /* JADX WARNING: Missing block: B:30:0x00ef, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            r25.state = 5;
            r25.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:34:0x00fc, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:44:?, code:
            com.android.org.conscrypt.NativeCrypto.SSL_set_accept_state(r25.sslNativePointer);
     */
    /* JADX WARNING: Missing block: B:45:0x0115, code:
            if (r25.sslParameters.getOCSPResponse() == null) goto L_0x007b;
     */
    /* JADX WARNING: Missing block: B:46:0x0117, code:
            com.android.org.conscrypt.NativeCrypto.SSL_enable_ocsp_stapling(r25.sslNativePointer);
     */
    /* JADX WARNING: Missing block: B:47:0x0120, code:
            r14 = move-exception;
     */
    /* JADX WARNING: Missing block: B:50:0x012f, code:
            throw ((javax.net.ssl.SSLHandshakeException) new javax.net.ssl.SSLHandshakeException("Handshake failed").initCause(r14));
     */
    /* JADX WARNING: Missing block: B:52:0x0131, code:
            if (r19 != false) goto L_0x0133;
     */
    /* JADX WARNING: Missing block: B:54:0x0137, code:
            monitor-enter(r25.stateLock);
     */
    /* JADX WARNING: Missing block: B:57:?, code:
            r25.state = 5;
            r25.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:60:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:67:?, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:69:?, code:
            com.android.org.conscrypt.NativeCrypto.SSL_do_handshake(r25.sslNativePointer, com.android.org.conscrypt.Platform.getFileDescriptor(r25.socket), r25, getSoTimeout());
            r4 = com.android.org.conscrypt.NativeCrypto.SSL_get1_session(r25.sslNativePointer);
     */
    /* JADX WARNING: Missing block: B:70:0x016b, code:
            r11 = DBG_STATE;
     */
    /* JADX WARNING: Missing block: B:72:?, code:
            r6 = r25.stateLock;
     */
    /* JADX WARNING: Missing block: B:73:0x0171, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:77:0x0177, code:
            if (r25.state != 2) goto L_0x0252;
     */
    /* JADX WARNING: Missing block: B:78:0x0179, code:
            r11 = true;
     */
    /* JADX WARNING: Missing block: B:80:?, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:81:0x017b, code:
            r25.sslSession = r25.sslParameters.setupSession(r4, r25.sslNativePointer, r23, getHostnameOrIP(), getPort(), r11);
     */
    /* JADX WARNING: Missing block: B:82:0x0199, code:
            if (r25.handshakeTimeoutMilliseconds < 0) goto L_0x01a9;
     */
    /* JADX WARNING: Missing block: B:83:0x019b, code:
            setSoTimeout(r20);
            setSoWriteTimeout(r21);
     */
    /* JADX WARNING: Missing block: B:84:0x01a9, code:
            if (r11 == false) goto L_0x01ae;
     */
    /* JADX WARNING: Missing block: B:85:0x01ab, code:
            notifyHandshakeCompletedListeners();
     */
    /* JADX WARNING: Missing block: B:86:0x01ae, code:
            r6 = r25.stateLock;
     */
    /* JADX WARNING: Missing block: B:87:0x01b2, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:90:0x01b8, code:
            if (r25.state != 5) goto L_0x027a;
     */
    /* JADX WARNING: Missing block: B:91:0x01ba, code:
            r19 = true;
     */
    /* JADX WARNING: Missing block: B:93:0x01c1, code:
            if (r25.state != 1) goto L_0x027e;
     */
    /* JADX WARNING: Missing block: B:94:0x01c3, code:
            r25.state = 3;
     */
    /* JADX WARNING: Missing block: B:95:0x01c8, code:
            if (r19 != false) goto L_0x01d1;
     */
    /* JADX WARNING: Missing block: B:96:0x01ca, code:
            r25.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:98:?, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:99:0x01d2, code:
            if (r19 == false) goto L_0x01e9;
     */
    /* JADX WARNING: Missing block: B:100:0x01d4, code:
            r6 = r25.stateLock;
     */
    /* JADX WARNING: Missing block: B:101:0x01d8, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:104:?, code:
            r25.state = 5;
            r25.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:105:0x01e5, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:107:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:113:0x01ed, code:
            r13 = move-exception;
     */
    /* JADX WARNING: Missing block: B:115:0x01f2, code:
            monitor-enter(r25.stateLock);
     */
    /* JADX WARNING: Missing block: B:119:0x01f8, code:
            if (r25.state == 5) goto L_0x01fa;
     */
    /* JADX WARNING: Missing block: B:122:0x01fb, code:
            if (1 != null) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:124:0x0201, code:
            monitor-enter(r25.stateLock);
     */
    /* JADX WARNING: Missing block: B:127:?, code:
            r25.state = 5;
            r25.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:130:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:131:0x0212, code:
            return;
     */
    /* JADX WARNING: Missing block: B:139:0x0226, code:
            if (r13.getMessage().contains("unexpected CCS") != false) goto L_0x0228;
     */
    /* JADX WARNING: Missing block: B:140:0x0228, code:
            com.android.org.conscrypt.Platform.logEvent(java.lang.String.format("ssl_unexpected_ccs: host=%s", new java.lang.Object[]{getHostnameOrIP()}));
     */
    /* JADX WARNING: Missing block: B:141:0x023c, code:
            throw r13;
     */
    /* JADX WARNING: Missing block: B:145:0x0240, code:
            r12 = move-exception;
     */
    /* JADX WARNING: Missing block: B:146:0x0241, code:
            r0 = new javax.net.ssl.SSLHandshakeException(r12.getMessage());
            r0.initCause(r12);
     */
    /* JADX WARNING: Missing block: B:147:0x0251, code:
            throw r0;
     */
    /* JADX WARNING: Missing block: B:151:0x0257, code:
            if (r25.state != 5) goto L_0x017a;
     */
    /* JADX WARNING: Missing block: B:153:?, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:154:0x025a, code:
            if (1 == null) goto L_0x0271;
     */
    /* JADX WARNING: Missing block: B:155:0x025c, code:
            r6 = r25.stateLock;
     */
    /* JADX WARNING: Missing block: B:156:0x0260, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:159:?, code:
            r25.state = 5;
            r25.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:160:0x026d, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:162:?, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:172:0x027a, code:
            r19 = DBG_STATE;
     */
    /* JADX WARNING: Missing block: B:175:0x0283, code:
            if (r25.state != 2) goto L_0x01c8;
     */
    /* JADX WARNING: Missing block: B:176:0x0285, code:
            r25.state = 4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startHandshake() throws IOException {
        checkOpen();
        synchronized (this.stateLock) {
            if (this.state == 0) {
                this.state = 1;
            }
        }
    }

    public String getHostname() {
        return this.peerHostname;
    }

    public String getHostnameOrIP() {
        if (this.peerHostname != null) {
            return this.peerHostname;
        }
        InetAddress peerAddress = getInetAddress();
        if (peerAddress != null) {
            return peerAddress.getHostAddress();
        }
        return null;
    }

    public int getPort() {
        return this.peerPort == -1 ? super.getPort() : this.peerPort;
    }

    public void clientCertificateRequested(byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals) throws CertificateEncodingException, SSLException {
        this.sslParameters.chooseClientCertificate(keyTypeBytes, asn1DerEncodedPrincipals, this.sslNativePointer, this);
    }

    public int clientPSKKeyRequested(String identityHint, byte[] identity, byte[] key) {
        return this.sslParameters.clientPSKKeyRequested(identityHint, identity, key, this);
    }

    public int serverPSKKeyRequested(String identityHint, String identity, byte[] key) {
        return this.sslParameters.serverPSKKeyRequested(identityHint, identity, key, this);
    }

    /* JADX WARNING: Missing block: B:16:0x0018, code:
            r4.sslSession.resetId();
     */
    /* JADX WARNING: Missing block: B:17:0x0023, code:
            if (r4.sslParameters.getUseClientMode() == false) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:18:0x0025, code:
            r0 = r4.sslParameters.getClientSessionContext();
     */
    /* JADX WARNING: Missing block: B:19:0x002b, code:
            r0.putSession(r4.sslSession);
            notifyHandshakeCompletedListeners();
            r2 = r4.stateLock;
     */
    /* JADX WARNING: Missing block: B:20:0x0035, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            r4.state = 4;
            r4.stateLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:24:0x003e, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            return;
     */
    /* JADX WARNING: Missing block: B:35:0x004a, code:
            r0 = r4.sslParameters.getServerSessionContext();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onSSLStateChange(int type, int val) {
        if (type == 32) {
            synchronized (this.stateLock) {
                if (this.state == 1) {
                    this.state = 2;
                } else if (this.state != 3) {
                    if (this.state == 5) {
                    }
                }
            }
        }
    }

    void notifyHandshakeCompletedListeners() {
        if (this.listeners != null && (this.listeners.isEmpty() ^ 1) != 0) {
            HandshakeCompletedEvent event = new HandshakeCompletedEvent(this, this.sslSession);
            for (HandshakeCompletedListener listener : this.listeners) {
                try {
                    listener.handshakeCompleted(event);
                } catch (RuntimeException e) {
                    Thread thread = Thread.currentThread();
                    thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
                }
            }
        }
    }

    public void verifyCertificateChain(long[] certRefs, String authMethod) throws CertificateException {
        try {
            X509TrustManager x509tm = this.sslParameters.getX509TrustManager();
            if (x509tm == null) {
                throw new CertificateException("No X.509 TrustManager");
            }
            if (certRefs != null) {
                if (certRefs.length != 0) {
                    X509Certificate[] peerCertChain = OpenSSLX509Certificate.createCertChain(certRefs);
                    this.handshakeSession = new OpenSSLSessionImpl(NativeCrypto.SSL_get1_session(this.sslNativePointer), null, peerCertChain, NativeCrypto.SSL_get_ocsp_response(this.sslNativePointer), NativeCrypto.SSL_get_signed_cert_timestamp_list(this.sslNativePointer), getHostnameOrIP(), getPort(), null);
                    if (this.sslParameters.getUseClientMode()) {
                        Platform.checkServerTrusted(x509tm, peerCertChain, authMethod, this);
                    } else {
                        Platform.checkClientTrusted(x509tm, peerCertChain, peerCertChain[0].getPublicKey().getAlgorithm(), this);
                    }
                    this.handshakeSession = null;
                    return;
                }
            }
            throw new SSLException("Peer sent no certificate");
        } catch (CertificateException e) {
            throw e;
        } catch (Throwable e2) {
            throw new CertificateException(e2);
        } catch (Throwable th) {
            this.handshakeSession = null;
        }
    }

    public InputStream getInputStream() throws IOException {
        InputStream returnVal;
        checkOpen();
        synchronized (this.stateLock) {
            if (this.state == 5) {
                throw new SocketException("Socket is closed.");
            }
            if (this.is == null) {
                this.is = new SSLInputStream();
            }
            returnVal = this.is;
        }
        waitForHandshake();
        return returnVal;
    }

    public OutputStream getOutputStream() throws IOException {
        OutputStream returnVal;
        checkOpen();
        synchronized (this.stateLock) {
            if (this.state == 5) {
                throw new SocketException("Socket is closed.");
            }
            if (this.os == null) {
                this.os = new SSLOutputStream();
            }
            returnVal = this.os;
        }
        waitForHandshake();
        return returnVal;
    }

    private void assertReadableOrWriteableState() {
        if (this.state != 4 && this.state != 3) {
            throw new AssertionError("Invalid state: " + this.state);
        }
    }

    private void waitForHandshake() throws IOException {
        startHandshake();
        synchronized (this.stateLock) {
            while (this.state != 4 && this.state != 3 && this.state != 5) {
                try {
                    this.stateLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    IOException ioe = new IOException("Interrupted waiting for handshake");
                    ioe.initCause(e);
                    throw ioe;
                }
            }
            if (this.state == 5) {
                throw new SocketException("Socket is closed");
            }
        }
    }

    public SSLSession getSession() {
        if (this.sslSession == null) {
            try {
                waitForHandshake();
            } catch (IOException e) {
                return SSLNullSession.getNullSession();
            }
        }
        return Platform.wrapSSLSession(this.sslSession);
    }

    public SSLSession getHandshakeSession() {
        return this.handshakeSession;
    }

    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Provided listener is null");
        }
        if (this.listeners == null) {
            this.listeners = new ArrayList();
        }
        this.listeners.add(listener);
    }

    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Provided listener is null");
        } else if (this.listeners == null) {
            throw new IllegalArgumentException("Provided listener is not registered");
        } else if (!this.listeners.remove(listener)) {
            throw new IllegalArgumentException("Provided listener is not registered");
        }
    }

    public boolean getEnableSessionCreation() {
        return this.sslParameters.getEnableSessionCreation();
    }

    public void setEnableSessionCreation(boolean flag) {
        this.sslParameters.setEnableSessionCreation(flag);
    }

    public String[] getSupportedCipherSuites() {
        return NativeCrypto.getSupportedCipherSuites();
    }

    public String[] getEnabledCipherSuites() {
        return this.sslParameters.getEnabledCipherSuites();
    }

    public void setEnabledCipherSuites(String[] suites) {
        this.sslParameters.setEnabledCipherSuites(suites);
    }

    public String[] getSupportedProtocols() {
        return NativeCrypto.getSupportedProtocols();
    }

    public String[] getEnabledProtocols() {
        return this.sslParameters.getEnabledProtocols();
    }

    public void setEnabledProtocols(String[] protocols) {
        this.sslParameters.setEnabledProtocols(protocols);
    }

    public void setUseSessionTickets(boolean useSessionTickets) {
        this.sslParameters.setUseSessionTickets(useSessionTickets);
    }

    public void setHostname(String hostname) {
        this.sslParameters.setUseSni(hostname != null ? true : DBG_STATE);
        this.peerHostname = hostname;
    }

    public void setChannelIdEnabled(boolean enabled) {
        if (getUseClientMode()) {
            throw new IllegalStateException("Client mode");
        }
        synchronized (this.stateLock) {
            if (this.state != 0) {
                throw new IllegalStateException("Could not enable/disable Channel ID after the initial handshake has begun.");
            }
        }
        this.sslParameters.channelIdEnabled = enabled;
    }

    public byte[] getChannelId() throws SSLException {
        if (getUseClientMode()) {
            throw new IllegalStateException("Client mode");
        }
        synchronized (this.stateLock) {
            if (this.state != 4) {
                throw new IllegalStateException("Channel ID is only available after handshake completes");
            }
        }
        return NativeCrypto.SSL_get_tls_channel_id(this.sslNativePointer);
    }

    public void setChannelIdPrivateKey(PrivateKey privateKey) {
        if (getUseClientMode()) {
            synchronized (this.stateLock) {
                if (this.state != 0) {
                    throw new IllegalStateException("Could not change Channel ID private key after the initial handshake has begun.");
                }
            }
            if (privateKey == null) {
                this.sslParameters.channelIdEnabled = DBG_STATE;
                this.channelIdPrivateKey = null;
                return;
            }
            this.sslParameters.channelIdEnabled = true;
            ECParameterSpec ecParams = null;
            try {
                if (privateKey instanceof ECKey) {
                    ecParams = ((ECKey) privateKey).getParams();
                }
                if (ecParams == null) {
                    ecParams = OpenSSLECGroupContext.getCurveByName("prime256v1").getECParameterSpec();
                }
                this.channelIdPrivateKey = OpenSSLKey.fromECPrivateKeyForTLSStackOnly(privateKey, ecParams);
                return;
            } catch (InvalidKeyException e) {
                return;
            }
        }
        throw new IllegalStateException("Server mode");
    }

    public boolean getUseClientMode() {
        return this.sslParameters.getUseClientMode();
    }

    public void setUseClientMode(boolean mode) {
        synchronized (this.stateLock) {
            if (this.state != 0) {
                throw new IllegalArgumentException("Could not change the mode after the initial handshake has begun.");
            }
        }
        this.sslParameters.setUseClientMode(mode);
    }

    public boolean getWantClientAuth() {
        return this.sslParameters.getWantClientAuth();
    }

    public boolean getNeedClientAuth() {
        return this.sslParameters.getNeedClientAuth();
    }

    public void setNeedClientAuth(boolean need) {
        this.sslParameters.setNeedClientAuth(need);
    }

    public void setWantClientAuth(boolean want) {
        this.sslParameters.setWantClientAuth(want);
    }

    public void sendUrgentData(int data) throws IOException {
        throw new SocketException("Method sendUrgentData() is not supported.");
    }

    public void setOOBInline(boolean on) throws SocketException {
        throw new SocketException("Methods sendUrgentData, setOOBInline are not supported.");
    }

    public void setSoTimeout(int readTimeoutMilliseconds) throws SocketException {
        if (this.socket != this) {
            this.socket.setSoTimeout(readTimeoutMilliseconds);
        } else {
            super.setSoTimeout(readTimeoutMilliseconds);
        }
        this.readTimeoutMilliseconds = readTimeoutMilliseconds;
    }

    public int getSoTimeout() throws SocketException {
        return this.readTimeoutMilliseconds;
    }

    public void setSoWriteTimeout(int writeTimeoutMilliseconds) throws SocketException {
        this.writeTimeoutMilliseconds = writeTimeoutMilliseconds;
        Platform.setSocketWriteTimeout(this, (long) writeTimeoutMilliseconds);
    }

    public int getSoWriteTimeout() throws SocketException {
        return this.writeTimeoutMilliseconds;
    }

    public void setHandshakeTimeout(int handshakeTimeoutMilliseconds) throws SocketException {
        this.handshakeTimeoutMilliseconds = handshakeTimeoutMilliseconds;
    }

    /* JADX WARNING: Missing block: B:24:0x0039, code:
            if (r1 != null) goto L_0x003d;
     */
    /* JADX WARNING: Missing block: B:25:0x003b, code:
            if (r2 == null) goto L_0x0042;
     */
    /* JADX WARNING: Missing block: B:26:0x003d, code:
            com.android.org.conscrypt.NativeCrypto.SSL_interrupt(r8.sslNativePointer);
     */
    /* JADX WARNING: Missing block: B:27:0x0042, code:
            if (r1 == null) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:28:0x0044, code:
            r1.awaitPendingOps();
     */
    /* JADX WARNING: Missing block: B:29:0x0047, code:
            if (r2 == null) goto L_0x004c;
     */
    /* JADX WARNING: Missing block: B:30:0x0049, code:
            r2.awaitPendingOps();
     */
    /* JADX WARNING: Missing block: B:31:0x004c, code:
            shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Missing block: B:32:0x004f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() throws IOException {
        synchronized (this.stateLock) {
            if (this.state == 5) {
                return;
            }
            int oldState = this.state;
            this.state = 5;
            if (oldState == 0) {
                closeUnderlyingSocket();
                this.stateLock.notifyAll();
            } else if (oldState == 4 || oldState == 3) {
                this.stateLock.notifyAll();
                SSLInputStream sslInputStream = this.is;
                SSLOutputStream sslOutputStream = this.os;
            } else {
                NativeCrypto.SSL_interrupt(this.sslNativePointer);
                this.stateLock.notifyAll();
            }
        }
    }

    private void shutdownAndFreeSslNative() throws IOException {
        try {
            Platform.blockGuardOnNetwork();
            NativeCrypto.SSL_shutdown(this.sslNativePointer, Platform.getFileDescriptor(this.socket), this);
        } catch (IOException e) {
        } finally {
            free();
            closeUnderlyingSocket();
        }
    }

    private void closeUnderlyingSocket() throws IOException {
        if (this.socket != this) {
            if (this.autoClose && (this.socket.isClosed() ^ 1) != 0) {
                this.socket.close();
            }
        } else if (!super.isClosed()) {
            super.close();
        }
    }

    private void free() {
        if (this.sslNativePointer != 0) {
            NativeCrypto.SSL_free(this.sslNativePointer);
            this.sslNativePointer = 0;
            Platform.closeGuardClose(this.guard);
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                Platform.closeGuardWarnIfOpen(this.guard);
            }
            free();
        } finally {
            super.finalize();
        }
    }

    public FileDescriptor getFileDescriptor$() {
        if (this.socket == this) {
            return Platform.getFileDescriptorFromSSLSocket(this);
        }
        return Platform.getFileDescriptor(this.socket);
    }

    public byte[] getNpnSelectedProtocol() {
        return null;
    }

    public byte[] getAlpnSelectedProtocol() {
        return NativeCrypto.SSL_get0_alpn_selected(this.sslNativePointer);
    }

    public void setNpnProtocols(byte[] npnProtocols) {
    }

    public void setAlpnProtocols(String[] alpnProtocols) {
        this.sslParameters.setAlpnProtocols(alpnProtocols);
    }

    public void setAlpnProtocols(byte[] alpnProtocols) {
        this.sslParameters.setAlpnProtocols(alpnProtocols);
    }

    public SSLParameters getSSLParameters() {
        SSLParameters params = super.getSSLParameters();
        Platform.getSSLParameters(params, this.sslParameters, this);
        return params;
    }

    public void setSSLParameters(SSLParameters p) {
        super.setSSLParameters(p);
        Platform.setSSLParameters(p, this.sslParameters, this);
    }

    public String chooseServerAlias(X509KeyManager keyManager, String keyType) {
        return keyManager.chooseServerAlias(keyType, null, this);
    }

    public String chooseClientAlias(X509KeyManager keyManager, X500Principal[] issuers, String[] keyTypes) {
        return keyManager.chooseClientAlias(keyTypes, null, this);
    }

    public String chooseServerPSKIdentityHint(PSKKeyManager keyManager) {
        return keyManager.chooseServerKeyIdentityHint((Socket) this);
    }

    public String chooseClientPSKIdentity(PSKKeyManager keyManager, String identityHint) {
        return keyManager.chooseClientKeyIdentity(identityHint, (Socket) this);
    }

    public SecretKey getPSKKey(PSKKeyManager keyManager, String identityHint, String identity) {
        return keyManager.getKey(identityHint, identity, (Socket) this);
    }
}
