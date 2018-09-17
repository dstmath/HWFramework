package sun.security.ssl;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.Subject;
import sun.net.util.IPAddressUtil;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;

final class ClientHandshaker extends Handshaker {
    private static final /* synthetic */ int[] -sun-security-ssl-CipherSuite$KeyExchangeSwitchesValues = null;
    private static final boolean enableSNIExtension = false;
    private CertificateRequest certRequest;
    private DHCrypt dh;
    private ECDHCrypt ecdh;
    private PublicKey ephemeralServerKey;
    private ProtocolVersion maxProtocolVersion;
    private BigInteger serverDH;
    private PublicKey serverKey;
    private boolean serverKeyExchangeReceived;

    private static /* synthetic */ int[] -getsun-security-ssl-CipherSuite$KeyExchangeSwitchesValues() {
        if (-sun-security-ssl-CipherSuite$KeyExchangeSwitchesValues != null) {
            return -sun-security-ssl-CipherSuite$KeyExchangeSwitchesValues;
        }
        int[] iArr = new int[KeyExchange.values().length];
        try {
            iArr[KeyExchange.K_DHE_DSS.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[KeyExchange.K_DHE_RSA.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[KeyExchange.K_DH_ANON.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[KeyExchange.K_DH_DSS.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[KeyExchange.K_DH_RSA.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[KeyExchange.K_ECDHE_ECDSA.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[KeyExchange.K_ECDHE_RSA.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[KeyExchange.K_ECDH_ANON.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[KeyExchange.K_ECDH_ECDSA.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[KeyExchange.K_ECDH_RSA.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[KeyExchange.K_KRB5.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[KeyExchange.K_KRB5_EXPORT.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[KeyExchange.K_NULL.ordinal()] = 15;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[KeyExchange.K_RSA.ordinal()] = 13;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[KeyExchange.K_RSA_EXPORT.ordinal()] = 14;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[KeyExchange.K_SCSV.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        -sun-security-ssl-CipherSuite$KeyExchangeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.ClientHandshaker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.ClientHandshaker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.ClientHandshaker.<clinit>():void");
    }

    ClientHandshaker(SSLSocketImpl socket, SSLContextImpl context, ProtocolList enabledProtocols, ProtocolVersion activeProtocolVersion, boolean isInitialHandshake, boolean secureRenegotiation, byte[] clientVerifyData, byte[] serverVerifyData) {
        super(socket, context, enabledProtocols, true, true, activeProtocolVersion, isInitialHandshake, secureRenegotiation, clientVerifyData, serverVerifyData);
    }

    ClientHandshaker(SSLEngineImpl engine, SSLContextImpl context, ProtocolList enabledProtocols, ProtocolVersion activeProtocolVersion, boolean isInitialHandshake, boolean secureRenegotiation, byte[] clientVerifyData, byte[] serverVerifyData) {
        super(engine, context, enabledProtocols, true, true, activeProtocolVersion, isInitialHandshake, secureRenegotiation, clientVerifyData, serverVerifyData);
    }

    void processMessage(byte type, int messageLen) throws IOException {
        if (this.state < type || type == null) {
            switch (type) {
                case GeneralNameInterface.NAME_MATCH /*0*/:
                    serverHelloRequest(new HelloRequest(this.input));
                    break;
                case BaseCalendar.MONDAY /*2*/:
                    serverHello(new ServerHello(this.input, messageLen));
                    break;
                case BaseCalendar.NOVEMBER /*11*/:
                    if (!(this.keyExchange == KeyExchange.K_DH_ANON || this.keyExchange == KeyExchange.K_ECDH_ANON || this.keyExchange == KeyExchange.K_KRB5)) {
                        if (this.keyExchange == KeyExchange.K_KRB5_EXPORT) {
                        }
                        serverCertificate(new CertificateMsg(this.input));
                        this.serverKey = this.session.getPeerCertificates()[0].getPublicKey();
                        break;
                    }
                    fatalSE((byte) 10, "unexpected server cert chain");
                    serverCertificate(new CertificateMsg(this.input));
                    this.serverKey = this.session.getPeerCertificates()[0].getPublicKey();
                case BaseCalendar.DECEMBER /*12*/:
                    this.serverKeyExchangeReceived = true;
                    switch (-getsun-security-ssl-CipherSuite$KeyExchangeSwitchesValues()[this.keyExchange.ordinal()]) {
                        case BaseCalendar.SUNDAY /*1*/:
                        case BaseCalendar.MONDAY /*2*/:
                            try {
                                serverKeyExchange(new DH_ServerKeyExchange(this.input, this.serverKey, this.clnt_random.random_bytes, this.svr_random.random_bytes, messageLen, this.localSupportedSignAlgs, this.protocolVersion));
                                break;
                            } catch (GeneralSecurityException e) {
                                Handshaker.throwSSLException("Server key", e);
                                break;
                            }
                        case BaseCalendar.TUESDAY /*3*/:
                            try {
                                serverKeyExchange(new DH_ServerKeyExchange(this.input, this.protocolVersion));
                                break;
                            } catch (GeneralSecurityException e2) {
                                Handshaker.throwSSLException("Server key", e2);
                                break;
                            }
                        case BaseCalendar.WEDNESDAY /*4*/:
                        case BaseCalendar.THURSDAY /*5*/:
                        case BaseCalendar.SEPTEMBER /*9*/:
                        case BaseCalendar.OCTOBER /*10*/:
                        case Calendar.SECOND /*13*/:
                            throw new SSLProtocolException("Protocol violation: server sent a server key exchangemessage for key exchange " + this.keyExchange);
                        case BaseCalendar.JUNE /*6*/:
                        case BaseCalendar.SATURDAY /*7*/:
                        case BaseCalendar.AUGUST /*8*/:
                            try {
                                serverKeyExchange(new ECDH_ServerKeyExchange(this.input, this.serverKey, this.clnt_random.random_bytes, this.svr_random.random_bytes, this.localSupportedSignAlgs, this.protocolVersion));
                                break;
                            } catch (GeneralSecurityException e22) {
                                Handshaker.throwSSLException("Server key", e22);
                                break;
                            }
                        case BaseCalendar.NOVEMBER /*11*/:
                        case BaseCalendar.DECEMBER /*12*/:
                            throw new SSLProtocolException("unexpected receipt of server key exchange algorithm");
                        case ZipConstants.LOCCRC /*14*/:
                            if (this.serverKey != null) {
                                if (this.serverKey instanceof RSAPublicKey) {
                                    if (JsseJce.getRSAKeyLength(this.serverKey) > Modifier.INTERFACE) {
                                        try {
                                            serverKeyExchange(new RSA_ServerKeyExchange(this.input));
                                            break;
                                        } catch (GeneralSecurityException e222) {
                                            Handshaker.throwSSLException("Server key", e222);
                                            break;
                                        }
                                    }
                                    throw new SSLProtocolException("Protocol violation: server sent a server key exchange message for key exchange " + this.keyExchange + " when the public key in the server certificate" + " is less than or equal to 512 bits in length");
                                }
                                throw new SSLProtocolException("Protocol violation: the certificate type must be appropriate for the selected cipher suite's key exchange algorithm");
                            }
                            throw new SSLProtocolException("Server did not send certificate message");
                        default:
                            throw new SSLProtocolException("unsupported key exchange algorithm = " + this.keyExchange);
                    }
                case Calendar.SECOND /*13*/:
                    if (this.keyExchange != KeyExchange.K_DH_ANON && this.keyExchange != KeyExchange.K_ECDH_ANON) {
                        if (this.keyExchange != KeyExchange.K_KRB5 && this.keyExchange != KeyExchange.K_KRB5_EXPORT) {
                            this.certRequest = new CertificateRequest(this.input, this.protocolVersion);
                            if (debug != null && Debug.isOn("handshake")) {
                                this.certRequest.print(System.out);
                            }
                            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                                Collection peerSignAlgs = this.certRequest.getSignAlgorithms();
                                if (peerSignAlgs != null && !peerSignAlgs.isEmpty()) {
                                    Collection<SignatureAndHashAlgorithm> supportedPeerSignAlgs = SignatureAndHashAlgorithm.getSupportedAlgorithms(peerSignAlgs);
                                    if (!supportedPeerSignAlgs.isEmpty()) {
                                        setPeerSupportedSignAlgs(supportedPeerSignAlgs);
                                        this.session.setPeerSupportedSignatureAlgorithms(supportedPeerSignAlgs);
                                        break;
                                    }
                                    throw new SSLHandshakeException("No supported signature and hash algorithm in common");
                                }
                                throw new SSLHandshakeException("No peer supported signature algorithms");
                            }
                        }
                        throw new SSLHandshakeException("Client certificate requested for kerberos cipher suite.");
                    }
                    throw new SSLHandshakeException("Client authentication requested for anonymous cipher suite.");
                    break;
                case ZipConstants.LOCCRC /*14*/:
                    serverHelloDone(new ServerHelloDone(this.input));
                    break;
                case Record.trailerSize /*20*/:
                    serverFinished(new Finished(this.protocolVersion, this.input, this.cipherSuite));
                    break;
                default:
                    throw new SSLProtocolException("Illegal client handshake msg, " + type);
            }
            if (this.state < type) {
                this.state = type;
                return;
            }
            return;
        }
        throw new SSLProtocolException("Handshake message sequence violation, " + type);
    }

    private void serverHelloRequest(HelloRequest mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (this.state >= 1) {
            return;
        }
        if (this.secureRenegotiation || allowUnsafeRenegotiation) {
            if (!(this.secureRenegotiation || debug == null || !Debug.isOn("handshake"))) {
                System.out.println("Warning: continue with insecure renegotiation");
            }
            kickstart();
        } else if (this.activeProtocolVersion.v >= ProtocolVersion.TLS10.v) {
            warningSE((byte) 100);
            this.invalidated = true;
        } else {
            fatalSE((byte) 40, "Renegotiation is not allowed");
        }
    }

    private void serverHello(ServerHello mesg) throws IOException {
        this.serverKeyExchangeReceived = false;
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        Object mesgVersion = mesg.protocolVersion;
        if (isNegotiable((ProtocolVersion) mesgVersion)) {
            this.handshakeHash.protocolDetermined(mesgVersion);
            setVersion(mesgVersion);
            RenegotiationInfoExtension serverHelloRI = (RenegotiationInfoExtension) mesg.extensions.get(ExtensionType.EXT_RENEGOTIATION_INFO);
            if (serverHelloRI != null) {
                if (this.isInitialHandshake) {
                    if (!serverHelloRI.isEmpty()) {
                        fatalSE((byte) 40, "The renegotiation_info field is not empty");
                    }
                    this.secureRenegotiation = true;
                } else {
                    if (!this.secureRenegotiation) {
                        fatalSE((byte) 40, "Unexpected renegotiation indication extension");
                    }
                    byte[] verifyData = new byte[(this.clientVerifyData.length + this.serverVerifyData.length)];
                    System.arraycopy(this.clientVerifyData, 0, verifyData, 0, this.clientVerifyData.length);
                    System.arraycopy(this.serverVerifyData, 0, verifyData, this.clientVerifyData.length, this.serverVerifyData.length);
                    if (!Arrays.equals(verifyData, serverHelloRI.getRenegotiatedConnection())) {
                        fatalSE((byte) 40, "Incorrect verify data in ServerHello renegotiation_info message");
                    }
                }
            } else if (this.isInitialHandshake) {
                if (!allowLegacyHelloMessages) {
                    fatalSE((byte) 40, "Failed to negotiate the use of secure renegotiation");
                }
                this.secureRenegotiation = false;
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println("Warning: No renegotiation indication extension in ServerHello");
                }
            } else if (this.secureRenegotiation) {
                fatalSE((byte) 40, "No renegotiation indication extension");
            }
            this.svr_random = mesg.svr_random;
            if (!isNegotiable(mesg.cipherSuite)) {
                fatalSE((byte) 47, "Server selected improper ciphersuite " + mesg.cipherSuite);
            }
            setCipherSuite(mesg.cipherSuite);
            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                this.handshakeHash.setFinishedAlg(this.cipherSuite.prfAlg.getPRFHashAlg());
            }
            if (mesg.compression_method != null) {
                fatalSE((byte) 47, "compression type not supported, " + mesg.compression_method);
            }
            if (this.session != null) {
                if (this.session.getSessionId().equals(mesg.sessionId)) {
                    CipherSuite sessionSuite = this.session.getSuite();
                    if (this.cipherSuite != sessionSuite) {
                        throw new SSLProtocolException("Server returned wrong cipher suite for session");
                    }
                    if (this.protocolVersion != this.session.getProtocolVersion()) {
                        throw new SSLProtocolException("Server resumed session with wrong protocol version");
                    }
                    if (sessionSuite.keyExchange == KeyExchange.K_KRB5 || sessionSuite.keyExchange == KeyExchange.K_KRB5_EXPORT) {
                        Subject subject;
                        Principal localPrincipal = this.session.getLocalPrincipal();
                        try {
                            subject = (Subject) AccessController.doPrivileged(new PrivilegedExceptionAction<Subject>() {
                                public Subject run() throws Exception {
                                    return Krb5Helper.getClientSubject(ClientHandshaker.this.getAccSE());
                                }
                            });
                        } catch (PrivilegedActionException e) {
                            subject = null;
                            if (debug != null && Debug.isOn("session")) {
                                System.out.println("Attempt to obtain subject failed!");
                            }
                        }
                        if (subject != null) {
                            if (!subject.getPrincipals(Principal.class).contains(localPrincipal)) {
                                throw new SSLProtocolException("Server resumed session with wrong subject identity");
                            } else if (debug != null && Debug.isOn("session")) {
                                System.out.println("Subject identity is same");
                            }
                        } else {
                            if (debug != null && Debug.isOn("session")) {
                                System.out.println("Kerberos credentials are not present in the current Subject; check if  javax.security.auth.useSubjectAsCreds system property has been set to false");
                            }
                            throw new SSLProtocolException("Server resumed session with no subject");
                        }
                    }
                    this.resumingSession = true;
                    this.state = 19;
                    calculateConnectionKeys(this.session.getMasterSecret());
                    if (debug != null && Debug.isOn("session")) {
                        System.out.println("%% Server resumed " + this.session);
                    }
                } else {
                    this.session = null;
                    if (!this.enableNewSession) {
                        throw new SSLException("New session creation is disabled");
                    }
                }
            }
            if (!this.resumingSession || this.session == null) {
                for (HelloExtension ext : mesg.extensions.list()) {
                    ExtensionType type = ext.type;
                    if (!(type == ExtensionType.EXT_ELLIPTIC_CURVES || type == ExtensionType.EXT_EC_POINT_FORMATS || type == ExtensionType.EXT_SERVER_NAME || type == ExtensionType.EXT_RENEGOTIATION_INFO)) {
                        fatalSE((byte) 110, "Server sent an unsupported extension: " + type);
                    }
                }
                this.session = new SSLSessionImpl(this.protocolVersion, this.cipherSuite, getLocalSupportedSignAlgs(), mesg.sessionId, getHostSE(), getPortSE());
                setHandshakeSessionSE(this.session);
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println("** " + this.cipherSuite);
                }
                return;
            }
            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                this.handshakeHash.setCertificateVerifyAlg(null);
            }
            setHandshakeSessionSE(this.session);
            return;
        }
        throw new SSLHandshakeException("Server chose " + mesgVersion + ", but that protocol version is not enabled or not supported " + "by the client.");
    }

    private void serverKeyExchange(RSA_ServerKeyExchange mesg) throws IOException, GeneralSecurityException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (!mesg.verify(this.serverKey, this.clnt_random, this.svr_random)) {
            fatalSE((byte) 40, "server key exchange invalid");
        }
        this.ephemeralServerKey = mesg.getPublicKey();
    }

    private void serverKeyExchange(DH_ServerKeyExchange mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        this.dh = new DHCrypt(mesg.getModulus(), mesg.getBase(), this.sslContext.getSecureRandom());
        this.serverDH = mesg.getServerPublicKey();
    }

    private void serverKeyExchange(ECDH_ServerKeyExchange mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        ECPublicKey key = mesg.getPublicKey();
        this.ecdh = new ECDHCrypt(key.getParams(), this.sslContext.getSecureRandom());
        this.ephemeralServerKey = key;
    }

    private void serverHelloDone(ServerHelloDone mesg) throws IOException {
        HandshakeMessage m2;
        SecretKey preMasterSecret;
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        this.input.digestNow();
        PrivateKey privateKey = null;
        if (this.certRequest != null) {
            CertificateMsg certificateMsg;
            X509ExtendedKeyManager km = this.sslContext.getX509KeyManager();
            ArrayList<String> arrayList = new ArrayList(4);
            for (byte b : this.certRequest.types) {
                String typeName;
                switch (b) {
                    case BaseCalendar.SUNDAY /*1*/:
                        typeName = "RSA";
                        break;
                    case BaseCalendar.MONDAY /*2*/:
                        typeName = "DSA";
                        break;
                    case Pattern.UNICODE_CASE /*64*/:
                        if (!JsseJce.isEcAvailable()) {
                            typeName = null;
                            break;
                        } else {
                            typeName = "EC";
                            break;
                        }
                    default:
                        typeName = null;
                        break;
                }
                if (!(typeName == null || arrayList.contains(typeName))) {
                    arrayList.add(typeName);
                }
            }
            String alias = null;
            int keytypesTmpSize = arrayList.size();
            if (keytypesTmpSize != 0) {
                String[] keytypes = (String[]) arrayList.toArray(new String[keytypesTmpSize]);
                if (this.conn != null) {
                    alias = km.chooseClientAlias(keytypes, this.certRequest.getAuthorities(), this.conn);
                } else {
                    alias = km.chooseEngineClientAlias(keytypes, this.certRequest.getAuthorities(), this.engine);
                }
            }
            CertificateMsg certificateMsg2 = null;
            if (alias != null) {
                X509Certificate[] certs = km.getCertificateChain(alias);
                if (!(certs == null || certs.length == 0)) {
                    PublicKey publicKey = certs[0].getPublicKey();
                    if ((publicKey instanceof ECPublicKey) && !SupportedEllipticCurvesExtension.isSupported(SupportedEllipticCurvesExtension.getCurveIndex(((ECPublicKey) publicKey).getParams()))) {
                        publicKey = null;
                    }
                    if (publicKey != null) {
                        certificateMsg = new CertificateMsg(certs);
                        privateKey = km.getPrivateKey(alias);
                        this.session.setLocalPrivateKey(privateKey);
                        this.session.setLocalCertificates(certs);
                    }
                }
            }
            if (certificateMsg2 == null) {
                if (this.protocolVersion.v >= ProtocolVersion.TLS10.v) {
                    certificateMsg = new CertificateMsg(new X509Certificate[0]);
                } else {
                    warningSE((byte) 41);
                }
            }
            if (certificateMsg2 != null) {
                if (debug != null && Debug.isOn("handshake")) {
                    certificateMsg2.print(System.out);
                }
                certificateMsg2.write(this.output);
            }
        }
        HandshakeMessage dHClientKeyExchange;
        switch (-getsun-security-ssl-CipherSuite$KeyExchangeSwitchesValues()[this.keyExchange.ordinal()]) {
            case BaseCalendar.SUNDAY /*1*/:
            case BaseCalendar.MONDAY /*2*/:
            case BaseCalendar.TUESDAY /*3*/:
                if (this.dh != null) {
                    dHClientKeyExchange = new DHClientKeyExchange(this.dh.getPublicKey());
                    break;
                }
                throw new SSLProtocolException("Server did not send a DH Server Key Exchange message");
            case BaseCalendar.WEDNESDAY /*4*/:
            case BaseCalendar.THURSDAY /*5*/:
                m2 = new DHClientKeyExchange();
                break;
            case BaseCalendar.JUNE /*6*/:
            case BaseCalendar.SATURDAY /*7*/:
            case BaseCalendar.AUGUST /*8*/:
                if (this.ecdh != null) {
                    dHClientKeyExchange = new ECDHClientKeyExchange(this.ecdh.getPublicKey());
                    break;
                }
                throw new SSLProtocolException("Server did not send a ECDH Server Key Exchange message");
            case BaseCalendar.SEPTEMBER /*9*/:
            case BaseCalendar.OCTOBER /*10*/:
                if (this.serverKey != null) {
                    if (this.serverKey instanceof ECPublicKey) {
                        this.ecdh = new ECDHCrypt(((ECPublicKey) this.serverKey).getParams(), this.sslContext.getSecureRandom());
                        dHClientKeyExchange = new ECDHClientKeyExchange(this.ecdh.getPublicKey());
                        break;
                    }
                    throw new SSLProtocolException("Server certificate does not include an EC key");
                }
                throw new SSLProtocolException("Server did not send certificate message");
            case BaseCalendar.NOVEMBER /*11*/:
            case BaseCalendar.DECEMBER /*12*/:
                String hostname = getHostSE();
                if (hostname != null) {
                    HandshakeMessage kerberosMsg = new KerberosClientKeyExchange(hostname, isLoopbackSE(), getAccSE(), this.protocolVersion, this.sslContext.getSecureRandom());
                    this.session.setPeerPrincipal(kerberosMsg.getPeerPrincipal());
                    this.session.setLocalPrincipal(kerberosMsg.getLocalPrincipal());
                    m2 = kerberosMsg;
                    break;
                }
                throw new IOException("Hostname is required to use Kerberos cipher suites");
            case Calendar.SECOND /*13*/:
            case ZipConstants.LOCCRC /*14*/:
                if (this.serverKey != null) {
                    if (this.serverKey instanceof RSAPublicKey) {
                        PublicKey key;
                        if (this.keyExchange == KeyExchange.K_RSA) {
                            key = this.serverKey;
                        } else if (JsseJce.getRSAKeyLength(this.serverKey) <= Modifier.INTERFACE) {
                            key = this.serverKey;
                        } else if (this.ephemeralServerKey == null) {
                            throw new SSLProtocolException("Server did not send a RSA_EXPORT Server Key Exchange message");
                        } else {
                            key = this.ephemeralServerKey;
                        }
                        dHClientKeyExchange = new RSAClientKeyExchange(this.protocolVersion, this.maxProtocolVersion, this.sslContext.getSecureRandom(), key);
                        break;
                    }
                    throw new SSLProtocolException("Server certificate does not include an RSA key");
                }
                throw new SSLProtocolException("Server did not send certificate message");
            default:
                throw new RuntimeException("Unsupported key exchange: " + this.keyExchange);
        }
        if (debug != null && Debug.isOn("handshake")) {
            m2.print(System.out);
        }
        m2.write(this.output);
        this.output.doHashes();
        this.output.flush();
        switch (-getsun-security-ssl-CipherSuite$KeyExchangeSwitchesValues()[this.keyExchange.ordinal()]) {
            case BaseCalendar.SUNDAY /*1*/:
            case BaseCalendar.MONDAY /*2*/:
            case BaseCalendar.TUESDAY /*3*/:
                preMasterSecret = this.dh.getAgreedSecret(this.serverDH, true);
                break;
            case BaseCalendar.JUNE /*6*/:
            case BaseCalendar.SATURDAY /*7*/:
            case BaseCalendar.AUGUST /*8*/:
                preMasterSecret = this.ecdh.getAgreedSecret(this.ephemeralServerKey);
                break;
            case BaseCalendar.SEPTEMBER /*9*/:
            case BaseCalendar.OCTOBER /*10*/:
                preMasterSecret = this.ecdh.getAgreedSecret(this.serverKey);
                break;
            case BaseCalendar.NOVEMBER /*11*/:
            case BaseCalendar.DECEMBER /*12*/:
                SecretKey secretKeySpec = new SecretKeySpec(((KerberosClientKeyExchange) m2).getUnencryptedPreMasterSecret(), "TlsPremasterSecret");
                break;
            case Calendar.SECOND /*13*/:
            case ZipConstants.LOCCRC /*14*/:
                preMasterSecret = ((RSAClientKeyExchange) m2).preMaster;
                break;
            default:
                throw new IOException("Internal error: unknown key exchange " + this.keyExchange);
        }
        calculateKeys(preMasterSecret, null);
        if (privateKey != null) {
            CertificateVerify m3;
            SignatureAndHashAlgorithm signatureAndHashAlgorithm = null;
            try {
                if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    signatureAndHashAlgorithm = SignatureAndHashAlgorithm.getPreferableAlgorithm(this.peerSupportedSignAlgs, privateKey.getAlgorithm(), privateKey);
                    if (signatureAndHashAlgorithm == null) {
                        throw new SSLHandshakeException("No supported signature algorithm");
                    }
                    String hashAlg = SignatureAndHashAlgorithm.getHashAlgorithmName(signatureAndHashAlgorithm);
                    if (hashAlg == null || hashAlg.length() == 0) {
                        throw new SSLHandshakeException("No supported hash algorithm");
                    }
                    this.handshakeHash.setCertificateVerifyAlg(hashAlg);
                }
                m3 = new CertificateVerify(this.protocolVersion, this.handshakeHash, privateKey, this.session.getMasterSecret(), this.sslContext.getSecureRandom(), signatureAndHashAlgorithm);
            } catch (GeneralSecurityException e) {
                fatalSE((byte) 40, "Error signing certificate verify", e);
                m3 = null;
            }
            if (debug != null && Debug.isOn("handshake")) {
                m3.print(System.out);
            }
            m3.write(this.output);
            this.output.doHashes();
        } else if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
            this.handshakeHash.setCertificateVerifyAlg(null);
        }
        sendChangeCipherAndFinish(false);
    }

    private void serverFinished(Finished mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (!mesg.verify(this.handshakeHash, 2, this.session.getMasterSecret())) {
            fatalSE((byte) 47, "server 'finished' message doesn't verify");
        }
        if (this.secureRenegotiation) {
            this.serverVerifyData = mesg.getVerifyData();
        }
        if (this.resumingSession) {
            this.input.digestNow();
            sendChangeCipherAndFinish(true);
        }
        this.session.setLastAccessedTime(System.currentTimeMillis());
        if (!this.resumingSession) {
            if (this.session.isRejoinable()) {
                ((SSLSessionContextImpl) this.sslContext.engineGetClientSessionContext()).put(this.session);
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% Cached client session: " + this.session);
                }
            } else if (debug != null && Debug.isOn("session")) {
                System.out.println("%% Didn't cache non-resumable client session: " + this.session);
            }
        }
    }

    private void sendChangeCipherAndFinish(boolean finishedTag) throws IOException {
        Finished mesg = new Finished(this.protocolVersion, this.handshakeHash, 1, this.session.getMasterSecret(), this.cipherSuite);
        sendChangeCipherSpec(mesg, finishedTag);
        if (this.secureRenegotiation) {
            this.clientVerifyData = mesg.getVerifyData();
        }
        this.state = 19;
    }

    HandshakeMessage getKickstartMessage() throws SSLException {
        Collection cipherList;
        SessionId sessionId = SSLSessionImpl.nullSession.getSessionId();
        CipherSuiteList activeCipherSuites = getActiveCipherSuites();
        this.maxProtocolVersion = this.protocolVersion;
        this.session = ((SSLSessionContextImpl) this.sslContext.engineGetClientSessionContext()).get(getHostSE(), getPortSE());
        if (debug != null && Debug.isOn("session")) {
            if (this.session != null) {
                System.out.println("%% Client cached " + this.session + (this.session.isRejoinable() ? "" : " (not rejoinable)"));
            } else {
                System.out.println("%% No cached client session");
            }
        }
        if (!(this.session == null || this.session.isRejoinable())) {
            this.session = null;
        }
        if (this.session != null) {
            CipherSuite sessionSuite = this.session.getSuite();
            ProtocolVersion sessionVersion = this.session.getProtocolVersion();
            if (!isNegotiable(sessionSuite)) {
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% can't resume, unavailable cipher");
                }
                this.session = null;
            }
            if (!(this.session == null || isNegotiable(sessionVersion))) {
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% can't resume, protocol disabled");
                }
                this.session = null;
            }
            if (this.session != null) {
                if (debug != null && (Debug.isOn("handshake") || Debug.isOn("session"))) {
                    System.out.println("%% Try resuming " + this.session + " from port " + getLocalPortSE());
                }
                sessionId = this.session.getSessionId();
                this.maxProtocolVersion = sessionVersion;
                setVersion(sessionVersion);
            }
            if (!this.enableNewSession) {
                if (this.session == null) {
                    throw new SSLHandshakeException("Can't reuse existing SSL client session");
                }
                cipherList = new ArrayList(2);
                cipherList.add(sessionSuite);
                if (!this.secureRenegotiation && activeCipherSuites.contains(CipherSuite.C_SCSV)) {
                    cipherList.add(CipherSuite.C_SCSV);
                }
                activeCipherSuites = new CipherSuiteList(cipherList);
            }
        }
        if (this.session != null || this.enableNewSession) {
            if (this.secureRenegotiation && activeCipherSuites.contains(CipherSuite.C_SCSV)) {
                cipherList = new ArrayList(activeCipherSuites.size() - 1);
                for (CipherSuite suite : activeCipherSuites.collection()) {
                    if (suite != CipherSuite.C_SCSV) {
                        cipherList.add(suite);
                    }
                }
                activeCipherSuites = new CipherSuiteList(cipherList);
            }
            boolean negotiable = false;
            for (CipherSuite suite2 : activeCipherSuites.collection()) {
                if (isNegotiable(suite2)) {
                    negotiable = true;
                    break;
                }
            }
            if (negotiable) {
                ClientHello clientHelloMessage = new ClientHello(this.sslContext.getSecureRandom(), this.maxProtocolVersion, sessionId, activeCipherSuites);
                if (this.maxProtocolVersion.v >= ProtocolVersion.TLS12.v) {
                    Collection<SignatureAndHashAlgorithm> localSignAlgs = getLocalSupportedSignAlgs();
                    if (localSignAlgs.isEmpty()) {
                        throw new SSLHandshakeException("No supported signature algorithm");
                    }
                    clientHelloMessage.addSignatureAlgorithmsExtension(localSignAlgs);
                }
                if (enableSNIExtension) {
                    String hostname = getRawHostnameSE();
                    if (!(hostname == null || hostname.indexOf(46) <= 0 || IPAddressUtil.isIPv4LiteralAddress(hostname) || IPAddressUtil.isIPv6LiteralAddress(hostname))) {
                        clientHelloMessage.addServerNameIndicationExtension(hostname);
                    }
                }
                this.clnt_random = clientHelloMessage.clnt_random;
                if (this.secureRenegotiation || !activeCipherSuites.contains(CipherSuite.C_SCSV)) {
                    clientHelloMessage.addRenegotiationInfoExtension(this.clientVerifyData);
                }
                return clientHelloMessage;
            }
            throw new SSLHandshakeException("No negotiable cipher suite");
        }
        throw new SSLHandshakeException("No existing session to resume");
    }

    void handshakeAlert(byte description) throws SSLProtocolException {
        String message = Alerts.alertDescription(description);
        if (debug != null && Debug.isOn("handshake")) {
            System.out.println("SSL - handshake alert: " + message);
        }
        throw new SSLProtocolException("handshake alert:  " + message);
    }

    private void serverCertificate(CertificateMsg mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        X509Certificate[] peerCerts = mesg.getCertificateChain();
        if (peerCerts.length == 0) {
            fatalSE((byte) 42, "empty certificate chain");
        }
        X509TrustManager tm = this.sslContext.getX509TrustManager();
        try {
            String keyExchangeString;
            if (this.keyExchange != KeyExchange.K_RSA_EXPORT || this.serverKeyExchangeReceived) {
                keyExchangeString = this.keyExchange.name;
            } else {
                keyExchangeString = KeyExchange.K_RSA.name;
            }
            if (tm instanceof X509ExtendedTrustManager) {
                if (this.conn != null) {
                    ((X509ExtendedTrustManager) tm).checkServerTrusted((X509Certificate[]) peerCerts.clone(), keyExchangeString, this.conn);
                } else {
                    ((X509ExtendedTrustManager) tm).checkServerTrusted((X509Certificate[]) peerCerts.clone(), keyExchangeString, this.engine);
                }
                this.session.setPeerCertificates(peerCerts);
                return;
            }
            throw new CertificateException("Improper X509TrustManager implementation");
        } catch (CertificateException e) {
            fatalSE((byte) 46, (Throwable) e);
        }
    }
}
