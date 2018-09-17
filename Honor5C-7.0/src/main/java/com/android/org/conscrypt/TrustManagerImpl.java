package com.android.org.conscrypt;

import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;

public final class TrustManagerImpl extends X509ExtendedTrustManager {
    private static final TrustAnchorComparator TRUST_ANCHOR_COMPARATOR = null;
    private final X509Certificate[] acceptedIssuers;
    private final CertBlacklist blacklist;
    private final Exception err;
    private final CertificateFactory factory;
    private final TrustedCertificateIndex intermediateIndex;
    private CertPinManager pinManager;
    private final KeyStore rootKeyStore;
    private final TrustedCertificateIndex trustedCertificateIndex;
    private final TrustedCertificateStore trustedCertificateStore;
    private final CertPathValidator validator;

    private static class ExtendedKeyUsagePKIXCertPathChecker extends PKIXCertPathChecker {
        private static final String EKU_OID = "2.5.29.37";
        private static final String EKU_anyExtendedKeyUsage = "2.5.29.37.0";
        private static final String EKU_clientAuth = "1.3.6.1.5.5.7.3.2";
        private static final String EKU_msSGC = "1.3.6.1.4.1.311.10.3.3";
        private static final String EKU_nsSGC = "2.16.840.1.113730.4.1";
        private static final String EKU_serverAuth = "1.3.6.1.5.5.7.3.1";
        private static final Set<String> SUPPORTED_EXTENSIONS = null;
        private final boolean clientAuth;
        private final X509Certificate leaf;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.TrustManagerImpl.ExtendedKeyUsagePKIXCertPathChecker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.TrustManagerImpl.ExtendedKeyUsagePKIXCertPathChecker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.TrustManagerImpl.ExtendedKeyUsagePKIXCertPathChecker.<clinit>():void");
        }

        private ExtendedKeyUsagePKIXCertPathChecker(boolean clientAuth, X509Certificate leaf) {
            this.clientAuth = clientAuth;
            this.leaf = leaf;
        }

        public void init(boolean forward) throws CertPathValidatorException {
        }

        public boolean isForwardCheckingSupported() {
            return true;
        }

        public Set<String> getSupportedExtensions() {
            return SUPPORTED_EXTENSIONS;
        }

        public void check(Certificate c, Collection<String> unresolvedCritExts) throws CertPathValidatorException {
            if (c == this.leaf) {
                try {
                    List<String> ekuOids = this.leaf.getExtendedKeyUsage();
                    if (ekuOids != null) {
                        boolean goodExtendedKeyUsage = false;
                        for (String ekuOid : ekuOids) {
                            if (ekuOid.equals(EKU_anyExtendedKeyUsage)) {
                                goodExtendedKeyUsage = true;
                                break;
                            } else if (this.clientAuth) {
                                if (ekuOid.equals(EKU_clientAuth)) {
                                    goodExtendedKeyUsage = true;
                                    break;
                                }
                            } else if (ekuOid.equals(EKU_serverAuth)) {
                                goodExtendedKeyUsage = true;
                                break;
                            } else if (ekuOid.equals(EKU_nsSGC)) {
                                goodExtendedKeyUsage = true;
                                break;
                            } else if (ekuOid.equals(EKU_msSGC)) {
                                goodExtendedKeyUsage = true;
                                break;
                            }
                        }
                        if (goodExtendedKeyUsage) {
                            unresolvedCritExts.remove(EKU_OID);
                            return;
                        }
                        throw new CertPathValidatorException("End-entity certificate does not have a valid extendedKeyUsage.");
                    }
                } catch (CertificateParsingException e) {
                    throw new CertPathValidatorException(e);
                }
            }
        }
    }

    private static class TrustAnchorComparator implements Comparator<TrustAnchor> {
        private static final CertificatePriorityComparator CERT_COMPARATOR = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.TrustManagerImpl.TrustAnchorComparator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.TrustManagerImpl.TrustAnchorComparator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.TrustManagerImpl.TrustAnchorComparator.<clinit>():void");
        }

        /* synthetic */ TrustAnchorComparator(TrustAnchorComparator trustAnchorComparator) {
            this();
        }

        private TrustAnchorComparator() {
        }

        public /* bridge */ /* synthetic */ int compare(Object lhs, Object rhs) {
            return compare((TrustAnchor) lhs, (TrustAnchor) rhs);
        }

        public int compare(TrustAnchor lhs, TrustAnchor rhs) {
            return CERT_COMPARATOR.compare(lhs.getTrustedCert(), rhs.getTrustedCert());
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.TrustManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.TrustManagerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.TrustManagerImpl.<clinit>():void");
    }

    public TrustManagerImpl(KeyStore keyStore) {
        this(keyStore, null);
    }

    public TrustManagerImpl(KeyStore keyStore, CertPinManager manager) {
        this(keyStore, manager, null);
    }

    public TrustManagerImpl(KeyStore keyStore, CertPinManager manager, TrustedCertificateStore certStore) {
        this(keyStore, manager, certStore, null);
    }

    public TrustManagerImpl(KeyStore keyStore, CertPinManager manager, TrustedCertificateStore certStore, CertBlacklist blacklist) {
        CertPathValidator certPathValidator = null;
        CertificateFactory certificateFactory = null;
        KeyStore keyStore2 = null;
        TrustedCertificateStore trustedCertificateStore = null;
        TrustedCertificateIndex trustedCertificateIndexLocal = null;
        X509Certificate[] acceptedIssuersLocal = null;
        Exception errLocal = null;
        try {
            certPathValidator = CertPathValidator.getInstance("PKIX");
            certificateFactory = CertificateFactory.getInstance("X509");
            if ("AndroidCAStore".equals(keyStore.getType())) {
                keyStore2 = keyStore;
                trustedCertificateStore = certStore != null ? certStore : new TrustedCertificateStore();
                acceptedIssuersLocal = null;
                trustedCertificateIndexLocal = new TrustedCertificateIndex();
            } else {
                keyStore2 = null;
                trustedCertificateStore = certStore;
                acceptedIssuersLocal = acceptedIssuers(keyStore);
                trustedCertificateIndexLocal = new TrustedCertificateIndex(trustAnchors(acceptedIssuersLocal));
            }
        } catch (Exception e) {
            errLocal = e;
        }
        if (manager != null) {
            this.pinManager = manager;
        } else {
            try {
                this.pinManager = new CertPinManager(trustedCertificateStore);
            } catch (PinManagerException e2) {
                throw new SecurityException("Could not initialize CertPinManager", e2);
            }
        }
        if (blacklist == null) {
            blacklist = new CertBlacklist();
        }
        this.rootKeyStore = keyStore2;
        this.trustedCertificateStore = trustedCertificateStore;
        this.validator = certPathValidator;
        this.factory = certificateFactory;
        this.trustedCertificateIndex = trustedCertificateIndexLocal;
        this.intermediateIndex = new TrustedCertificateIndex();
        this.acceptedIssuers = acceptedIssuersLocal;
        this.err = errLocal;
        this.blacklist = blacklist;
    }

    private static X509Certificate[] acceptedIssuers(KeyStore ks) {
        try {
            List<X509Certificate> trusted = new ArrayList();
            Enumeration<String> en = ks.aliases();
            while (en.hasMoreElements()) {
                X509Certificate cert = (X509Certificate) ks.getCertificate((String) en.nextElement());
                if (cert != null) {
                    trusted.add(cert);
                }
            }
            return (X509Certificate[]) trusted.toArray(new X509Certificate[trusted.size()]);
        } catch (KeyStoreException e) {
            return new X509Certificate[0];
        }
    }

    private static Set<TrustAnchor> trustAnchors(X509Certificate[] certs) {
        Set<TrustAnchor> trustAnchors = new HashSet(certs.length);
        for (X509Certificate cert : certs) {
            trustAnchors.add(new TrustAnchor(cert, null));
        }
        return trustAnchors;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkTrusted(chain, authType, null, null, true);
    }

    public List<X509Certificate> checkClientTrusted(X509Certificate[] chain, String authType, String hostname) throws CertificateException {
        return checkTrusted(chain, authType, hostname, true);
    }

    private static SSLSession getHandshakeSessionOrThrow(SSLSocket sslSocket) throws CertificateException {
        SSLSession session = sslSocket.getHandshakeSession();
        if (session != null) {
            return session;
        }
        throw new CertificateException("Not in handshake; no session available");
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        SSLSession sSLSession = null;
        SSLParameters parameters = null;
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            sSLSession = getHandshakeSessionOrThrow(sslSocket);
            parameters = sslSocket.getSSLParameters();
        }
        checkTrusted(chain, authType, sSLSession, parameters, true);
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        SSLSession session = engine.getHandshakeSession();
        if (session == null) {
            throw new CertificateException("Not in handshake; no session available");
        }
        checkTrusted(chain, authType, session, engine.getSSLParameters(), true);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkTrusted(chain, authType, null, null, false);
    }

    public List<X509Certificate> checkServerTrusted(X509Certificate[] chain, String authType, String hostname) throws CertificateException {
        return checkTrusted(chain, authType, hostname, false);
    }

    public List<X509Certificate> getTrustedChainForServer(X509Certificate[] certs, String authType, Socket socket) throws CertificateException {
        SSLSession sSLSession = null;
        SSLParameters parameters = null;
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            sSLSession = getHandshakeSessionOrThrow(sslSocket);
            parameters = sslSocket.getSSLParameters();
        }
        return checkTrusted(certs, authType, sSLSession, parameters, false);
    }

    public List<X509Certificate> getTrustedChainForServer(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException {
        SSLSession session = engine.getHandshakeSession();
        if (session == null) {
            throw new CertificateException("Not in handshake; no session available");
        }
        return checkTrusted(certs, authType, session, engine.getSSLParameters(), false);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        getTrustedChainForServer(chain, authType, socket);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        getTrustedChainForServer(chain, authType, engine);
    }

    public boolean isUserAddedCertificate(X509Certificate cert) {
        if (this.trustedCertificateStore == null) {
            return false;
        }
        return this.trustedCertificateStore.isUserAddedCertificate(cert);
    }

    public List<X509Certificate> checkServerTrusted(X509Certificate[] chain, String authType, SSLSession session) throws CertificateException {
        return checkTrusted(chain, authType, session, null, false);
    }

    public void handleTrustStorageUpdate() {
        if (this.acceptedIssuers == null) {
            this.trustedCertificateIndex.reset();
        } else {
            this.trustedCertificateIndex.reset(trustAnchors(this.acceptedIssuers));
        }
    }

    private List<X509Certificate> checkTrusted(X509Certificate[] certs, String authType, SSLSession session, SSLParameters parameters, boolean clientAuth) throws CertificateException {
        String hostname = session != null ? session.getPeerHost() : null;
        if (!(session == null || parameters == null)) {
            String identificationAlgorithm = parameters.getEndpointIdentificationAlgorithm();
            if (!(identificationAlgorithm == null || !"HTTPS".equals(identificationAlgorithm.toUpperCase(Locale.US)) || HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session))) {
                throw new CertificateException("No subjectAltNames on the certificate match");
            }
        }
        return checkTrusted(certs, authType, hostname, clientAuth);
    }

    private List<X509Certificate> checkTrusted(X509Certificate[] certs, String authType, String host, boolean clientAuth) throws CertificateException {
        if (certs == null || certs.length == 0 || authType == null || authType.length() == 0) {
            throw new IllegalArgumentException("null or zero-length parameter");
        } else if (this.err != null) {
            throw new CertificateException(this.err);
        } else {
            Set<X509Certificate> used = new HashSet();
            ArrayList<X509Certificate> untrustedChain = new ArrayList();
            ArrayList<TrustAnchor> trustedChain = new ArrayList();
            X509Certificate leaf = certs[0];
            TrustAnchor leafAsAnchor = findTrustAnchorBySubjectAndPublicKey(leaf);
            if (leafAsAnchor != null) {
                trustedChain.add(leafAsAnchor);
                used.add(leafAsAnchor.getTrustedCert());
            } else {
                untrustedChain.add(leaf);
            }
            used.add(leaf);
            return checkTrustedRecursive(certs, host, clientAuth, untrustedChain, trustedChain, used);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<X509Certificate> checkTrustedRecursive(X509Certificate[] certs, String host, boolean clientAuth, ArrayList<X509Certificate> untrustedChain, ArrayList<TrustAnchor> trustAnchorChain, Set<X509Certificate> used) throws CertificateException {
        X509Certificate current;
        CertificateException lastException = null;
        if (trustAnchorChain.isEmpty()) {
            current = (X509Certificate) untrustedChain.get(untrustedChain.size() - 1);
        } else {
            current = ((TrustAnchor) trustAnchorChain.get(trustAnchorChain.size() - 1)).getTrustedCert();
        }
        checkBlacklist(current);
        if (current.getIssuerDN().equals(current.getSubjectDN())) {
            return verifyChain(untrustedChain, trustAnchorChain, host, clientAuth);
        }
        boolean seenIssuer = false;
        for (TrustAnchor anchor : sortPotentialAnchors(findAllTrustAnchorsByIssuerAndSignature(current))) {
            X509Certificate anchorCert = anchor.getTrustedCert();
            if (!used.contains(anchorCert)) {
                seenIssuer = true;
                used.add(anchorCert);
                trustAnchorChain.add(anchor);
                try {
                    return checkTrustedRecursive(certs, host, clientAuth, untrustedChain, trustAnchorChain, used);
                } catch (CertificateException ex) {
                    lastException = ex;
                    trustAnchorChain.remove(trustAnchorChain.size() - 1);
                    used.remove(anchorCert);
                }
            }
        }
        if (trustAnchorChain.isEmpty()) {
            int i = 1;
            while (true) {
                int length = certs.length;
                if (i >= r0) {
                    break;
                }
                X509Certificate candidateIssuer = certs[i];
                if (!used.contains(candidateIssuer) && current.getIssuerDN().equals(candidateIssuer.getSubjectDN())) {
                    try {
                        candidateIssuer.checkValidity();
                        ChainStrengthAnalyzer.checkCert(candidateIssuer);
                        used.add(candidateIssuer);
                        untrustedChain.add(candidateIssuer);
                        try {
                            break;
                        } catch (CertificateException ex2) {
                            lastException = ex2;
                            used.remove(candidateIssuer);
                            untrustedChain.remove(untrustedChain.size() - 1);
                        }
                    } catch (CertificateException ex22) {
                        CertificateException certificateException = new CertificateException("Unacceptable certificate: " + candidateIssuer.getSubjectX500Principal(), ex22);
                    }
                }
                i++;
            }
            for (TrustAnchor intermediate : sortPotentialAnchors(this.intermediateIndex.findAllByIssuerAndSignature(current))) {
                X509Certificate intermediateCert = intermediate.getTrustedCert();
                if (!used.contains(intermediateCert)) {
                    used.add(intermediateCert);
                    untrustedChain.add(intermediateCert);
                    try {
                        return checkTrustedRecursive(certs, host, clientAuth, untrustedChain, trustAnchorChain, used);
                    } catch (CertificateException ex222) {
                        lastException = ex222;
                        untrustedChain.remove(untrustedChain.size() - 1);
                        used.remove(intermediateCert);
                    }
                }
            }
            if (lastException != null) {
                throw lastException;
            }
            throw new CertificateException(new CertPathValidatorException("Trust anchor for certification path not found.", null, this.factory.generateCertPath(untrustedChain), -1));
        } else if (!seenIssuer) {
            return verifyChain(untrustedChain, trustAnchorChain, host, clientAuth);
        } else {
            throw lastException;
        }
    }

    private List<X509Certificate> verifyChain(List<X509Certificate> untrustedChain, List<TrustAnchor> trustAnchorChain, String host, boolean clientAuth) throws CertificateException {
        CertPath certPath = this.factory.generateCertPath(untrustedChain);
        if (trustAnchorChain.isEmpty()) {
            throw new CertificateException(new CertPathValidatorException("Trust anchor for certification path not found.", null, certPath, -1));
        }
        List<X509Certificate> wholeChain = new ArrayList();
        wholeChain.addAll(untrustedChain);
        for (TrustAnchor anchor : trustAnchorChain) {
            wholeChain.add(anchor.getTrustedCert());
        }
        if (host != null) {
            try {
                if (!this.pinManager.isChainValid(host, wholeChain)) {
                    throw new CertificateException("Pinning failure", new CertPathValidatorException("Certificate path is not properly pinned.", null, certPath, -1));
                }
            } catch (PinManagerException e) {
                throw new CertificateException("Failed to check pinning", e);
            }
        }
        for (X509Certificate cert : wholeChain) {
            checkBlacklist(cert);
        }
        if (untrustedChain.isEmpty()) {
            return wholeChain;
        }
        ChainStrengthAnalyzer.check((List) untrustedChain);
        try {
            Set<TrustAnchor> anchorSet = new HashSet();
            anchorSet.add((TrustAnchor) trustAnchorChain.get(0));
            PKIXParameters params = new PKIXParameters(anchorSet);
            params.setRevocationEnabled(false);
            params.addCertPathChecker(new ExtendedKeyUsagePKIXCertPathChecker((X509Certificate) untrustedChain.get(0), null));
            this.validator.validate(certPath, params);
            for (int i = 1; i < untrustedChain.size(); i++) {
                this.intermediateIndex.index((X509Certificate) untrustedChain.get(i));
            }
            return wholeChain;
        } catch (InvalidAlgorithmParameterException e2) {
            throw new CertificateException("Chain validation failed", e2);
        } catch (CertPathValidatorException e3) {
            throw new CertificateException("Chain validation failed", e3);
        }
    }

    private void checkBlacklist(X509Certificate cert) throws CertificateException {
        if (this.blacklist.isPublicKeyBlackListed(cert.getPublicKey())) {
            throw new CertificateException("Certificate blacklisted by public key: " + cert);
        }
    }

    private static Collection<TrustAnchor> sortPotentialAnchors(Set<TrustAnchor> anchors) {
        if (anchors.size() <= 1) {
            return anchors;
        }
        List<TrustAnchor> sortedAnchors = new ArrayList(anchors);
        Collections.sort(sortedAnchors, TRUST_ANCHOR_COMPARATOR);
        return sortedAnchors;
    }

    private Set<TrustAnchor> findAllTrustAnchorsByIssuerAndSignature(X509Certificate cert) {
        Set<TrustAnchor> indexedAnchors = this.trustedCertificateIndex.findAllByIssuerAndSignature(cert);
        if (!indexedAnchors.isEmpty() || this.trustedCertificateStore == null) {
            return indexedAnchors;
        }
        Set<X509Certificate> storeAnchors = this.trustedCertificateStore.findAllIssuers(cert);
        if (storeAnchors.isEmpty()) {
            return indexedAnchors;
        }
        Set<TrustAnchor> result = new HashSet(storeAnchors.size());
        for (X509Certificate storeCert : storeAnchors) {
            result.add(this.trustedCertificateIndex.index(storeCert));
        }
        return result;
    }

    private TrustAnchor findTrustAnchorBySubjectAndPublicKey(X509Certificate cert) {
        TrustAnchor trustAnchor = this.trustedCertificateIndex.findBySubjectAndPublicKey(cert);
        if (trustAnchor != null) {
            return trustAnchor;
        }
        if (this.trustedCertificateStore == null) {
            return null;
        }
        X509Certificate systemCert = this.trustedCertificateStore.getTrustAnchor(cert);
        if (systemCert != null) {
            return new TrustAnchor(systemCert, null);
        }
        return null;
    }

    public X509Certificate[] getAcceptedIssuers() {
        return this.acceptedIssuers != null ? (X509Certificate[]) this.acceptedIssuers.clone() : acceptedIssuers(this.rootKeyStore);
    }
}
