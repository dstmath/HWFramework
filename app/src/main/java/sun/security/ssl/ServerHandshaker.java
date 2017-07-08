package sun.security.ssl;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.Subject;
import sun.security.util.SecurityConstants;
import sun.util.calendar.BaseCalendar;
import sun.util.locale.BaseLocale;

final class ServerHandshaker extends Handshaker {
    private static final /* synthetic */ int[] -sun-security-ssl-CipherSuite$KeyExchangeSwitchesValues = null;
    private X509Certificate[] certs;
    private ProtocolVersion clientRequestedVersion;
    private DHCrypt dh;
    private byte doClientAuth;
    private ECDHCrypt ecdh;
    private SecretKey[] kerberosKeys;
    private boolean needClientVerify;
    SignatureAndHashAlgorithm preferableSignatureAlgorithm;
    private PrivateKey privateKey;
    private SupportedEllipticCurvesExtension supportedCurves;
    private PrivateKey tempPrivateKey;
    private PublicKey tempPublicKey;

    /* renamed from: sun.security.ssl.ServerHandshaker.2 */
    class AnonymousClass2 implements PrivilegedExceptionAction<SecretKey[]> {
        final /* synthetic */ AccessControlContext val$acc;

        AnonymousClass2(AccessControlContext val$acc) {
            this.val$acc = val$acc;
        }

        public SecretKey[] run() throws Exception {
            return Krb5Helper.getServerKeys(this.val$acc);
        }
    }

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

    ServerHandshaker(SSLSocketImpl socket, SSLContextImpl context, ProtocolList enabledProtocols, byte clientAuth, ProtocolVersion activeProtocolVersion, boolean isInitialHandshake, boolean secureRenegotiation, byte[] clientVerifyData, byte[] serverVerifyData) {
        super(socket, context, enabledProtocols, clientAuth != null, false, activeProtocolVersion, isInitialHandshake, secureRenegotiation, clientVerifyData, serverVerifyData);
        this.needClientVerify = false;
        this.doClientAuth = clientAuth;
    }

    ServerHandshaker(SSLEngineImpl engine, SSLContextImpl context, ProtocolList enabledProtocols, byte clientAuth, ProtocolVersion activeProtocolVersion, boolean isInitialHandshake, boolean secureRenegotiation, byte[] clientVerifyData, byte[] serverVerifyData) {
        super(engine, context, enabledProtocols, clientAuth != null, false, activeProtocolVersion, isInitialHandshake, secureRenegotiation, clientVerifyData, serverVerifyData);
        this.needClientVerify = false;
        this.doClientAuth = clientAuth;
    }

    void setClientAuth(byte clientAuth) {
        this.doClientAuth = clientAuth;
    }

    void processMessage(byte type, int message_len) throws IOException {
        if (this.state < type || this.state == 16 || type == Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE) {
            switch (type) {
                case BaseCalendar.SUNDAY /*1*/:
                    clientHello(new ClientHello(this.input, message_len));
                    break;
                case BaseCalendar.NOVEMBER /*11*/:
                    if (this.doClientAuth == null) {
                        fatalSE((byte) 10, "client sent unsolicited cert chain");
                    }
                    clientCertificate(new CertificateMsg(this.input));
                    break;
                case Calendar.ZONE_OFFSET /*15*/:
                    clientCertificateVerify(new CertificateVerify(this.input, this.localSupportedSignAlgs, this.protocolVersion));
                    break;
                case AbstractSpinedBuffer.MIN_CHUNK_SIZE /*16*/:
                    SecretKey preMasterSecret;
                    switch (-getsun-security-ssl-CipherSuite$KeyExchangeSwitchesValues()[this.keyExchange.ordinal()]) {
                        case BaseCalendar.SUNDAY /*1*/:
                        case BaseCalendar.MONDAY /*2*/:
                        case BaseCalendar.TUESDAY /*3*/:
                            preMasterSecret = clientKeyExchange(new DHClientKeyExchange(this.input));
                            break;
                        case BaseCalendar.JUNE /*6*/:
                        case BaseCalendar.SATURDAY /*7*/:
                        case BaseCalendar.AUGUST /*8*/:
                        case BaseCalendar.SEPTEMBER /*9*/:
                        case BaseCalendar.OCTOBER /*10*/:
                            preMasterSecret = clientKeyExchange(new ECDHClientKeyExchange(this.input));
                            break;
                        case BaseCalendar.NOVEMBER /*11*/:
                        case BaseCalendar.DECEMBER /*12*/:
                            preMasterSecret = clientKeyExchange(new KerberosClientKeyExchange(this.protocolVersion, this.clientRequestedVersion, this.sslContext.getSecureRandom(), this.input, this.kerberosKeys));
                            break;
                        case Calendar.SECOND /*13*/:
                        case ZipConstants.LOCCRC /*14*/:
                            preMasterSecret = clientKeyExchange(new RSAClientKeyExchange(this.protocolVersion, this.clientRequestedVersion, this.sslContext.getSecureRandom(), this.input, message_len, this.privateKey));
                            break;
                        default:
                            throw new SSLProtocolException("Unrecognized key exchange: " + this.keyExchange);
                    }
                    calculateKeys(preMasterSecret, this.clientRequestedVersion);
                    break;
                case Record.trailerSize /*20*/:
                    clientFinished(new Finished(this.protocolVersion, this.input, this.cipherSuite));
                    break;
                default:
                    throw new SSLProtocolException("Illegal server handshake msg, " + type);
            }
            if (this.state >= type) {
                return;
            }
            if (type == Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE) {
                this.state = type + 2;
                return;
            } else {
                this.state = type;
                return;
            }
        }
        throw new SSLProtocolException("Handshake message sequence violation, state = " + this.state + ", type = " + type);
    }

    private void clientHello(ClientHello mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        boolean renegotiationIndicated = false;
        if (mesg.getCipherSuites().contains(CipherSuite.C_SCSV)) {
            renegotiationIndicated = true;
            if (this.isInitialHandshake) {
                this.secureRenegotiation = true;
            } else if (this.secureRenegotiation) {
                fatalSE((byte) 40, "The SCSV is present in a secure renegotiation");
            } else {
                fatalSE((byte) 40, "The SCSV is present in a insecure renegotiation");
            }
        }
        RenegotiationInfoExtension clientHelloRI = (RenegotiationInfoExtension) mesg.extensions.get(ExtensionType.EXT_RENEGOTIATION_INFO);
        if (clientHelloRI != null) {
            renegotiationIndicated = true;
            if (this.isInitialHandshake) {
                if (!clientHelloRI.isEmpty()) {
                    fatalSE((byte) 40, "The renegotiation_info field is not empty");
                }
                this.secureRenegotiation = true;
            } else {
                if (!this.secureRenegotiation) {
                    fatalSE((byte) 40, "The renegotiation_info is present in a insecure renegotiation");
                }
                if (!Arrays.equals(this.clientVerifyData, clientHelloRI.getRenegotiatedConnection())) {
                    fatalSE((byte) 40, "Incorrect verify data in ClientHello renegotiation_info message");
                }
            }
        } else if (!this.isInitialHandshake && this.secureRenegotiation) {
            fatalSE((byte) 40, "Inconsistent secure renegotiation indication");
        }
        if (!(renegotiationIndicated && this.secureRenegotiation)) {
            if (this.isInitialHandshake) {
                if (!allowLegacyHelloMessages) {
                    fatalSE((byte) 40, "Failed to negotiate the use of secure renegotiation");
                }
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println("Warning: No renegotiation indication in ClientHello, allow legacy ClientHello");
                }
            } else if (allowUnsafeRenegotiation) {
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println("Warning: continue with insecure renegotiation");
                }
            } else if (this.activeProtocolVersion.v >= ProtocolVersion.TLS10.v) {
                warningSE((byte) 100);
                this.invalidated = true;
                if (this.input.available() > 0) {
                    fatalSE((byte) 10, "ClientHello followed by an unexpected  handshake message");
                }
                return;
            } else {
                fatalSE((byte) 40, "Renegotiation is not allowed");
            }
        }
        this.input.digestNow();
        ServerHello m1 = new ServerHello();
        this.clientRequestedVersion = mesg.protocolVersion;
        ProtocolVersion selectedVersion = selectProtocolVersion(this.clientRequestedVersion);
        if (selectedVersion == null || selectedVersion.v == ProtocolVersion.SSL20Hello.v) {
            fatalSE((byte) 40, "Client requested protocol " + this.clientRequestedVersion + " not enabled or not supported");
        }
        this.handshakeHash.protocolDetermined(selectedVersion);
        setVersion(selectedVersion);
        m1.protocolVersion = this.protocolVersion;
        this.clnt_random = mesg.clnt_random;
        this.svr_random = new RandomCookie(this.sslContext.getSecureRandom());
        m1.svr_random = this.svr_random;
        this.session = null;
        if (mesg.sessionId.length() != 0) {
            SSLSessionImpl previous = ((SSLSessionContextImpl) this.sslContext.engineGetServerSessionContext()).get(mesg.sessionId.getId());
            if (previous != null) {
                CipherSuite suite;
                this.resumingSession = previous.isRejoinable();
                if (this.resumingSession && previous.getProtocolVersion() != this.protocolVersion) {
                    this.resumingSession = false;
                }
                if (this.resumingSession && this.doClientAuth == 2) {
                    try {
                        previous.getPeerPrincipal();
                    } catch (SSLPeerUnverifiedException e) {
                        this.resumingSession = false;
                    }
                }
                if (this.resumingSession) {
                    suite = previous.getSuite();
                    if (suite.keyExchange == KeyExchange.K_KRB5 || suite.keyExchange == KeyExchange.K_KRB5_EXPORT) {
                        Subject subject;
                        Principal localPrincipal = previous.getLocalPrincipal();
                        try {
                            subject = (Subject) AccessController.doPrivileged(new PrivilegedExceptionAction<Subject>() {
                                public Subject run() throws Exception {
                                    return Krb5Helper.getServerSubject(ServerHandshaker.this.getAccSE());
                                }
                            });
                        } catch (PrivilegedActionException e2) {
                            subject = null;
                            if (debug != null && Debug.isOn("session")) {
                                System.out.println("Attempt to obtain subject failed!");
                            }
                        }
                        if (subject != null) {
                            if (!subject.getPrincipals(Principal.class).contains(localPrincipal)) {
                                this.resumingSession = false;
                                if (debug != null && Debug.isOn("session")) {
                                    System.out.println("Subject identity is not the same");
                                }
                            } else if (debug != null && Debug.isOn("session")) {
                                System.out.println("Subject identity is same");
                            }
                        } else {
                            this.resumingSession = false;
                            if (debug != null && Debug.isOn("session")) {
                                System.out.println("Kerberos credentials are not present in the current Subject; check if  javax.security.auth.useSubjectAsCreds system property has been set to false");
                            }
                        }
                    }
                }
                if (this.resumingSession) {
                    suite = previous.getSuite();
                    if (isNegotiable(suite) && mesg.getCipherSuites().contains(suite)) {
                        setCipherSuite(suite);
                    } else {
                        this.resumingSession = false;
                    }
                }
                if (this.resumingSession) {
                    this.session = previous;
                    if (debug != null && (Debug.isOn("handshake") || Debug.isOn("session"))) {
                        System.out.println("%% Resuming " + this.session);
                    }
                }
            }
        }
        if (this.session != null) {
            setHandshakeSessionSE(this.session);
        } else if (this.enableNewSession) {
            this.supportedCurves = (SupportedEllipticCurvesExtension) mesg.extensions.get(ExtensionType.EXT_ELLIPTIC_CURVES);
            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                SignatureAlgorithmsExtension signAlgs = (SignatureAlgorithmsExtension) mesg.extensions.get(ExtensionType.EXT_SIGNATURE_ALGORITHMS);
                if (signAlgs != null) {
                    Collection peerSignAlgs = signAlgs.getSignAlgorithms();
                    if (peerSignAlgs == null || peerSignAlgs.isEmpty()) {
                        throw new SSLHandshakeException("No peer supported signature algorithms");
                    }
                    Collection<SignatureAndHashAlgorithm> supportedPeerSignAlgs = SignatureAndHashAlgorithm.getSupportedAlgorithms(peerSignAlgs);
                    if (supportedPeerSignAlgs.isEmpty()) {
                        throw new SSLHandshakeException("No supported signature and hash algorithm in common");
                    }
                    setPeerSupportedSignAlgs(supportedPeerSignAlgs);
                }
            }
            this.session = new SSLSessionImpl(this.protocolVersion, CipherSuite.C_NULL, getLocalSupportedSignAlgs(), this.sslContext.getSecureRandom(), getHostAddressSE(), getPortSE());
            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v && this.peerSupportedSignAlgs != null) {
                this.session.setPeerSupportedSignatureAlgorithms(this.peerSupportedSignAlgs);
            }
            setHandshakeSessionSE(this.session);
            chooseCipherSuite(mesg);
            this.session.setSuite(this.cipherSuite);
            this.session.setLocalPrivateKey(this.privateKey);
        } else {
            throw new SSLException("Client did not resume a session");
        }
        if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
            if (this.resumingSession) {
                this.handshakeHash.setCertificateVerifyAlg(null);
            }
            this.handshakeHash.setFinishedAlg(this.cipherSuite.prfAlg.getPRFHashAlg());
        }
        m1.cipherSuite = this.cipherSuite;
        m1.sessionId = this.session.getSessionId();
        m1.compression_method = this.session.getCompression();
        if (this.secureRenegotiation) {
            m1.extensions.add(new RenegotiationInfoExtension(this.clientVerifyData, this.serverVerifyData));
        }
        if (debug != null && Debug.isOn("handshake")) {
            m1.print(System.out);
            System.out.println("Cipher suite:  " + this.session.getSuite());
        }
        m1.write(this.output);
        if (this.resumingSession) {
            calculateConnectionKeys(this.session.getMasterSecret());
            sendChangeCipherAndFinish(false);
            return;
        }
        ServerKeyExchange m3;
        if (!(this.keyExchange == KeyExchange.K_KRB5 || this.keyExchange == KeyExchange.K_KRB5_EXPORT)) {
            if (this.keyExchange == KeyExchange.K_DH_ANON || this.keyExchange == KeyExchange.K_ECDH_ANON) {
                if (this.certs != null) {
                    throw new RuntimeException("anonymous keyexchange with certs");
                }
            } else if (this.certs == null) {
                throw new RuntimeException("no certificates");
            } else {
                CertificateMsg certificateMsg = new CertificateMsg(this.certs);
                this.session.setLocalCertificates(this.certs);
                if (debug != null && Debug.isOn("handshake")) {
                    certificateMsg.print(System.out);
                }
                certificateMsg.write(this.output);
            }
        }
        switch (-getsun-security-ssl-CipherSuite$KeyExchangeSwitchesValues()[this.keyExchange.ordinal()]) {
            case BaseCalendar.SUNDAY /*1*/:
            case BaseCalendar.MONDAY /*2*/:
                try {
                    m3 = new DH_ServerKeyExchange(this.dh, this.privateKey, this.clnt_random.random_bytes, this.svr_random.random_bytes, this.sslContext.getSecureRandom(), this.preferableSignatureAlgorithm, this.protocolVersion);
                    break;
                } catch (GeneralSecurityException e3) {
                    Handshaker.throwSSLException("Error generating DH server key exchange", e3);
                    m3 = null;
                    break;
                }
            case BaseCalendar.TUESDAY /*3*/:
                m3 = new DH_ServerKeyExchange(this.dh, this.protocolVersion);
                break;
            case BaseCalendar.JUNE /*6*/:
            case BaseCalendar.SATURDAY /*7*/:
            case BaseCalendar.AUGUST /*8*/:
                try {
                    m3 = new ECDH_ServerKeyExchange(this.ecdh, this.privateKey, this.clnt_random.random_bytes, this.svr_random.random_bytes, this.sslContext.getSecureRandom(), this.preferableSignatureAlgorithm, this.protocolVersion);
                    break;
                } catch (GeneralSecurityException e32) {
                    Handshaker.throwSSLException("Error generating ECDH server key exchange", e32);
                    m3 = null;
                    break;
                }
            case BaseCalendar.SEPTEMBER /*9*/:
            case BaseCalendar.OCTOBER /*10*/:
                m3 = null;
                break;
            case BaseCalendar.NOVEMBER /*11*/:
            case BaseCalendar.DECEMBER /*12*/:
            case Calendar.SECOND /*13*/:
                m3 = null;
                break;
            case ZipConstants.LOCCRC /*14*/:
                if (JsseJce.getRSAKeyLength(this.certs[0].getPublicKey()) <= Modifier.INTERFACE) {
                    m3 = null;
                    break;
                }
                try {
                    m3 = new RSA_ServerKeyExchange(this.tempPublicKey, this.privateKey, this.clnt_random, this.svr_random, this.sslContext.getSecureRandom());
                    this.privateKey = this.tempPrivateKey;
                    break;
                } catch (GeneralSecurityException e322) {
                    Handshaker.throwSSLException("Error generating RSA server key exchange", e322);
                    m3 = null;
                    break;
                }
            default:
                throw new RuntimeException("internal error: " + this.keyExchange);
        }
        if (m3 != null) {
            if (debug != null && Debug.isOn("handshake")) {
                m3.print(System.out);
            }
            m3.write(this.output);
        }
        if (this.doClientAuth != null && this.keyExchange != KeyExchange.K_DH_ANON && this.keyExchange != KeyExchange.K_ECDH_ANON && this.keyExchange != KeyExchange.K_KRB5 && this.keyExchange != KeyExchange.K_KRB5_EXPORT) {
            Collection<SignatureAndHashAlgorithm> collection = null;
            if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                collection = getLocalSupportedSignAlgs();
                if (collection.isEmpty()) {
                    throw new SSLHandshakeException("No supported signature algorithm");
                }
                Set<String> localHashAlgs = SignatureAndHashAlgorithm.getHashAlgorithmNames(collection);
                if (localHashAlgs.isEmpty()) {
                    throw new SSLHandshakeException("No supported signature algorithm");
                }
                this.handshakeHash.restrictCertificateVerifyAlgs(localHashAlgs);
            }
            CertificateRequest certificateRequest = new CertificateRequest(this.sslContext.getX509TrustManager().getAcceptedIssuers(), this.keyExchange, collection, this.protocolVersion);
            if (debug != null && Debug.isOn("handshake")) {
                certificateRequest.print(System.out);
            }
            certificateRequest.write(this.output);
        } else if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
            this.handshakeHash.setCertificateVerifyAlg(null);
        }
        ServerHelloDone m5 = new ServerHelloDone();
        if (debug != null && Debug.isOn("handshake")) {
            m5.print(System.out);
        }
        m5.write(this.output);
        this.output.flush();
    }

    private void chooseCipherSuite(ClientHello mesg) throws IOException {
        for (CipherSuite suite : mesg.getCipherSuites().collection()) {
            if (isNegotiable(suite) && ((this.doClientAuth != 2 || (suite.keyExchange != KeyExchange.K_DH_ANON && suite.keyExchange != KeyExchange.K_ECDH_ANON)) && trySetCipherSuite(suite))) {
                return;
            }
        }
        fatalSE((byte) 40, "no cipher suites in common");
    }

    boolean trySetCipherSuite(CipherSuite suite) {
        if (this.resumingSession) {
            return true;
        }
        if (!suite.isNegotiable() || this.protocolVersion.v >= suite.obsoleted || this.protocolVersion.v < suite.supported) {
            return false;
        }
        KeyExchange keyExchange = suite.keyExchange;
        this.privateKey = null;
        this.certs = null;
        this.dh = null;
        this.tempPrivateKey = null;
        this.tempPublicKey = null;
        Collection supportedSignAlgs = null;
        if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
            if (this.peerSupportedSignAlgs != null) {
                supportedSignAlgs = this.peerSupportedSignAlgs;
            } else {
                Object algorithm = null;
                switch (-getsun-security-ssl-CipherSuite$KeyExchangeSwitchesValues()[keyExchange.ordinal()]) {
                    case BaseCalendar.SUNDAY /*1*/:
                    case BaseCalendar.WEDNESDAY /*4*/:
                        algorithm = SignatureAndHashAlgorithm.valueOf(HashAlgorithm.SHA1.value, SignatureAlgorithm.DSA.value, 0);
                        break;
                    case BaseCalendar.MONDAY /*2*/:
                    case BaseCalendar.THURSDAY /*5*/:
                    case BaseCalendar.SATURDAY /*7*/:
                    case BaseCalendar.OCTOBER /*10*/:
                    case Calendar.SECOND /*13*/:
                        algorithm = SignatureAndHashAlgorithm.valueOf(HashAlgorithm.SHA1.value, SignatureAlgorithm.RSA.value, 0);
                        break;
                    case BaseCalendar.JUNE /*6*/:
                    case BaseCalendar.SEPTEMBER /*9*/:
                        algorithm = SignatureAndHashAlgorithm.valueOf(HashAlgorithm.SHA1.value, SignatureAlgorithm.ECDSA.value, 0);
                        break;
                }
                if (algorithm == null) {
                    supportedSignAlgs = Collections.emptySet();
                } else {
                    supportedSignAlgs = new ArrayList(1);
                    supportedSignAlgs.add(algorithm);
                }
                this.session.setPeerSupportedSignatureAlgorithms(supportedSignAlgs);
            }
        }
        switch (-getsun-security-ssl-CipherSuite$KeyExchangeSwitchesValues()[keyExchange.ordinal()]) {
            case BaseCalendar.SUNDAY /*1*/:
                if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    this.preferableSignatureAlgorithm = SignatureAndHashAlgorithm.getPreferableAlgorithm(supportedSignAlgs, "DSA");
                    if (this.preferableSignatureAlgorithm == null) {
                        return false;
                    }
                }
                if (setupPrivateKeyAndChain("DSA")) {
                    setupEphemeralDHKeys(suite.exportable);
                    break;
                }
                return false;
            case BaseCalendar.MONDAY /*2*/:
                if (setupPrivateKeyAndChain("RSA")) {
                    if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                        this.preferableSignatureAlgorithm = SignatureAndHashAlgorithm.getPreferableAlgorithm(supportedSignAlgs, "RSA", this.privateKey);
                        if (this.preferableSignatureAlgorithm == null) {
                            return false;
                        }
                    }
                    setupEphemeralDHKeys(suite.exportable);
                    break;
                }
                return false;
            case BaseCalendar.TUESDAY /*3*/:
                setupEphemeralDHKeys(suite.exportable);
                break;
            case BaseCalendar.JUNE /*6*/:
                if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    this.preferableSignatureAlgorithm = SignatureAndHashAlgorithm.getPreferableAlgorithm(supportedSignAlgs, "ECDSA");
                    if (this.preferableSignatureAlgorithm == null) {
                        return false;
                    }
                }
                if (!(setupPrivateKeyAndChain("EC_EC") && setupEphemeralECDHKeys())) {
                    return false;
                }
            case BaseCalendar.SATURDAY /*7*/:
                if (!setupPrivateKeyAndChain("RSA")) {
                    return false;
                }
                if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    this.preferableSignatureAlgorithm = SignatureAndHashAlgorithm.getPreferableAlgorithm(supportedSignAlgs, "RSA", this.privateKey);
                    if (this.preferableSignatureAlgorithm == null) {
                        return false;
                    }
                }
                if (!setupEphemeralECDHKeys()) {
                    return false;
                }
                break;
            case BaseCalendar.AUGUST /*8*/:
                if (!setupEphemeralECDHKeys()) {
                    return false;
                }
                break;
            case BaseCalendar.SEPTEMBER /*9*/:
                if (setupPrivateKeyAndChain("EC_EC")) {
                    setupStaticECDHKeys();
                    break;
                }
                return false;
            case BaseCalendar.OCTOBER /*10*/:
                if (setupPrivateKeyAndChain("EC_RSA")) {
                    setupStaticECDHKeys();
                    break;
                }
                return false;
            case BaseCalendar.NOVEMBER /*11*/:
            case BaseCalendar.DECEMBER /*12*/:
                if (!setupKerberosKeys()) {
                    return false;
                }
                break;
            case Calendar.SECOND /*13*/:
                if (!setupPrivateKeyAndChain("RSA")) {
                    return false;
                }
                break;
            case ZipConstants.LOCCRC /*14*/:
                if (!setupPrivateKeyAndChain("RSA")) {
                    return false;
                }
                try {
                    if (JsseJce.getRSAKeyLength(this.certs[0].getPublicKey()) > Modifier.INTERFACE && !setupEphemeralRSAKeys(suite.exportable)) {
                        return false;
                    }
                } catch (RuntimeException e) {
                    return false;
                }
            default:
                throw new RuntimeException("Unrecognized cipherSuite: " + suite);
        }
        setCipherSuite(suite);
        if (this.protocolVersion.v >= ProtocolVersion.TLS12.v && this.peerSupportedSignAlgs == null) {
            setPeerSupportedSignAlgs(supportedSignAlgs);
        }
        return true;
    }

    private boolean setupEphemeralRSAKeys(boolean export) {
        KeyPair kp = this.sslContext.getEphemeralKeyManager().getRSAKeyPair(export, this.sslContext.getSecureRandom());
        if (kp == null) {
            return false;
        }
        this.tempPublicKey = kp.getPublic();
        this.tempPrivateKey = kp.getPrivate();
        return true;
    }

    private void setupEphemeralDHKeys(boolean export) {
        int i;
        if (export) {
            i = Modifier.INTERFACE;
        } else {
            i = 768;
        }
        this.dh = new DHCrypt(i, this.sslContext.getSecureRandom());
    }

    private boolean setupEphemeralECDHKeys() {
        int index = -1;
        if (this.supportedCurves != null) {
            for (int curveId : this.supportedCurves.curveIds()) {
                if (SupportedEllipticCurvesExtension.isSupported(curveId)) {
                    index = curveId;
                    break;
                }
            }
            if (index < 0) {
                return false;
            }
        }
        index = SupportedEllipticCurvesExtension.DEFAULT.curveIds()[0];
        this.ecdh = new ECDHCrypt(SupportedEllipticCurvesExtension.getCurveOid(index), this.sslContext.getSecureRandom());
        return true;
    }

    private void setupStaticECDHKeys() {
        this.ecdh = new ECDHCrypt(this.privateKey, this.certs[0].getPublicKey());
    }

    private boolean setupPrivateKeyAndChain(String algorithm) {
        String alias;
        X509ExtendedKeyManager km = this.sslContext.getX509KeyManager();
        if (this.conn != null) {
            alias = km.chooseServerAlias(algorithm, null, this.conn);
        } else {
            alias = km.chooseEngineServerAlias(algorithm, null, this.engine);
        }
        if (alias == null) {
            return false;
        }
        PrivateKey tempPrivateKey = km.getPrivateKey(alias);
        if (tempPrivateKey == null) {
            return false;
        }
        X509Certificate[] tempCerts = km.getCertificateChain(alias);
        if (tempCerts == null || tempCerts.length == 0) {
            return false;
        }
        String keyAlgorithm = algorithm.split(BaseLocale.SEP)[0];
        PublicKey publicKey = tempCerts[0].getPublicKey();
        if (!tempPrivateKey.getAlgorithm().equals(keyAlgorithm) || !publicKey.getAlgorithm().equals(keyAlgorithm)) {
            return false;
        }
        if (keyAlgorithm.equals("EC")) {
            if (!(publicKey instanceof ECPublicKey)) {
                return false;
            }
            int index = SupportedEllipticCurvesExtension.getCurveIndex(((ECPublicKey) publicKey).getParams());
            if (!SupportedEllipticCurvesExtension.isSupported(index)) {
                return false;
            }
            if (!(this.supportedCurves == null || this.supportedCurves.contains(index))) {
                return false;
            }
        }
        this.privateKey = tempPrivateKey;
        this.certs = tempCerts;
        return true;
    }

    private boolean setupKerberosKeys() {
        if (this.kerberosKeys != null) {
            return true;
        }
        try {
            boolean z;
            AccessControlContext acc = getAccSE();
            this.kerberosKeys = (SecretKey[]) AccessController.doPrivileged(new AnonymousClass2(acc));
            if (this.kerberosKeys != null && this.kerberosKeys.length > 0) {
                if (debug != null && Debug.isOn("handshake")) {
                    for (Object k : this.kerberosKeys) {
                        System.out.println("Using Kerberos key: " + k);
                    }
                }
                String serverPrincipal = Krb5Helper.getServerPrincipalName(this.kerberosKeys[0]);
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    try {
                        sm.checkPermission(Krb5Helper.getServicePermission(serverPrincipal, SecurityConstants.SOCKET_ACCEPT_ACTION), acc);
                    } catch (SecurityException e) {
                        this.kerberosKeys = null;
                        if (debug != null && Debug.isOn("handshake")) {
                            System.out.println("Permission to access Kerberos secret key denied");
                        }
                        return false;
                    }
                }
            }
            if (this.kerberosKeys == null || this.kerberosKeys.length <= 0) {
                z = false;
            } else {
                z = true;
            }
            return z;
        } catch (PrivilegedActionException e2) {
            if (debug != null && Debug.isOn("handshake")) {
                System.out.println("Attempt to obtain Kerberos key failed: " + e2.toString());
            }
            return false;
        }
    }

    private SecretKey clientKeyExchange(KerberosClientKeyExchange mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        this.session.setPeerPrincipal(mesg.getPeerPrincipal());
        this.session.setLocalPrincipal(mesg.getLocalPrincipal());
        return new SecretKeySpec(mesg.getUnencryptedPreMasterSecret(), "TlsPremasterSecret");
    }

    private SecretKey clientKeyExchange(DHClientKeyExchange mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        return this.dh.getAgreedSecret(mesg.getClientPublicKey(), false);
    }

    private SecretKey clientKeyExchange(ECDHClientKeyExchange mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        return this.ecdh.getAgreedSecret(mesg.getEncodedPoint());
    }

    private void clientCertificateVerify(CertificateVerify mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
            SignatureAndHashAlgorithm signAlg = mesg.getPreferableSignatureAlgorithm();
            if (signAlg == null) {
                throw new SSLHandshakeException("Illegal CertificateVerify message");
            }
            String hashAlg = SignatureAndHashAlgorithm.getHashAlgorithmName(signAlg);
            if (hashAlg == null || hashAlg.length() == 0) {
                throw new SSLHandshakeException("No supported hash algorithm");
            }
            this.handshakeHash.setCertificateVerifyAlg(hashAlg);
        }
        try {
            if (!mesg.verify(this.protocolVersion, this.handshakeHash, this.session.getPeerCertificates()[0].getPublicKey(), this.session.getMasterSecret())) {
                fatalSE((byte) 42, "certificate verify message signature error");
            }
        } catch (GeneralSecurityException e) {
            fatalSE((byte) 42, "certificate verify format error", e);
        }
        this.needClientVerify = false;
    }

    private void clientFinished(Finished mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (this.doClientAuth == 2) {
            this.session.getPeerPrincipal();
        }
        if (this.needClientVerify) {
            fatalSE((byte) 40, "client did not send certificate verify message");
        }
        if (!mesg.verify(this.handshakeHash, 1, this.session.getMasterSecret())) {
            fatalSE((byte) 40, "client 'finished' message doesn't verify");
        }
        if (this.secureRenegotiation) {
            this.clientVerifyData = mesg.getVerifyData();
        }
        if (!this.resumingSession) {
            this.input.digestNow();
            sendChangeCipherAndFinish(true);
        }
        this.session.setLastAccessedTime(System.currentTimeMillis());
        if (!this.resumingSession && this.session.isRejoinable()) {
            ((SSLSessionContextImpl) this.sslContext.engineGetServerSessionContext()).put(this.session);
            if (debug != null && Debug.isOn("session")) {
                System.out.println("%% Cached server session: " + this.session);
            }
        } else if (!this.resumingSession && debug != null && Debug.isOn("session")) {
            System.out.println("%% Didn't cache non-resumable server session: " + this.session);
        }
    }

    private void sendChangeCipherAndFinish(boolean finishedTag) throws IOException {
        this.output.flush();
        Finished mesg = new Finished(this.protocolVersion, this.handshakeHash, 2, this.session.getMasterSecret(), this.cipherSuite);
        sendChangeCipherSpec(mesg, finishedTag);
        if (this.secureRenegotiation) {
            this.serverVerifyData = mesg.getVerifyData();
        }
        if (finishedTag) {
            this.state = 20;
        }
    }

    HandshakeMessage getKickstartMessage() {
        return new HelloRequest();
    }

    void handshakeAlert(byte description) throws SSLProtocolException {
        String message = Alerts.alertDescription(description);
        if (debug != null && Debug.isOn("handshake")) {
            System.out.println("SSL -- handshake alert:  " + message);
        }
        if (description != 41 || this.doClientAuth != 1) {
            throw new SSLProtocolException("handshake alert: " + message);
        }
    }

    private SecretKey clientKeyExchange(RSAClientKeyExchange mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        return mesg.preMaster;
    }

    private void clientCertificate(CertificateMsg mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        X509Certificate[] peerCerts = mesg.getCertificateChain();
        if (peerCerts.length == 0) {
            if (this.doClientAuth == (byte) 1) {
                if (this.protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    this.handshakeHash.setCertificateVerifyAlg(null);
                }
                return;
            }
            fatalSE((byte) 42, "null cert chain");
        }
        X509TrustManager tm = this.sslContext.getX509TrustManager();
        try {
            String authType;
            String keyAlgorithm = peerCerts[0].getPublicKey().getAlgorithm();
            if (keyAlgorithm.equals("RSA")) {
                authType = "RSA";
            } else if (keyAlgorithm.equals("DSA")) {
                authType = "DSA";
            } else if (keyAlgorithm.equals("EC")) {
                authType = "EC";
            } else {
                authType = "UNKNOWN";
            }
            if (tm instanceof X509ExtendedTrustManager) {
                if (this.conn != null) {
                    ((X509ExtendedTrustManager) tm).checkClientTrusted((X509Certificate[]) peerCerts.clone(), authType, this.conn);
                } else {
                    ((X509ExtendedTrustManager) tm).checkClientTrusted((X509Certificate[]) peerCerts.clone(), authType, this.engine);
                }
                this.needClientVerify = true;
                this.session.setPeerCertificates(peerCerts);
                return;
            }
            throw new CertificateException("Improper X509TrustManager implementation");
        } catch (CertificateException e) {
            fatalSE((byte) 46, (Throwable) e);
        }
    }
}
