package com.android.org.conscrypt;

import com.android.org.conscrypt.ExternalSession;
import com.android.org.conscrypt.NativeCrypto;
import com.android.org.conscrypt.NativeRef;
import com.android.org.conscrypt.SSLParametersImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

class ConscryptFileDescriptorSocket extends OpenSSLSocketImpl implements NativeCrypto.SSLHandshakeCallbacks, SSLParametersImpl.AliasChooser, SSLParametersImpl.PSKCallbacks {
    private static final boolean DBG_STATE = false;
    private final ActiveSession activeSession;
    private OpenSSLKey channelIdPrivateKey;
    private SessionSnapshot closedSession;
    private final SSLSession externalSession = Platform.wrapSSLSession(new ExternalSession(new ExternalSession.Provider() {
        public ConscryptSession provideSession() {
            return ConscryptFileDescriptorSocket.this.provideSession();
        }
    }));
    private final Object guard = Platform.closeGuardGet();
    private int handshakeTimeoutMilliseconds = -1;
    private SSLInputStream is;
    private SSLOutputStream os;
    /* access modifiers changed from: private */
    public final NativeSsl ssl;
    private final SSLParametersImpl sslParameters;
    /* access modifiers changed from: private */
    public int state = 0;
    /* access modifiers changed from: private */
    public int writeTimeoutMilliseconds = 0;

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
            int ret;
            Platform.blockGuardOnNetwork();
            ConscryptFileDescriptorSocket.this.checkOpen();
            ArrayUtils.checkOffsetAndCount(buf.length, offset, byteCount);
            if (byteCount == 0) {
                return 0;
            }
            synchronized (this.readLock) {
                synchronized (ConscryptFileDescriptorSocket.this.ssl) {
                    if (ConscryptFileDescriptorSocket.this.state == 8) {
                        throw new SocketException("socket is closed");
                    }
                }
                ret = ConscryptFileDescriptorSocket.this.ssl.read(Platform.getFileDescriptor(ConscryptFileDescriptorSocket.this.socket), buf, offset, byteCount, ConscryptFileDescriptorSocket.this.getSoTimeout());
                if (ret == -1) {
                    synchronized (ConscryptFileDescriptorSocket.this.ssl) {
                        if (ConscryptFileDescriptorSocket.this.state == 8) {
                            throw new SocketException("socket is closed");
                        }
                    }
                }
            }
            return ret;
        }

        /* access modifiers changed from: package-private */
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
            ConscryptFileDescriptorSocket.this.checkOpen();
            ArrayUtils.checkOffsetAndCount(buf.length, offset, byteCount);
            if (byteCount != 0) {
                synchronized (this.writeLock) {
                    synchronized (ConscryptFileDescriptorSocket.this.ssl) {
                        if (ConscryptFileDescriptorSocket.this.state == 8) {
                            throw new SocketException("socket is closed");
                        }
                    }
                    ConscryptFileDescriptorSocket.this.ssl.write(Platform.getFileDescriptor(ConscryptFileDescriptorSocket.this.socket), buf, offset, byteCount, ConscryptFileDescriptorSocket.this.writeTimeoutMilliseconds);
                    synchronized (ConscryptFileDescriptorSocket.this.ssl) {
                        if (ConscryptFileDescriptorSocket.this.state == 8) {
                            throw new SocketException("socket is closed");
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void awaitPendingOps() {
            synchronized (this.writeLock) {
            }
        }
    }

    ConscryptFileDescriptorSocket(SSLParametersImpl sslParameters2) throws IOException {
        this.sslParameters = sslParameters2;
        this.ssl = newSsl(sslParameters2, this);
        this.activeSession = new ActiveSession(this.ssl, sslParameters2.getSessionContext());
    }

    ConscryptFileDescriptorSocket(String hostname, int port, SSLParametersImpl sslParameters2) throws IOException {
        super(hostname, port);
        this.sslParameters = sslParameters2;
        this.ssl = newSsl(sslParameters2, this);
        this.activeSession = new ActiveSession(this.ssl, sslParameters2.getSessionContext());
    }

    ConscryptFileDescriptorSocket(InetAddress address, int port, SSLParametersImpl sslParameters2) throws IOException {
        super(address, port);
        this.sslParameters = sslParameters2;
        this.ssl = newSsl(sslParameters2, this);
        this.activeSession = new ActiveSession(this.ssl, sslParameters2.getSessionContext());
    }

    ConscryptFileDescriptorSocket(String hostname, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters2) throws IOException {
        super(hostname, port, clientAddress, clientPort);
        this.sslParameters = sslParameters2;
        this.ssl = newSsl(sslParameters2, this);
        this.activeSession = new ActiveSession(this.ssl, sslParameters2.getSessionContext());
    }

    ConscryptFileDescriptorSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters2) throws IOException {
        super(address, port, clientAddress, clientPort);
        this.sslParameters = sslParameters2;
        this.ssl = newSsl(sslParameters2, this);
        this.activeSession = new ActiveSession(this.ssl, sslParameters2.getSessionContext());
    }

    ConscryptFileDescriptorSocket(Socket socket, String hostname, int port, boolean autoClose, SSLParametersImpl sslParameters2) throws IOException {
        super(socket, hostname, port, autoClose);
        this.sslParameters = sslParameters2;
        this.ssl = newSsl(sslParameters2, this);
        this.activeSession = new ActiveSession(this.ssl, sslParameters2.getSessionContext());
    }

    private static NativeSsl newSsl(SSLParametersImpl sslParameters2, ConscryptFileDescriptorSocket engine) throws SSLException {
        return NativeSsl.newInstance(sslParameters2, engine, engine, engine);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:101:?, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:?, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x0103, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0029, code lost:
        if (getUseClientMode() == false) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0106, code lost:
        monitor-enter(r10.ssl);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0109, code lost:
        if (r10.state == 8) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x010c, code lost:
        if (1 != 0) goto L_0x010e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x0110, code lost:
        monitor-enter(r10.ssl);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002b, code lost:
        r4 = clientSessionContext().getCachedSession(getHostnameOrIP(), getPort(), r10.sslParameters);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:?, code lost:
        transitionTo(8);
        r10.ssl.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:?, code lost:
        shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x0123, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003d, code lost:
        if (r4 == null) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x012f, code lost:
        if (r1.getMessage().contains("unexpected CCS") != false) goto L_0x0131;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x0131, code lost:
        com.android.org.conscrypt.Platform.logEvent(java.lang.String.format("ssl_unexpected_ccs: host=%s", new java.lang.Object[]{getHostnameOrIP()}));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x0142, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003f, code lost:
        r4.offerToResume(r10.ssl);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x0146, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x0147, code lost:
        r1 = new javax.net.ssl.SSLHandshakeException(r0.getMessage());
        r1.initCause(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x0153, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0044, code lost:
        r4 = getSoTimeout();
        r5 = getSoWriteTimeout();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x0157, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x0159, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x0167, code lost:
        throw ((javax.net.ssl.SSLHandshakeException) new javax.net.ssl.SSLHandshakeException("Handshake failed").initCause(r0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x0168, code lost:
        if (r2 != false) goto L_0x016a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x016c, code lost:
        monitor-enter(r10.ssl);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:?, code lost:
        transitionTo(8);
        r10.ssl.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004e, code lost:
        if (r10.handshakeTimeoutMilliseconds < 0) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:?, code lost:
        shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x017f, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0050, code lost:
        setSoTimeout(r10.handshakeTimeoutMilliseconds);
        setSoWriteTimeout(r10.handshakeTimeoutMilliseconds);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005a, code lost:
        r6 = r10.ssl;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005c, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005f, code lost:
        if (r10.state != 8) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0061, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0062, code lost:
        if (1 == 0) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0064, code lost:
        r0 = r10.ssl;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0066, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        transitionTo(8);
        r10.ssl.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006f, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        r10.ssl.doHandshake(com.android.org.conscrypt.Platform.getFileDescriptor(r10.socket), getSoTimeout());
        r10.activeSession.onPeerCertificateAvailable(getHostnameOrIP(), getPort());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r7 = r10.ssl;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x009b, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x009e, code lost:
        if (r10.state != 8) goto L_0x00b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00a0, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00a1, code lost:
        if (1 == 0) goto L_0x00b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a3, code lost:
        r0 = r10.ssl;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00a5, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        transitionTo(8);
        r10.ssl.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ae, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:?, code lost:
        shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:?, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00bc, code lost:
        if (r10.handshakeTimeoutMilliseconds < 0) goto L_0x00c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00be, code lost:
        setSoTimeout(r4);
        setSoWriteTimeout(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00c4, code lost:
        r7 = r10.ssl;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00c6, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x00c9, code lost:
        if (r10.state != 8) goto L_0x00cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x00cc, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x00cf, code lost:
        if (r10.state != 2) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x00d1, code lost:
        transitionTo(4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x00d6, code lost:
        transitionTo(5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000f, code lost:
        r0 = true;
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x00da, code lost:
        if (r0 != false) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x00dc, code lost:
        r10.ssl.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x00e1, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x00e2, code lost:
        if (r0 == false) goto L_0x00f9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x00e4, code lost:
        r1 = r10.ssl;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x00e6, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:?, code lost:
        transitionTo(8);
        r10.ssl.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x00ef, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:?, code lost:
        shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x00fa, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x00fb, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x00fd, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:?, code lost:
        com.android.org.conscrypt.Platform.closeGuardOpen(r10.guard, "close");
        r10.ssl.initialize(getHostname(), r10.channelIdPrivateKey);
     */
    public final void startHandshake() throws IOException {
        checkOpen();
        synchronized (this.ssl) {
            if (this.state == 0) {
                transitionTo(2);
            }
        }
    }

    public final void clientCertificateRequested(byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals) throws CertificateEncodingException, SSLException {
        this.ssl.chooseClientCertificate(keyTypeBytes, asn1DerEncodedPrincipals);
    }

    public final int clientPSKKeyRequested(String identityHint, byte[] identity, byte[] key) {
        return this.ssl.clientPSKKeyRequested(identityHint, identity, key);
    }

    public final int serverPSKKeyRequested(String identityHint, String identity, byte[] key) {
        return this.ssl.serverPSKKeyRequested(identityHint, identity, key);
    }

    public final void onSSLStateChange(int type, int val) {
        if (type == 32) {
            synchronized (this.ssl) {
                if (this.state != 8) {
                    transitionTo(5);
                    notifyHandshakeCompletedListeners();
                    synchronized (this.ssl) {
                        this.ssl.notifyAll();
                    }
                }
            }
        }
    }

    public final void onNewSessionEstablished(long sslSessionNativePtr) {
        try {
            NativeCrypto.SSL_SESSION_up_ref(sslSessionNativePtr);
            sessionContext().cacheSession(NativeSslSession.newInstance(new NativeRef.SSL_SESSION(sslSessionNativePtr), this.activeSession));
        } catch (Exception e) {
        }
    }

    public final long serverSessionRequested(byte[] id) {
        return 0;
    }

    public final void verifyCertificateChain(byte[][] certChain, String authMethod) throws CertificateException {
        if (certChain != null) {
            try {
                if (certChain.length != 0) {
                    X509Certificate[] peerCertChain = SSLUtils.decodeX509CertificateChain(certChain);
                    X509TrustManager x509tm = this.sslParameters.getX509TrustManager();
                    if (x509tm != null) {
                        this.activeSession.onPeerCertificatesReceived(getHostnameOrIP(), getPort(), peerCertChain);
                        if (getUseClientMode()) {
                            Platform.checkServerTrusted(x509tm, peerCertChain, authMethod, (AbstractConscryptSocket) this);
                            return;
                        } else {
                            Platform.checkClientTrusted(x509tm, peerCertChain, peerCertChain[0].getPublicKey().getAlgorithm(), (AbstractConscryptSocket) this);
                            return;
                        }
                    } else {
                        throw new CertificateException("No X.509 TrustManager");
                    }
                }
            } catch (CertificateException e) {
                throw e;
            } catch (Exception e2) {
                throw new CertificateException(e2);
            }
        }
        throw new CertificateException("Peer sent no certificate");
    }

    public final InputStream getInputStream() throws IOException {
        InputStream returnVal;
        checkOpen();
        synchronized (this.ssl) {
            if (this.state != 8) {
                if (this.is == null) {
                    this.is = new SSLInputStream();
                }
                returnVal = this.is;
            } else {
                throw new SocketException("Socket is closed.");
            }
        }
        waitForHandshake();
        return returnVal;
    }

    public final OutputStream getOutputStream() throws IOException {
        OutputStream returnVal;
        checkOpen();
        synchronized (this.ssl) {
            if (this.state != 8) {
                if (this.os == null) {
                    this.os = new SSLOutputStream();
                }
                returnVal = this.os;
            } else {
                throw new SocketException("Socket is closed.");
            }
        }
        waitForHandshake();
        return returnVal;
    }

    private void assertReadableOrWriteableState() {
        if (this.state != 5 && this.state != 4) {
            throw new AssertionError("Invalid state: " + this.state);
        }
    }

    private void waitForHandshake() throws IOException {
        startHandshake();
        synchronized (this.ssl) {
            while (this.state != 5 && this.state != 4 && this.state != 8) {
                try {
                    this.ssl.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted waiting for handshake", e);
                }
            }
            if (this.state == 8) {
                throw new SocketException("Socket is closed");
            }
        }
    }

    public final SSLSession getSession() {
        return this.externalSession;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x002f, code lost:
        if (r0 != false) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0035, code lost:
        return com.android.org.conscrypt.SSLNullSession.getNullSession();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0038, code lost:
        return r4.activeSession;
     */
    public ConscryptSession provideSession() {
        boolean handshakeCompleted = DBG_STATE;
        synchronized (this.ssl) {
            if (this.state == 8) {
                ConscryptSession nullSession = this.closedSession != null ? this.closedSession : SSLNullSession.getNullSession();
            } else {
                try {
                    handshakeCompleted = this.state >= 5 ? true : DBG_STATE;
                    if (!handshakeCompleted && isConnected()) {
                        waitForHandshake();
                        handshakeCompleted = true;
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public ConscryptSession provideHandshakeSession() {
        ConscryptSession conscryptSession;
        synchronized (this.ssl) {
            if (this.state < 2 || this.state >= 5) {
                conscryptSession = SSLNullSession.getNullSession();
            } else {
                conscryptSession = this.activeSession;
            }
        }
        return conscryptSession;
    }

    /* access modifiers changed from: package-private */
    public final SSLSession getActiveSession() {
        return this.activeSession;
    }

    public final SSLSession getHandshakeSession() {
        synchronized (this.ssl) {
            if (this.state < 2 || this.state >= 5) {
                return null;
            }
            SSLSession wrapSSLSession = Platform.wrapSSLSession(new ExternalSession(new ExternalSession.Provider() {
                public ConscryptSession provideSession() {
                    return ConscryptFileDescriptorSocket.this.provideHandshakeSession();
                }
            }));
            return wrapSSLSession;
        }
    }

    public final boolean getEnableSessionCreation() {
        return this.sslParameters.getEnableSessionCreation();
    }

    public final void setEnableSessionCreation(boolean flag) {
        this.sslParameters.setEnableSessionCreation(flag);
    }

    public final String[] getSupportedCipherSuites() {
        return NativeCrypto.getSupportedCipherSuites();
    }

    public final String[] getEnabledCipherSuites() {
        return this.sslParameters.getEnabledCipherSuites();
    }

    public final void setEnabledCipherSuites(String[] suites) {
        this.sslParameters.setEnabledCipherSuites(suites);
    }

    public final String[] getSupportedProtocols() {
        return NativeCrypto.getSupportedProtocols();
    }

    public final String[] getEnabledProtocols() {
        return this.sslParameters.getEnabledProtocols();
    }

    public final void setEnabledProtocols(String[] protocols) {
        this.sslParameters.setEnabledProtocols(protocols);
    }

    public final void setUseSessionTickets(boolean useSessionTickets) {
        this.sslParameters.setUseSessionTickets(useSessionTickets);
    }

    public final void setHostname(String hostname) {
        this.sslParameters.setUseSni(hostname != null ? true : DBG_STATE);
        super.setHostname(hostname);
    }

    public final void setChannelIdEnabled(boolean enabled) {
        if (!getUseClientMode()) {
            synchronized (this.ssl) {
                if (this.state != 0) {
                    throw new IllegalStateException("Could not enable/disable Channel ID after the initial handshake has begun.");
                }
            }
            this.sslParameters.channelIdEnabled = enabled;
            return;
        }
        throw new IllegalStateException("Client mode");
    }

    public final byte[] getChannelId() throws SSLException {
        if (!getUseClientMode()) {
            synchronized (this.ssl) {
                if (this.state != 5) {
                    throw new IllegalStateException("Channel ID is only available after handshake completes");
                }
            }
            return this.ssl.getTlsChannelId();
        }
        throw new IllegalStateException("Client mode");
    }

    public final void setChannelIdPrivateKey(PrivateKey privateKey) {
        if (getUseClientMode()) {
            synchronized (this.ssl) {
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
            } catch (InvalidKeyException e) {
            }
        } else {
            throw new IllegalStateException("Server mode");
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] getTlsUnique() {
        return this.ssl.getTlsUnique();
    }

    public final boolean getUseClientMode() {
        return this.sslParameters.getUseClientMode();
    }

    public final void setUseClientMode(boolean mode) {
        synchronized (this.ssl) {
            if (this.state != 0) {
                throw new IllegalArgumentException("Could not change the mode after the initial handshake has begun.");
            }
        }
        this.sslParameters.setUseClientMode(mode);
    }

    public final boolean getWantClientAuth() {
        return this.sslParameters.getWantClientAuth();
    }

    public final boolean getNeedClientAuth() {
        return this.sslParameters.getNeedClientAuth();
    }

    public final void setNeedClientAuth(boolean need) {
        this.sslParameters.setNeedClientAuth(need);
    }

    public final void setWantClientAuth(boolean want) {
        this.sslParameters.setWantClientAuth(want);
    }

    public final void setSoWriteTimeout(int writeTimeoutMilliseconds2) throws SocketException {
        this.writeTimeoutMilliseconds = writeTimeoutMilliseconds2;
        Platform.setSocketWriteTimeout(this, (long) writeTimeoutMilliseconds2);
    }

    public final int getSoWriteTimeout() throws SocketException {
        return this.writeTimeoutMilliseconds;
    }

    public final void setHandshakeTimeout(int handshakeTimeoutMilliseconds2) throws SocketException {
        this.handshakeTimeoutMilliseconds = handshakeTimeoutMilliseconds2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0041, code lost:
        if (r2 != null) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0043, code lost:
        if (r1 == null) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0045, code lost:
        r4.ssl.interrupt();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004a, code lost:
        if (r2 == null) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004c, code lost:
        r2.awaitPendingOps();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004f, code lost:
        if (r1 == null) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
        r1.awaitPendingOps();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0054, code lost:
        shutdownAndFreeSslNative();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0057, code lost:
        return;
     */
    public final void close() throws IOException {
        if (this.ssl != null) {
            synchronized (this.ssl) {
                if (this.state != 8) {
                    int oldState = this.state;
                    transitionTo(8);
                    if (oldState == 0) {
                        free();
                        closeUnderlyingSocket();
                        this.ssl.notifyAll();
                    } else if (oldState == 5 || oldState == 4) {
                        this.ssl.notifyAll();
                        SSLInputStream sslInputStream = this.is;
                        SSLOutputStream sslOutputStream = this.os;
                    } else {
                        this.ssl.interrupt();
                        this.ssl.notifyAll();
                    }
                }
            }
        }
    }

    private void shutdownAndFreeSslNative() throws IOException {
        try {
            Platform.blockGuardOnNetwork();
            this.ssl.shutdown(Platform.getFileDescriptor(this.socket));
        } catch (IOException e) {
        } catch (Throwable th) {
            free();
            closeUnderlyingSocket();
            throw th;
        }
        free();
        closeUnderlyingSocket();
    }

    private void closeUnderlyingSocket() throws IOException {
        super.close();
    }

    private void free() {
        if (!this.ssl.isClosed()) {
            this.ssl.close();
            Platform.closeGuardClose(this.guard);
        }
    }

    /* access modifiers changed from: protected */
    public final void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                Platform.closeGuardWarnIfOpen(this.guard);
            }
            synchronized (this.ssl) {
                transitionTo(8);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
            throw th;
        }
    }

    public final void setApplicationProtocolSelector(ApplicationProtocolSelector selector) {
        setApplicationProtocolSelector(selector == null ? null : new ApplicationProtocolSelectorAdapter((SSLSocket) this, selector));
    }

    /* access modifiers changed from: package-private */
    public final void setApplicationProtocolSelector(ApplicationProtocolSelectorAdapter selector) {
        this.sslParameters.setApplicationProtocolSelector(selector);
    }

    /* access modifiers changed from: package-private */
    public final void setApplicationProtocols(String[] protocols) {
        this.sslParameters.setApplicationProtocols(protocols);
    }

    /* access modifiers changed from: package-private */
    public final String[] getApplicationProtocols() {
        return this.sslParameters.getApplicationProtocols();
    }

    public final String getApplicationProtocol() {
        return SSLUtils.toProtocolString(this.ssl.getApplicationProtocol());
    }

    public final String getHandshakeApplicationProtocol() {
        String applicationProtocol;
        synchronized (this.ssl) {
            applicationProtocol = (this.state < 2 || this.state >= 5) ? null : getApplicationProtocol();
        }
        return applicationProtocol;
    }

    public final SSLParameters getSSLParameters() {
        SSLParameters params = super.getSSLParameters();
        Platform.getSSLParameters(params, this.sslParameters, (AbstractConscryptSocket) this);
        return params;
    }

    public final void setSSLParameters(SSLParameters p) {
        super.setSSLParameters(p);
        Platform.setSSLParameters(p, this.sslParameters, (AbstractConscryptSocket) this);
    }

    public final String chooseServerAlias(X509KeyManager keyManager, String keyType) {
        return keyManager.chooseServerAlias(keyType, null, this);
    }

    public final String chooseClientAlias(X509KeyManager keyManager, X500Principal[] issuers, String[] keyTypes) {
        return keyManager.chooseClientAlias(keyTypes, issuers, this);
    }

    public final String chooseServerPSKIdentityHint(PSKKeyManager keyManager) {
        return keyManager.chooseServerKeyIdentityHint((Socket) this);
    }

    public final String chooseClientPSKIdentity(PSKKeyManager keyManager, String identityHint) {
        return keyManager.chooseClientKeyIdentity(identityHint, (Socket) this);
    }

    public final SecretKey getPSKKey(PSKKeyManager keyManager, String identityHint, String identity) {
        return keyManager.getKey(identityHint, identity, (Socket) this);
    }

    private ClientSessionContext clientSessionContext() {
        return this.sslParameters.getClientSessionContext();
    }

    private AbstractSessionContext sessionContext() {
        return this.sslParameters.getSessionContext();
    }

    private void transitionTo(int newState) {
        if (newState == 8 && !this.ssl.isClosed() && this.state >= 2 && this.state < 8) {
            this.closedSession = new SessionSnapshot(this.activeSession);
        }
        this.state = newState;
    }
}
