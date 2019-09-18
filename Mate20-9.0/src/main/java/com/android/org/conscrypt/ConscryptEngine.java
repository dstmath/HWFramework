package com.android.org.conscrypt;

import com.android.org.conscrypt.ExternalSession;
import com.android.org.conscrypt.NativeCrypto;
import com.android.org.conscrypt.NativeRef;
import com.android.org.conscrypt.NativeSsl;
import com.android.org.conscrypt.SSLParametersImpl;
import com.android.org.conscrypt.ct.CTConstants;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

final class ConscryptEngine extends AbstractConscryptEngine implements NativeCrypto.SSLHandshakeCallbacks, SSLParametersImpl.AliasChooser, SSLParametersImpl.PSKCallbacks {
    private static final SSLEngineResult CLOSED_NOT_HANDSHAKING = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
    private static final ByteBuffer EMPTY = ByteBuffer.allocateDirect(0);
    private static final SSLEngineResult NEED_UNWRAP_CLOSED = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
    private static final SSLEngineResult NEED_UNWRAP_OK = new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
    private static final SSLEngineResult NEED_WRAP_CLOSED = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, 0);
    private static final SSLEngineResult NEED_WRAP_OK = new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, 0);
    private final ActiveSession activeSession;
    private BufferAllocator bufferAllocator;
    private OpenSSLKey channelIdPrivateKey;
    private SessionSnapshot closedSession;
    private final SSLSession externalSession = Platform.wrapSSLSession(new ExternalSession(new ExternalSession.Provider() {
        public ConscryptSession provideSession() {
            return ConscryptEngine.this.provideSession();
        }
    }));
    private SSLException handshakeException;
    private boolean handshakeFinished;
    private HandshakeListener handshakeListener;
    private ByteBuffer lazyDirectBuffer;
    private int maxSealOverhead;
    private final NativeSsl.BioWrapper networkBio;
    private String peerHostname;
    private final PeerInfoProvider peerInfoProvider;
    private final ByteBuffer[] singleDstBuffer = new ByteBuffer[1];
    private final ByteBuffer[] singleSrcBuffer = new ByteBuffer[1];
    private final NativeSsl ssl;
    private final SSLParametersImpl sslParameters;
    private int state = 0;

    ConscryptEngine(SSLParametersImpl sslParameters2) {
        this.sslParameters = sslParameters2;
        this.peerInfoProvider = PeerInfoProvider.nullProvider();
        this.ssl = newSsl(sslParameters2, this);
        this.networkBio = this.ssl.newBio();
        this.activeSession = new ActiveSession(this.ssl, sslParameters2.getSessionContext());
    }

    ConscryptEngine(String host, int port, SSLParametersImpl sslParameters2) {
        this.sslParameters = sslParameters2;
        this.peerInfoProvider = PeerInfoProvider.forHostAndPort(host, port);
        this.ssl = newSsl(sslParameters2, this);
        this.networkBio = this.ssl.newBio();
        this.activeSession = new ActiveSession(this.ssl, sslParameters2.getSessionContext());
    }

    ConscryptEngine(SSLParametersImpl sslParameters2, PeerInfoProvider peerInfoProvider2) {
        this.sslParameters = sslParameters2;
        this.peerInfoProvider = (PeerInfoProvider) Preconditions.checkNotNull(peerInfoProvider2, "peerInfoProvider");
        this.ssl = newSsl(sslParameters2, this);
        this.networkBio = this.ssl.newBio();
        this.activeSession = new ActiveSession(this.ssl, sslParameters2.getSessionContext());
    }

    private static NativeSsl newSsl(SSLParametersImpl sslParameters2, ConscryptEngine engine) {
        try {
            return NativeSsl.newInstance(sslParameters2, engine, engine, engine);
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public void setBufferAllocator(BufferAllocator bufferAllocator2) {
        synchronized (this.ssl) {
            if (!isHandshakeStarted()) {
                this.bufferAllocator = bufferAllocator2;
            } else {
                throw new IllegalStateException("Could not set buffer allocator after the initial handshake has begun.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int maxSealOverhead() {
        return this.maxSealOverhead;
    }

    /* access modifiers changed from: package-private */
    public void setChannelIdEnabled(boolean enabled) {
        synchronized (this.ssl) {
            if (getUseClientMode()) {
                throw new IllegalStateException("Not allowed in client mode");
            } else if (!isHandshakeStarted()) {
                this.sslParameters.channelIdEnabled = enabled;
            } else {
                throw new IllegalStateException("Could not enable/disable Channel ID after the initial handshake has begun.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] getChannelId() throws SSLException {
        byte[] tlsChannelId;
        synchronized (this.ssl) {
            if (getUseClientMode()) {
                throw new IllegalStateException("Not allowed in client mode");
            } else if (!isHandshakeStarted()) {
                tlsChannelId = this.ssl.getTlsChannelId();
            } else {
                throw new IllegalStateException("Channel ID is only available after handshake completes");
            }
        }
        return tlsChannelId;
    }

    /* access modifiers changed from: package-private */
    public void setChannelIdPrivateKey(PrivateKey privateKey) {
        if (getUseClientMode()) {
            synchronized (this.ssl) {
                if (isHandshakeStarted()) {
                    throw new IllegalStateException("Could not change Channel ID private key after the initial handshake has begun.");
                } else if (privateKey == null) {
                    this.sslParameters.channelIdEnabled = false;
                    this.channelIdPrivateKey = null;
                } else {
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
                }
            }
        } else {
            throw new IllegalStateException("Not allowed in server mode");
        }
    }

    /* access modifiers changed from: package-private */
    public void setHandshakeListener(HandshakeListener handshakeListener2) {
        synchronized (this.ssl) {
            if (!isHandshakeStarted()) {
                this.handshakeListener = handshakeListener2;
            } else {
                throw new IllegalStateException("Handshake listener must be set before starting the handshake.");
            }
        }
    }

    private boolean isHandshakeStarted() {
        switch (this.state) {
            case 0:
            case 1:
                return false;
            default:
                return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setHostname(String hostname) {
        this.sslParameters.setUseSni(hostname != null);
        this.peerHostname = hostname;
    }

    /* access modifiers changed from: package-private */
    public String getHostname() {
        return this.peerHostname != null ? this.peerHostname : this.peerInfoProvider.getHostname();
    }

    public String getPeerHost() {
        return this.peerHostname != null ? this.peerHostname : this.peerInfoProvider.getHostnameOrIP();
    }

    public int getPeerPort() {
        return this.peerInfoProvider.getPort();
    }

    public void beginHandshake() throws SSLException {
        synchronized (this.ssl) {
            beginHandshakeInternal();
        }
    }

    private void beginHandshakeInternal() throws SSLException {
        int i = this.state;
        switch (i) {
            case 0:
                throw new IllegalStateException("Client/server mode must be set before handshake");
            case 1:
                transitionTo(2);
                try {
                    this.ssl.initialize(getHostname(), this.channelIdPrivateKey);
                    if (getUseClientMode()) {
                        NativeSslSession cachedSession = clientSessionContext().getCachedSession(getHostname(), getPeerPort(), this.sslParameters);
                        if (cachedSession != null) {
                            cachedSession.offerToResume(this.ssl);
                        }
                    }
                    this.maxSealOverhead = this.ssl.getMaxSealOverhead();
                    handshake();
                    if (0 != 0) {
                        closeAndFreeResources();
                    }
                    return;
                } catch (IOException e) {
                    if (e.getMessage().contains("unexpected CCS")) {
                        Platform.logEvent(String.format("ssl_unexpected_ccs: host=%s", new Object[]{getPeerHost()}));
                    }
                    throw SSLUtils.toSSLHandshakeException(e);
                } catch (Throwable th) {
                    if (1 != 0) {
                        closeAndFreeResources();
                    }
                    throw th;
                }
            default:
                switch (i) {
                    case 6:
                    case 7:
                    case 8:
                        throw new IllegalStateException("Engine has already been closed");
                    default:
                        return;
                }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001f, code lost:
        return;
     */
    public void closeInbound() throws SSLException {
        synchronized (this.ssl) {
            if (this.state != 8) {
                if (this.state != 6) {
                    if (isOutboundDone()) {
                        transitionTo(8);
                    } else {
                        transitionTo(6);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002c, code lost:
        return;
     */
    public void closeOutbound() {
        synchronized (this.ssl) {
            if (this.state != 8) {
                if (this.state != 7) {
                    if (isHandshakeStarted()) {
                        sendSSLShutdown();
                        if (isInboundDone()) {
                            closeAndFreeResources();
                        } else {
                            transitionTo(7);
                        }
                    } else {
                        closeAndFreeResources();
                    }
                }
            }
        }
    }

    public Runnable getDelegatedTask() {
        return null;
    }

    public String[] getEnabledCipherSuites() {
        return this.sslParameters.getEnabledCipherSuites();
    }

    public String[] getEnabledProtocols() {
        return this.sslParameters.getEnabledProtocols();
    }

    public boolean getEnableSessionCreation() {
        return this.sslParameters.getEnableSessionCreation();
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

    public SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        SSLEngineResult.HandshakeStatus handshakeStatusInternal;
        synchronized (this.ssl) {
            handshakeStatusInternal = getHandshakeStatusInternal();
        }
        return handshakeStatusInternal;
    }

    private SSLEngineResult.HandshakeStatus getHandshakeStatusInternal() {
        if (this.handshakeFinished) {
            return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
        }
        switch (this.state) {
            case 0:
            case 1:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
            case 2:
                return pendingStatus(pendingOutboundEncryptedBytes());
            case CTConstants.CERTIFICATE_LENGTH_BYTES /*3*/:
                return SSLEngineResult.HandshakeStatus.NEED_WRAP;
            default:
                throw new IllegalStateException("Unexpected engine state: " + this.state);
        }
    }

    private int pendingOutboundEncryptedBytes() {
        return this.networkBio.getPendingWrittenBytes();
    }

    private int pendingInboundCleartextBytes() {
        return this.ssl.getPendingReadableBytes();
    }

    private static SSLEngineResult.HandshakeStatus pendingStatus(int pendingOutboundBytes) {
        return pendingOutboundBytes > 0 ? SSLEngineResult.HandshakeStatus.NEED_WRAP : SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
    }

    public boolean getNeedClientAuth() {
        return this.sslParameters.getNeedClientAuth();
    }

    /* access modifiers changed from: package-private */
    public SSLSession handshakeSession() {
        synchronized (this.ssl) {
            if (this.state != 2) {
                return null;
            }
            SSLSession wrapSSLSession = Platform.wrapSSLSession(new ExternalSession(new ExternalSession.Provider() {
                public ConscryptSession provideSession() {
                    return ConscryptEngine.this.provideHandshakeSession();
                }
            }));
            return wrapSSLSession;
        }
    }

    public SSLSession getSession() {
        return this.externalSession;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0015, code lost:
        return r1;
     */
    public ConscryptSession provideSession() {
        synchronized (this.ssl) {
            if (this.state == 8) {
                ConscryptSession nullSession = this.closedSession != null ? this.closedSession : SSLNullSession.getNullSession();
            } else if (this.state < 3) {
                ConscryptSession nullSession2 = SSLNullSession.getNullSession();
                return nullSession2;
            } else {
                ActiveSession activeSession2 = this.activeSession;
                return activeSession2;
            }
        }
    }

    /* access modifiers changed from: private */
    public ConscryptSession provideHandshakeSession() {
        ConscryptSession conscryptSession;
        synchronized (this.ssl) {
            if (this.state == 2) {
                conscryptSession = this.activeSession;
            } else {
                conscryptSession = SSLNullSession.getNullSession();
            }
        }
        return conscryptSession;
    }

    public String[] getSupportedCipherSuites() {
        return NativeCrypto.getSupportedCipherSuites();
    }

    public String[] getSupportedProtocols() {
        return NativeCrypto.getSupportedProtocols();
    }

    public boolean getUseClientMode() {
        return this.sslParameters.getUseClientMode();
    }

    public boolean getWantClientAuth() {
        return this.sslParameters.getWantClientAuth();
    }

    public boolean isInboundDone() {
        boolean z;
        synchronized (this.ssl) {
            if (!(this.state == 8 || this.state == 6)) {
                if (!this.ssl.wasShutdownReceived()) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public boolean isOutboundDone() {
        boolean z;
        synchronized (this.ssl) {
            if (!(this.state == 8 || this.state == 7)) {
                if (!this.ssl.wasShutdownSent()) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public void setEnabledCipherSuites(String[] suites) {
        this.sslParameters.setEnabledCipherSuites(suites);
    }

    public void setEnabledProtocols(String[] protocols) {
        this.sslParameters.setEnabledProtocols(protocols);
    }

    public void setEnableSessionCreation(boolean flag) {
        this.sslParameters.setEnableSessionCreation(flag);
    }

    public void setNeedClientAuth(boolean need) {
        this.sslParameters.setNeedClientAuth(need);
    }

    public void setUseClientMode(boolean mode) {
        synchronized (this.ssl) {
            if (!isHandshakeStarted()) {
                transitionTo(1);
                this.sslParameters.setUseClientMode(mode);
            } else {
                throw new IllegalArgumentException("Can not change mode after handshake: state == " + this.state);
            }
        }
    }

    public void setWantClientAuth(boolean want) {
        this.sslParameters.setWantClientAuth(want);
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        SSLEngineResult unwrap;
        synchronized (this.ssl) {
            try {
                unwrap = unwrap(singleSrcBuffer(src), singleDstBuffer(dst));
                resetSingleSrcBuffer();
                resetSingleDstBuffer();
            } catch (Throwable th) {
                resetSingleSrcBuffer();
                resetSingleDstBuffer();
                throw th;
            }
        }
        return unwrap;
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts) throws SSLException {
        SSLEngineResult unwrap;
        synchronized (this.ssl) {
            try {
                unwrap = unwrap(singleSrcBuffer(src), dsts);
                resetSingleSrcBuffer();
            } catch (Throwable th) {
                resetSingleSrcBuffer();
                throw th;
            }
        }
        return unwrap;
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length) throws SSLException {
        SSLEngineResult unwrap;
        synchronized (this.ssl) {
            try {
                unwrap = unwrap(singleSrcBuffer(src), 0, 1, dsts, offset, length);
                resetSingleSrcBuffer();
            } catch (Throwable th) {
                resetSingleSrcBuffer();
                throw th;
            }
        }
        return unwrap;
    }

    /* access modifiers changed from: package-private */
    public SSLEngineResult unwrap(ByteBuffer[] srcs, ByteBuffer[] dsts) throws SSLException {
        boolean z = false;
        Preconditions.checkArgument(srcs != null, "srcs is null");
        if (dsts != null) {
            z = true;
        }
        Preconditions.checkArgument(z, "dsts is null");
        return unwrap(srcs, 0, srcs.length, dsts, 0, dsts.length);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x01b8, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x01c1, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x01d1, code lost:
        r0 = e;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:126:0x016e, B:143:0x018e] */
    public SSLEngineResult unwrap(ByteBuffer[] srcs, int srcsOffset, int srcsLength, ByteBuffer[] dsts, int dstsOffset, int dstsLength) throws SSLException {
        int packetLength;
        SSLEngineResult.HandshakeStatus handshakeStatus;
        ByteBuffer dst;
        ByteBuffer[] byteBufferArr = srcs;
        int srcsOffset2 = srcsOffset;
        ByteBuffer[] byteBufferArr2 = dsts;
        int i = dstsOffset;
        Preconditions.checkArgument(byteBufferArr != null, "srcs is null");
        Preconditions.checkArgument(byteBufferArr2 != null, "dsts is null");
        Preconditions.checkPositionIndexes(srcsOffset2, srcsOffset2 + srcsLength, byteBufferArr.length);
        Preconditions.checkPositionIndexes(i, i + dstsLength, byteBufferArr2.length);
        int dstLength = calcDstsLength(dsts, dstsOffset, dstsLength);
        int endOffset = i + dstsLength;
        int srcsEndOffset = srcsOffset2 + srcsLength;
        long srcLength = calcSrcsLength(byteBufferArr, srcsOffset2, srcsEndOffset);
        synchronized (this.ssl) {
            try {
                int i2 = this.state;
                if (i2 == 6 || i2 == 8) {
                    SSLEngineResult sSLEngineResult = new SSLEngineResult(SSLEngineResult.Status.CLOSED, getHandshakeStatusInternal(), 0, 0);
                    return sSLEngineResult;
                }
                switch (i2) {
                    case 0:
                        throw new IllegalStateException("Client/server mode must be set before calling unwrap");
                    case 1:
                        beginHandshakeInternal();
                        break;
                }
                SSLEngineResult.HandshakeStatus handshakeStatus2 = SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
                if (!this.handshakeFinished) {
                    try {
                        handshakeStatus2 = handshake();
                        if (handshakeStatus2 == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                            SSLEngineResult sSLEngineResult2 = NEED_WRAP_OK;
                            return sSLEngineResult2;
                        } else if (this.state == 8) {
                            SSLEngineResult sSLEngineResult3 = NEED_WRAP_CLOSED;
                            return sSLEngineResult3;
                        }
                    } catch (Throwable th) {
                        th = th;
                        int i3 = srcsOffset2;
                        long j = srcLength;
                    }
                }
                boolean noCleartextDataAvailable = pendingInboundCleartextBytes() <= 0;
                if (srcLength <= 0 || !noCleartextDataAvailable) {
                    long j2 = srcLength;
                    if (noCleartextDataAvailable) {
                        SSLEngineResult sSLEngineResult4 = new SSLEngineResult(SSLEngineResult.Status.BUFFER_UNDERFLOW, getHandshakeStatus(), 0, 0);
                        return sSLEngineResult4;
                    }
                    packetLength = 0;
                } else if (srcLength < 5) {
                    SSLEngineResult sSLEngineResult5 = new SSLEngineResult(SSLEngineResult.Status.BUFFER_UNDERFLOW, getHandshakeStatus(), 0, 0);
                    return sSLEngineResult5;
                } else {
                    packetLength = SSLUtils.getEncryptedPacketLength(srcs, srcsOffset);
                    if (packetLength < 0) {
                        throw new SSLException("Unable to parse TLS packet header");
                    } else if (srcLength < ((long) packetLength)) {
                        long j3 = srcLength;
                        try {
                            SSLEngineResult sSLEngineResult6 = new SSLEngineResult(SSLEngineResult.Status.BUFFER_UNDERFLOW, getHandshakeStatus(), 0, 0);
                            return sSLEngineResult6;
                        } catch (Throwable th2) {
                            th = th2;
                            int i4 = srcsOffset2;
                        }
                    }
                }
                int bytesConsumed = 0;
                if (packetLength > 0 && srcsOffset2 < srcsEndOffset) {
                    while (true) {
                        ByteBuffer src = byteBufferArr[srcsOffset2];
                        int remaining = src.remaining();
                        if (remaining == 0) {
                            srcsOffset2++;
                            continue;
                        } else {
                            int written = writeEncryptedData(src, Math.min(packetLength, remaining));
                            if (written > 0) {
                                bytesConsumed += written;
                                packetLength -= written;
                                if (packetLength != 0) {
                                    if (written == remaining) {
                                        srcsOffset2++;
                                        continue;
                                    }
                                }
                            } else {
                                NativeCrypto.SSL_clear_error();
                            }
                        }
                        if (srcsOffset2 >= srcsEndOffset) {
                        }
                    }
                }
                int lenRemaining = packetLength;
                int bytesProduced = 0;
                if (dstLength > 0) {
                    int idx = dstsOffset;
                    while (true) {
                        if (idx < endOffset) {
                            try {
                                dst = byteBufferArr2[idx];
                                idx++;
                            } catch (SSLException e) {
                                e = e;
                                int i5 = lenRemaining;
                                if (pendingOutboundEncryptedBytes() > 0) {
                                    if (!this.handshakeFinished && this.handshakeException == null) {
                                        this.handshakeException = e;
                                    }
                                    SSLEngineResult sSLEngineResult7 = new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_WRAP, bytesConsumed, bytesProduced);
                                    return sSLEngineResult7;
                                }
                                sendSSLShutdown();
                                throw convertException(e);
                            } catch (InterruptedIOException e2) {
                                int i6 = lenRemaining;
                                SSLEngineResult newResult = newResult(bytesConsumed, bytesProduced, handshakeStatus2);
                                return newResult;
                            } catch (EOFException e3) {
                                e = e3;
                                int i7 = lenRemaining;
                                closeAll();
                                throw convertException(e);
                            } catch (IOException e4) {
                                e = e4;
                                int i8 = lenRemaining;
                                sendSSLShutdown();
                                throw convertException(e);
                            }
                            if (dst.hasRemaining()) {
                                int bytesRead = readPlaintextData(dst);
                                if (bytesRead > 0) {
                                    bytesProduced += bytesRead;
                                    try {
                                        if (dst.hasRemaining()) {
                                            int i9 = lenRemaining;
                                        }
                                    } catch (SSLException e5) {
                                        e = e5;
                                        int i10 = lenRemaining;
                                    } catch (InterruptedIOException e6) {
                                        int i11 = lenRemaining;
                                        SSLEngineResult newResult2 = newResult(bytesConsumed, bytesProduced, handshakeStatus2);
                                        return newResult2;
                                    } catch (EOFException e7) {
                                        e = e7;
                                        int i12 = lenRemaining;
                                        closeAll();
                                        throw convertException(e);
                                    } catch (IOException e8) {
                                        e = e8;
                                        int i13 = lenRemaining;
                                        sendSSLShutdown();
                                        throw convertException(e);
                                    }
                                } else {
                                    int i14 = idx;
                                    if (bytesRead != -6) {
                                        switch (bytesRead) {
                                            case -3:
                                            case -2:
                                                SSLEngineResult newResult3 = newResult(bytesConsumed, bytesProduced, handshakeStatus2);
                                                return newResult3;
                                            default:
                                                sendSSLShutdown();
                                                throw newSslExceptionWithMessage("SSL_read");
                                        }
                                        th = th;
                                        throw th;
                                    }
                                    closeInbound();
                                    sendSSLShutdown();
                                    int i15 = lenRemaining;
                                    SSLEngineResult sSLEngineResult8 = new SSLEngineResult(SSLEngineResult.Status.CLOSED, pendingOutboundEncryptedBytes() > 0 ? SSLEngineResult.HandshakeStatus.NEED_WRAP : SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, bytesConsumed, bytesProduced);
                                    return sSLEngineResult8;
                                }
                            }
                        }
                    }
                } else {
                    readPlaintextData(EMPTY);
                }
                if ((this.handshakeFinished ? pendingInboundCleartextBytes() : 0) > 0) {
                    SSLEngineResult.Status status = SSLEngineResult.Status.BUFFER_OVERFLOW;
                    if (handshakeStatus2 == SSLEngineResult.HandshakeStatus.FINISHED) {
                        handshakeStatus = handshakeStatus2;
                    } else {
                        handshakeStatus = getHandshakeStatusInternal();
                    }
                    SSLEngineResult sSLEngineResult9 = new SSLEngineResult(status, mayFinishHandshake(handshakeStatus), bytesConsumed, bytesProduced);
                    return sSLEngineResult9;
                }
                SSLEngineResult newResult4 = newResult(bytesConsumed, bytesProduced, handshakeStatus2);
                return newResult4;
            } catch (Throwable th3) {
                th = th3;
                long j4 = srcLength;
                int i16 = srcsOffset2;
            }
        }
    }

    private static int calcDstsLength(ByteBuffer[] dsts, int dstsOffset, int dstsLength) {
        int capacity = 0;
        int i = 0;
        while (i < dsts.length) {
            ByteBuffer dst = dsts[i];
            Preconditions.checkArgument(dst != null, "dsts[%d] is null", Integer.valueOf(i));
            if (!dst.isReadOnly()) {
                if (i >= dstsOffset && i < dstsOffset + dstsLength) {
                    capacity += dst.remaining();
                }
                i++;
            } else {
                throw new ReadOnlyBufferException();
            }
        }
        return capacity;
    }

    private static long calcSrcsLength(ByteBuffer[] srcs, int srcsOffset, int srcsEndOffset) {
        long len = 0;
        int i = srcsOffset;
        while (i < srcsEndOffset) {
            ByteBuffer src = srcs[i];
            if (src != null) {
                len += (long) src.remaining();
                i++;
            } else {
                throw new IllegalArgumentException("srcs[" + i + "] is null");
            }
        }
        return len;
    }

    private SSLEngineResult.HandshakeStatus handshake() throws SSLException {
        try {
            if (this.handshakeException == null) {
                switch (this.ssl.doHandshake()) {
                    case 2:
                        return pendingStatus(pendingOutboundEncryptedBytes());
                    case CTConstants.CERTIFICATE_LENGTH_BYTES /*3*/:
                        return SSLEngineResult.HandshakeStatus.NEED_WRAP;
                    default:
                        this.activeSession.onPeerCertificateAvailable(getPeerHost(), getPeerPort());
                        finishHandshake();
                        return SSLEngineResult.HandshakeStatus.FINISHED;
                }
                throw SSLUtils.toSSLHandshakeException(e);
            } else if (pendingOutboundEncryptedBytes() > 0) {
                return SSLEngineResult.HandshakeStatus.NEED_WRAP;
            } else {
                SSLException e = this.handshakeException;
                this.handshakeException = null;
                throw e;
            }
        } catch (SSLException e2) {
            if (pendingOutboundEncryptedBytes() > 0) {
                this.handshakeException = e2;
                return SSLEngineResult.HandshakeStatus.NEED_WRAP;
            }
            sendSSLShutdown();
            throw e2;
        } catch (IOException e3) {
            sendSSLShutdown();
            throw e3;
        } catch (Exception e4) {
            throw SSLUtils.toSSLHandshakeException(e4);
        }
    }

    private void finishHandshake() throws SSLException {
        this.handshakeFinished = true;
        if (this.handshakeListener != null) {
            this.handshakeListener.onHandshakeFinished();
        }
    }

    private int writePlaintextData(ByteBuffer src, int len) throws SSLException {
        int sslWrote;
        try {
            int pos = src.position();
            if (src.isDirect()) {
                sslWrote = writePlaintextDataDirect(src, pos, len);
            } else {
                sslWrote = writePlaintextDataHeap(src, pos, len);
            }
            if (sslWrote > 0) {
                src.position(pos + sslWrote);
            }
            return sslWrote;
        } catch (Exception e) {
            throw convertException(e);
        }
    }

    private int writePlaintextDataDirect(ByteBuffer src, int pos, int len) throws IOException {
        return this.ssl.writeDirectByteBuffer(directByteBufferAddress(src, pos), len);
    }

    private int writePlaintextDataHeap(ByteBuffer src, int pos, int len) throws IOException {
        ByteBuffer buffer;
        AllocatedBuffer allocatedBuffer = null;
        try {
            if (this.bufferAllocator != null) {
                allocatedBuffer = this.bufferAllocator.allocateDirectBuffer(len);
                buffer = allocatedBuffer.nioBuffer();
            } else {
                buffer = getOrCreateLazyDirectBuffer();
            }
            int limit = src.limit();
            int bytesToWrite = Math.min(len, buffer.remaining());
            src.limit(pos + bytesToWrite);
            buffer.put(src);
            buffer.flip();
            src.limit(limit);
            src.position(pos);
            return writePlaintextDataDirect(buffer, 0, bytesToWrite);
        } finally {
            if (allocatedBuffer != null) {
                allocatedBuffer.release();
            }
        }
    }

    private int readPlaintextData(ByteBuffer dst) throws IOException {
        try {
            int pos = dst.position();
            int len = Math.min(16709, dst.limit() - pos);
            if (!dst.isDirect()) {
                return readPlaintextDataHeap(dst, len);
            }
            int bytesRead = readPlaintextDataDirect(dst, pos, len);
            if (bytesRead > 0) {
                dst.position(pos + bytesRead);
            }
            return bytesRead;
        } catch (CertificateException e) {
            throw convertException(e);
        }
    }

    private int readPlaintextDataDirect(ByteBuffer dst, int pos, int len) throws IOException, CertificateException {
        return this.ssl.readDirectByteBuffer(directByteBufferAddress(dst, pos), len);
    }

    private int readPlaintextDataHeap(ByteBuffer dst, int len) throws IOException, CertificateException {
        ByteBuffer buffer;
        AllocatedBuffer allocatedBuffer = null;
        try {
            if (this.bufferAllocator != null) {
                allocatedBuffer = this.bufferAllocator.allocateDirectBuffer(len);
                buffer = allocatedBuffer.nioBuffer();
            } else {
                buffer = getOrCreateLazyDirectBuffer();
            }
            int bytesRead = readPlaintextDataDirect(buffer, 0, Math.min(len, buffer.remaining()));
            if (bytesRead > 0) {
                buffer.position(bytesRead);
                buffer.flip();
                dst.put(buffer);
            }
            return bytesRead;
        } finally {
            if (allocatedBuffer != null) {
                allocatedBuffer.release();
            }
        }
    }

    private SSLException convertException(Throwable e) {
        if ((e instanceof SSLHandshakeException) || !this.handshakeFinished) {
            return SSLUtils.toSSLHandshakeException(e);
        }
        return SSLUtils.toSSLException(e);
    }

    private int writeEncryptedData(ByteBuffer src, int len) throws SSLException {
        int bytesWritten;
        try {
            int pos = src.position();
            if (src.isDirect()) {
                bytesWritten = writeEncryptedDataDirect(src, pos, len);
            } else {
                bytesWritten = writeEncryptedDataHeap(src, pos, len);
            }
            if (bytesWritten > 0) {
                src.position(pos + bytesWritten);
            }
            return bytesWritten;
        } catch (IOException e) {
            throw new SSLException(e);
        }
    }

    private int writeEncryptedDataDirect(ByteBuffer src, int pos, int len) throws IOException {
        return this.networkBio.writeDirectByteBuffer(directByteBufferAddress(src, pos), len);
    }

    private int writeEncryptedDataHeap(ByteBuffer src, int pos, int len) throws IOException {
        ByteBuffer buffer;
        AllocatedBuffer allocatedBuffer = null;
        try {
            if (this.bufferAllocator != null) {
                allocatedBuffer = this.bufferAllocator.allocateDirectBuffer(len);
                buffer = allocatedBuffer.nioBuffer();
            } else {
                buffer = getOrCreateLazyDirectBuffer();
            }
            int limit = src.limit();
            int bytesToCopy = Math.min(Math.min(limit - pos, len), buffer.remaining());
            src.limit(pos + bytesToCopy);
            buffer.put(src);
            src.limit(limit);
            src.position(pos);
            int bytesWritten = writeEncryptedDataDirect(buffer, 0, bytesToCopy);
            src.position(pos);
            return bytesWritten;
        } finally {
            if (allocatedBuffer != null) {
                allocatedBuffer.release();
            }
        }
    }

    private ByteBuffer getOrCreateLazyDirectBuffer() {
        if (this.lazyDirectBuffer == null) {
            this.lazyDirectBuffer = ByteBuffer.allocateDirect(Math.max(16384, 16709));
        }
        this.lazyDirectBuffer.clear();
        return this.lazyDirectBuffer;
    }

    private long directByteBufferAddress(ByteBuffer directBuffer, int pos) {
        return NativeCrypto.getDirectBufferAddress(directBuffer) + ((long) pos);
    }

    private SSLEngineResult readPendingBytesFromBIO(ByteBuffer dst, int bytesConsumed, int bytesProduced, SSLEngineResult.HandshakeStatus status) throws SSLException {
        SSLEngineResult.HandshakeStatus handshakeStatus;
        SSLEngineResult.HandshakeStatus handshakeStatus2;
        try {
            int pendingNet = pendingOutboundEncryptedBytes();
            if (pendingNet <= 0) {
                return null;
            }
            if (dst.remaining() < pendingNet) {
                SSLEngineResult.Status status2 = SSLEngineResult.Status.BUFFER_OVERFLOW;
                if (status == SSLEngineResult.HandshakeStatus.FINISHED) {
                    handshakeStatus2 = status;
                } else {
                    handshakeStatus2 = getHandshakeStatus(pendingNet);
                }
                return new SSLEngineResult(status2, mayFinishHandshake(handshakeStatus2), bytesConsumed, bytesProduced);
            }
            int produced = readEncryptedData(dst, pendingNet);
            if (produced <= 0) {
                NativeCrypto.SSL_clear_error();
            } else {
                bytesProduced += produced;
                pendingNet -= produced;
            }
            SSLEngineResult.Status engineStatus = getEngineStatus();
            if (status == SSLEngineResult.HandshakeStatus.FINISHED) {
                handshakeStatus = status;
            } else {
                handshakeStatus = getHandshakeStatus(pendingNet);
            }
            return new SSLEngineResult(engineStatus, mayFinishHandshake(handshakeStatus), bytesConsumed, bytesProduced);
        } catch (Exception e) {
            throw convertException(e);
        }
    }

    private int readEncryptedData(ByteBuffer dst, int pending) throws SSLException {
        try {
            int pos = dst.position();
            if (dst.remaining() < pending) {
                return 0;
            }
            int len = Math.min(pending, dst.limit() - pos);
            if (!dst.isDirect()) {
                return readEncryptedDataHeap(dst, len);
            }
            int bytesRead = readEncryptedDataDirect(dst, pos, len);
            if (bytesRead <= 0) {
                return bytesRead;
            }
            dst.position(pos + bytesRead);
            return bytesRead;
        } catch (Exception e) {
            throw convertException(e);
        }
    }

    private int readEncryptedDataDirect(ByteBuffer dst, int pos, int len) throws IOException {
        return this.networkBio.readDirectByteBuffer(directByteBufferAddress(dst, pos), len);
    }

    private int readEncryptedDataHeap(ByteBuffer dst, int len) throws IOException {
        ByteBuffer buffer;
        AllocatedBuffer allocatedBuffer = null;
        try {
            if (this.bufferAllocator != null) {
                allocatedBuffer = this.bufferAllocator.allocateDirectBuffer(len);
                buffer = allocatedBuffer.nioBuffer();
            } else {
                buffer = getOrCreateLazyDirectBuffer();
            }
            int bytesRead = readEncryptedDataDirect(buffer, 0, Math.min(len, buffer.remaining()));
            if (bytesRead > 0) {
                buffer.position(bytesRead);
                buffer.flip();
                dst.put(buffer);
            }
            return bytesRead;
        } finally {
            if (allocatedBuffer != null) {
                allocatedBuffer.release();
            }
        }
    }

    private SSLEngineResult.HandshakeStatus mayFinishHandshake(SSLEngineResult.HandshakeStatus status) throws SSLException {
        if (this.handshakeFinished || status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            return status;
        }
        return handshake();
    }

    private SSLEngineResult.HandshakeStatus getHandshakeStatus(int pending) {
        return !this.handshakeFinished ? pendingStatus(pending) : SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
    }

    private SSLEngineResult.Status getEngineStatus() {
        switch (this.state) {
            case 6:
            case 7:
            case 8:
                return SSLEngineResult.Status.CLOSED;
            default:
                return SSLEngineResult.Status.OK;
        }
    }

    private void closeAll() throws SSLException {
        closeOutbound();
        closeInbound();
    }

    private SSLException newSslExceptionWithMessage(String err) {
        if (!this.handshakeFinished) {
            return new SSLException(err);
        }
        return new SSLHandshakeException(err);
    }

    private SSLEngineResult newResult(int bytesConsumed, int bytesProduced, SSLEngineResult.HandshakeStatus status) throws SSLException {
        return new SSLEngineResult(getEngineStatus(), mayFinishHandshake(status == SSLEngineResult.HandshakeStatus.FINISHED ? status : getHandshakeStatusInternal()), bytesConsumed, bytesProduced);
    }

    public SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        SSLEngineResult wrap;
        synchronized (this.ssl) {
            try {
                wrap = wrap(singleSrcBuffer(src), dst);
                resetSingleSrcBuffer();
            } catch (Throwable th) {
                resetSingleSrcBuffer();
                throw th;
            }
        }
        return wrap;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0162, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0134, code lost:
        return r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0150, code lost:
        return r11;
     */
    public SSLEngineResult wrap(ByteBuffer[] srcs, int srcsOffset, int srcsLength, ByteBuffer dst) throws SSLException {
        SSLEngineResult sSLEngineResult;
        SSLEngineResult sSLEngineResult2;
        ByteBuffer[] byteBufferArr = srcs;
        int i = srcsOffset;
        ByteBuffer byteBuffer = dst;
        boolean z = false;
        Preconditions.checkArgument(byteBufferArr != null, "srcs is null");
        Preconditions.checkArgument(byteBuffer != null, "dst is null");
        Preconditions.checkPositionIndexes(i, i + srcsLength, byteBufferArr.length);
        if (!dst.isReadOnly()) {
            synchronized (this.ssl) {
                switch (this.state) {
                    case 0:
                        throw new IllegalStateException("Client/server mode must be set before calling wrap");
                    case 1:
                        beginHandshakeInternal();
                        break;
                    case 7:
                    case 8:
                        SSLEngineResult pendingNetResult = readPendingBytesFromBIO(byteBuffer, 0, 0, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING);
                        if (pendingNetResult != null) {
                            return pendingNetResult;
                        }
                        SSLEngineResult sSLEngineResult3 = new SSLEngineResult(SSLEngineResult.Status.CLOSED, getHandshakeStatusInternal(), 0, 0);
                        return sSLEngineResult3;
                }
                SSLEngineResult.HandshakeStatus handshakeStatus = SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
                if (!this.handshakeFinished) {
                    handshakeStatus = handshake();
                    if (handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                        SSLEngineResult sSLEngineResult4 = NEED_UNWRAP_OK;
                        return sSLEngineResult4;
                    } else if (this.state == 8) {
                        SSLEngineResult sSLEngineResult5 = NEED_UNWRAP_CLOSED;
                        return sSLEngineResult5;
                    }
                }
                int endOffset = i + srcsLength;
                int srcsLen = 0;
                int i2 = i;
                while (i2 < endOffset) {
                    ByteBuffer src = byteBufferArr[i2];
                    if (src != null) {
                        if (srcsLen != 16384) {
                            srcsLen += src.remaining();
                            if (srcsLen > 16384 || srcsLen < 0) {
                                srcsLen = 16384;
                            }
                        }
                        i2++;
                    } else {
                        throw new IllegalArgumentException("srcs[" + i2 + "] is null");
                    }
                }
                if (dst.remaining() < SSLUtils.calculateOutNetBufSize(srcsLen)) {
                    SSLEngineResult sSLEngineResult6 = new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, getHandshakeStatusInternal(), 0, 0);
                    return sSLEngineResult6;
                }
                int bytesConsumed = 0;
                int bytesProduced = 0;
                int i3 = i;
                while (true) {
                    if (i3 < endOffset) {
                        ByteBuffer src2 = byteBufferArr[i3];
                        Preconditions.checkArgument(src2 != null ? true : z, "srcs[%d] is null", Integer.valueOf(i3));
                        while (src2.hasRemaining()) {
                            int result = writePlaintextData(src2, Math.min(src2.remaining(), 16384 - bytesConsumed));
                            if (result > 0) {
                                bytesConsumed += result;
                                SSLEngineResult pendingNetResult2 = readPendingBytesFromBIO(byteBuffer, bytesConsumed, bytesProduced, handshakeStatus);
                                if (pendingNetResult2 != null) {
                                    if (pendingNetResult2.getStatus() != SSLEngineResult.Status.OK) {
                                        return pendingNetResult2;
                                    }
                                    bytesProduced = pendingNetResult2.bytesProduced();
                                }
                                if (bytesConsumed != 16384) {
                                    ByteBuffer[] byteBufferArr2 = srcs;
                                }
                            } else {
                                int sslError = this.ssl.getError(result);
                                if (sslError != 6) {
                                    switch (sslError) {
                                        case 2:
                                            SSLEngineResult pendingNetResult3 = readPendingBytesFromBIO(byteBuffer, bytesConsumed, bytesProduced, handshakeStatus);
                                            if (pendingNetResult3 == null) {
                                                int i4 = result;
                                                int i5 = sslError;
                                                sSLEngineResult = new SSLEngineResult(getEngineStatus(), SSLEngineResult.HandshakeStatus.NEED_UNWRAP, bytesConsumed, bytesProduced);
                                                break;
                                            } else {
                                                int i6 = result;
                                                int i7 = sslError;
                                                sSLEngineResult = pendingNetResult3;
                                                break;
                                            }
                                        case CTConstants.CERTIFICATE_LENGTH_BYTES /*3*/:
                                            SSLEngineResult pendingNetResult4 = readPendingBytesFromBIO(byteBuffer, bytesConsumed, bytesProduced, handshakeStatus);
                                            if (pendingNetResult4 == null) {
                                                sSLEngineResult2 = NEED_WRAP_CLOSED;
                                                break;
                                            } else {
                                                sSLEngineResult2 = pendingNetResult4;
                                                break;
                                            }
                                        default:
                                            sendSSLShutdown();
                                            throw newSslExceptionWithMessage("SSL_write");
                                    }
                                } else {
                                    int i8 = sslError;
                                    closeAll();
                                    SSLEngineResult pendingNetResult5 = readPendingBytesFromBIO(byteBuffer, bytesConsumed, bytesProduced, handshakeStatus);
                                    SSLEngineResult sSLEngineResult7 = pendingNetResult5 != null ? pendingNetResult5 : CLOSED_NOT_HANDSHAKING;
                                }
                            }
                        }
                        i3++;
                        byteBufferArr = srcs;
                        z = false;
                    }
                }
                if (bytesConsumed == 0) {
                    SSLEngineResult pendingNetResult6 = readPendingBytesFromBIO(byteBuffer, 0, bytesProduced, handshakeStatus);
                    if (pendingNetResult6 != null) {
                        return pendingNetResult6;
                    }
                }
                SSLEngineResult pendingNetResult7 = newResult(bytesConsumed, bytesProduced, handshakeStatus);
                return pendingNetResult7;
            }
        }
        throw new ReadOnlyBufferException();
    }

    public int clientPSKKeyRequested(String identityHint, byte[] identity, byte[] key) {
        return this.ssl.clientPSKKeyRequested(identityHint, identity, key);
    }

    public int serverPSKKeyRequested(String identityHint, String identity, byte[] key) {
        return this.ssl.serverPSKKeyRequested(identityHint, identity, key);
    }

    public void onSSLStateChange(int type, int val) {
        synchronized (this.ssl) {
            if (type == 16) {
                transitionTo(2);
            } else if (type == 32) {
                if (this.state != 2) {
                    if (this.state != 4) {
                        throw new IllegalStateException("Completed handshake while in mode " + this.state);
                    }
                }
                transitionTo(3);
            }
        }
    }

    public void onNewSessionEstablished(long sslSessionNativePtr) {
        try {
            NativeCrypto.SSL_SESSION_up_ref(sslSessionNativePtr);
            sessionContext().cacheSession(NativeSslSession.newInstance(new NativeRef.SSL_SESSION(sslSessionNativePtr), this.activeSession));
        } catch (Exception e) {
        }
    }

    public long serverSessionRequested(byte[] id) {
        return 0;
    }

    public void verifyCertificateChain(byte[][] certChain, String authMethod) throws CertificateException {
        if (certChain != null) {
            try {
                if (certChain.length != 0) {
                    X509Certificate[] peerCertChain = SSLUtils.decodeX509CertificateChain(certChain);
                    X509TrustManager x509tm = this.sslParameters.getX509TrustManager();
                    if (x509tm != null) {
                        this.activeSession.onPeerCertificatesReceived(getPeerHost(), getPeerPort(), peerCertChain);
                        if (getUseClientMode()) {
                            Platform.checkServerTrusted(x509tm, peerCertChain, authMethod, this);
                            return;
                        } else {
                            Platform.checkClientTrusted(x509tm, peerCertChain, peerCertChain[0].getPublicKey().getAlgorithm(), this);
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

    public void clientCertificateRequested(byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals) throws CertificateEncodingException, SSLException {
        this.ssl.chooseClientCertificate(keyTypeBytes, asn1DerEncodedPrincipals);
    }

    private void sendSSLShutdown() {
        try {
            this.ssl.shutdown();
        } catch (IOException e) {
        }
    }

    private void closeAndFreeResources() {
        transitionTo(8);
        if (!this.ssl.isClosed()) {
            this.ssl.close();
            this.networkBio.close();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            transitionTo(8);
        } finally {
            super.finalize();
        }
    }

    public String chooseServerAlias(X509KeyManager keyManager, String keyType) {
        if (keyManager instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager) keyManager).chooseEngineServerAlias(keyType, null, this);
        }
        return keyManager.chooseServerAlias(keyType, null, null);
    }

    public String chooseClientAlias(X509KeyManager keyManager, X500Principal[] issuers, String[] keyTypes) {
        if (keyManager instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager) keyManager).chooseEngineClientAlias(keyTypes, issuers, this);
        }
        return keyManager.chooseClientAlias(keyTypes, issuers, null);
    }

    public String chooseServerPSKIdentityHint(PSKKeyManager keyManager) {
        return keyManager.chooseServerKeyIdentityHint((SSLEngine) this);
    }

    public String chooseClientPSKIdentity(PSKKeyManager keyManager, String identityHint) {
        return keyManager.chooseClientKeyIdentity(identityHint, (SSLEngine) this);
    }

    public SecretKey getPSKKey(PSKKeyManager keyManager, String identityHint, String identity) {
        return keyManager.getKey(identityHint, identity, (SSLEngine) this);
    }

    /* access modifiers changed from: package-private */
    public void setUseSessionTickets(boolean useSessionTickets) {
        this.sslParameters.setUseSessionTickets(useSessionTickets);
    }

    /* access modifiers changed from: package-private */
    public String[] getApplicationProtocols() {
        return this.sslParameters.getApplicationProtocols();
    }

    /* access modifiers changed from: package-private */
    public void setApplicationProtocols(String[] protocols) {
        this.sslParameters.setApplicationProtocols(protocols);
    }

    /* access modifiers changed from: package-private */
    public void setApplicationProtocolSelector(ApplicationProtocolSelector selector) {
        setApplicationProtocolSelector(selector == null ? null : new ApplicationProtocolSelectorAdapter((SSLEngine) this, selector));
    }

    /* access modifiers changed from: package-private */
    public byte[] getTlsUnique() {
        return this.ssl.getTlsUnique();
    }

    /* access modifiers changed from: package-private */
    public void setApplicationProtocolSelector(ApplicationProtocolSelectorAdapter adapter) {
        this.sslParameters.setApplicationProtocolSelector(adapter);
    }

    public String getApplicationProtocol() {
        return SSLUtils.toProtocolString(this.ssl.getApplicationProtocol());
    }

    public String getHandshakeApplicationProtocol() {
        String applicationProtocol;
        synchronized (this.ssl) {
            applicationProtocol = this.state == 2 ? getApplicationProtocol() : null;
        }
        return applicationProtocol;
    }

    private ByteBuffer[] singleSrcBuffer(ByteBuffer src) {
        this.singleSrcBuffer[0] = src;
        return this.singleSrcBuffer;
    }

    private void resetSingleSrcBuffer() {
        this.singleSrcBuffer[0] = null;
    }

    private ByteBuffer[] singleDstBuffer(ByteBuffer src) {
        this.singleDstBuffer[0] = src;
        return this.singleDstBuffer;
    }

    private void resetSingleDstBuffer() {
        this.singleDstBuffer[0] = null;
    }

    private ClientSessionContext clientSessionContext() {
        return this.sslParameters.getClientSessionContext();
    }

    private AbstractSessionContext sessionContext() {
        return this.sslParameters.getSessionContext();
    }

    private void transitionTo(int newState) {
        if (newState == 2) {
            this.handshakeFinished = false;
        } else if (newState == 8 && !this.ssl.isClosed() && this.state >= 2 && this.state < 8) {
            this.closedSession = new SessionSnapshot(this.activeSession);
        }
        this.state = newState;
    }
}
