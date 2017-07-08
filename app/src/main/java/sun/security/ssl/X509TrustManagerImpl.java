package sun.security.ssl;

import java.net.Socket;
import java.security.AlgorithmConstraints;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import sun.security.util.HostnameChecker;
import sun.security.validator.KeyStores;
import sun.security.validator.Validator;

final class X509TrustManagerImpl extends X509ExtendedTrustManager implements X509TrustManager {
    private static final Debug debug = null;
    private volatile Validator clientValidator;
    private final PKIXBuilderParameters pkixParams;
    private volatile Validator serverValidator;
    private final Collection<X509Certificate> trustedCerts;
    private final String validatorType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.X509TrustManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.X509TrustManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.X509TrustManagerImpl.<clinit>():void");
    }

    X509TrustManagerImpl(String validatorType, KeyStore ks) throws KeyStoreException {
        this.validatorType = validatorType;
        this.pkixParams = null;
        if (ks == null) {
            this.trustedCerts = Collections.emptySet();
        } else {
            this.trustedCerts = KeyStores.getTrustedCerts(ks);
        }
        showTrustedCerts();
    }

    X509TrustManagerImpl(String validatorType, PKIXBuilderParameters params) {
        this.validatorType = validatorType;
        this.pkixParams = params;
        Validator v = getValidator(Validator.VAR_TLS_SERVER);
        this.trustedCerts = v.getTrustedCertificates();
        this.serverValidator = v;
        showTrustedCerts();
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkTrusted(chain, authType, (Socket) null, true);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkTrusted(chain, authType, (Socket) null, false);
    }

    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] certsArray = new X509Certificate[this.trustedCerts.size()];
        this.trustedCerts.toArray(certsArray);
        return certsArray;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        checkTrusted(chain, authType, socket, true);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        checkTrusted(chain, authType, socket, false);
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        checkTrusted(chain, authType, engine, true);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        checkTrusted(chain, authType, engine, false);
    }

    private Validator checkTrustedInit(X509Certificate[] chain, String authType, boolean isClient) {
        if (chain == null || chain.length == 0) {
            throw new IllegalArgumentException("null or zero-length certificate chain");
        } else if (authType == null || authType.length() == 0) {
            throw new IllegalArgumentException("null or zero-length authentication type");
        } else {
            Validator v;
            if (isClient) {
                v = this.clientValidator;
                if (v == null) {
                    synchronized (this) {
                        v = this.clientValidator;
                        if (v == null) {
                            v = getValidator(Validator.VAR_TLS_CLIENT);
                            this.clientValidator = v;
                        }
                    }
                }
                return v;
            }
            v = this.serverValidator;
            if (v == null) {
                synchronized (this) {
                    v = this.serverValidator;
                    if (v == null) {
                        v = getValidator(Validator.VAR_TLS_SERVER);
                        this.serverValidator = v;
                    }
                }
            }
            return v;
            return v;
        }
    }

    private void checkTrusted(X509Certificate[] chain, String authType, Socket socket, boolean isClient) throws CertificateException {
        X509Certificate[] trustedChain;
        Validator v = checkTrustedInit(chain, authType, isClient);
        AlgorithmConstraints constraints = null;
        if (socket != null && socket.isConnected() && (socket instanceof SSLSocket)) {
            SSLSocket sslSocket = (SSLSocket) socket;
            SSLSession session = sslSocket.getHandshakeSession();
            if (session == null) {
                throw new CertificateException("No handshake session");
            }
            String identityAlg = sslSocket.getSSLParameters().getEndpointIdentificationAlgorithm();
            if (!(identityAlg == null || identityAlg.length() == 0)) {
                checkIdentity(session.getPeerHost(), chain[0], identityAlg);
            }
            constraints = ProtocolVersion.valueOf(session.getProtocol()).v >= ProtocolVersion.TLS12.v ? session instanceof ExtendedSSLSession ? new SSLAlgorithmConstraints(sslSocket, ((ExtendedSSLSession) session).getLocalSupportedSignatureAlgorithms(), false) : new SSLAlgorithmConstraints(sslSocket, false) : new SSLAlgorithmConstraints(sslSocket, false);
        }
        if (isClient) {
            trustedChain = validate(v, chain, constraints, null);
        } else {
            trustedChain = validate(v, chain, constraints, authType);
        }
        if (debug != null && Debug.isOn("trustmanager")) {
            System.out.println("Found trusted certificate:");
            System.out.println(trustedChain[trustedChain.length - 1]);
        }
    }

    private void checkTrusted(X509Certificate[] chain, String authType, SSLEngine engine, boolean isClient) throws CertificateException {
        X509Certificate[] trustedChain;
        Validator v = checkTrustedInit(chain, authType, isClient);
        AlgorithmConstraints constraints = null;
        if (engine != null) {
            SSLSession session = engine.getHandshakeSession();
            if (session == null) {
                throw new CertificateException("No handshake session");
            }
            String identityAlg = engine.getSSLParameters().getEndpointIdentificationAlgorithm();
            if (!(identityAlg == null || identityAlg.length() == 0)) {
                checkIdentity(session.getPeerHost(), chain[0], identityAlg);
            }
            if (ProtocolVersion.valueOf(session.getProtocol()).v < ProtocolVersion.TLS12.v) {
                constraints = new SSLAlgorithmConstraints(engine, false);
            } else if (session instanceof ExtendedSSLSession) {
                constraints = new SSLAlgorithmConstraints(engine, ((ExtendedSSLSession) session).getLocalSupportedSignatureAlgorithms(), false);
            } else {
                constraints = new SSLAlgorithmConstraints(engine, false);
            }
        }
        if (isClient) {
            trustedChain = validate(v, chain, constraints, null);
        } else {
            trustedChain = validate(v, chain, constraints, authType);
        }
        if (debug != null && Debug.isOn("trustmanager")) {
            System.out.println("Found trusted certificate:");
            System.out.println(trustedChain[trustedChain.length - 1]);
        }
    }

    private void showTrustedCerts() {
        if (debug != null && Debug.isOn("trustmanager")) {
            for (X509Certificate cert : this.trustedCerts) {
                System.out.println("adding as trusted cert:");
                System.out.println("  Subject: " + cert.getSubjectX500Principal());
                System.out.println("  Issuer:  " + cert.getIssuerX500Principal());
                System.out.println("  Algorithm: " + cert.getPublicKey().getAlgorithm() + "; Serial number: 0x" + cert.getSerialNumber().toString(16));
                System.out.println("  Valid from " + cert.getNotBefore() + " until " + cert.getNotAfter());
                System.out.println();
            }
        }
    }

    private Validator getValidator(String variant) {
        if (this.pkixParams == null) {
            return Validator.getInstance(this.validatorType, variant, this.trustedCerts);
        }
        return Validator.getInstance(this.validatorType, variant, this.pkixParams);
    }

    private static X509Certificate[] validate(Validator v, X509Certificate[] chain, AlgorithmConstraints constraints, String authType) throws CertificateException {
        Object o = JsseJce.beginFipsProvider();
        try {
            X509Certificate[] validate = v.validate(chain, null, constraints, authType);
            return validate;
        } finally {
            JsseJce.endFipsProvider(o);
        }
    }

    static void checkIdentity(String hostname, X509Certificate cert, String algorithm) throws CertificateException {
        if (algorithm != null && algorithm.length() != 0) {
            if (hostname != null && hostname.startsWith("[") && hostname.endsWith("]")) {
                hostname = hostname.substring(1, hostname.length() - 1);
            }
            if (algorithm.equalsIgnoreCase("HTTPS")) {
                HostnameChecker.getInstance((byte) 1).match(hostname, cert);
            } else if (algorithm.equalsIgnoreCase("LDAP") || algorithm.equalsIgnoreCase("LDAPS")) {
                HostnameChecker.getInstance((byte) 2).match(hostname, cert);
            } else {
                throw new CertificateException("Unknown identification algorithm: " + algorithm);
            }
        }
    }
}
