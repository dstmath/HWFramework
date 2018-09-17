package sun.security.ssl;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AlgorithmConstraints;
import java.security.CryptoPrimitive;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;
import sun.misc.HexDumpEncoder;
import sun.security.internal.interfaces.TlsMasterSecret;
import sun.security.internal.spec.TlsKeyMaterialParameterSpec;
import sun.security.internal.spec.TlsKeyMaterialSpec;
import sun.security.internal.spec.TlsMasterSecretParameterSpec;
import sun.security.util.DerValue;

abstract class Handshaker {
    static final boolean allowLegacyHelloMessages = false;
    static final boolean allowUnsafeRenegotiation = false;
    static final Debug debug = null;
    private CipherSuiteList activeCipherSuites;
    ProtocolVersion activeProtocolVersion;
    private ProtocolList activeProtocols;
    private AlgorithmConstraints algorithmConstraints;
    CipherSuite cipherSuite;
    byte[] clientVerifyData;
    private SecretKey clntMacSecret;
    private IvParameterSpec clntWriteIV;
    private SecretKey clntWriteKey;
    RandomCookie clnt_random;
    SSLSocketImpl conn;
    private volatile DelegatedTask delegatedTask;
    boolean enableNewSession;
    private CipherSuiteList enabledCipherSuites;
    private ProtocolList enabledProtocols;
    SSLEngineImpl engine;
    HandshakeHash handshakeHash;
    String identificationProtocol;
    HandshakeInStream input;
    boolean invalidated;
    private boolean isClient;
    boolean isInitialHandshake;
    KeyExchange keyExchange;
    Collection<SignatureAndHashAlgorithm> localSupportedSignAlgs;
    private boolean needCertVerify;
    HandshakeOutStream output;
    Collection<SignatureAndHashAlgorithm> peerSupportedSignAlgs;
    ProtocolVersion protocolVersion;
    boolean resumingSession;
    boolean secureRenegotiation;
    byte[] serverVerifyData;
    SSLSessionImpl session;
    SSLContextImpl sslContext;
    int state;
    private SecretKey svrMacSecret;
    private IvParameterSpec svrWriteIV;
    private SecretKey svrWriteKey;
    RandomCookie svr_random;
    private volatile boolean taskDelegated;
    private volatile Exception thrown;
    private Object thrownLock;

    class DelegatedTask<E> implements Runnable {
        private PrivilegedExceptionAction<E> pea;

        DelegatedTask(PrivilegedExceptionAction<E> pea) {
            this.pea = pea;
        }

        public void run() {
            synchronized (Handshaker.this.engine) {
                try {
                    AccessController.doPrivileged(this.pea, Handshaker.this.engine.getAcc());
                } catch (PrivilegedActionException pae) {
                    Handshaker.this.thrown = pae.getException();
                } catch (RuntimeException rte) {
                    Handshaker.this.thrown = rte;
                }
                Handshaker.this.delegatedTask = null;
                Handshaker.this.taskDelegated = false;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.Handshaker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.Handshaker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.Handshaker.<clinit>():void");
    }

    abstract HandshakeMessage getKickstartMessage() throws SSLException;

    abstract void handshakeAlert(byte b) throws SSLProtocolException;

    abstract void processMessage(byte b, int i) throws IOException;

    Handshaker(SSLSocketImpl c, SSLContextImpl context, ProtocolList enabledProtocols, boolean needCertVerify, boolean isClient, ProtocolVersion activeProtocolVersion, boolean isInitialHandshake, boolean secureRenegotiation, byte[] clientVerifyData, byte[] serverVerifyData) {
        this.algorithmConstraints = null;
        this.conn = null;
        this.engine = null;
        this.taskDelegated = false;
        this.delegatedTask = null;
        this.thrown = null;
        this.thrownLock = new Object();
        this.conn = c;
        init(context, enabledProtocols, needCertVerify, isClient, activeProtocolVersion, isInitialHandshake, secureRenegotiation, clientVerifyData, serverVerifyData);
    }

    Handshaker(SSLEngineImpl engine, SSLContextImpl context, ProtocolList enabledProtocols, boolean needCertVerify, boolean isClient, ProtocolVersion activeProtocolVersion, boolean isInitialHandshake, boolean secureRenegotiation, byte[] clientVerifyData, byte[] serverVerifyData) {
        this.algorithmConstraints = null;
        this.conn = null;
        this.engine = null;
        this.taskDelegated = false;
        this.delegatedTask = null;
        this.thrown = null;
        this.thrownLock = new Object();
        this.engine = engine;
        init(context, enabledProtocols, needCertVerify, isClient, activeProtocolVersion, isInitialHandshake, secureRenegotiation, clientVerifyData, serverVerifyData);
    }

    private void init(SSLContextImpl context, ProtocolList enabledProtocols, boolean needCertVerify, boolean isClient, ProtocolVersion activeProtocolVersion, boolean isInitialHandshake, boolean secureRenegotiation, byte[] clientVerifyData, byte[] serverVerifyData) {
        if (debug != null && Debug.isOn("handshake")) {
            System.out.println("Allow unsafe renegotiation: " + allowUnsafeRenegotiation + "\nAllow legacy hello messages: " + allowLegacyHelloMessages + "\nIs initial handshake: " + isInitialHandshake + "\nIs secure renegotiation: " + secureRenegotiation);
        }
        this.sslContext = context;
        this.isClient = isClient;
        this.needCertVerify = needCertVerify;
        this.activeProtocolVersion = activeProtocolVersion;
        this.isInitialHandshake = isInitialHandshake;
        this.secureRenegotiation = secureRenegotiation;
        this.clientVerifyData = clientVerifyData;
        this.serverVerifyData = serverVerifyData;
        this.enableNewSession = true;
        this.invalidated = false;
        setCipherSuite(CipherSuite.C_NULL);
        setEnabledProtocols(enabledProtocols);
        if (this.conn != null) {
            this.algorithmConstraints = new SSLAlgorithmConstraints(this.conn, true);
        } else {
            this.algorithmConstraints = new SSLAlgorithmConstraints(this.engine, true);
        }
        this.state = -2;
    }

    void fatalSE(byte b, String diagnostic) throws IOException {
        fatalSE(b, diagnostic, null);
    }

    void fatalSE(byte b, Throwable cause) throws IOException {
        fatalSE(b, null, cause);
    }

    void fatalSE(byte b, String diagnostic, Throwable cause) throws IOException {
        if (this.conn != null) {
            this.conn.fatal(b, diagnostic, cause);
        } else {
            this.engine.fatal(b, diagnostic, cause);
        }
    }

    void warningSE(byte b) {
        if (this.conn != null) {
            this.conn.warning(b);
        } else {
            this.engine.warning(b);
        }
    }

    String getRawHostnameSE() {
        if (this.conn != null) {
            return this.conn.getRawHostname();
        }
        return this.engine.getPeerHost();
    }

    String getHostSE() {
        if (this.conn != null) {
            return this.conn.getHost();
        }
        return this.engine.getPeerHost();
    }

    String getHostAddressSE() {
        if (this.conn != null) {
            return this.conn.getInetAddress().getHostAddress();
        }
        return this.engine.getPeerHost();
    }

    boolean isLoopbackSE() {
        if (this.conn != null) {
            return this.conn.getInetAddress().isLoopbackAddress();
        }
        return false;
    }

    int getPortSE() {
        if (this.conn != null) {
            return this.conn.getPort();
        }
        return this.engine.getPeerPort();
    }

    int getLocalPortSE() {
        if (this.conn != null) {
            return this.conn.getLocalPort();
        }
        return -1;
    }

    AccessControlContext getAccSE() {
        if (this.conn != null) {
            return this.conn.getAcc();
        }
        return this.engine.getAcc();
    }

    private void setVersionSE(ProtocolVersion protocolVersion) {
        if (this.conn != null) {
            this.conn.setVersion(protocolVersion);
        } else {
            this.engine.setVersion(protocolVersion);
        }
    }

    void setVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        setVersionSE(protocolVersion);
        this.output.r.setVersion(protocolVersion);
    }

    void setEnabledProtocols(ProtocolList enabledProtocols) {
        this.activeCipherSuites = null;
        this.activeProtocols = null;
        this.enabledProtocols = enabledProtocols;
    }

    void setEnabledCipherSuites(CipherSuiteList enabledCipherSuites) {
        this.activeCipherSuites = null;
        this.activeProtocols = null;
        this.enabledCipherSuites = enabledCipherSuites;
    }

    void setAlgorithmConstraints(AlgorithmConstraints algorithmConstraints) {
        this.activeCipherSuites = null;
        this.activeProtocols = null;
        this.algorithmConstraints = new SSLAlgorithmConstraints(algorithmConstraints);
        this.localSupportedSignAlgs = null;
    }

    Collection<SignatureAndHashAlgorithm> getLocalSupportedSignAlgs() {
        if (this.localSupportedSignAlgs == null) {
            this.localSupportedSignAlgs = SignatureAndHashAlgorithm.getSupportedAlgorithms(this.algorithmConstraints);
        }
        return this.localSupportedSignAlgs;
    }

    void setPeerSupportedSignAlgs(Collection<SignatureAndHashAlgorithm> algorithms) {
        this.peerSupportedSignAlgs = new ArrayList((Collection) algorithms);
    }

    Collection<SignatureAndHashAlgorithm> getPeerSupportedSignAlgs() {
        return this.peerSupportedSignAlgs;
    }

    void setIdentificationProtocol(String protocol) {
        this.identificationProtocol = protocol;
    }

    void activate(ProtocolVersion helloVersion) throws IOException {
        if (this.activeProtocols == null) {
            this.activeProtocols = getActiveProtocols();
        }
        if (this.activeProtocols.collection().isEmpty() || this.activeProtocols.max.v == ProtocolVersion.NONE.v) {
            throw new SSLHandshakeException("No appropriate protocol");
        }
        if (this.activeCipherSuites == null) {
            this.activeCipherSuites = getActiveCipherSuites();
        }
        if (this.activeCipherSuites.collection().isEmpty()) {
            throw new SSLHandshakeException("No appropriate cipher suite");
        }
        if (this.isInitialHandshake) {
            this.protocolVersion = this.activeProtocols.max;
        } else {
            this.protocolVersion = this.activeProtocolVersion;
        }
        if (helloVersion == null || helloVersion.v == ProtocolVersion.NONE.v) {
            helloVersion = this.activeProtocols.helloVersion;
        }
        this.handshakeHash = new HandshakeHash(!this.isClient, this.needCertVerify, SignatureAndHashAlgorithm.getHashAlgorithmNames(getLocalSupportedSignAlgs()));
        this.input = new HandshakeInStream(this.handshakeHash);
        if (this.conn != null) {
            this.output = new HandshakeOutStream(this.protocolVersion, helloVersion, this.handshakeHash, this.conn);
            this.conn.getAppInputStream().r.setHandshakeHash(this.handshakeHash);
            this.conn.getAppInputStream().r.setHelloVersion(helloVersion);
            this.conn.getAppOutputStream().r.setHelloVersion(helloVersion);
        } else {
            this.output = new HandshakeOutStream(this.protocolVersion, helloVersion, this.handshakeHash, this.engine);
            this.engine.inputRecord.setHandshakeHash(this.handshakeHash);
            this.engine.inputRecord.setHelloVersion(helloVersion);
            this.engine.outputRecord.setHelloVersion(helloVersion);
        }
        this.state = -1;
    }

    void setCipherSuite(CipherSuite s) {
        this.cipherSuite = s;
        this.keyExchange = s.keyExchange;
    }

    boolean isNegotiable(CipherSuite s) {
        if (this.activeCipherSuites == null) {
            this.activeCipherSuites = getActiveCipherSuites();
        }
        return this.activeCipherSuites.contains(s) ? s.isNegotiable() : false;
    }

    boolean isNegotiable(ProtocolVersion protocolVersion) {
        if (this.activeProtocols == null) {
            this.activeProtocols = getActiveProtocols();
        }
        return this.activeProtocols.contains(protocolVersion);
    }

    ProtocolVersion selectProtocolVersion(ProtocolVersion protocolVersion) {
        if (this.activeProtocols == null) {
            this.activeProtocols = getActiveProtocols();
        }
        return this.activeProtocols.selectProtocolVersion(protocolVersion);
    }

    CipherSuiteList getActiveCipherSuites() {
        if (this.activeCipherSuites == null) {
            if (this.activeProtocols == null) {
                this.activeProtocols = getActiveProtocols();
            }
            Collection suites = new ArrayList();
            if (!(this.activeProtocols.collection().isEmpty() || this.activeProtocols.min.v == ProtocolVersion.NONE.v)) {
                for (Object suite : this.enabledCipherSuites.collection()) {
                    if (suite.obsoleted <= this.activeProtocols.min.v || suite.supported > this.activeProtocols.max.v) {
                        if (debug != null && Debug.isOn("verbose")) {
                            if (suite.obsoleted <= this.activeProtocols.min.v) {
                                System.out.println("Ignoring obsoleted cipher suite: " + suite);
                            } else {
                                System.out.println("Ignoring unsupported cipher suite: " + suite);
                            }
                        }
                    } else if (this.algorithmConstraints.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), suite.name, null)) {
                        suites.add(suite);
                    }
                }
            }
            this.activeCipherSuites = new CipherSuiteList(suites);
        }
        return this.activeCipherSuites;
    }

    ProtocolList getActiveProtocols() {
        if (this.activeProtocols == null) {
            ArrayList protocols = new ArrayList(4);
            for (Object protocol : this.enabledProtocols.collection()) {
                boolean found = false;
                for (Object suite : this.enabledCipherSuites.collection()) {
                    if (!suite.isAvailable() || suite.obsoleted <= protocol.v || suite.supported > protocol.v) {
                        if (debug != null && Debug.isOn("verbose")) {
                            System.out.println("Ignoring unsupported cipher suite: " + suite + " for " + protocol);
                        }
                    } else if (this.algorithmConstraints.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), suite.name, null)) {
                        protocols.add(protocol);
                        found = true;
                        break;
                    } else if (debug != null && Debug.isOn("verbose")) {
                        System.out.println("Ignoring disabled cipher suite: " + suite + " for " + protocol);
                    }
                }
                if (!(found || debug == null || !Debug.isOn("handshake"))) {
                    System.out.println("No available cipher suite for " + protocol);
                }
            }
            this.activeProtocols = new ProtocolList(protocols);
        }
        return this.activeProtocols;
    }

    void setEnableSessionCreation(boolean newSessions) {
        this.enableNewSession = newSessions;
    }

    CipherBox newReadCipher() throws NoSuchAlgorithmException {
        BulkCipher cipher = this.cipherSuite.cipher;
        if (this.isClient) {
            CipherBox box = cipher.newCipher(this.protocolVersion, this.svrWriteKey, this.svrWriteIV, this.sslContext.getSecureRandom(), false);
            this.svrWriteKey = null;
            this.svrWriteIV = null;
            return box;
        }
        box = cipher.newCipher(this.protocolVersion, this.clntWriteKey, this.clntWriteIV, this.sslContext.getSecureRandom(), false);
        this.clntWriteKey = null;
        this.clntWriteIV = null;
        return box;
    }

    CipherBox newWriteCipher() throws NoSuchAlgorithmException {
        BulkCipher cipher = this.cipherSuite.cipher;
        if (this.isClient) {
            CipherBox box = cipher.newCipher(this.protocolVersion, this.clntWriteKey, this.clntWriteIV, this.sslContext.getSecureRandom(), true);
            this.clntWriteKey = null;
            this.clntWriteIV = null;
            return box;
        }
        box = cipher.newCipher(this.protocolVersion, this.svrWriteKey, this.svrWriteIV, this.sslContext.getSecureRandom(), true);
        this.svrWriteKey = null;
        this.svrWriteIV = null;
        return box;
    }

    MAC newReadMAC() throws NoSuchAlgorithmException, InvalidKeyException {
        MacAlg macAlg = this.cipherSuite.macAlg;
        if (this.isClient) {
            MAC mac = macAlg.newMac(this.protocolVersion, this.svrMacSecret);
            this.svrMacSecret = null;
            return mac;
        }
        mac = macAlg.newMac(this.protocolVersion, this.clntMacSecret);
        this.clntMacSecret = null;
        return mac;
    }

    MAC newWriteMAC() throws NoSuchAlgorithmException, InvalidKeyException {
        MacAlg macAlg = this.cipherSuite.macAlg;
        if (this.isClient) {
            MAC mac = macAlg.newMac(this.protocolVersion, this.clntMacSecret);
            this.clntMacSecret = null;
            return mac;
        }
        mac = macAlg.newMac(this.protocolVersion, this.svrMacSecret);
        this.svrMacSecret = null;
        return mac;
    }

    boolean isDone() {
        return this.state == 20;
    }

    SSLSessionImpl getSession() {
        return this.session;
    }

    void setHandshakeSessionSE(SSLSessionImpl handshakeSession) {
        if (this.conn != null) {
            this.conn.setHandshakeSession(handshakeSession);
        } else {
            this.engine.setHandshakeSession(handshakeSession);
        }
    }

    boolean isSecureRenegotiation() {
        return this.secureRenegotiation;
    }

    byte[] getClientVerifyData() {
        return this.clientVerifyData;
    }

    byte[] getServerVerifyData() {
        return this.serverVerifyData;
    }

    void process_record(InputRecord r, boolean expectingFinished) throws IOException {
        checkThrown();
        this.input.incomingRecord(r);
        if (this.conn != null || expectingFinished) {
            processLoop();
        } else {
            delegateTask(new PrivilegedExceptionAction<Void>() {
                public Void run() throws Exception {
                    Handshaker.this.processLoop();
                    return null;
                }
            });
        }
    }

    void processLoop() throws IOException {
        while (this.input.available() >= 4) {
            this.input.mark(4);
            byte messageType = (byte) this.input.getInt8();
            int messageLen = this.input.getInt24();
            if (this.input.available() < messageLen) {
                this.input.reset();
                return;
            } else if (messageType == null) {
                this.input.reset();
                processMessage(messageType, messageLen);
                this.input.ignore(messageLen + 4);
            } else {
                this.input.mark(messageLen);
                processMessage(messageType, messageLen);
                this.input.digestNow();
            }
        }
    }

    boolean activated() {
        return this.state >= -1;
    }

    boolean started() {
        return this.state >= 0;
    }

    void kickstart() throws IOException {
        if (this.state < 0) {
            HandshakeMessage m = getKickstartMessage();
            if (debug != null && Debug.isOn("handshake")) {
                m.print(System.out);
            }
            m.write(this.output);
            this.output.flush();
            this.state = m.messageType();
        }
    }

    void sendChangeCipherSpec(Finished mesg, boolean lastMessage) throws IOException {
        OutputRecord r;
        this.output.flush();
        if (this.conn != null) {
            r = new OutputRecord(DerValue.tag_T61String);
        } else {
            r = new EngineOutputRecord(DerValue.tag_T61String, this.engine);
        }
        r.setVersion(this.protocolVersion);
        r.write(1);
        if (this.conn != null) {
            this.conn.writeLock.lock();
            try {
                this.conn.writeRecord(r);
                this.conn.changeWriteCiphers();
                if (debug != null && Debug.isOn("handshake")) {
                    mesg.print(System.out);
                }
                mesg.write(this.output);
                this.output.flush();
            } finally {
                this.conn.writeLock.unlock();
            }
        } else {
            synchronized (this.engine.writeLock) {
                this.engine.writeRecord((EngineOutputRecord) r);
                this.engine.changeWriteCiphers();
                if (debug != null && Debug.isOn("handshake")) {
                    mesg.print(System.out);
                }
                mesg.write(this.output);
                if (lastMessage) {
                    this.output.setFinishedMsg();
                }
                this.output.flush();
            }
        }
    }

    void calculateKeys(SecretKey preMasterSecret, ProtocolVersion version) {
        SecretKey master = calculateMasterSecret(preMasterSecret, version);
        this.session.setMasterSecret(master);
        calculateConnectionKeys(master);
    }

    private SecretKey calculateMasterSecret(SecretKey preMasterSecret, ProtocolVersion requestedVersion) {
        String masterAlg;
        PRF prf;
        if (debug != null && Debug.isOn("keygen")) {
            HexDumpEncoder dump = new HexDumpEncoder();
            System.out.println("SESSION KEYGEN:");
            System.out.println("PreMaster Secret:");
            printHex(dump, preMasterSecret.getEncoded());
        }
        if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
            masterAlg = "SunTls12MasterSecret";
            prf = this.cipherSuite.prfAlg;
        } else {
            masterAlg = "SunTlsMasterSecret";
            prf = PRF.P_NONE;
        }
        AlgorithmParameterSpec spec = new TlsMasterSecretParameterSpec(preMasterSecret, this.protocolVersion.major, this.protocolVersion.minor, this.clnt_random.random_bytes, this.svr_random.random_bytes, prf.getPRFHashAlg(), prf.getPRFHashLength(), prf.getPRFBlockSize());
        try {
            KeyGenerator kg = JsseJce.getKeyGenerator(masterAlg);
            kg.init(spec);
            SecretKey masterSecret = kg.generateKey();
            if (requestedVersion == null || !(masterSecret instanceof TlsMasterSecret)) {
                return masterSecret;
            }
            TlsMasterSecret tlsKey = (TlsMasterSecret) masterSecret;
            int major = tlsKey.getMajorVersion();
            int minor = tlsKey.getMinorVersion();
            if (major < 0 || minor < 0) {
                return masterSecret;
            }
            ProtocolVersion premasterVersion = ProtocolVersion.valueOf(major, minor);
            boolean versionMismatch = premasterVersion.v != requestedVersion.v;
            if (versionMismatch && requestedVersion.v <= ProtocolVersion.TLS10.v) {
                versionMismatch = premasterVersion.v != this.protocolVersion.v;
            }
            if (!versionMismatch) {
                return masterSecret;
            }
            if (debug != null && Debug.isOn("handshake")) {
                System.out.println("RSA PreMasterSecret version error: expected" + this.protocolVersion + " or " + requestedVersion + ", decrypted: " + premasterVersion);
            }
            return calculateMasterSecret(RSAClientKeyExchange.generateDummySecret(requestedVersion), null);
        } catch (Throwable e) {
            if (preMasterSecret.getAlgorithm().equals("TlsRsaPremasterSecret")) {
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println("RSA master secret generation error:");
                    e.printStackTrace(System.out);
                }
                if (requestedVersion != null) {
                    preMasterSecret = RSAClientKeyExchange.generateDummySecret(requestedVersion);
                } else {
                    preMasterSecret = RSAClientKeyExchange.generateDummySecret(this.protocolVersion);
                }
                return calculateMasterSecret(preMasterSecret, null);
            }
            throw new ProviderException(e);
        }
    }

    void calculateConnectionKeys(SecretKey masterKey) {
        String keyMaterialAlg;
        PRF prf;
        int hashSize = this.cipherSuite.macAlg.size;
        boolean is_exportable = this.cipherSuite.exportable;
        BulkCipher cipher = this.cipherSuite.cipher;
        int expandedKeySize = is_exportable ? cipher.expandedKeySize : 0;
        if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
            keyMaterialAlg = "SunTls12KeyMaterial";
            prf = this.cipherSuite.prfAlg;
        } else {
            keyMaterialAlg = "SunTlsKeyMaterial";
            prf = PRF.P_NONE;
        }
        AlgorithmParameterSpec spec = new TlsKeyMaterialParameterSpec(masterKey, this.protocolVersion.major, this.protocolVersion.minor, this.clnt_random.random_bytes, this.svr_random.random_bytes, cipher.algorithm, cipher.keySize, expandedKeySize, cipher.ivSize, hashSize, prf.getPRFHashAlg(), prf.getPRFHashLength(), prf.getPRFBlockSize());
        try {
            KeyGenerator kg = JsseJce.getKeyGenerator(keyMaterialAlg);
            kg.init(spec);
            TlsKeyMaterialSpec keySpec = (TlsKeyMaterialSpec) kg.generateKey();
            this.clntWriteKey = keySpec.getClientCipherKey();
            this.svrWriteKey = keySpec.getServerCipherKey();
            this.clntWriteIV = keySpec.getClientIv();
            this.svrWriteIV = keySpec.getServerIv();
            this.clntMacSecret = keySpec.getClientMacKey();
            this.svrMacSecret = keySpec.getServerMacKey();
            if (debug != null && Debug.isOn("keygen")) {
                synchronized (System.out) {
                    HexDumpEncoder dump = new HexDumpEncoder();
                    System.out.println("CONNECTION KEYGEN:");
                    System.out.println("Client Nonce:");
                    printHex(dump, this.clnt_random.random_bytes);
                    System.out.println("Server Nonce:");
                    printHex(dump, this.svr_random.random_bytes);
                    System.out.println("Master Secret:");
                    printHex(dump, masterKey.getEncoded());
                    System.out.println("Client MAC write Secret:");
                    printHex(dump, this.clntMacSecret.getEncoded());
                    System.out.println("Server MAC write Secret:");
                    printHex(dump, this.svrMacSecret.getEncoded());
                    if (this.clntWriteKey != null) {
                        System.out.println("Client write key:");
                        printHex(dump, this.clntWriteKey.getEncoded());
                        System.out.println("Server write key:");
                        printHex(dump, this.svrWriteKey.getEncoded());
                    } else {
                        System.out.println("... no encryption keys used");
                    }
                    if (this.clntWriteIV != null) {
                        System.out.println("Client write IV:");
                        printHex(dump, this.clntWriteIV.getIV());
                        System.out.println("Server write IV:");
                        printHex(dump, this.svrWriteIV.getIV());
                    } else if (this.protocolVersion.v >= ProtocolVersion.TLS11.v) {
                        System.out.println("... no IV derived for this protocol");
                    } else {
                        System.out.println("... no IV used for this cipher");
                    }
                    System.out.flush();
                }
            }
        } catch (Throwable e) {
            throw new ProviderException(e);
        }
    }

    private static void printHex(HexDumpEncoder dump, byte[] bytes) {
        if (bytes == null) {
            System.out.println("(key bytes not available)");
            return;
        }
        try {
            dump.encodeBuffer(bytes, System.out);
        } catch (IOException e) {
        }
    }

    static void throwSSLException(String msg, Throwable cause) throws SSLException {
        SSLException e = new SSLException(msg);
        e.initCause(cause);
        throw e;
    }

    private <T> void delegateTask(PrivilegedExceptionAction<T> pea) {
        this.delegatedTask = new DelegatedTask(pea);
        this.taskDelegated = false;
        this.thrown = null;
    }

    DelegatedTask getTask() {
        if (this.taskDelegated) {
            return null;
        }
        this.taskDelegated = true;
        return this.delegatedTask;
    }

    boolean taskOutstanding() {
        return this.delegatedTask != null;
    }

    void checkThrown() throws SSLException {
        synchronized (this.thrownLock) {
            if (this.thrown != null) {
                String msg = this.thrown.getMessage();
                if (msg == null) {
                    msg = "Delegated task threw Exception/Error";
                }
                Exception e = this.thrown;
                this.thrown = null;
                if (e instanceof RuntimeException) {
                    throw ((RuntimeException) new RuntimeException(msg).initCause(e));
                } else if (e instanceof SSLHandshakeException) {
                    throw ((SSLHandshakeException) new SSLHandshakeException(msg).initCause(e));
                } else if (e instanceof SSLKeyException) {
                    throw ((SSLKeyException) new SSLKeyException(msg).initCause(e));
                } else if (e instanceof SSLPeerUnverifiedException) {
                    throw ((SSLPeerUnverifiedException) new SSLPeerUnverifiedException(msg).initCause(e));
                } else if (e instanceof SSLProtocolException) {
                    throw ((SSLProtocolException) new SSLProtocolException(msg).initCause(e));
                } else {
                    throw ((SSLException) new SSLException(msg).initCause(e));
                }
            }
        }
    }
}
