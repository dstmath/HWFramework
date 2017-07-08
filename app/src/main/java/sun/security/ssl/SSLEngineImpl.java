package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AlgorithmConstraints;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLSession;
import sun.security.util.DerValue;
import sun.security.x509.GeneralNameInterface;

public final class SSLEngineImpl extends SSLEngine {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    static final byte clauth_none = (byte) 0;
    static final byte clauth_requested = (byte) 1;
    static final byte clauth_required = (byte) 2;
    private static final int cs_CLOSED = 6;
    private static final int cs_DATA = 2;
    private static final int cs_ERROR = 4;
    private static final int cs_HANDSHAKE = 1;
    private static final int cs_RENEGOTIATE = 3;
    private static final int cs_START = 0;
    private static final Debug debug = null;
    private AccessControlContext acc;
    private AlgorithmConstraints algorithmConstraints;
    private byte[] clientVerifyData;
    private SSLException closeReason;
    private int connectionState;
    private byte doClientAuth;
    private boolean enableSessionCreation;
    private CipherSuiteList enabledCipherSuites;
    private ProtocolList enabledProtocols;
    private boolean expectingFinished;
    private volatile SSLSessionImpl handshakeSession;
    private Handshaker handshaker;
    private String identificationProtocol;
    private boolean inboundDone;
    EngineInputRecord inputRecord;
    private boolean isFirstAppOutputRecord;
    EngineOutputRecord outputRecord;
    private ProtocolVersion protocolVersion;
    private CipherBox readCipher;
    private MAC readMAC;
    private boolean recvCN;
    private boolean roleIsServer;
    private boolean secureRenegotiation;
    private boolean serverModeSet;
    private byte[] serverVerifyData;
    private SSLSessionImpl sess;
    private SSLContextImpl sslContext;
    private Object unwrapLock;
    private Object wrapLock;
    private CipherBox writeCipher;
    Object writeLock;
    private MAC writeMAC;
    EngineWriter writer;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SSLEngineImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SSLEngineImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SSLEngineImpl.<clinit>():void");
    }

    SSLEngineImpl(SSLContextImpl ctx) {
        this.inboundDone = -assertionsDisabled;
        this.enableSessionCreation = true;
        this.identificationProtocol = null;
        this.algorithmConstraints = null;
        this.serverModeSet = -assertionsDisabled;
        this.protocolVersion = ProtocolVersion.DEFAULT;
        this.isFirstAppOutputRecord = true;
        init(ctx);
    }

    SSLEngineImpl(SSLContextImpl ctx, String host, int port) {
        super(host, port);
        this.inboundDone = -assertionsDisabled;
        this.enableSessionCreation = true;
        this.identificationProtocol = null;
        this.algorithmConstraints = null;
        this.serverModeSet = -assertionsDisabled;
        this.protocolVersion = ProtocolVersion.DEFAULT;
        this.isFirstAppOutputRecord = true;
        init(ctx);
    }

    private void init(SSLContextImpl ctx) {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println("Using SSLEngineImpl.");
        }
        this.sslContext = ctx;
        this.sess = SSLSessionImpl.nullSession;
        this.handshakeSession = null;
        this.roleIsServer = true;
        this.connectionState = 0;
        this.readCipher = CipherBox.NULL;
        this.readMAC = MAC.NULL;
        this.writeCipher = CipherBox.NULL;
        this.writeMAC = MAC.NULL;
        this.secureRenegotiation = -assertionsDisabled;
        this.clientVerifyData = new byte[0];
        this.serverVerifyData = new byte[0];
        this.enabledCipherSuites = this.sslContext.getDefaultCipherSuiteList(this.roleIsServer);
        this.enabledProtocols = this.sslContext.getDefaultProtocolList(this.roleIsServer);
        this.wrapLock = new Object();
        this.unwrapLock = new Object();
        this.writeLock = new Object();
        this.acc = AccessController.getContext();
        this.outputRecord = new EngineOutputRecord(DerValue.tag_UtcTime, this);
        this.inputRecord = new EngineInputRecord(this);
        this.inputRecord.enableFormatChecks();
        this.writer = new EngineWriter();
    }

    private void initHandshaker() {
        boolean z = true;
        switch (this.connectionState) {
            case GeneralNameInterface.NAME_MATCH /*0*/:
            case cs_DATA /*2*/:
                if (this.connectionState == 0) {
                    this.connectionState = cs_HANDSHAKE;
                } else {
                    this.connectionState = cs_RENEGOTIATE;
                }
                SSLContextImpl sSLContextImpl;
                ProtocolList protocolList;
                if (this.roleIsServer) {
                    sSLContextImpl = this.sslContext;
                    protocolList = this.enabledProtocols;
                    byte b = this.doClientAuth;
                    ProtocolVersion protocolVersion = this.protocolVersion;
                    if (this.connectionState != cs_HANDSHAKE) {
                        z = -assertionsDisabled;
                    }
                    this.handshaker = new ServerHandshaker(this, sSLContextImpl, protocolList, b, protocolVersion, z, this.secureRenegotiation, this.clientVerifyData, this.serverVerifyData);
                } else {
                    boolean z2;
                    sSLContextImpl = this.sslContext;
                    protocolList = this.enabledProtocols;
                    ProtocolVersion protocolVersion2 = this.protocolVersion;
                    if (this.connectionState == cs_HANDSHAKE) {
                        z2 = true;
                    } else {
                        z2 = -assertionsDisabled;
                    }
                    this.handshaker = new ClientHandshaker(this, sSLContextImpl, protocolList, protocolVersion2, z2, this.secureRenegotiation, this.clientVerifyData, this.serverVerifyData);
                }
                this.handshaker.setEnabledCipherSuites(this.enabledCipherSuites);
                this.handshaker.setEnableSessionCreation(this.enableSessionCreation);
            case cs_HANDSHAKE /*1*/:
            case cs_RENEGOTIATE /*3*/:
            default:
                throw new IllegalStateException("Internal error");
        }
    }

    private HandshakeStatus getHSStatus(HandshakeStatus hss) {
        if (hss != null) {
            return hss;
        }
        synchronized (this) {
            if (this.writer.hasOutboundData()) {
                HandshakeStatus handshakeStatus = HandshakeStatus.NEED_WRAP;
                return handshakeStatus;
            }
            if (this.handshaker != null) {
                if (this.handshaker.taskOutstanding()) {
                    handshakeStatus = HandshakeStatus.NEED_TASK;
                    return handshakeStatus;
                }
                handshakeStatus = HandshakeStatus.NEED_UNWRAP;
                return handshakeStatus;
            } else if (this.connectionState != cs_CLOSED || isInboundDone()) {
                handshakeStatus = HandshakeStatus.NOT_HANDSHAKING;
                return handshakeStatus;
            } else {
                handshakeStatus = HandshakeStatus.NEED_UNWRAP;
                return handshakeStatus;
            }
        }
    }

    private synchronized void checkTaskThrown() throws SSLException {
        if (this.handshaker != null) {
            this.handshaker.checkThrown();
        }
    }

    private synchronized int getConnectionState() {
        return this.connectionState;
    }

    private synchronized void setConnectionState(int state) {
        this.connectionState = state;
    }

    AccessControlContext getAcc() {
        return this.acc;
    }

    public HandshakeStatus getHandshakeStatus() {
        return getHSStatus(null);
    }

    private void changeReadCiphers() throws SSLException {
        if (this.connectionState == cs_HANDSHAKE || this.connectionState == cs_RENEGOTIATE) {
            CipherBox oldCipher = this.readCipher;
            try {
                this.readCipher = this.handshaker.newReadCipher();
                this.readMAC = this.handshaker.newReadMAC();
                oldCipher.dispose();
                return;
            } catch (GeneralSecurityException e) {
                throw ((SSLException) new SSLException("Algorithm missing:  ").initCause(e));
            }
        }
        throw new SSLProtocolException("State error, change cipher specs");
    }

    void changeWriteCiphers() throws SSLException {
        if (this.connectionState == cs_HANDSHAKE || this.connectionState == cs_RENEGOTIATE) {
            CipherBox oldCipher = this.writeCipher;
            try {
                this.writeCipher = this.handshaker.newWriteCipher();
                this.writeMAC = this.handshaker.newWriteMAC();
                oldCipher.dispose();
                this.isFirstAppOutputRecord = true;
                return;
            } catch (GeneralSecurityException e) {
                throw ((SSLException) new SSLException("Algorithm missing:  ").initCause(e));
            }
        }
        throw new SSLProtocolException("State error, change cipher specs");
    }

    synchronized void setVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.outputRecord.setVersion(protocolVersion);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void kickstartHandshake() throws IOException {
        switch (this.connectionState) {
            case GeneralNameInterface.NAME_MATCH /*0*/:
                if (this.serverModeSet) {
                    initHandshaker();
                } else {
                    throw new IllegalStateException("Client/Server mode not yet set.");
                }
                break;
            case cs_DATA /*2*/:
                if (this.secureRenegotiation || Handshaker.allowUnsafeRenegotiation) {
                    if (!(this.secureRenegotiation || debug == null || !Debug.isOn("handshake"))) {
                        System.out.println("Warning: Using insecure renegotiation");
                    }
                    initHandshaker();
                } else {
                    throw new SSLHandshakeException("Insecure renegotiation is not allowed");
                }
                break;
            case cs_HANDSHAKE /*1*/:
                if (!this.handshaker.activated()) {
                    if (this.connectionState != cs_RENEGOTIATE) {
                        this.handshaker.activate(null);
                        break;
                    }
                    this.handshaker.activate(this.protocolVersion);
                    if (this.handshaker instanceof ClientHandshaker) {
                        this.handshaker.kickstart();
                    } else if (this.connectionState != cs_HANDSHAKE) {
                        this.handshaker.kickstart();
                        this.handshaker.handshakeHash.reset();
                    }
                }
                return;
            case cs_RENEGOTIATE /*3*/:
                return;
            default:
                throw new SSLException("SSLEngine is closing/closed");
        }
    }

    public void beginHandshake() throws SSLException {
        try {
            kickstartHandshake();
        } catch (Exception e) {
            fatal((byte) 40, "Couldn't kickstart handshaking", e);
        }
    }

    public SSLEngineResult unwrap(ByteBuffer netData, ByteBuffer[] appData, int offset, int length) throws SSLException {
        EngineArgs ea = new EngineArgs(netData, appData, offset, length);
        try {
            SSLEngineResult readNetRecord;
            synchronized (this.unwrapLock) {
                readNetRecord = readNetRecord(ea);
            }
            ea.resetLim();
            return readNetRecord;
        } catch (Exception e) {
            try {
                fatal((byte) 80, "problem unwrapping net record", e);
                return null;
            } finally {
                ea.resetLim();
            }
        }
    }

    private SSLEngineResult readNetRecord(EngineArgs ea) throws IOException {
        HandshakeStatus handshakeStatus = null;
        checkTaskThrown();
        if (isInboundDone()) {
            return new SSLEngineResult(Status.CLOSED, getHSStatus(null), 0, 0);
        }
        synchronized (this) {
            if (this.connectionState == cs_HANDSHAKE || this.connectionState == 0) {
                kickstartHandshake();
                handshakeStatus = getHSStatus(null);
                if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                    SSLEngineResult sSLEngineResult = new SSLEngineResult(Status.OK, handshakeStatus, 0, 0);
                    return sSLEngineResult;
                }
            }
            if (handshakeStatus == null) {
                handshakeStatus = getHSStatus(null);
            }
            if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                return new SSLEngineResult(Status.OK, handshakeStatus, 0, 0);
            }
            int packetLen = this.inputRecord.bytesInCompletePacket(ea.netData);
            if (packetLen > this.sess.getPacketBufferSize()) {
                if (packetLen > Record.maxLargeRecordSize) {
                    throw new SSLProtocolException("Input SSL/TLS record too big: max = 33305 len = " + packetLen);
                }
                this.sess.expandBufferSizes();
            }
            if (packetLen - 5 > ea.getAppRemaining()) {
                return new SSLEngineResult(Status.BUFFER_OVERFLOW, handshakeStatus, 0, 0);
            }
            if (packetLen == -1 || ea.netData.remaining() < packetLen) {
                return new SSLEngineResult(Status.BUFFER_UNDERFLOW, handshakeStatus, 0, 0);
            }
            try {
                return new SSLEngineResult(isInboundDone() ? Status.CLOSED : Status.OK, getHSStatus(readRecord(ea)), ea.deltaNet(), ea.deltaApp());
            } catch (SSLException e) {
                throw e;
            } catch (IOException e2) {
                SSLException ex = new SSLException("readRecord");
                ex.initCause(e2);
                throw ex;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private HandshakeStatus readRecord(EngineArgs ea) throws IOException {
        HandshakeStatus handshakeStatus = null;
        ByteBuffer readBB = null;
        ByteBuffer decryptedBB = null;
        if (getConnectionState() != cs_ERROR) {
            try {
                readBB = this.inputRecord.read(ea.netData);
            } catch (Throwable e) {
                fatal((byte) 10, e);
            }
            try {
                decryptedBB = this.inputRecord.decrypt(this.readMAC, this.readCipher, readBB);
            } catch (BadPaddingException e2) {
                byte alertType;
                if (this.inputRecord.contentType() == 22) {
                    alertType = (byte) 40;
                } else {
                    alertType = DerValue.tag_T61String;
                }
                fatal(alertType, e2.getMessage(), e2);
            }
            synchronized (this) {
                switch (this.inputRecord.contentType()) {
                    case Record.trailerSize /*20*/:
                        if ((this.connectionState == cs_HANDSHAKE || this.connectionState == cs_RENEGOTIATE) && this.inputRecord.available() == cs_HANDSHAKE) {
                            if (this.inputRecord.read() != cs_HANDSHAKE) {
                            }
                            changeReadCiphers();
                            this.expectingFinished = true;
                            break;
                        }
                        fatal((byte) 10, "illegal change cipher spec msg, state = " + this.connectionState);
                        changeReadCiphers();
                        this.expectingFinished = true;
                    case (byte) 21:
                        recvAlert();
                        break;
                    case ZipConstants.LOCLEN /*22*/:
                        initHandshaker();
                        if (!this.handshaker.activated()) {
                            if (this.connectionState != cs_RENEGOTIATE) {
                                this.handshaker.activate(null);
                                break;
                            }
                            this.handshaker.activate(this.protocolVersion);
                        }
                        this.handshaker.process_record(this.inputRecord, this.expectingFinished);
                        this.expectingFinished = -assertionsDisabled;
                        if (!this.handshaker.invalidated) {
                            if (!this.handshaker.isDone()) {
                                if (this.handshaker.taskOutstanding()) {
                                    handshakeStatus = HandshakeStatus.NEED_TASK;
                                    break;
                                }
                            }
                            this.secureRenegotiation = this.handshaker.isSecureRenegotiation();
                            this.clientVerifyData = this.handshaker.getClientVerifyData();
                            this.serverVerifyData = this.handshaker.getServerVerifyData();
                            this.sess = this.handshaker.getSession();
                            this.handshakeSession = null;
                            if (!this.writer.hasOutboundData()) {
                                handshakeStatus = HandshakeStatus.FINISHED;
                            }
                            this.handshaker = null;
                            this.connectionState = cs_DATA;
                            break;
                        }
                        this.handshaker = null;
                        if (this.connectionState == cs_RENEGOTIATE) {
                            this.connectionState = cs_DATA;
                            break;
                        }
                        break;
                    case SecureRandom.DEFAULT_SDK_TARGET_FOR_CRYPTO_PROVIDER_WORKAROUND /*23*/:
                        if (this.connectionState == cs_DATA || this.connectionState == cs_RENEGOTIATE || this.connectionState == cs_CLOSED) {
                            if (!this.expectingFinished) {
                                if (!this.inboundDone) {
                                    ea.scatter(decryptedBB.slice());
                                    break;
                                }
                            }
                            throw new SSLProtocolException("Expecting finished message, received data");
                        }
                        throw new SSLProtocolException("Data received in non-data state: " + this.connectionState);
                        break;
                    default:
                        if (debug != null && Debug.isOn("ssl")) {
                            System.out.println(threadName() + ", Received record type: " + this.inputRecord.contentType());
                            break;
                        }
                }
            }
        }
        return handshakeStatus;
    }

    public SSLEngineResult wrap(ByteBuffer[] appData, int offset, int length, ByteBuffer netData) throws SSLException {
        EngineArgs ea = new EngineArgs(appData, offset, length, netData);
        if (netData.remaining() < Record.maxRecordSize) {
            return new SSLEngineResult(Status.BUFFER_OVERFLOW, getHSStatus(null), 0, 0);
        }
        try {
            SSLEngineResult writeAppRecord;
            synchronized (this.wrapLock) {
                writeAppRecord = writeAppRecord(ea);
            }
            ea.resetLim();
            return writeAppRecord;
        } catch (Exception e) {
            try {
                ea.resetPos();
                fatal((byte) 80, "problem wrapping app data", e);
                return null;
            } finally {
                ea.resetLim();
            }
        }
    }

    private SSLEngineResult writeAppRecord(EngineArgs ea) throws IOException {
        HandshakeStatus handshakeStatus = null;
        checkTaskThrown();
        if (this.writer.isOutboundDone()) {
            return new SSLEngineResult(Status.CLOSED, getHSStatus(null), 0, 0);
        }
        synchronized (this) {
            if (this.connectionState == cs_HANDSHAKE || this.connectionState == 0) {
                kickstartHandshake();
                handshakeStatus = getHSStatus(null);
                if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                    SSLEngineResult sSLEngineResult = new SSLEngineResult(Status.OK, handshakeStatus, 0, 0);
                    return sSLEngineResult;
                }
            }
            if (handshakeStatus == null) {
                handshakeStatus = getHSStatus(null);
            }
            if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                return new SSLEngineResult(Status.OK, handshakeStatus, 0, 0);
            }
            try {
                synchronized (this.writeLock) {
                    handshakeStatus = writeRecord(this.outputRecord, ea);
                }
                return new SSLEngineResult(isOutboundDone() ? Status.CLOSED : Status.OK, getHSStatus(handshakeStatus), ea.deltaApp(), ea.deltaNet());
            } catch (SSLException e) {
                throw e;
            } catch (IOException e2) {
                SSLException ex = new SSLException("Write problems");
                ex.initCause(e2);
                throw ex;
            }
        }
    }

    private HandshakeStatus writeRecord(EngineOutputRecord eor, EngineArgs ea) throws IOException {
        HandshakeStatus hsStatus = this.writer.writeRecord(eor, ea, this.writeMAC, this.writeCipher);
        if (this.connectionState < cs_ERROR && !isOutboundDone() && hsStatus == HandshakeStatus.NOT_HANDSHAKING && checkSequenceNumber(this.writeMAC, eor.contentType())) {
            hsStatus = getHSStatus(null);
        }
        if (this.isFirstAppOutputRecord && ea.deltaApp() > 0) {
            this.isFirstAppOutputRecord = -assertionsDisabled;
        }
        return hsStatus;
    }

    boolean needToSplitPayload(CipherBox cipher, ProtocolVersion protocol) {
        if (protocol.v > ProtocolVersion.TLS10.v || !cipher.isCBCMode() || this.isFirstAppOutputRecord) {
            return -assertionsDisabled;
        }
        return Record.enableCBCProtection;
    }

    void writeRecord(EngineOutputRecord eor) throws IOException {
        this.writer.writeRecord(eor, this.writeMAC, this.writeCipher);
        if (this.connectionState < cs_ERROR && !isOutboundDone()) {
            checkSequenceNumber(this.writeMAC, eor.contentType());
        }
    }

    private boolean checkSequenceNumber(MAC mac, byte type) throws IOException {
        if (this.connectionState >= cs_ERROR || mac == MAC.NULL) {
            return -assertionsDisabled;
        }
        if (mac.seqNumOverflow()) {
            if (debug != null && Debug.isOn("ssl")) {
                System.out.println(threadName() + ", sequence number extremely close to overflow " + "(2^64-1 packets). Closing connection.");
            }
            fatal((byte) 40, "sequence number overflow");
            return true;
        } else if (type == 22 || !mac.seqNumIsHuge()) {
            return -assertionsDisabled;
        } else {
            if (debug != null && Debug.isOn("ssl")) {
                System.out.println(threadName() + ", request renegotiation " + "to avoid sequence number overflow");
            }
            beginHandshake();
            return true;
        }
    }

    private void closeOutboundInternal() {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", closeOutboundInternal()");
        }
        if (!this.writer.isOutboundDone()) {
            switch (this.connectionState) {
                case GeneralNameInterface.NAME_MATCH /*0*/:
                    this.writer.closeOutbound();
                    this.inboundDone = true;
                    break;
                case cs_ERROR /*4*/:
                case cs_CLOSED /*6*/:
                    break;
                default:
                    warning(clauth_none);
                    this.writer.closeOutbound();
                    break;
            }
            this.writeCipher.dispose();
            this.connectionState = cs_CLOSED;
        }
    }

    public synchronized void closeOutbound() {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", called closeOutbound()");
        }
        closeOutboundInternal();
    }

    public boolean isOutboundDone() {
        return this.writer.isOutboundDone();
    }

    private void closeInboundInternal() {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", closeInboundInternal()");
        }
        if (!this.inboundDone) {
            closeOutboundInternal();
            this.inboundDone = true;
            this.readCipher.dispose();
            this.connectionState = cs_CLOSED;
        }
    }

    public synchronized void closeInbound() throws SSLException {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", called closeInbound()");
        }
        if (this.connectionState == 0 || this.recvCN) {
            closeInboundInternal();
        } else {
            this.recvCN = true;
            fatal((byte) 80, "Inbound closed before receiving peer's close_notify: possible truncation attack?");
        }
    }

    public synchronized boolean isInboundDone() {
        return this.inboundDone;
    }

    public synchronized SSLSession getSession() {
        return this.sess;
    }

    public synchronized SSLSession getHandshakeSession() {
        return this.handshakeSession;
    }

    synchronized void setHandshakeSession(SSLSessionImpl session) {
        this.handshakeSession = session;
    }

    public synchronized Runnable getDelegatedTask() {
        if (this.handshaker == null) {
            return null;
        }
        return this.handshaker.getTask();
    }

    void warning(byte description) {
        sendAlert(clauth_requested, description);
    }

    synchronized void fatal(byte description, String diagnostic) throws SSLException {
        fatal(description, diagnostic, null);
    }

    synchronized void fatal(byte description, Throwable cause) throws SSLException {
        fatal(description, null, cause);
    }

    synchronized void fatal(byte description, String diagnostic, Throwable cause) throws SSLException {
        int oldState;
        if (diagnostic == null) {
        }
        if (cause == null) {
            cause = Alerts.getSSLException(description, cause, diagnostic);
        }
        if (this.closeReason != null) {
            if (debug != null && Debug.isOn("ssl")) {
                System.out.println(threadName() + ", fatal: engine already closed.  Rethrowing " + cause.toString());
            }
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            } else if (cause instanceof SSLException) {
                throw ((SSLException) cause);
            } else if (cause instanceof Exception) {
                SSLException ssle = new SSLException("fatal SSLEngine condition");
                ssle.initCause(cause);
                throw ssle;
            }
        }
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", fatal error: " + description + ": " + diagnostic + "\n" + cause.toString());
        }
        oldState = this.connectionState;
        this.connectionState = cs_ERROR;
        this.inboundDone = true;
        this.sess.invalidate();
        if (this.handshakeSession != null) {
            this.handshakeSession.invalidate();
        }
        if (oldState != 0) {
            sendAlert(clauth_required, description);
        }
        if (cause instanceof SSLException) {
            this.closeReason = Alerts.getSSLException(description, cause, diagnostic);
        } else {
            this.closeReason = (SSLException) cause;
        }
        this.writer.closeOutbound();
        this.connectionState = cs_CLOSED;
        this.readCipher.dispose();
        this.writeCipher.dispose();
        if (cause instanceof RuntimeException) {
            throw this.closeReason;
        }
        throw ((RuntimeException) cause);
        diagnostic = "General SSLEngine problem";
        if (cause == null) {
            cause = Alerts.getSSLException(description, cause, diagnostic);
        }
        if (this.closeReason != null) {
            System.out.println(threadName() + ", fatal: engine already closed.  Rethrowing " + cause.toString());
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            } else if (cause instanceof SSLException) {
                throw ((SSLException) cause);
            } else if (cause instanceof Exception) {
                SSLException ssle2 = new SSLException("fatal SSLEngine condition");
                ssle2.initCause(cause);
                throw ssle2;
            }
        }
        System.out.println(threadName() + ", fatal error: " + description + ": " + diagnostic + "\n" + cause.toString());
        oldState = this.connectionState;
        this.connectionState = cs_ERROR;
        this.inboundDone = true;
        this.sess.invalidate();
        if (this.handshakeSession != null) {
            this.handshakeSession.invalidate();
        }
        if (oldState != 0) {
            sendAlert(clauth_required, description);
        }
        if (cause instanceof SSLException) {
            this.closeReason = Alerts.getSSLException(description, cause, diagnostic);
        } else {
            this.closeReason = (SSLException) cause;
        }
        this.writer.closeOutbound();
        this.connectionState = cs_CLOSED;
        this.readCipher.dispose();
        this.writeCipher.dispose();
        if (cause instanceof RuntimeException) {
            throw this.closeReason;
        }
        throw ((RuntimeException) cause);
    }

    private void recvAlert() throws IOException {
        byte level = (byte) this.inputRecord.read();
        byte description = (byte) this.inputRecord.read();
        if (description == -1) {
            fatal((byte) 47, "Short alert message");
        }
        if (debug != null && (Debug.isOn("record") || Debug.isOn("handshake"))) {
            synchronized (System.out) {
                System.out.print(threadName());
                System.out.print(", RECV " + this.protocolVersion + " ALERT:  ");
                if (level == cs_DATA) {
                    System.out.print("fatal, ");
                } else if (level == clauth_requested) {
                    System.out.print("warning, ");
                } else {
                    System.out.print("<level " + (level & 255) + ">, ");
                }
                System.out.println(Alerts.alertDescription(description));
            }
        }
        if (level != clauth_requested) {
            String reason = "Received fatal alert: " + Alerts.alertDescription(description);
            if (this.closeReason == null) {
                this.closeReason = Alerts.getSSLException(description, reason);
            }
            fatal((byte) 10, reason);
        } else if (description == null) {
            if (this.connectionState == cs_HANDSHAKE) {
                fatal((byte) 10, "Received close_notify during handshake");
                return;
            }
            this.recvCN = true;
            closeInboundInternal();
        } else if (this.handshaker != null) {
            this.handshaker.handshakeAlert(description);
        }
    }

    private void sendAlert(byte level, byte description) {
        if (this.connectionState < cs_CLOSED) {
            if (this.connectionState != cs_HANDSHAKE || (this.handshaker != null && this.handshaker.started())) {
                EngineOutputRecord r = new EngineOutputRecord(Record.ct_alert, this);
                r.setVersion(this.protocolVersion);
                boolean useDebug = debug != null ? Debug.isOn("ssl") : -assertionsDisabled;
                if (useDebug) {
                    synchronized (System.out) {
                        System.out.print(threadName());
                        System.out.print(", SEND " + this.protocolVersion + " ALERT:  ");
                        if (level == cs_DATA) {
                            System.out.print("fatal, ");
                        } else if (level == clauth_requested) {
                            System.out.print("warning, ");
                        } else {
                            System.out.print("<level = " + (level & 255) + ">, ");
                        }
                        System.out.println("description = " + Alerts.alertDescription(description));
                    }
                }
                r.write(level);
                r.write(description);
                try {
                    writeRecord(r);
                } catch (Object e) {
                    if (useDebug) {
                        System.out.println(threadName() + ", Exception sending alert: " + e);
                    }
                }
            }
        }
    }

    public synchronized void setEnableSessionCreation(boolean flag) {
        this.enableSessionCreation = flag;
        if (!(this.handshaker == null || this.handshaker.activated())) {
            this.handshaker.setEnableSessionCreation(this.enableSessionCreation);
        }
    }

    public synchronized boolean getEnableSessionCreation() {
        return this.enableSessionCreation;
    }

    public synchronized void setNeedClientAuth(boolean flag) {
        this.doClientAuth = flag ? clauth_required : clauth_none;
        if (!(this.handshaker == null || !(this.handshaker instanceof ServerHandshaker) || this.handshaker.activated())) {
            ((ServerHandshaker) this.handshaker).setClientAuth(this.doClientAuth);
        }
    }

    public synchronized boolean getNeedClientAuth() {
        return this.doClientAuth == cs_DATA ? true : -assertionsDisabled;
    }

    public synchronized void setWantClientAuth(boolean flag) {
        this.doClientAuth = flag ? clauth_requested : clauth_none;
        if (!(this.handshaker == null || !(this.handshaker instanceof ServerHandshaker) || this.handshaker.activated())) {
            ((ServerHandshaker) this.handshaker).setClientAuth(this.doClientAuth);
        }
    }

    public synchronized boolean getWantClientAuth() {
        boolean z = true;
        synchronized (this) {
            if (this.doClientAuth != clauth_requested) {
                z = -assertionsDisabled;
            }
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setUseClientMode(boolean flag) {
        boolean z = -assertionsDisabled;
        synchronized (this) {
            boolean z2;
            SSLContextImpl sSLContextImpl;
            switch (this.connectionState) {
                case GeneralNameInterface.NAME_MATCH /*0*/:
                    boolean z3 = this.roleIsServer;
                    if (flag) {
                        z2 = -assertionsDisabled;
                    } else {
                        z2 = cs_HANDSHAKE;
                    }
                    if (z3 != z2 && this.sslContext.isDefaultProtocolList(this.enabledProtocols)) {
                        sSLContextImpl = this.sslContext;
                        if (flag) {
                            z2 = -assertionsDisabled;
                        } else {
                            z2 = true;
                        }
                        this.enabledProtocols = sSLContextImpl.getDefaultProtocolList(z2);
                    }
                    if (!flag) {
                        z = true;
                    }
                    this.roleIsServer = z;
                    this.serverModeSet = true;
                    break;
                case cs_HANDSHAKE /*1*/:
                    if (!-assertionsDisabled) {
                        if (!(this.handshaker != null ? cs_HANDSHAKE : -assertionsDisabled)) {
                            throw new AssertionError();
                        }
                    }
                    if (!this.handshaker.activated()) {
                        if (this.roleIsServer != (flag ? -assertionsDisabled : cs_HANDSHAKE) && this.sslContext.isDefaultProtocolList(this.enabledProtocols)) {
                            sSLContextImpl = this.sslContext;
                            if (flag) {
                                z2 = -assertionsDisabled;
                            } else {
                                z2 = true;
                            }
                            this.enabledProtocols = sSLContextImpl.getDefaultProtocolList(z2);
                        }
                        if (!flag) {
                            z = true;
                        }
                        this.roleIsServer = z;
                        this.connectionState = 0;
                        initHandshaker();
                        break;
                    }
                    break;
            }
        }
    }

    public synchronized boolean getUseClientMode() {
        return this.roleIsServer ? -assertionsDisabled : true;
    }

    public String[] getSupportedCipherSuites() {
        return this.sslContext.getSupportedCipherSuiteList().toStringArray();
    }

    public synchronized void setEnabledCipherSuites(String[] suites) {
        this.enabledCipherSuites = new CipherSuiteList(suites);
        if (!(this.handshaker == null || this.handshaker.activated())) {
            this.handshaker.setEnabledCipherSuites(this.enabledCipherSuites);
        }
    }

    public synchronized String[] getEnabledCipherSuites() {
        return this.enabledCipherSuites.toStringArray();
    }

    public String[] getSupportedProtocols() {
        return this.sslContext.getSuportedProtocolList().toStringArray();
    }

    public synchronized void setEnabledProtocols(String[] protocols) {
        this.enabledProtocols = new ProtocolList(protocols);
        if (!(this.handshaker == null || this.handshaker.activated())) {
            this.handshaker.setEnabledProtocols(this.enabledProtocols);
        }
    }

    public synchronized String[] getEnabledProtocols() {
        return this.enabledProtocols.toStringArray();
    }

    public synchronized SSLParameters getSSLParameters() {
        SSLParameters params;
        params = super.getSSLParameters();
        params.setEndpointIdentificationAlgorithm(this.identificationProtocol);
        params.setAlgorithmConstraints(this.algorithmConstraints);
        return params;
    }

    public synchronized void setSSLParameters(SSLParameters params) {
        super.setSSLParameters(params);
        this.identificationProtocol = params.getEndpointIdentificationAlgorithm();
        this.algorithmConstraints = params.getAlgorithmConstraints();
        if (!(this.handshaker == null || this.handshaker.started())) {
            this.handshaker.setIdentificationProtocol(this.identificationProtocol);
            this.handshaker.setAlgorithmConstraints(this.algorithmConstraints);
        }
    }

    private static String threadName() {
        return Thread.currentThread().getName();
    }

    public String toString() {
        StringBuilder retval = new StringBuilder(80);
        retval.append(Integer.toHexString(hashCode()));
        retval.append("[");
        retval.append("SSLEngine[hostname=");
        String host = getPeerHost();
        if (host == null) {
            host = "null";
        }
        retval.append(host);
        retval.append(" port=");
        retval.append(Integer.toString(getPeerPort()));
        retval.append("] ");
        retval.append(getSession().getCipherSuite());
        retval.append("]");
        return retval.toString();
    }
}
