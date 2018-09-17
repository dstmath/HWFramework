package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeCrypto.SSLHandshakeCallbacks;
import com.android.org.conscrypt.SSLParametersImpl.AliasChooser;
import com.android.org.conscrypt.SSLParametersImpl.PSKCallbacks;
import java.io.IOException;
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
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

final class OpenSSLEngineImpl extends SSLEngine implements SSLHandshakeCallbacks, AliasChooser, PSKCallbacks {
    private static final /* synthetic */ int[] -com-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues = null;
    private static final SSLEngineResult CLOSED_NOT_HANDSHAKING = new SSLEngineResult(Status.CLOSED, HandshakeStatus.NOT_HANDSHAKING, 0, 0);
    private static final ByteBuffer EMPTY = ByteBuffer.allocateDirect(0);
    private static final long EMPTY_ADDR = NativeCrypto.getDirectBufferAddress(EMPTY);
    private static final SSLEngineResult NEED_UNWRAP_CLOSED = new SSLEngineResult(Status.CLOSED, HandshakeStatus.NEED_UNWRAP, 0, 0);
    private static final SSLEngineResult NEED_UNWRAP_OK = new SSLEngineResult(Status.OK, HandshakeStatus.NEED_UNWRAP, 0, 0);
    private static final SSLEngineResult NEED_WRAP_CLOSED = new SSLEngineResult(Status.CLOSED, HandshakeStatus.NEED_WRAP, 0, 0);
    private static final SSLEngineResult NEED_WRAP_OK = new SSLEngineResult(Status.OK, HandshakeStatus.NEED_WRAP, 0, 0);
    private OpenSSLKey channelIdPrivateKey;
    private EngineState engineState;
    private boolean handshakeFinished;
    private HandshakeListener handshakeListener;
    private AbstractOpenSSLSession handshakeSession;
    private int maxSealOverhead;
    private long networkBio;
    private final ByteBuffer[] singleDstBuffer;
    private final ByteBuffer[] singleSrcBuffer;
    private String sniHostname;
    private long sslNativePointer;
    private final SSLParametersImpl sslParameters;
    private AbstractOpenSSLSession sslSession;
    private final Object stateLock;

    private enum EngineState {
        NEW,
        MODE_SET,
        HANDSHAKE_STARTED,
        HANDSHAKE_COMPLETED,
        READY_HANDSHAKE_CUT_THROUGH,
        READY,
        CLOSED_INBOUND,
        CLOSED_OUTBOUND,
        CLOSED
    }

    private static /* synthetic */ int[] -getcom-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues() {
        if (-com-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues != null) {
            return -com-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues;
        }
        int[] iArr = new int[EngineState.values().length];
        try {
            iArr[EngineState.CLOSED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[EngineState.CLOSED_INBOUND.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[EngineState.CLOSED_OUTBOUND.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[EngineState.HANDSHAKE_COMPLETED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[EngineState.HANDSHAKE_STARTED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[EngineState.MODE_SET.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[EngineState.NEW.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[EngineState.READY.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[EngineState.READY_HANDSHAKE_CUT_THROUGH.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        -com-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues = iArr;
        return iArr;
    }

    OpenSSLEngineImpl(SSLParametersImpl sslParameters) {
        this.stateLock = new Object();
        this.engineState = EngineState.NEW;
        this.singleSrcBuffer = new ByteBuffer[1];
        this.singleDstBuffer = new ByteBuffer[1];
        this.sslParameters = sslParameters;
    }

    OpenSSLEngineImpl(String host, int port, SSLParametersImpl sslParameters) {
        super(host, port);
        this.stateLock = new Object();
        this.engineState = EngineState.NEW;
        this.singleSrcBuffer = new ByteBuffer[1];
        this.singleDstBuffer = new ByteBuffer[1];
        this.sslParameters = sslParameters;
    }

    int maxSealOverhead() {
        return this.maxSealOverhead;
    }

    void setChannelIdEnabled(boolean enabled) {
        synchronized (this.stateLock) {
            if (getUseClientMode()) {
                throw new IllegalStateException("Not allowed in client mode");
            } else if (isHandshakeStarted()) {
                throw new IllegalStateException("Could not enable/disable Channel ID after the initial handshake has begun.");
            } else {
                this.sslParameters.channelIdEnabled = enabled;
            }
        }
    }

    byte[] getChannelId() throws SSLException {
        byte[] SSL_get_tls_channel_id;
        synchronized (this.stateLock) {
            if (getUseClientMode()) {
                throw new IllegalStateException("Not allowed in client mode");
            } else if (isHandshakeStarted()) {
                throw new IllegalStateException("Channel ID is only available after handshake completes");
            } else {
                SSL_get_tls_channel_id = NativeCrypto.SSL_get_tls_channel_id(this.sslNativePointer);
            }
        }
        return SSL_get_tls_channel_id;
    }

    void setChannelIdPrivateKey(PrivateKey privateKey) {
        if (getUseClientMode()) {
            synchronized (this.stateLock) {
                if (isHandshakeStarted()) {
                    throw new IllegalStateException("Could not change Channel ID private key after the initial handshake has begun.");
                } else if (privateKey == null) {
                    this.sslParameters.channelIdEnabled = false;
                    this.channelIdPrivateKey = null;
                    return;
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

    void setHandshakeListener(HandshakeListener handshakeListener) {
        synchronized (this.stateLock) {
            if (isHandshakeStarted()) {
                throw new IllegalStateException("Handshake listener must be set before starting the handshake.");
            }
            this.handshakeListener = handshakeListener;
        }
    }

    private boolean isHandshakeStarted() {
        switch (-getcom-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues()[this.engineState.ordinal()]) {
            case 6:
            case 7:
                return false;
            default:
                return true;
        }
    }

    public void beginHandshake() throws SSLException {
        synchronized (this.stateLock) {
            beginHandshakeInternal();
        }
    }

    private void beginHandshakeInternal() throws SSLException {
        switch (-getcom-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues()[this.engineState.ordinal()]) {
            case 1:
            case 2:
            case 3:
                throw new IllegalStateException("Engine has already been closed");
            case NativeConstants.SSL3_RT_HEADER_LENGTH /*5*/:
                throw new IllegalStateException("Handshake has already been started");
            case 6:
                this.engineState = EngineState.HANDSHAKE_STARTED;
                try {
                    this.sslNativePointer = NativeCrypto.SSL_new(this.sslParameters.getSessionContext().sslCtxNativePointer);
                    this.networkBio = NativeCrypto.SSL_BIO_new(this.sslNativePointer);
                    this.sslSession = this.sslParameters.getSessionToReuse(this.sslNativePointer, getSniHostname(), getPeerPort());
                    this.sslParameters.setSSLParameters(this.sslNativePointer, this, this, getSniHostname());
                    this.sslParameters.setCertificateValidation(this.sslNativePointer);
                    this.sslParameters.setTlsChannelId(this.sslNativePointer, this.channelIdPrivateKey);
                    if (getUseClientMode()) {
                        NativeCrypto.SSL_set_connect_state(this.sslNativePointer);
                    } else {
                        NativeCrypto.SSL_set_accept_state(this.sslNativePointer);
                    }
                    this.maxSealOverhead = NativeCrypto.SSL_max_seal_overhead(this.sslNativePointer);
                    handshake();
                    if (false) {
                        this.engineState = EngineState.CLOSED;
                        shutdownAndFreeSslNative();
                        return;
                    }
                    return;
                } catch (IOException e) {
                    if (e.getMessage().contains("unexpected CCS")) {
                        Platform.logEvent(String.format("ssl_unexpected_ccs: host=%s", new Object[]{getSniHostname()}));
                    }
                    throw new SSLException(e);
                } catch (Throwable th) {
                    if (true) {
                        this.engineState = EngineState.CLOSED;
                        shutdownAndFreeSslNative();
                    }
                }
            default:
                throw new IllegalStateException("Client/server mode must be set before handshake");
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0016, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void closeInbound() throws SSLException {
        synchronized (this.stateLock) {
            if (this.engineState == EngineState.CLOSED) {
            } else if (this.engineState == EngineState.CLOSED_OUTBOUND) {
                this.engineState = EngineState.CLOSED;
            } else {
                this.engineState = EngineState.CLOSED_INBOUND;
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0010, code:
            return;
     */
    /* JADX WARNING: Missing block: B:19:0x002b, code:
            shutdown();
     */
    /* JADX WARNING: Missing block: B:20:0x002e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void closeOutbound() {
        synchronized (this.stateLock) {
            if (this.engineState == EngineState.CLOSED || this.engineState == EngineState.CLOSED_OUTBOUND) {
            } else {
                if (!(this.engineState == EngineState.MODE_SET || this.engineState == EngineState.NEW)) {
                    shutdownAndFreeSslNative();
                }
                if (this.engineState == EngineState.CLOSED_INBOUND) {
                    this.engineState = EngineState.CLOSED;
                } else {
                    this.engineState = EngineState.CLOSED_OUTBOUND;
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

    public void setSniHostname(String sniHostname) {
        this.sniHostname = sniHostname;
    }

    public String getSniHostname() {
        return this.sniHostname != null ? this.sniHostname : getPeerHost();
    }

    public void setSSLParameters(SSLParameters p) {
        super.setSSLParameters(p);
        Platform.setSSLParameters(p, this.sslParameters, this);
    }

    public HandshakeStatus getHandshakeStatus() {
        HandshakeStatus handshakeStatusInternal;
        synchronized (this.stateLock) {
            handshakeStatusInternal = getHandshakeStatusInternal();
        }
        return handshakeStatusInternal;
    }

    private HandshakeStatus getHandshakeStatusInternal() {
        if (this.handshakeFinished) {
            return HandshakeStatus.NOT_HANDSHAKING;
        }
        switch (-getcom-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues()[this.engineState.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 6:
            case 7:
            case 8:
            case 9:
                return HandshakeStatus.NOT_HANDSHAKING;
            case 4:
                return HandshakeStatus.NEED_WRAP;
            case NativeConstants.SSL3_RT_HEADER_LENGTH /*5*/:
                return pendingStatus(pendingOutboundEncryptedBytes());
            default:
                throw new IllegalStateException("Unexpected engine state: " + this.engineState);
        }
    }

    private int pendingOutboundEncryptedBytes() {
        return NativeCrypto.SSL_pending_written_bytes_in_BIO(this.networkBio);
    }

    private int pendingInboundCleartextBytes() {
        return NativeCrypto.SSL_pending_readable_bytes(this.sslNativePointer);
    }

    private static HandshakeStatus pendingStatus(int pendingOutboundBytes) {
        return pendingOutboundBytes > 0 ? HandshakeStatus.NEED_WRAP : HandshakeStatus.NEED_UNWRAP;
    }

    public boolean getNeedClientAuth() {
        return this.sslParameters.getNeedClientAuth();
    }

    public SSLSession getSession() {
        if (this.sslSession != null) {
            return Platform.wrapSSLSession(this.sslSession);
        }
        SSLSession wrapSSLSession;
        if (this.handshakeSession != null) {
            wrapSSLSession = Platform.wrapSSLSession(this.handshakeSession);
        } else {
            wrapSSLSession = SSLNullSession.getNullSession();
        }
        return wrapSSLSession;
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
        boolean z = true;
        if (this.sslNativePointer == 0) {
            synchronized (this.stateLock) {
                if (!(this.engineState == EngineState.CLOSED || this.engineState == EngineState.CLOSED_INBOUND)) {
                    z = false;
                }
            }
            return z;
        }
        if ((NativeCrypto.SSL_get_shutdown(this.sslNativePointer) & 2) == 0) {
            z = false;
        }
        return z;
    }

    public boolean isOutboundDone() {
        boolean z = true;
        if (this.sslNativePointer == 0) {
            synchronized (this.stateLock) {
                if (!(this.engineState == EngineState.CLOSED || this.engineState == EngineState.CLOSED_OUTBOUND)) {
                    z = false;
                }
            }
            return z;
        }
        if ((NativeCrypto.SSL_get_shutdown(this.sslNativePointer) & 1) == 0) {
            z = false;
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
        synchronized (this.stateLock) {
            if (this.engineState == EngineState.MODE_SET || this.engineState == EngineState.NEW) {
                this.engineState = EngineState.MODE_SET;
            } else {
                throw new IllegalArgumentException("Can not change mode after handshake: engineState == " + this.engineState);
            }
        }
        this.sslParameters.setUseClientMode(mode);
    }

    public void setWantClientAuth(boolean want) {
        this.sslParameters.setWantClientAuth(want);
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        SSLEngineResult unwrap;
        synchronized (this.stateLock) {
            try {
                unwrap = unwrap(singleSrcBuffer(src), singleDstBuffer(dst));
                resetSingleSrcBuffer();
                resetSingleDstBuffer();
            } catch (Throwable th) {
                resetSingleSrcBuffer();
                resetSingleDstBuffer();
            }
        }
        return unwrap;
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts) throws SSLException {
        SSLEngineResult unwrap;
        synchronized (this.stateLock) {
            try {
                unwrap = unwrap(singleSrcBuffer(src), dsts);
                resetSingleSrcBuffer();
            } catch (Throwable th) {
                resetSingleSrcBuffer();
            }
        }
        return unwrap;
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length) throws SSLException {
        SSLEngineResult unwrap;
        synchronized (this.stateLock) {
            try {
                unwrap = unwrap(singleSrcBuffer(src), 0, 1, dsts, offset, length);
                resetSingleSrcBuffer();
            } catch (Throwable th) {
                resetSingleSrcBuffer();
            }
        }
        return unwrap;
    }

    SSLEngineResult unwrap(ByteBuffer[] srcs, ByteBuffer[] dsts) throws SSLException {
        checkNotNull(srcs, "srcs", new Object[0]);
        checkNotNull(dsts, "dsts", new Object[0]);
        return unwrap(srcs, 0, srcs.length, dsts, 0, dsts.length);
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    SSLEngineResult unwrap(ByteBuffer[] srcs, int srcsOffset, int srcsLength, ByteBuffer[] dsts, int dstsOffset, int dstsLength) throws SSLException {
        checkNotNull(srcs, "srcs", new Object[0]);
        checkNotNull(dsts, "dsts", new Object[0]);
        checkIndex(srcs.length, srcsOffset, srcsLength, "srcs");
        checkIndex(dsts.length, dstsOffset, dstsLength, "dsts");
        int dstLength = calcDstsLength(dsts, dstsOffset, dstsLength);
        int endOffset = dstsOffset + dstsLength;
        int srcsEndOffset = srcsOffset + srcsLength;
        long srcLength = calcSrcsLength(srcs, srcsOffset, srcsEndOffset);
        synchronized (this.stateLock) {
            SSLEngineResult sSLEngineResult;
            switch (-getcom-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues()[this.engineState.ordinal()]) {
                case 1:
                case 2:
                    sSLEngineResult = new SSLEngineResult(Status.CLOSED, getHandshakeStatusInternal(), 0, 0);
                    return sSLEngineResult;
                case 6:
                    beginHandshakeInternal();
                case 7:
                    throw new IllegalStateException("Client/server mode must be set before calling unwrap");
                default:
                    HandshakeStatus handshakeStatus = HandshakeStatus.NOT_HANDSHAKING;
                    if (!this.handshakeFinished) {
                        handshakeStatus = handshake();
                        if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                            sSLEngineResult = NEED_WRAP_OK;
                            return sSLEngineResult;
                        } else if (this.engineState == EngineState.CLOSED) {
                            sSLEngineResult = NEED_WRAP_CLOSED;
                            return sSLEngineResult;
                        }
                    }
                    boolean noCleartextDataAvailable = pendingInboundCleartextBytes() <= 0;
                    int lenRemaining = 0;
                    if (srcLength <= 0 || !noCleartextDataAvailable) {
                        if (noCleartextDataAvailable) {
                            sSLEngineResult = new SSLEngineResult(Status.BUFFER_UNDERFLOW, getHandshakeStatus(), 0, 0);
                            return sSLEngineResult;
                        }
                    } else if (srcLength < 5) {
                        sSLEngineResult = new SSLEngineResult(Status.BUFFER_UNDERFLOW, getHandshakeStatus(), 0, 0);
                        return sSLEngineResult;
                    } else {
                        int packetLength = SSLUtils.getEncryptedPacketLength(srcs, srcsOffset);
                        if (packetLength < 0) {
                            throw new SSLException("Unable to parse TLS packet header");
                        } else if (srcLength < ((long) packetLength)) {
                            sSLEngineResult = new SSLEngineResult(Status.BUFFER_UNDERFLOW, getHandshakeStatus(), 0, 0);
                            return sSLEngineResult;
                        } else {
                            lenRemaining = packetLength;
                        }
                    }
                    int bytesConsumed = 0;
                    if (lenRemaining > 0 && srcsOffset < srcsEndOffset) {
                        do {
                            ByteBuffer src = srcs[srcsOffset];
                            int remaining = src.remaining();
                            if (remaining == 0) {
                                srcsOffset++;
                            } else {
                                int written = writeEncryptedData(src, Math.min(lenRemaining, remaining));
                                if (written > 0) {
                                    bytesConsumed += written;
                                    lenRemaining -= written;
                                    if (lenRemaining != 0 && written == remaining) {
                                        srcsOffset++;
                                    }
                                } else {
                                    NativeCrypto.SSL_clear_error();
                                }
                            }
                        } while (srcsOffset < srcsEndOffset);
                    }
                    int bytesProduced = 0;
                    if (dstLength > 0) {
                        for (int idx = dstsOffset; idx < endOffset; idx++) {
                            ByteBuffer dst = dsts[idx];
                            if (dst.hasRemaining()) {
                                int bytesRead = readPlaintextData(dst);
                                if (bytesRead > 0) {
                                    bytesProduced += bytesRead;
                                    if (dst.hasRemaining()) {
                                    }
                                } else {
                                    switch (NativeCrypto.SSL_get_error(this.sslNativePointer, bytesRead)) {
                                        case 2:
                                        case 3:
                                            sSLEngineResult = newResult(bytesConsumed, bytesProduced, handshakeStatus);
                                            return sSLEngineResult;
                                        case 6:
                                            closeAll();
                                            sSLEngineResult = newResult(bytesConsumed, bytesProduced, handshakeStatus);
                                            return sSLEngineResult;
                                        default:
                                            sSLEngineResult = sslReadErrorResult(NativeCrypto.SSL_get_last_error_number(), bytesConsumed, bytesProduced);
                                            return sSLEngineResult;
                                    }
                                }
                            }
                        }
                    } else {
                        try {
                            if (NativeCrypto.ENGINE_SSL_read_direct(this.sslNativePointer, EMPTY_ADDR, 0, this) <= 0) {
                                int err = NativeCrypto.SSL_get_last_error_number();
                                if (err != 0) {
                                    sSLEngineResult = sslReadErrorResult(err, bytesConsumed, 0);
                                    return sSLEngineResult;
                                }
                            }
                        } catch (IOException e) {
                            throw new SSLException(e);
                        }
                    }
                    if ((this.handshakeFinished ? pendingInboundCleartextBytes() : 0) > 0) {
                        Status status = Status.BUFFER_OVERFLOW;
                        if (handshakeStatus != HandshakeStatus.FINISHED) {
                            handshakeStatus = getHandshakeStatusInternal();
                        }
                        sSLEngineResult = new SSLEngineResult(status, mayFinishHandshake(handshakeStatus), bytesConsumed, bytesProduced);
                        return sSLEngineResult;
                    }
                    sSLEngineResult = newResult(bytesConsumed, bytesProduced, handshakeStatus);
                    return sSLEngineResult;
            }
        }
    }

    private static int calcDstsLength(ByteBuffer[] dsts, int dstsOffset, int dstsLength) {
        int capacity = 0;
        int i = 0;
        while (i < dsts.length) {
            ByteBuffer dst = dsts[i];
            checkNotNull(dst, "one of the dst", new Object[0]);
            if (dst.isReadOnly()) {
                throw new ReadOnlyBufferException();
            }
            if (i >= dstsOffset && i < dstsOffset + dstsLength) {
                capacity += dst.remaining();
            }
            i++;
        }
        return capacity;
    }

    private static long calcSrcsLength(ByteBuffer[] srcs, int srcsOffset, int srcsEndOffset) {
        long len = 0;
        for (int i = srcsOffset; i < srcsEndOffset; i++) {
            ByteBuffer src = srcs[i];
            if (src == null) {
                throw new IllegalArgumentException("srcs[" + i + "] is null");
            }
            len += (long) src.remaining();
        }
        return len;
    }

    private HandshakeStatus handshake() throws SSLException {
        long sslSessionCtx = 0;
        try {
            int code = NativeCrypto.ENGINE_SSL_do_handshake(this.sslNativePointer, this);
            HandshakeStatus pendingStatus;
            if (code <= 0) {
                switch (NativeCrypto.SSL_get_error(this.sslNativePointer, code)) {
                    case 2:
                    case 3:
                        pendingStatus = pendingStatus(pendingOutboundEncryptedBytes());
                        AbstractOpenSSLSession abstractOpenSSLSession = this.sslSession;
                        return pendingStatus;
                    default:
                        throw shutdownWithError("SSL_do_handshake");
                }
                throw SSLUtils.toSSLHandshakeException(e);
            }
            sslSessionCtx = NativeCrypto.SSL_get1_session(this.sslNativePointer);
            if (sslSessionCtx == 0) {
                throw shutdownWithError("Failed to obtain session after handshake completed");
            }
            this.sslSession = this.sslParameters.setupSession(sslSessionCtx, this.sslNativePointer, this.sslSession, getSniHostname(), getPeerPort(), true);
            if (this.sslSession == null || this.engineState != EngineState.HANDSHAKE_STARTED) {
                this.engineState = EngineState.READY;
            } else {
                this.engineState = EngineState.READY_HANDSHAKE_CUT_THROUGH;
            }
            finishHandshake();
            pendingStatus = HandshakeStatus.FINISHED;
            if (this.sslSession == null && sslSessionCtx != 0) {
                NativeCrypto.SSL_SESSION_free(sslSessionCtx);
            }
            return pendingStatus;
        } catch (Exception e) {
            throw SSLUtils.toSSLHandshakeException(e);
        } catch (Throwable th) {
            if (this.sslSession == null && sslSessionCtx != 0) {
                NativeCrypto.SSL_SESSION_free(sslSessionCtx);
            }
        }
    }

    private void finishHandshake() throws SSLException {
        this.handshakeFinished = true;
        if (this.handshakeListener != null) {
            this.handshakeListener.onHandshakeFinished();
        }
    }

    private int writePlaintextData(ByteBuffer src, int len) throws SSLException {
        try {
            int sslWrote;
            int pos = src.position();
            if (src.isDirect()) {
                sslWrote = NativeCrypto.ENGINE_SSL_write_direct(this.sslNativePointer, NativeCrypto.getDirectBufferAddress(src) + ((long) pos), len, this);
            } else {
                ByteBuffer heapSrc = toHeapBuffer(src, len);
                sslWrote = NativeCrypto.ENGINE_SSL_write_heap(this.sslNativePointer, heapSrc.array(), heapSrc.arrayOffset() + heapSrc.position(), len, this);
            }
            if (sslWrote > 0) {
                src.position(pos + sslWrote);
            }
            return sslWrote;
        } catch (Exception e) {
            throw convertException(e);
        }
    }

    private int readPlaintextData(ByteBuffer dst) throws SSLException {
        try {
            int sslRead;
            int pos = dst.position();
            int len = Math.min(NativeConstants.SSL3_RT_MAX_PACKET_SIZE, dst.limit() - pos);
            if (dst.isDirect()) {
                sslRead = NativeCrypto.ENGINE_SSL_read_direct(this.sslNativePointer, NativeCrypto.getDirectBufferAddress(dst) + ((long) pos), len, this);
                if (sslRead > 0) {
                    dst.position(pos + sslRead);
                }
            } else if (dst.hasArray()) {
                sslRead = NativeCrypto.ENGINE_SSL_read_heap(this.sslNativePointer, dst.array(), dst.arrayOffset() + pos, len, this);
                if (sslRead > 0) {
                    dst.position(pos + sslRead);
                }
            } else {
                byte[] data = new byte[len];
                sslRead = NativeCrypto.ENGINE_SSL_read_heap(this.sslNativePointer, data, 0, len, this);
                if (sslRead > 0) {
                    dst.put(data, 0, sslRead);
                }
            }
            return sslRead;
        } catch (Exception e) {
            throw convertException(e);
        }
    }

    private SSLException convertException(Throwable e) {
        if ((e instanceof SSLHandshakeException) || (this.handshakeFinished ^ 1) != 0) {
            return SSLUtils.toSSLHandshakeException(e);
        }
        return SSLUtils.toSSLException(e);
    }

    private int writeEncryptedData(ByteBuffer src, int len) throws SSLException {
        try {
            int netWrote;
            int pos = src.position();
            if (src.isDirect()) {
                netWrote = NativeCrypto.ENGINE_SSL_write_BIO_direct(this.sslNativePointer, this.networkBio, NativeCrypto.getDirectBufferAddress(src) + ((long) pos), len, this);
            } else {
                ByteBuffer heapSrc = toHeapBuffer(src, len);
                netWrote = NativeCrypto.ENGINE_SSL_write_BIO_heap(this.sslNativePointer, this.networkBio, heapSrc.array(), heapSrc.arrayOffset() + heapSrc.position(), len, this);
            }
            if (netWrote >= 0) {
                src.position(pos + netWrote);
            }
            return netWrote;
        } catch (Throwable e) {
            throw new SSLException(e);
        }
    }

    private SSLEngineResult readPendingBytesFromBIO(ByteBuffer dst, int bytesConsumed, int bytesProduced, HandshakeStatus status) throws SSLException {
        try {
            int pendingNet = pendingOutboundEncryptedBytes();
            if (pendingNet <= 0) {
                return null;
            }
            Status status2;
            if (dst.remaining() < pendingNet) {
                status2 = Status.BUFFER_OVERFLOW;
                if (status != HandshakeStatus.FINISHED) {
                    status = getHandshakeStatus(pendingNet);
                }
                return new SSLEngineResult(status2, mayFinishHandshake(status), bytesConsumed, bytesProduced);
            }
            int produced = readEncryptedData(dst, pendingNet);
            if (produced <= 0) {
                NativeCrypto.SSL_clear_error();
            } else {
                bytesProduced += produced;
                pendingNet -= produced;
            }
            status2 = getEngineStatus();
            if (status != HandshakeStatus.FINISHED) {
                status = getHandshakeStatus(pendingNet);
            }
            return new SSLEngineResult(status2, mayFinishHandshake(status), bytesConsumed, bytesProduced);
        } catch (Exception e) {
            throw convertException(e);
        }
    }

    private int readEncryptedData(ByteBuffer dst, int pending) throws SSLException {
        int bioRead = 0;
        try {
            if (dst.remaining() >= pending) {
                int pos = dst.position();
                int len = Math.min(pending, dst.limit() - pos);
                if (dst.isDirect()) {
                    bioRead = NativeCrypto.ENGINE_SSL_read_BIO_direct(this.sslNativePointer, this.networkBio, NativeCrypto.getDirectBufferAddress(dst) + ((long) pos), len, this);
                    if (bioRead > 0) {
                        dst.position(pos + bioRead);
                        return bioRead;
                    }
                } else if (dst.hasArray()) {
                    bioRead = NativeCrypto.ENGINE_SSL_read_BIO_heap(this.sslNativePointer, this.networkBio, dst.array(), dst.arrayOffset() + pos, pending, this);
                    if (bioRead > 0) {
                        dst.position(pos + bioRead);
                        return bioRead;
                    }
                } else {
                    byte[] data = new byte[len];
                    bioRead = NativeCrypto.ENGINE_SSL_read_BIO_heap(this.sslNativePointer, this.networkBio, data, 0, pending, this);
                    if (bioRead > 0) {
                        dst.put(data, 0, bioRead);
                        return bioRead;
                    }
                }
            }
            return bioRead;
        } catch (Throwable e) {
            throw convertException(e);
        }
    }

    private HandshakeStatus mayFinishHandshake(HandshakeStatus status) throws SSLException {
        if (this.handshakeFinished || status != HandshakeStatus.NOT_HANDSHAKING) {
            return status;
        }
        return handshake();
    }

    private HandshakeStatus getHandshakeStatus(int pending) {
        return !this.handshakeFinished ? pendingStatus(pending) : HandshakeStatus.NOT_HANDSHAKING;
    }

    private Status getEngineStatus() {
        switch (-getcom-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues()[this.engineState.ordinal()]) {
            case 1:
            case 2:
            case 3:
                return Status.CLOSED;
            default:
                return Status.OK;
        }
    }

    private void closeAll() throws SSLException {
        closeOutbound();
        closeInbound();
    }

    private SSLEngineResult sslReadErrorResult(int err, int bytesConsumed, int bytesProduced) throws SSLException {
        if (!this.handshakeFinished && pendingOutboundEncryptedBytes() > 0) {
            return new SSLEngineResult(Status.OK, HandshakeStatus.NEED_WRAP, bytesConsumed, bytesProduced);
        }
        throw shutdownWithError(NativeCrypto.SSL_get_error_string((long) err));
    }

    private SSLException shutdownWithError(String err) {
        shutdown();
        if (getHandshakeStatusInternal() == HandshakeStatus.FINISHED) {
            return new SSLException(err);
        }
        return new SSLHandshakeException(err);
    }

    private SSLEngineResult newResult(int bytesConsumed, int bytesProduced, HandshakeStatus status) throws SSLException {
        Status engineStatus = getEngineStatus();
        if (status != HandshakeStatus.FINISHED) {
            status = getHandshakeStatusInternal();
        }
        return new SSLEngineResult(engineStatus, mayFinishHandshake(status), bytesConsumed, bytesProduced);
    }

    public final SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        SSLEngineResult wrap;
        synchronized (this.stateLock) {
            try {
                wrap = wrap(singleSrcBuffer(src), dst);
                resetSingleSrcBuffer();
            } catch (Throwable th) {
                resetSingleSrcBuffer();
            }
        }
        return wrap;
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x013a  */
    /* JADX WARNING: Missing block: B:83:0x016a, code:
            return r7;
     */
    /* JADX WARNING: Missing block: B:89:0x0179, code:
            return r7;
     */
    /* JADX WARNING: Missing block: B:95:0x0191, code:
            return r7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst) throws SSLException {
        checkNotNull(srcs, "srcs", new Object[0]);
        checkNotNull(dst, "dst", new Object[0]);
        checkIndex(srcs.length, offset, length, "srcs");
        if (dst.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        synchronized (this.stateLock) {
            SSLEngineResult sSLEngineResult;
            switch (-getcom-android-org-conscrypt-OpenSSLEngineImpl$EngineStateSwitchesValues()[this.engineState.ordinal()]) {
                case 1:
                case 3:
                    sSLEngineResult = new SSLEngineResult(Status.CLOSED, getHandshakeStatusInternal(), 0, 0);
                    return sSLEngineResult;
                case 6:
                    beginHandshakeInternal();
                case 7:
                    throw new IllegalStateException("Client/server mode must be set before calling wrap");
                default:
                    int i;
                    ByteBuffer src;
                    HandshakeStatus handshakeStatus = HandshakeStatus.NOT_HANDSHAKING;
                    if (!this.handshakeFinished) {
                        handshakeStatus = handshake();
                        if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                            sSLEngineResult = NEED_UNWRAP_OK;
                            return sSLEngineResult;
                        } else if (this.engineState == EngineState.CLOSED) {
                            sSLEngineResult = NEED_UNWRAP_CLOSED;
                            return sSLEngineResult;
                        }
                    }
                    int srcsLen = 0;
                    int endOffset = offset + length;
                    for (i = offset; i < endOffset; i++) {
                        src = srcs[i];
                        if (src == null) {
                            throw new IllegalArgumentException("srcs[" + i + "] is null");
                        }
                        if (srcsLen != 16384) {
                            srcsLen += src.remaining();
                            if (srcsLen > 16384 || srcsLen < 0) {
                                srcsLen = 16384;
                            }
                        }
                    }
                    if (dst.remaining() < SSLUtils.calculateOutNetBufSize(srcsLen)) {
                        sSLEngineResult = new SSLEngineResult(Status.BUFFER_OVERFLOW, getHandshakeStatusInternal(), 0, 0);
                        return sSLEngineResult;
                    }
                    int bytesProduced = 0;
                    int bytesConsumed = 0;
                    for (i = offset; i < endOffset; i++) {
                        src = srcs[i];
                        checkNotNull(src, "srcs[%d] is null", Integer.valueOf(i));
                        while (src.hasRemaining()) {
                            int result = writePlaintextData(src, Math.min(src.remaining(), 16384 - bytesConsumed));
                            SSLEngineResult pendingNetResult;
                            if (result > 0) {
                                bytesConsumed += result;
                                pendingNetResult = readPendingBytesFromBIO(dst, bytesConsumed, bytesProduced, handshakeStatus);
                                if (pendingNetResult != null) {
                                    if (pendingNetResult.getStatus() != Status.OK) {
                                        return pendingNetResult;
                                    }
                                    bytesProduced = pendingNetResult.bytesProduced();
                                }
                                if (bytesConsumed == 16384) {
                                    if (bytesConsumed == 0) {
                                        pendingNetResult = readPendingBytesFromBIO(dst, 0, bytesProduced, handshakeStatus);
                                        if (pendingNetResult != null) {
                                            return pendingNetResult;
                                        }
                                    }
                                    sSLEngineResult = newResult(bytesConsumed, bytesProduced, handshakeStatus);
                                    return sSLEngineResult;
                                }
                            }
                            switch (NativeCrypto.SSL_get_error(this.sslNativePointer, result)) {
                                case 2:
                                    pendingNetResult = readPendingBytesFromBIO(dst, bytesConsumed, bytesProduced, handshakeStatus);
                                    if (pendingNetResult == null) {
                                        pendingNetResult = new SSLEngineResult(getEngineStatus(), HandshakeStatus.NEED_UNWRAP, bytesConsumed, bytesProduced);
                                        break;
                                    }
                                    break;
                                case 3:
                                    pendingNetResult = readPendingBytesFromBIO(dst, bytesConsumed, bytesProduced, handshakeStatus);
                                    if (pendingNetResult == null) {
                                        pendingNetResult = NEED_WRAP_CLOSED;
                                        break;
                                    }
                                    break;
                                case 6:
                                    closeAll();
                                    pendingNetResult = readPendingBytesFromBIO(dst, bytesConsumed, bytesProduced, handshakeStatus);
                                    if (pendingNetResult == null) {
                                        pendingNetResult = CLOSED_NOT_HANDSHAKING;
                                        break;
                                    }
                                    break;
                                default:
                                    throw shutdownWithError("SSL_write");
                            }
                        }
                    }
                    if (bytesConsumed == 0) {
                    }
                    sSLEngineResult = newResult(bytesConsumed, bytesProduced, handshakeStatus);
                    return sSLEngineResult;
            }
        }
    }

    public int clientPSKKeyRequested(String identityHint, byte[] identity, byte[] key) {
        return this.sslParameters.clientPSKKeyRequested(identityHint, identity, key, this);
    }

    public int serverPSKKeyRequested(String identityHint, String identity, byte[] key) {
        return this.sslParameters.serverPSKKeyRequested(identityHint, identity, key, this);
    }

    public void onSSLStateChange(int type, int val) {
        synchronized (this.stateLock) {
            switch (type) {
                case 16:
                    this.engineState = EngineState.HANDSHAKE_STARTED;
                    break;
                case 32:
                    if (this.engineState == EngineState.HANDSHAKE_STARTED || this.engineState == EngineState.READY_HANDSHAKE_CUT_THROUGH) {
                        this.engineState = EngineState.HANDSHAKE_COMPLETED;
                        break;
                    }
                    throw new IllegalStateException("Completed handshake while in mode " + this.engineState);
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
                    this.handshakeSession = new OpenSSLSessionImpl(NativeCrypto.SSL_get1_session(this.sslNativePointer), null, peerCertChain, NativeCrypto.SSL_get_ocsp_response(this.sslNativePointer), NativeCrypto.SSL_get_signed_cert_timestamp_list(this.sslNativePointer), getSniHostname(), getPeerPort(), null);
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

    public void clientCertificateRequested(byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals) throws CertificateEncodingException, SSLException {
        this.sslParameters.chooseClientCertificate(keyTypeBytes, asn1DerEncodedPrincipals, this.sslNativePointer, this);
    }

    private void shutdown() {
        try {
            NativeCrypto.ENGINE_SSL_shutdown(this.sslNativePointer, this);
        } catch (IOException e) {
        }
    }

    private void shutdownAndFreeSslNative() {
        try {
            shutdown();
        } finally {
            free();
        }
    }

    private void free() {
        if (this.sslNativePointer != 0) {
            NativeCrypto.SSL_free(this.sslNativePointer);
            NativeCrypto.BIO_free_all(this.networkBio);
            this.sslNativePointer = 0;
            this.networkBio = 0;
        }
    }

    protected void finalize() throws Throwable {
        try {
            free();
        } finally {
            super.finalize();
        }
    }

    public SSLSession getHandshakeSession() {
        return this.handshakeSession;
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

    void setUseSessionTickets(boolean useSessionTickets) {
        this.sslParameters.setUseSessionTickets(useSessionTickets);
    }

    void setAlpnProtocols(String[] alpnProtocols) {
        this.sslParameters.setAlpnProtocols(alpnProtocols);
    }

    byte[] getAlpnSelectedProtocol() {
        return NativeCrypto.SSL_get0_alpn_selected(this.sslNativePointer);
    }

    private ByteBuffer toHeapBuffer(ByteBuffer buffer, int len) {
        if (buffer.hasArray()) {
            return buffer;
        }
        ByteBuffer heapBuffer = ByteBuffer.allocate(len);
        int pos = buffer.position();
        int limit = buffer.limit();
        buffer.limit(pos + len);
        try {
            heapBuffer.put(buffer);
            heapBuffer.flip();
            return heapBuffer;
        } finally {
            buffer.limit(limit);
            buffer.position(pos);
        }
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

    private static void checkIndex(int arrayLength, int offset, int length, String arrayName) {
        if ((offset | length) < 0 || offset + length > arrayLength) {
            throw new IndexOutOfBoundsException("offset: " + offset + ", length: " + length + " (expected: offset <= offset + length <= " + arrayName + ".length (" + arrayLength + "))");
        }
    }

    private static <T> T checkNotNull(T obj, String fmt, Object... args) {
        if (obj != null) {
            return obj;
        }
        throw new IllegalArgumentException(String.format(fmt, args));
    }
}
