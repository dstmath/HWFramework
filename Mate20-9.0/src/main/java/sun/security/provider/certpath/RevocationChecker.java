package sun.security.provider.certpath;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CRLReason;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.Extension;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.certpath.OCSP;
import sun.security.provider.certpath.PKIX;
import sun.security.util.Debug;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.CRLDistributionPointsExtension;
import sun.security.x509.DistributionPoint;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.GeneralNames;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X500Name;
import sun.security.x509.X509CRLEntryImpl;
import sun.security.x509.X509CertImpl;

class RevocationChecker extends PKIXRevocationChecker {
    private static final boolean[] ALL_REASONS = {true, true, true, true, true, true, true, true, true};
    private static final boolean[] CRL_SIGN_USAGE = {false, false, false, false, false, false, true};
    private static final String HEX_DIGITS = "0123456789ABCDEFabcdef";
    private static final long MAX_CLOCK_SKEW = 900000;
    /* access modifiers changed from: private */
    public static final Debug debug = Debug.getInstance("certpath");
    private TrustAnchor anchor;
    private int certIndex;
    private List<CertStore> certStores;
    private boolean crlDP;
    private boolean crlSignFlag;
    private X509Certificate issuerCert;
    private boolean legacy = false;
    private Mode mode = Mode.PREFER_OCSP;
    private List<Extension> ocspExtensions;
    private Map<X509Certificate, byte[]> ocspResponses;
    private boolean onlyEE;
    private PKIX.ValidatorParams params;
    private PublicKey prevPubKey;
    private X509Certificate responderCert;
    private URI responderURI;
    private boolean softFail;
    private LinkedList<CertPathValidatorException> softFailExceptions = new LinkedList<>();

    private enum Mode {
        PREFER_OCSP,
        PREFER_CRLS,
        ONLY_CRLS,
        ONLY_OCSP
    }

    private static class RejectKeySelector extends X509CertSelector {
        private final Set<PublicKey> badKeySet;

        RejectKeySelector(Set<PublicKey> badPublicKeys) {
            this.badKeySet = badPublicKeys;
        }

        public boolean match(Certificate cert) {
            if (!super.match(cert)) {
                return false;
            }
            if (this.badKeySet.contains(cert.getPublicKey())) {
                if (RevocationChecker.debug != null) {
                    RevocationChecker.debug.println("RejectKeySelector.match: bad key");
                }
                return false;
            }
            if (RevocationChecker.debug != null) {
                RevocationChecker.debug.println("RejectKeySelector.match: returning true");
            }
            return true;
        }

        public String toString() {
            return "RejectKeySelector: [\n" + super.toString() + this.badKeySet + "]";
        }
    }

    private static class RevocationProperties {
        boolean crlDPEnabled;
        boolean ocspEnabled;
        String ocspIssuer;
        String ocspSerial;
        String ocspSubject;
        String ocspUrl;
        boolean onlyEE;

        private RevocationProperties() {
        }
    }

    RevocationChecker() {
    }

    RevocationChecker(TrustAnchor anchor2, PKIX.ValidatorParams params2) throws CertPathValidatorException {
        init(anchor2, params2);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0038  */
    public void init(TrustAnchor anchor2, PKIX.ValidatorParams params2) throws CertPathValidatorException {
        X509Certificate x509Certificate;
        RevocationProperties rp = getRevocationProperties();
        URI uri = getOcspResponder();
        this.responderURI = uri == null ? toURI(rp.ocspUrl) : uri;
        X509Certificate cert = getOcspResponderCert();
        if (cert == null) {
            x509Certificate = getResponderCert(rp, params2.trustAnchors(), params2.certStores());
        } else {
            x509Certificate = cert;
        }
        this.responderCert = x509Certificate;
        Set<PKIXRevocationChecker.Option> options = getOptions();
        for (PKIXRevocationChecker.Option option : options) {
            switch (option) {
                case ONLY_END_ENTITY:
                case PREFER_CRLS:
                case SOFT_FAIL:
                case NO_FALLBACK:
                    while (r4.hasNext()) {
                    }
                    break;
            }
            throw new CertPathValidatorException("Unrecognized revocation parameter option: " + option);
        }
        this.softFail = options.contains(PKIXRevocationChecker.Option.SOFT_FAIL);
        if (this.legacy) {
            this.mode = rp.ocspEnabled ? Mode.PREFER_OCSP : Mode.ONLY_CRLS;
            this.onlyEE = rp.onlyEE;
        } else {
            if (options.contains(PKIXRevocationChecker.Option.NO_FALLBACK)) {
                if (options.contains(PKIXRevocationChecker.Option.PREFER_CRLS)) {
                    this.mode = Mode.ONLY_CRLS;
                } else {
                    this.mode = Mode.ONLY_OCSP;
                }
            } else if (options.contains(PKIXRevocationChecker.Option.PREFER_CRLS)) {
                this.mode = Mode.PREFER_CRLS;
            }
            this.onlyEE = options.contains(PKIXRevocationChecker.Option.ONLY_END_ENTITY);
        }
        if (this.legacy) {
            this.crlDP = rp.crlDPEnabled;
        } else {
            this.crlDP = true;
        }
        this.ocspResponses = getOcspResponses();
        this.ocspExtensions = getOcspExtensions();
        this.anchor = anchor2;
        this.params = params2;
        this.certStores = new ArrayList(params2.certStores());
        try {
            this.certStores.add(CertStore.getInstance("Collection", new CollectionCertStoreParameters(params2.certificates())));
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            if (debug != null) {
                Debug debug2 = debug;
                debug2.println("RevocationChecker: error creating Collection CertStore: " + e);
            }
        }
    }

    private static URI toURI(String uriString) throws CertPathValidatorException {
        if (uriString == null) {
            return null;
        }
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new CertPathValidatorException("cannot parse ocsp.responderURL property", e);
        }
    }

    private static RevocationProperties getRevocationProperties() {
        return (RevocationProperties) AccessController.doPrivileged(new PrivilegedAction<RevocationProperties>() {
            public RevocationProperties run() {
                RevocationProperties rp = new RevocationProperties();
                String onlyEE = Security.getProperty("com.sun.security.onlyCheckRevocationOfEECert");
                boolean z = false;
                rp.onlyEE = onlyEE != null && onlyEE.equalsIgnoreCase("true");
                String ocspEnabled = Security.getProperty("ocsp.enable");
                if (ocspEnabled != null && ocspEnabled.equalsIgnoreCase("true")) {
                    z = true;
                }
                rp.ocspEnabled = z;
                rp.ocspUrl = Security.getProperty("ocsp.responderURL");
                rp.ocspSubject = Security.getProperty("ocsp.responderCertSubjectName");
                rp.ocspIssuer = Security.getProperty("ocsp.responderCertIssuerName");
                rp.ocspSerial = Security.getProperty("ocsp.responderCertSerialNumber");
                rp.crlDPEnabled = Boolean.getBoolean("com.sun.security.enableCRLDP");
                return rp;
            }
        });
    }

    private static X509Certificate getResponderCert(RevocationProperties rp, Set<TrustAnchor> anchors, List<CertStore> stores) throws CertPathValidatorException {
        if (rp.ocspSubject != null) {
            return getResponderCert(rp.ocspSubject, anchors, stores);
        }
        if (rp.ocspIssuer != null && rp.ocspSerial != null) {
            return getResponderCert(rp.ocspIssuer, rp.ocspSerial, anchors, stores);
        }
        if (rp.ocspIssuer == null && rp.ocspSerial == null) {
            return null;
        }
        throw new CertPathValidatorException("Must specify both ocsp.responderCertIssuerName and ocsp.responderCertSerialNumber properties");
    }

    private static X509Certificate getResponderCert(String subject, Set<TrustAnchor> anchors, List<CertStore> stores) throws CertPathValidatorException {
        X509CertSelector sel = new X509CertSelector();
        try {
            sel.setSubject(new X500Principal(subject));
            return getResponderCert(sel, anchors, stores);
        } catch (IllegalArgumentException e) {
            throw new CertPathValidatorException("cannot parse ocsp.responderCertSubjectName property", e);
        }
    }

    private static X509Certificate getResponderCert(String issuer, String serial, Set<TrustAnchor> anchors, List<CertStore> stores) throws CertPathValidatorException {
        X509CertSelector sel = new X509CertSelector();
        try {
            sel.setIssuer(new X500Principal(issuer));
            try {
                sel.setSerialNumber(new BigInteger(stripOutSeparators(serial), 16));
                return getResponderCert(sel, anchors, stores);
            } catch (NumberFormatException e) {
                throw new CertPathValidatorException("cannot parse ocsp.responderCertSerialNumber property", e);
            }
        } catch (IllegalArgumentException e2) {
            throw new CertPathValidatorException("cannot parse ocsp.responderCertIssuerName property", e2);
        }
    }

    private static X509Certificate getResponderCert(X509CertSelector sel, Set<TrustAnchor> anchors, List<CertStore> stores) throws CertPathValidatorException {
        for (TrustAnchor anchor2 : anchors) {
            X509Certificate cert = anchor2.getTrustedCert();
            if (cert != null && sel.match(cert)) {
                return cert;
            }
        }
        for (CertStore store : stores) {
            try {
                Collection<? extends Certificate> certs = store.getCertificates(sel);
                if (!certs.isEmpty()) {
                    return (X509Certificate) certs.iterator().next();
                }
            } catch (CertStoreException e) {
                if (debug != null) {
                    Debug debug2 = debug;
                    debug2.println("CertStore exception:" + e);
                }
            }
        }
        throw new CertPathValidatorException("Cannot find the responder's certificate (set using the OCSP security properties).");
    }

    public void init(boolean forward) throws CertPathValidatorException {
        PublicKey publicKey;
        if (!forward) {
            if (this.anchor != null) {
                this.issuerCert = this.anchor.getTrustedCert();
                if (this.issuerCert != null) {
                    publicKey = this.issuerCert.getPublicKey();
                } else {
                    publicKey = this.anchor.getCAPublicKey();
                }
                this.prevPubKey = publicKey;
            }
            this.crlSignFlag = true;
            if (this.params == null || this.params.certPath() == null) {
                this.certIndex = -1;
            } else {
                this.certIndex = this.params.certPath().getCertificates().size() - 1;
            }
            this.softFailExceptions.clear();
            return;
        }
        throw new CertPathValidatorException("forward checking not supported");
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        return null;
    }

    public List<CertPathValidatorException> getSoftFailExceptions() {
        return Collections.unmodifiableList(this.softFailExceptions);
    }

    public void check(Certificate cert, Collection<String> unresolvedCritExts) throws CertPathValidatorException {
        check((X509Certificate) cert, unresolvedCritExts, this.prevPubKey, this.crlSignFlag);
    }

    private void check(X509Certificate xcert, Collection<String> unresolvedCritExts, PublicKey pubKey, boolean crlSignFlag2) throws CertPathValidatorException {
        boolean eSoftFail;
        CertPathValidatorException cause;
        if (debug != null) {
            Debug debug2 = debug;
            debug2.println("RevocationChecker.check: checking cert\n  SN: " + Debug.toHexString(xcert.getSerialNumber()) + "\n  Subject: " + xcert.getSubjectX500Principal() + "\n  Issuer: " + xcert.getIssuerX500Principal());
        }
        try {
            if (!this.onlyEE || xcert.getBasicConstraints() == -1) {
                switch (this.mode) {
                    case PREFER_OCSP:
                    case ONLY_OCSP:
                        checkOCSP(xcert, unresolvedCritExts);
                        break;
                    case PREFER_CRLS:
                    case ONLY_CRLS:
                        checkCRLs(xcert, unresolvedCritExts, null, pubKey, crlSignFlag2);
                        break;
                }
                updateState(xcert);
                return;
            }
            if (debug != null) {
                debug.println("Skipping revocation check; cert is not an end entity cert");
            }
            updateState(xcert);
        } catch (CertPathValidatorException x) {
            if (debug != null) {
                debug.println("RevocationChecker.check() failover failed");
                Debug debug3 = debug;
                debug3.println("RevocationChecker.check() " + x.getMessage());
            }
            if (x.getReason() == CertPathValidatorException.BasicReason.REVOKED) {
                throw x;
            } else if (!isSoftFailException(x)) {
                cause.addSuppressed(x);
                throw cause;
            } else if (!eSoftFail) {
                throw cause;
            }
        } catch (CertPathValidatorException e) {
            if (e.getReason() != CertPathValidatorException.BasicReason.REVOKED) {
                eSoftFail = isSoftFailException(e);
                if (eSoftFail) {
                    if (this.mode == Mode.ONLY_OCSP || this.mode == Mode.ONLY_CRLS) {
                        updateState(xcert);
                        return;
                    }
                } else if (this.mode == Mode.ONLY_OCSP || this.mode == Mode.ONLY_CRLS) {
                    throw e;
                }
                cause = e;
                if (debug != null) {
                    Debug debug4 = debug;
                    debug4.println("RevocationChecker.check() " + e.getMessage());
                    debug.println("RevocationChecker.check() preparing to failover");
                }
                int i = AnonymousClass2.$SwitchMap$sun$security$provider$certpath$RevocationChecker$Mode[this.mode.ordinal()];
                if (i == 1) {
                    checkCRLs(xcert, unresolvedCritExts, null, pubKey, crlSignFlag2);
                } else if (i == 3) {
                    checkOCSP(xcert, unresolvedCritExts);
                }
            } else {
                throw e;
            }
        } catch (Throwable th) {
            updateState(xcert);
            throw th;
        }
    }

    private boolean isSoftFailException(CertPathValidatorException e) {
        if (!this.softFail || e.getReason() != CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS) {
            return false;
        }
        CertPathValidatorException certPathValidatorException = new CertPathValidatorException(e.getMessage(), e.getCause(), this.params.certPath(), this.certIndex, e.getReason());
        this.softFailExceptions.addFirst(certPathValidatorException);
        return true;
    }

    private void updateState(X509Certificate cert) throws CertPathValidatorException {
        this.issuerCert = cert;
        PublicKey pubKey = cert.getPublicKey();
        if (PKIX.isDSAPublicKeyWithoutParams(pubKey)) {
            pubKey = BasicChecker.makeInheritedParamsKey(pubKey, this.prevPubKey);
        }
        this.prevPubKey = pubKey;
        this.crlSignFlag = certCanSignCrl(cert);
        if (this.certIndex > 0) {
            this.certIndex--;
        }
    }

    private void checkCRLs(X509Certificate cert, Collection<String> collection, Set<X509Certificate> stackedCerts, PublicKey pubKey, boolean signFlag) throws CertPathValidatorException {
        checkCRLs(cert, pubKey, null, signFlag, true, stackedCerts, this.params.trustAnchors());
    }

    private void checkCRLs(X509Certificate cert, PublicKey prevKey, X509Certificate prevCert, boolean signFlag, boolean allowSeparateKey, Set<X509Certificate> stackedCerts, Set<TrustAnchor> anchors) throws CertPathValidatorException {
        boolean[] reasonsMask;
        X509Certificate x509Certificate = cert;
        Set<X509Certificate> set = stackedCerts;
        if (debug != null) {
            debug.println("RevocationChecker.checkCRLs() ---checking revocation status ...");
        }
        if (set == null || !set.contains(x509Certificate)) {
            Set<X509CRL> possibleCRLs = new HashSet<>();
            Set<X509CRL> approvedCRLs = new HashSet<>();
            X509CRLSelector sel = new X509CRLSelector();
            sel.setCertificateChecking(x509Certificate);
            CertPathHelper.setDateAndTime(sel, this.params.date(), MAX_CLOCK_SKEW);
            CertPathValidatorException networkFailureException = null;
            for (CertStore store : this.certStores) {
                try {
                    for (CRL crl : store.getCRLs(sel)) {
                        possibleCRLs.add((X509CRL) crl);
                    }
                } catch (CertStoreException e) {
                    if (debug != null) {
                        debug.println("RevocationChecker.checkCRLs() CertStoreException: " + e.getMessage());
                    }
                    if (networkFailureException == null && CertStoreHelper.isCausedByNetworkIssue(store.getType(), e)) {
                        CertPathValidatorException networkFailureException2 = new CertPathValidatorException("Unable to determine revocation status due to network error", e, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                        networkFailureException = networkFailureException2;
                    }
                }
            }
            if (debug != null) {
                debug.println("RevocationChecker.checkCRLs() possible crls.size() = " + possibleCRLs.size());
            }
            boolean[] reasonsMask2 = new boolean[9];
            if (!possibleCRLs.isEmpty()) {
                approvedCRLs.addAll(verifyPossibleCRLs(possibleCRLs, x509Certificate, prevKey, signFlag, reasonsMask2, anchors));
            }
            if (debug != null) {
                debug.println("RevocationChecker.checkCRLs() approved crls.size() = " + approvedCRLs.size());
            }
            if (approvedCRLs.isEmpty() || !Arrays.equals(reasonsMask2, ALL_REASONS)) {
                try {
                    if (this.crlDP) {
                        try {
                            reasonsMask = reasonsMask2;
                            X509CRLSelector x509CRLSelector = sel;
                            try {
                                approvedCRLs.addAll(DistributionPointFetcher.getCRLs(sel, signFlag, prevKey, prevCert, this.params.sigProvider(), this.certStores, reasonsMask, anchors, null));
                            } catch (CertStoreException e2) {
                                e = e2;
                                PublicKey publicKey = prevKey;
                                boolean z = signFlag;
                            }
                        } catch (CertStoreException e3) {
                            e = e3;
                            boolean[] zArr = reasonsMask2;
                            X509CRLSelector x509CRLSelector2 = sel;
                            PublicKey publicKey2 = prevKey;
                            boolean z2 = signFlag;
                            if (e instanceof PKIX.CertStoreTypeException) {
                            }
                            throw new CertPathValidatorException((Throwable) e);
                        }
                    } else {
                        reasonsMask = reasonsMask2;
                        X509CRLSelector x509CRLSelector3 = sel;
                    }
                    if (!approvedCRLs.isEmpty() && Arrays.equals(reasonsMask, ALL_REASONS)) {
                        checkApprovedCRLs(x509Certificate, approvedCRLs);
                    } else if (allowSeparateKey) {
                        try {
                            verifyWithSeparateSigningKey(x509Certificate, prevKey, signFlag, set);
                            return;
                        } catch (CertPathValidatorException cpve) {
                            CertPathValidatorException certPathValidatorException = cpve;
                            if (networkFailureException != null) {
                                throw networkFailureException;
                            }
                            throw cpve;
                        }
                    } else {
                        PublicKey publicKey3 = prevKey;
                        boolean z3 = signFlag;
                        if (networkFailureException != null) {
                            throw networkFailureException;
                        }
                        CertPathValidatorException certPathValidatorException2 = new CertPathValidatorException("Could not determine revocation status", null, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                        throw certPathValidatorException2;
                    }
                } catch (CertStoreException e4) {
                    e = e4;
                    PublicKey publicKey4 = prevKey;
                    boolean z4 = signFlag;
                    boolean[] zArr2 = reasonsMask2;
                    X509CRLSelector x509CRLSelector4 = sel;
                    if ((e instanceof PKIX.CertStoreTypeException) || !CertStoreHelper.isCausedByNetworkIssue(((PKIX.CertStoreTypeException) e).getType(), e)) {
                        throw new CertPathValidatorException((Throwable) e);
                    }
                    CertPathValidatorException certPathValidatorException3 = new CertPathValidatorException("Unable to determine revocation status due to network error", e, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                    throw certPathValidatorException3;
                }
            } else {
                checkApprovedCRLs(x509Certificate, approvedCRLs);
                boolean[] zArr3 = reasonsMask2;
                X509CRLSelector x509CRLSelector5 = sel;
            }
            return;
        }
        if (debug != null) {
            debug.println("RevocationChecker.checkCRLs() circular dependency");
        }
        CertPathValidatorException certPathValidatorException4 = new CertPathValidatorException("Could not determine revocation status", null, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
        throw certPathValidatorException4;
    }

    private void checkApprovedCRLs(X509Certificate cert, Set<X509CRL> approvedCRLs) throws CertPathValidatorException {
        if (debug != null) {
            BigInteger sn = cert.getSerialNumber();
            debug.println("RevocationChecker.checkApprovedCRLs() starting the final sweep...");
            Debug debug2 = debug;
            debug2.println("RevocationChecker.checkApprovedCRLs() cert SN: " + sn.toString());
        }
        CRLReason reasonCode = CRLReason.UNSPECIFIED;
        CRLReason cRLReason = reasonCode;
        for (X509CRL crl : approvedCRLs) {
            X509CRLEntry e = crl.getRevokedCertificate(cert);
            if (e != null) {
                try {
                    X509CRLEntryImpl entry = X509CRLEntryImpl.toImpl(e);
                    if (debug != null) {
                        Debug debug3 = debug;
                        debug3.println("RevocationChecker.checkApprovedCRLs() CRL entry: " + entry.toString());
                    }
                    Set<String> unresCritExts = entry.getCriticalExtensionOIDs();
                    if (unresCritExts != null && !unresCritExts.isEmpty()) {
                        unresCritExts.remove(PKIXExtensions.ReasonCode_Id.toString());
                        unresCritExts.remove(PKIXExtensions.CertificateIssuer_Id.toString());
                        if (!unresCritExts.isEmpty()) {
                            throw new CertPathValidatorException("Unrecognized critical extension(s) in revoked CRL entry");
                        }
                    }
                    CRLReason reasonCode2 = entry.getRevocationReason();
                    if (reasonCode2 == null) {
                        reasonCode2 = CRLReason.UNSPECIFIED;
                    }
                    Date revocationDate = entry.getRevocationDate();
                    if (!revocationDate.before(this.params.date())) {
                        X509CRLEntryImpl x509CRLEntryImpl = entry;
                    } else {
                        Throwable t = new CertificateRevokedException(revocationDate, reasonCode2, crl.getIssuerX500Principal(), entry.getExtensions());
                        CertPathValidatorException certPathValidatorException = new CertPathValidatorException(t.getMessage(), t, null, -1, CertPathValidatorException.BasicReason.REVOKED);
                        throw certPathValidatorException;
                    }
                } catch (CRLException ce) {
                    CRLException cRLException = ce;
                    throw new CertPathValidatorException((Throwable) ce);
                }
            }
        }
        X509Certificate x509Certificate = cert;
    }

    private void checkOCSP(X509Certificate cert, Collection<String> collection) throws CertPathValidatorException {
        IOException e;
        CertId certId;
        OCSPResponse response;
        URI responderURI2;
        try {
            X509CertImpl currCert = X509CertImpl.toImpl(cert);
            try {
                if (this.issuerCert != null) {
                    certId = new CertId(this.issuerCert, currCert.getSerialNumberObject());
                } else {
                    certId = new CertId(this.anchor.getCA(), this.anchor.getCAPublicKey(), currCert.getSerialNumberObject());
                }
                try {
                    byte[] responseBytes = this.ocspResponses.get(cert);
                    if (responseBytes != null) {
                        if (debug != null) {
                            debug.println("Found cached OCSP response");
                        }
                        OCSPResponse response2 = new OCSPResponse(responseBytes);
                        byte[] nonce = null;
                        try {
                            for (Extension ext : this.ocspExtensions) {
                                if (ext.getId().equals("1.3.6.1.5.5.7.48.1.2")) {
                                    nonce = ext.getValue();
                                }
                            }
                            response2.verify(Collections.singletonList(certId), this.issuerCert, this.responderCert, this.params.date(), nonce);
                            response = response2;
                        } catch (IOException e2) {
                            e = e2;
                            OCSPResponse oCSPResponse = response2;
                            CertPathValidatorException certPathValidatorException = new CertPathValidatorException("Unable to determine revocation status due to network error", e, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                            throw certPathValidatorException;
                        }
                    } else {
                        if (this.responderURI != null) {
                            responderURI2 = this.responderURI;
                        } else {
                            responderURI2 = OCSP.getResponderURI(currCert);
                        }
                        if (responderURI2 != null) {
                            response = OCSP.check((List<CertId>) Collections.singletonList(certId), responderURI2, this.issuerCert, this.responderCert, (Date) null, this.ocspExtensions);
                        } else {
                            throw new CertPathValidatorException("Certificate does not specify OCSP responder", null, null, -1);
                        }
                    }
                    OCSP.RevocationStatus rs = response.getSingleResponse(certId);
                    OCSP.RevocationStatus.CertStatus certStatus = rs.getCertStatus();
                    if (certStatus == OCSP.RevocationStatus.CertStatus.REVOKED) {
                        Date revocationTime = rs.getRevocationTime();
                        if (revocationTime.before(this.params.date())) {
                            Throwable t = new CertificateRevokedException(revocationTime, rs.getRevocationReason(), response.getSignerCertificate().getSubjectX500Principal(), rs.getSingleExtensions());
                            CertPathValidatorException certPathValidatorException2 = new CertPathValidatorException(t.getMessage(), t, null, -1, CertPathValidatorException.BasicReason.REVOKED);
                            throw certPathValidatorException2;
                        }
                    } else if (certStatus == OCSP.RevocationStatus.CertStatus.UNKNOWN) {
                        CertPathValidatorException certPathValidatorException3 = new CertPathValidatorException("Certificate's revocation status is unknown", null, this.params.certPath(), -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                        throw certPathValidatorException3;
                    }
                } catch (IOException e3) {
                    e = e3;
                    e = e;
                    CertPathValidatorException certPathValidatorException4 = new CertPathValidatorException("Unable to determine revocation status due to network error", e, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                    throw certPathValidatorException4;
                }
            } catch (IOException e4) {
                e = e4;
                X509Certificate x509Certificate = cert;
                e = e;
                CertPathValidatorException certPathValidatorException42 = new CertPathValidatorException("Unable to determine revocation status due to network error", e, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                throw certPathValidatorException42;
            }
        } catch (CertificateException ce) {
            X509Certificate x509Certificate2 = cert;
            CertificateException certificateException = ce;
            throw new CertPathValidatorException((Throwable) ce);
        }
    }

    private static String stripOutSeparators(String value) {
        char[] chars = value.toCharArray();
        StringBuilder hexNumber = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (HEX_DIGITS.indexOf((int) chars[i]) != -1) {
                hexNumber.append(chars[i]);
            }
        }
        return hexNumber.toString();
    }

    static boolean certCanSignCrl(X509Certificate cert) {
        boolean[] keyUsage = cert.getKeyUsage();
        if (keyUsage != null) {
            return keyUsage[6];
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d0  */
    private Collection<X509CRL> verifyPossibleCRLs(Set<X509CRL> crls, X509Certificate cert, PublicKey prevKey, boolean signFlag, boolean[] reasonsMask, Set<TrustAnchor> anchors) throws CertPathValidatorException {
        List<DistributionPoint> points;
        try {
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            if (debug != null) {
                Debug debug2 = debug;
                debug2.println("RevocationChecker.verifyPossibleCRLs: Checking CRLDPs for " + certImpl.getSubjectX500Principal());
            }
            CRLDistributionPointsExtension ext = certImpl.getCRLDistributionPointsExtension();
            if (ext == null) {
                points = Collections.singletonList(new DistributionPoint(new GeneralNames().add(new GeneralName((GeneralNameInterface) (X500Name) certImpl.getIssuerDN())), (boolean[]) null, (GeneralNames) null));
            } else {
                points = ext.get(CRLDistributionPointsExtension.POINTS);
            }
            Set<X509CRL> results = new HashSet<>();
            Iterator<DistributionPoint> it = points.iterator();
            while (true) {
                if (!it.hasNext()) {
                    boolean[] zArr = reasonsMask;
                    break;
                }
                DistributionPoint point = it.next();
                Iterator<X509CRL> it2 = crls.iterator();
                while (it2.hasNext()) {
                    X509CRL crl = it2.next();
                    String sigProvider = this.params.sigProvider();
                    List<CertStore> list = this.certStores;
                    X509CRL crl2 = crl;
                    Iterator<X509CRL> it3 = it2;
                    Iterator<DistributionPoint> it4 = it;
                    if (DistributionPointFetcher.verifyCRL(certImpl, point, crl, reasonsMask, signFlag, prevKey, null, sigProvider, anchors, list, this.params.date())) {
                        results.add(crl2);
                    }
                    it = it4;
                    it2 = it3;
                }
                Iterator<DistributionPoint> it5 = it;
                try {
                    if (Arrays.equals(reasonsMask, ALL_REASONS)) {
                        break;
                    }
                    it = it5;
                } catch (IOException | CRLException | CertificateException e) {
                    e = e;
                    if (debug != null) {
                    }
                    return Collections.emptySet();
                }
            }
            return results;
        } catch (IOException | CRLException | CertificateException e2) {
            e = e2;
            boolean[] zArr2 = reasonsMask;
            if (debug != null) {
                Debug debug3 = debug;
                debug3.println("Exception while verifying CRL: " + e.getMessage());
                e.printStackTrace();
            }
            return Collections.emptySet();
        }
    }

    private void verifyWithSeparateSigningKey(X509Certificate cert, PublicKey prevKey, boolean signFlag, Set<X509Certificate> stackedCerts) throws CertPathValidatorException {
        if (debug != null) {
            Debug debug2 = debug;
            debug2.println("RevocationChecker.verifyWithSeparateSigningKey() ---checking " + "revocation status" + "...");
        }
        if (stackedCerts != null && stackedCerts.contains(cert)) {
            if (debug != null) {
                debug.println("RevocationChecker.verifyWithSeparateSigningKey() circular dependency");
            }
            CertPathValidatorException certPathValidatorException = new CertPathValidatorException("Could not determine revocation status", null, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
            throw certPathValidatorException;
        } else if (!signFlag) {
            buildToNewKey(cert, null, stackedCerts);
        } else {
            buildToNewKey(cert, prevKey, stackedCerts);
        }
    }

    private void buildToNewKey(X509Certificate currCert, PublicKey prevKey, Set<X509Certificate> stackedCerts) throws CertPathValidatorException {
        Set<TrustAnchor> set;
        Set<X509Certificate> stackedCerts2;
        PublicKey prevKey2;
        Set<X509Certificate> stackedCerts3;
        boolean z;
        CertPathBuilder builder;
        int i;
        PKIXCertPathBuilderResult cpbr;
        boolean z2;
        PublicKey newKey;
        boolean z3;
        X509Certificate newCert;
        PublicKey newKey2;
        TrustAnchor ta;
        Set<X509Certificate> stackedCerts4;
        CertPathBuilder builder2;
        PKIXCertPathBuilderResult cpbr2;
        int i2;
        RevocationChecker revocationChecker = this;
        PublicKey publicKey = prevKey;
        if (debug != null) {
            debug.println("RevocationChecker.buildToNewKey() starting work");
        }
        Set<PublicKey> badKeys = new HashSet<>();
        if (publicKey != null) {
            badKeys.add(publicKey);
        }
        X509CertSelector certSel = new RejectKeySelector(badKeys);
        certSel.setSubject(currCert.getIssuerX500Principal());
        certSel.setKeyUsage(CRL_SIGN_USAGE);
        if (revocationChecker.anchor == null) {
            set = revocationChecker.params.trustAnchors();
        } else {
            set = Collections.singleton(revocationChecker.anchor);
        }
        Set<TrustAnchor> newAnchors = set;
        try {
            PKIXBuilderParameters builderParams = new PKIXBuilderParameters(newAnchors, (CertSelector) certSel);
            builderParams.setInitialPolicies(revocationChecker.params.initialPolicies());
            builderParams.setCertStores(revocationChecker.certStores);
            builderParams.setExplicitPolicyRequired(revocationChecker.params.explicitPolicyRequired());
            builderParams.setPolicyMappingInhibited(revocationChecker.params.policyMappingInhibited());
            builderParams.setAnyPolicyInhibited(revocationChecker.params.anyPolicyInhibited());
            builderParams.setDate(revocationChecker.params.date());
            builderParams.setCertPathCheckers(revocationChecker.params.getPKIXParameters().getCertPathCheckers());
            builderParams.setSigProvider(revocationChecker.params.sigProvider());
            builderParams.setRevocationEnabled(false);
            int i3 = 1;
            if (Builder.USE_AIA) {
                X509CertImpl currCertImpl = null;
                try {
                    currCertImpl = X509CertImpl.toImpl(currCert);
                } catch (CertificateException ce) {
                    CertificateException certificateException = ce;
                    if (debug != null) {
                        debug.println("RevocationChecker.buildToNewKey: error decoding cert: " + ce);
                    }
                }
                AuthorityInfoAccessExtension aiaExt = null;
                if (currCertImpl != null) {
                    aiaExt = currCertImpl.getAuthorityInfoAccessExtension();
                }
                if (aiaExt != null) {
                    List<AccessDescription> adList = aiaExt.getAccessDescriptions();
                    if (adList != null) {
                        for (AccessDescription ad : adList) {
                            CertStore cs = URICertStore.getInstance(ad);
                            if (cs != null) {
                                if (debug != null) {
                                    debug.println("adding AIAext CertStore");
                                }
                                builderParams.addCertStore(cs);
                            }
                        }
                    }
                }
            }
            try {
                CertPathBuilder builder3 = CertPathBuilder.getInstance("PKIX");
                Set<X509Certificate> stackedCerts5 = stackedCerts;
                while (true) {
                    try {
                        if (debug != null) {
                            try {
                                debug.println("RevocationChecker.buildToNewKey() about to try build ...");
                            } catch (InvalidAlgorithmParameterException e) {
                                iape = e;
                                Set<X509Certificate> set2 = stackedCerts5;
                            } catch (CertPathBuilderException e2) {
                                Set<X509Certificate> set3 = stackedCerts5;
                                CertPathValidatorException certPathValidatorException = new CertPathValidatorException("Could not determine revocation status", null, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                                throw certPathValidatorException;
                            }
                        }
                        PKIXCertPathBuilderResult cpbr3 = (PKIXCertPathBuilderResult) builder3.build(builderParams);
                        if (debug != null) {
                            debug.println("RevocationChecker.buildToNewKey() about to check revocation ...");
                        }
                        if (stackedCerts5 == null) {
                            stackedCerts2 = new HashSet<>();
                        } else {
                            stackedCerts2 = stackedCerts5;
                        }
                        try {
                            stackedCerts2.add(currCert);
                            TrustAnchor ta2 = cpbr3.getTrustAnchor();
                            PublicKey prevKey22 = ta2.getCAPublicKey();
                            if (prevKey22 == null) {
                                try {
                                    prevKey2 = ta2.getTrustedCert().getPublicKey();
                                } catch (InvalidAlgorithmParameterException e3) {
                                    iape = e3;
                                    Set<X509Certificate> set4 = stackedCerts2;
                                    throw new CertPathValidatorException((Throwable) iape);
                                } catch (CertPathBuilderException e4) {
                                    Set<X509Certificate> set5 = stackedCerts2;
                                    CertPathValidatorException certPathValidatorException2 = new CertPathValidatorException("Could not determine revocation status", null, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                                    throw certPathValidatorException2;
                                }
                            } else {
                                prevKey2 = prevKey22;
                            }
                            List<? extends Certificate> cpList = cpbr3.getCertPath().getCertificates();
                            try {
                                int i4 = cpList.size() - i3;
                                PublicKey prevKey23 = prevKey2;
                                boolean signFlag = true;
                                while (i4 >= 0) {
                                    try {
                                        X509Certificate cert = (X509Certificate) cpList.get(i4);
                                        if (debug != null) {
                                            try {
                                                Debug debug2 = debug;
                                                try {
                                                    StringBuilder sb = new StringBuilder();
                                                    ta = ta2;
                                                    try {
                                                        sb.append("RevocationChecker.buildToNewKey() index ");
                                                        sb.append(i4);
                                                        sb.append(" checking ");
                                                        sb.append((Object) cert);
                                                        debug2.println(sb.toString());
                                                    } catch (CertPathValidatorException e5) {
                                                        stackedCerts4 = stackedCerts2;
                                                        cpbr2 = cpbr3;
                                                        builder2 = builder3;
                                                        i2 = 1;
                                                    }
                                                } catch (CertPathValidatorException e6) {
                                                    TrustAnchor trustAnchor = ta2;
                                                    stackedCerts3 = stackedCerts2;
                                                    cpbr = cpbr3;
                                                    builder = builder3;
                                                    i = 1;
                                                    z2 = false;
                                                    badKeys.add(cpbr.getPublicKey());
                                                    i3 = i;
                                                    builder3 = builder;
                                                    boolean z4 = z;
                                                    stackedCerts5 = stackedCerts3;
                                                    revocationChecker = this;
                                                    PublicKey publicKey2 = prevKey;
                                                }
                                            } catch (CertPathValidatorException e7) {
                                                TrustAnchor trustAnchor2 = ta2;
                                                stackedCerts3 = stackedCerts2;
                                                cpbr = cpbr3;
                                                builder = builder3;
                                                i = i3;
                                                z2 = false;
                                                badKeys.add(cpbr.getPublicKey());
                                                i3 = i;
                                                builder3 = builder;
                                                boolean z42 = z;
                                                stackedCerts5 = stackedCerts3;
                                                revocationChecker = this;
                                                PublicKey publicKey22 = prevKey;
                                            }
                                        } else {
                                            ta = ta2;
                                        }
                                        X509Certificate cert2 = cert;
                                        stackedCerts4 = stackedCerts2;
                                        builder2 = builder3;
                                        cpbr2 = cpbr3;
                                        i2 = 1;
                                        try {
                                            revocationChecker.checkCRLs(cert, prevKey23, null, signFlag, true, stackedCerts4, newAnchors);
                                            X509Certificate cert3 = cert2;
                                            signFlag = certCanSignCrl(cert3);
                                            prevKey23 = cert3.getPublicKey();
                                            i4--;
                                            X509Certificate x509Certificate = currCert;
                                            cpbr3 = cpbr2;
                                            i3 = 1;
                                            ta2 = ta;
                                            builder3 = builder2;
                                            stackedCerts2 = stackedCerts4;
                                            PublicKey publicKey3 = prevKey;
                                        } catch (CertPathValidatorException e8) {
                                            z2 = false;
                                            badKeys.add(cpbr.getPublicKey());
                                            i3 = i;
                                            builder3 = builder;
                                            boolean z422 = z;
                                            stackedCerts5 = stackedCerts3;
                                            revocationChecker = this;
                                            PublicKey publicKey222 = prevKey;
                                        }
                                    } catch (CertPathValidatorException e9) {
                                        TrustAnchor trustAnchor3 = ta2;
                                        stackedCerts3 = stackedCerts2;
                                        cpbr = cpbr3;
                                        builder = builder3;
                                        i = i3;
                                        z2 = false;
                                        badKeys.add(cpbr.getPublicKey());
                                        i3 = i;
                                        builder3 = builder;
                                        boolean z4222 = z;
                                        stackedCerts5 = stackedCerts3;
                                        revocationChecker = this;
                                        PublicKey publicKey2222 = prevKey;
                                    }
                                }
                                TrustAnchor trustAnchor4 = ta2;
                                stackedCerts3 = stackedCerts2;
                                PKIXCertPathBuilderResult cpbr4 = cpbr3;
                                builder = builder3;
                                i = i3;
                                try {
                                    if (debug != null) {
                                        debug.println("RevocationChecker.buildToNewKey() got key " + cpbr4.getPublicKey());
                                    }
                                    newKey = cpbr4.getPublicKey();
                                    if (cpList.isEmpty()) {
                                        newCert = null;
                                        z3 = false;
                                    } else {
                                        z3 = false;
                                        newCert = (X509Certificate) cpList.get(0);
                                    }
                                } catch (InvalidAlgorithmParameterException e10) {
                                    iape = e10;
                                    throw new CertPathValidatorException((Throwable) iape);
                                } catch (CertPathBuilderException e11) {
                                    CertPathValidatorException certPathValidatorException22 = new CertPathValidatorException("Could not determine revocation status", null, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                                    throw certPathValidatorException22;
                                }
                                try {
                                    Set<TrustAnchor> trustAnchors = revocationChecker.params.trustAnchors();
                                    RevocationChecker revocationChecker2 = revocationChecker;
                                    z = z3;
                                    newKey2 = newKey;
                                    try {
                                        revocationChecker2.checkCRLs(currCert, newKey, newCert, true, false, null, trustAnchors);
                                        return;
                                    } catch (CertPathValidatorException e12) {
                                        cpve = e12;
                                    }
                                } catch (CertPathValidatorException e13) {
                                    cpve = e13;
                                    z = z3;
                                    newKey2 = newKey;
                                    if (cpve.getReason() != CertPathValidatorException.BasicReason.REVOKED) {
                                        badKeys.add(newKey2);
                                        i3 = i;
                                        builder3 = builder;
                                        boolean z42222 = z;
                                        stackedCerts5 = stackedCerts3;
                                        revocationChecker = this;
                                        PublicKey publicKey22222 = prevKey;
                                    } else {
                                        throw cpve;
                                    }
                                }
                            } catch (CertPathValidatorException e14) {
                                TrustAnchor trustAnchor5 = ta2;
                                stackedCerts3 = stackedCerts2;
                                cpbr = cpbr3;
                                builder = builder3;
                                i = i3;
                                z2 = false;
                                PublicKey publicKey4 = prevKey2;
                                badKeys.add(cpbr.getPublicKey());
                                i3 = i;
                                builder3 = builder;
                                boolean z422222 = z;
                                stackedCerts5 = stackedCerts3;
                                revocationChecker = this;
                                PublicKey publicKey222222 = prevKey;
                            }
                            i3 = i;
                            builder3 = builder;
                            boolean z4222222 = z;
                            stackedCerts5 = stackedCerts3;
                            revocationChecker = this;
                            PublicKey publicKey2222222 = prevKey;
                        } catch (InvalidAlgorithmParameterException e15) {
                            iape = e15;
                            Set<X509Certificate> set6 = stackedCerts2;
                            CertPathBuilder certPathBuilder = builder3;
                            throw new CertPathValidatorException((Throwable) iape);
                        } catch (CertPathBuilderException e16) {
                            Set<X509Certificate> set7 = stackedCerts2;
                            CertPathBuilder certPathBuilder2 = builder3;
                            CertPathValidatorException certPathValidatorException222 = new CertPathValidatorException("Could not determine revocation status", null, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                            throw certPathValidatorException222;
                        }
                    } catch (InvalidAlgorithmParameterException e17) {
                        iape = e17;
                        CertPathBuilder certPathBuilder3 = builder3;
                        Set<X509Certificate> set8 = stackedCerts5;
                        throw new CertPathValidatorException((Throwable) iape);
                    } catch (CertPathBuilderException e18) {
                        CertPathBuilder certPathBuilder4 = builder3;
                        Set<X509Certificate> set9 = stackedCerts5;
                        CertPathValidatorException certPathValidatorException2222 = new CertPathValidatorException("Could not determine revocation status", null, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                        throw certPathValidatorException2222;
                    }
                }
            } catch (NoSuchAlgorithmException nsae) {
                throw new CertPathValidatorException((Throwable) nsae);
            }
        } catch (InvalidAlgorithmParameterException iape) {
            throw new RuntimeException((Throwable) iape);
        }
    }

    public RevocationChecker clone() {
        RevocationChecker copy = (RevocationChecker) super.clone();
        copy.softFailExceptions = new LinkedList<>(this.softFailExceptions);
        return copy;
    }
}
