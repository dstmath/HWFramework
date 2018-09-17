package sun.security.provider.certpath;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivilegedAction;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CRLReason;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
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
import java.security.cert.PKIXRevocationChecker.Option;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.certpath.OCSP.RevocationStatus;
import sun.security.provider.certpath.OCSP.RevocationStatus.CertStatus;
import sun.security.util.Debug;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.CRLDistributionPointsExtension;
import sun.security.x509.DistributionPoint;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNames;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X500Name;
import sun.security.x509.X509CRLEntryImpl;
import sun.security.x509.X509CertImpl;

class RevocationChecker extends PKIXRevocationChecker {
    private static final /* synthetic */ int[] -java-security-cert-PKIXRevocationChecker$OptionSwitchesValues = null;
    private static final /* synthetic */ int[] -sun-security-provider-certpath-RevocationChecker$ModeSwitchesValues = null;
    private static final boolean[] ALL_REASONS = new boolean[]{true, true, true, true, true, true, true, true, true};
    private static final boolean[] CRL_SIGN_USAGE = new boolean[]{false, false, false, false, false, false, true};
    private static final String HEX_DIGITS = "0123456789ABCDEFabcdef";
    private static final long MAX_CLOCK_SKEW = 900000;
    private static final Debug debug = Debug.getInstance("certpath");
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
    private ValidatorParams params;
    private PublicKey prevPubKey;
    private X509Certificate responderCert;
    private URI responderURI;
    private boolean softFail;
    private LinkedList<CertPathValidatorException> softFailExceptions = new LinkedList();

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
            StringBuilder sb = new StringBuilder();
            sb.append("RejectKeySelector: [\n");
            sb.append(super.toString());
            sb.append(this.badKeySet);
            sb.append("]");
            return sb.-java_util_stream_Collectors-mthref-7();
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

        /* synthetic */ RevocationProperties(RevocationProperties -this0) {
            this();
        }

        private RevocationProperties() {
        }
    }

    private static /* synthetic */ int[] -getjava-security-cert-PKIXRevocationChecker$OptionSwitchesValues() {
        if (-java-security-cert-PKIXRevocationChecker$OptionSwitchesValues != null) {
            return -java-security-cert-PKIXRevocationChecker$OptionSwitchesValues;
        }
        int[] iArr = new int[Option.values().length];
        try {
            iArr[Option.NO_FALLBACK.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Option.ONLY_END_ENTITY.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Option.PREFER_CRLS.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Option.SOFT_FAIL.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -java-security-cert-PKIXRevocationChecker$OptionSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getsun-security-provider-certpath-RevocationChecker$ModeSwitchesValues() {
        if (-sun-security-provider-certpath-RevocationChecker$ModeSwitchesValues != null) {
            return -sun-security-provider-certpath-RevocationChecker$ModeSwitchesValues;
        }
        int[] iArr = new int[Mode.values().length];
        try {
            iArr[Mode.ONLY_CRLS.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Mode.ONLY_OCSP.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Mode.PREFER_CRLS.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Mode.PREFER_OCSP.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -sun-security-provider-certpath-RevocationChecker$ModeSwitchesValues = iArr;
        return iArr;
    }

    RevocationChecker() {
    }

    RevocationChecker(TrustAnchor anchor, ValidatorParams params) throws CertPathValidatorException {
        init(anchor, params);
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0034  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00e8 A:{Splitter: B:23:0x009c, ExcHandler: java.security.InvalidAlgorithmParameterException (r1_0 'e' java.lang.Object)} */
    /* JADX WARNING: Missing block: B:37:0x00e8, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:39:0x00eb, code:
            if (debug != null) goto L_0x00ed;
     */
    /* JADX WARNING: Missing block: B:40:0x00ed, code:
            debug.println("RevocationChecker: error creating Collection CertStore: " + r1);
     */
    /* JADX WARNING: Missing block: B:44:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void init(TrustAnchor anchor, ValidatorParams params) throws CertPathValidatorException {
        RevocationProperties rp = getRevocationProperties();
        URI uri = getOcspResponder();
        if (uri == null) {
            uri = toURI(rp.ocspUrl);
        }
        this.responderURI = uri;
        X509Certificate cert = getOcspResponderCert();
        if (cert == null) {
            cert = getResponderCert(rp, params.trustAnchors(), params.certStores());
        }
        this.responderCert = cert;
        Set<Option> options = getOptions();
        for (Object option : options) {
            switch (-getjava-security-cert-PKIXRevocationChecker$OptionSwitchesValues()[option.ordinal()]) {
                case 1:
                case 2:
                case 3:
                case 4:
                    for (Object option2 : options) {
                    }
                    break;
            }
            throw new CertPathValidatorException("Unrecognized revocation parameter option: " + option2);
        }
        this.softFail = options.contains(Option.SOFT_FAIL);
        if (this.legacy) {
            this.mode = rp.ocspEnabled ? Mode.PREFER_OCSP : Mode.ONLY_CRLS;
            this.onlyEE = rp.onlyEE;
        } else {
            if (options.contains(Option.NO_FALLBACK)) {
                if (options.contains(Option.PREFER_CRLS)) {
                    this.mode = Mode.ONLY_CRLS;
                } else {
                    this.mode = Mode.ONLY_OCSP;
                }
            } else if (options.contains(Option.PREFER_CRLS)) {
                this.mode = Mode.PREFER_CRLS;
            }
            this.onlyEE = options.contains(Option.ONLY_END_ENTITY);
        }
        if (this.legacy) {
            this.crlDP = rp.crlDPEnabled;
        } else {
            this.crlDP = true;
        }
        this.ocspResponses = getOcspResponses();
        this.ocspExtensions = getOcspExtensions();
        this.anchor = anchor;
        this.params = params;
        this.certStores = new ArrayList(params.certStores());
        try {
            this.certStores.-java_util_stream_Collectors-mthref-2(CertStore.getInstance("Collection", new CollectionCertStoreParameters(params.certificates())));
        } catch (Object e) {
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
                boolean equalsIgnoreCase;
                boolean z = false;
                RevocationProperties rp = new RevocationProperties();
                String onlyEE = Security.getProperty("com.sun.security.onlyCheckRevocationOfEECert");
                if (onlyEE != null) {
                    equalsIgnoreCase = onlyEE.equalsIgnoreCase("true");
                } else {
                    equalsIgnoreCase = false;
                }
                rp.onlyEE = equalsIgnoreCase;
                String ocspEnabled = Security.getProperty("ocsp.enable");
                if (ocspEnabled != null) {
                    z = ocspEnabled.equalsIgnoreCase("true");
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
            return getResponderCert(rp.ocspSubject, (Set) anchors, (List) stores);
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
            return getResponderCert(sel, (Set) anchors, (List) stores);
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
                return getResponderCert(sel, (Set) anchors, (List) stores);
            } catch (NumberFormatException e) {
                throw new CertPathValidatorException("cannot parse ocsp.responderCertSerialNumber property", e);
            }
        } catch (IllegalArgumentException e2) {
            throw new CertPathValidatorException("cannot parse ocsp.responderCertIssuerName property", e2);
        }
    }

    private static X509Certificate getResponderCert(X509CertSelector sel, Set<TrustAnchor> anchors, List<CertStore> stores) throws CertPathValidatorException {
        for (TrustAnchor anchor : anchors) {
            X509Certificate cert = anchor.getTrustedCert();
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
            } catch (Object e) {
                if (debug != null) {
                    debug.println("CertStore exception:" + e);
                }
            }
        }
        throw new CertPathValidatorException("Cannot find the responder's certificate (set using the OCSP security properties).");
    }

    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        if (this.anchor != null) {
            PublicKey publicKey;
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

    /* JADX WARNING: Missing block: B:52:0x00ea, code:
            updateState(r11);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void check(X509Certificate xcert, Collection<String> unresolvedCritExts, PublicKey pubKey, boolean crlSignFlag) throws CertPathValidatorException {
        boolean eSoftFail;
        if (debug != null) {
            debug.println("RevocationChecker.check: checking cert\n  SN: " + Debug.toHexString(xcert.getSerialNumber()) + "\n  Subject: " + xcert.getSubjectX500Principal() + "\n  Issuer: " + xcert.getIssuerX500Principal());
        }
        try {
            if (!this.onlyEE || xcert.getBasicConstraints() == -1) {
                switch (-getsun-security-provider-certpath-RevocationChecker$ModeSwitchesValues()[this.mode.ordinal()]) {
                    case 1:
                    case 3:
                        checkCRLs(xcert, unresolvedCritExts, null, pubKey, crlSignFlag);
                        break;
                    case 2:
                    case 4:
                        checkOCSP(xcert, unresolvedCritExts);
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
                debug.println("RevocationChecker.check() " + x.getMessage());
            }
            if (x.getReason() == BasicReason.REVOKED) {
                throw x;
            } else if (!isSoftFailException(x)) {
                e.addSuppressed(x);
                throw e;
            } else if (!eSoftFail) {
                throw e;
            }
        } catch (CertPathValidatorException e) {
            if (e.getReason() != BasicReason.REVOKED) {
                eSoftFail = isSoftFailException(e);
                if (eSoftFail) {
                    if (this.mode == Mode.ONLY_OCSP || this.mode == Mode.ONLY_CRLS) {
                        updateState(xcert);
                        return;
                    }
                } else if (this.mode == Mode.ONLY_OCSP || this.mode == Mode.ONLY_CRLS) {
                    throw e;
                }
                CertPathValidatorException cause = e;
                if (debug != null) {
                    debug.println("RevocationChecker.check() " + e.getMessage());
                    debug.println("RevocationChecker.check() preparing to failover");
                }
                switch (-getsun-security-provider-certpath-RevocationChecker$ModeSwitchesValues()[this.mode.ordinal()]) {
                    case 3:
                        checkOCSP(xcert, unresolvedCritExts);
                        break;
                    case 4:
                        checkCRLs(xcert, unresolvedCritExts, null, pubKey, crlSignFlag);
                        break;
                }
            }
            throw e;
        } catch (Throwable th) {
            updateState(xcert);
        }
    }

    private boolean isSoftFailException(CertPathValidatorException e) {
        if (!this.softFail || e.getReason() != BasicReason.UNDETERMINED_REVOCATION_STATUS) {
            return false;
        }
        this.softFailExceptions.addFirst(new CertPathValidatorException(e.getMessage(), e.getCause(), this.params.certPath(), this.certIndex, e.getReason()));
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
        if (debug != null) {
            debug.println("RevocationChecker.checkCRLs() ---checking revocation status ...");
        }
        if (stackedCerts == null || !stackedCerts.contains(cert)) {
            Set<X509CRL> possibleCRLs = new HashSet();
            Set<X509CRL> approvedCRLs = new HashSet();
            X509CRLSelector sel = new X509CRLSelector();
            sel.setCertificateChecking(cert);
            CertPathHelper.setDateAndTime(sel, this.params.date(), MAX_CLOCK_SKEW);
            CertPathValidatorException networkFailureException = null;
            for (CertStore store : this.certStores) {
                try {
                    for (CRL crl : store.getCRLs(sel)) {
                        possibleCRLs.-java_util_stream_Collectors-mthref-4((X509CRL) crl);
                    }
                } catch (CertStoreException e) {
                    if (debug != null) {
                        debug.println("RevocationChecker.checkCRLs() CertStoreException: " + e.getMessage());
                    }
                    if (networkFailureException == null && CertStoreHelper.isCausedByNetworkIssue(store.getType(), e)) {
                        networkFailureException = new CertPathValidatorException("Unable to determine revocation status due to network error", e, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
                    }
                }
            }
            if (debug != null) {
                debug.println("RevocationChecker.checkCRLs() possible crls.size() = " + possibleCRLs.size());
            }
            boolean[] reasonsMask = new boolean[9];
            if (!possibleCRLs.isEmpty()) {
                approvedCRLs.addAll(verifyPossibleCRLs(possibleCRLs, cert, prevKey, signFlag, reasonsMask, anchors));
            }
            if (debug != null) {
                debug.println("RevocationChecker.checkCRLs() approved crls.size() = " + approvedCRLs.size());
            }
            if (approvedCRLs.isEmpty() || !Arrays.equals(reasonsMask, ALL_REASONS)) {
                try {
                    if (this.crlDP) {
                        Set<X509CRL> set = approvedCRLs;
                        set.addAll(DistributionPointFetcher.getCRLs(sel, signFlag, prevKey, prevCert, this.params.sigProvider(), this.certStores, reasonsMask, anchors, null));
                    }
                    if (!approvedCRLs.isEmpty() && Arrays.equals(reasonsMask, ALL_REASONS)) {
                        checkApprovedCRLs(cert, approvedCRLs);
                    } else if (allowSeparateKey) {
                        try {
                            verifyWithSeparateSigningKey(cert, prevKey, signFlag, stackedCerts);
                            return;
                        } catch (CertPathValidatorException cpve) {
                            if (networkFailureException != null) {
                                throw networkFailureException;
                            }
                            throw cpve;
                        }
                    } else if (networkFailureException != null) {
                        throw networkFailureException;
                    } else {
                        throw new CertPathValidatorException("Could not determine revocation status", null, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
                    }
                } catch (Throwable e2) {
                    if ((e2 instanceof CertStoreTypeException) && CertStoreHelper.isCausedByNetworkIssue(((CertStoreTypeException) e2).getType(), e2)) {
                        throw new CertPathValidatorException("Unable to determine revocation status due to network error", e2, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
                    }
                    throw new CertPathValidatorException(e2);
                }
            }
            checkApprovedCRLs(cert, approvedCRLs);
            return;
        }
        if (debug != null) {
            debug.println("RevocationChecker.checkCRLs() circular dependency");
        }
        throw new CertPathValidatorException("Could not determine revocation status", null, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
    }

    private void checkApprovedCRLs(X509Certificate cert, Set<X509CRL> approvedCRLs) throws CertPathValidatorException {
        if (debug != null) {
            BigInteger sn = cert.getSerialNumber();
            debug.println("RevocationChecker.checkApprovedCRLs() starting the final sweep...");
            debug.println("RevocationChecker.checkApprovedCRLs() cert SN: " + sn.toString());
        }
        CRLReason reasonCode = CRLReason.UNSPECIFIED;
        for (X509CRL crl : approvedCRLs) {
            X509CRLEntry e = crl.getRevokedCertificate(cert);
            if (e != null) {
                try {
                    X509CRLEntryImpl entry = X509CRLEntryImpl.toImpl(e);
                    if (debug != null) {
                        debug.println("RevocationChecker.checkApprovedCRLs() CRL entry: " + entry.toString());
                    }
                    Set<String> unresCritExts = entry.getCriticalExtensionOIDs();
                    if (!(unresCritExts == null || (unresCritExts.isEmpty() ^ 1) == 0)) {
                        unresCritExts.remove(PKIXExtensions.ReasonCode_Id.toString());
                        unresCritExts.remove(PKIXExtensions.CertificateIssuer_Id.toString());
                        if (!unresCritExts.isEmpty()) {
                            throw new CertPathValidatorException("Unrecognized critical extension(s) in revoked CRL entry");
                        }
                    }
                    reasonCode = entry.getRevocationReason();
                    if (reasonCode == null) {
                        reasonCode = CRLReason.UNSPECIFIED;
                    }
                    Date revocationDate = entry.getRevocationDate();
                    if (revocationDate.before(this.params.date())) {
                        Throwable t = new CertificateRevokedException(revocationDate, reasonCode, crl.getIssuerX500Principal(), entry.getExtensions());
                        throw new CertPathValidatorException(t.getMessage(), t, null, -1, BasicReason.REVOKED);
                    }
                } catch (Throwable ce) {
                    throw new CertPathValidatorException(ce);
                }
            }
        }
    }

    private void checkOCSP(X509Certificate cert, Collection<String> collection) throws CertPathValidatorException {
        IOException e;
        try {
            X509CertImpl currCert = X509CertImpl.toImpl(cert);
            OCSPResponse response;
            try {
                CertId certId;
                if (this.issuerCert != null) {
                    certId = new CertId(this.issuerCert, currCert.getSerialNumberObject());
                } else {
                    certId = new CertId(this.anchor.getCA(), this.anchor.getCAPublicKey(), currCert.getSerialNumberObject());
                }
                byte[] responseBytes = (byte[]) this.ocspResponses.get(cert);
                if (responseBytes != null) {
                    if (debug != null) {
                        debug.println("Found cached OCSP response");
                    }
                    response = new OCSPResponse(responseBytes);
                    byte[] nonce = null;
                    try {
                        for (Extension ext : this.ocspExtensions) {
                            if (ext.getId().equals("1.3.6.1.5.5.7.48.1.2")) {
                                nonce = ext.getValue();
                            }
                        }
                        response.verify(Collections.singletonList(certId), this.issuerCert, this.responderCert, this.params.date(), nonce);
                    } catch (IOException e2) {
                        e = e2;
                        throw new CertPathValidatorException("Unable to determine revocation status due to network error", e, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
                    }
                }
                URI responderURI;
                if (this.responderURI != null) {
                    responderURI = this.responderURI;
                } else {
                    responderURI = OCSP.getResponderURI(currCert);
                }
                if (responderURI == null) {
                    throw new CertPathValidatorException("Certificate does not specify OCSP responder", null, null, -1);
                }
                response = OCSP.check(Collections.singletonList(certId), responderURI, this.issuerCert, this.responderCert, null, this.ocspExtensions);
                RevocationStatus rs = response.getSingleResponse(certId);
                CertStatus certStatus = rs.getCertStatus();
                if (certStatus == CertStatus.REVOKED) {
                    Date revocationTime = rs.getRevocationTime();
                    if (revocationTime.before(this.params.date())) {
                        Throwable t = new CertificateRevokedException(revocationTime, rs.getRevocationReason(), response.getSignerCertificate().getSubjectX500Principal(), rs.getSingleExtensions());
                        throw new CertPathValidatorException(t.getMessage(), t, null, -1, BasicReason.REVOKED);
                    }
                } else if (certStatus == CertStatus.UNKNOWN) {
                    throw new CertPathValidatorException("Certificate's revocation status is unknown", null, this.params.certPath(), -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
                }
            } catch (IOException e3) {
                e = e3;
                response = null;
                throw new CertPathValidatorException("Unable to determine revocation status due to network error", e, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
            }
        } catch (Throwable ce) {
            throw new CertPathValidatorException(ce);
        }
    }

    private static String stripOutSeparators(String value) {
        char[] chars = value.toCharArray();
        StringBuilder hexNumber = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (HEX_DIGITS.indexOf(chars[i]) != -1) {
                hexNumber.append(chars[i]);
            }
        }
        return hexNumber.-java_util_stream_Collectors-mthref-7();
    }

    static boolean certCanSignCrl(X509Certificate cert) {
        boolean[] keyUsage = cert.getKeyUsage();
        if (keyUsage != null) {
            return keyUsage[6];
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x009a A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x009a A:{Splitter: B:0:0x0000, ExcHandler: java.security.cert.CertificateException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:17:0x009a, code:
            r14 = move-exception;
     */
    /* JADX WARNING: Missing block: B:19:0x009d, code:
            if (debug != null) goto L_0x009f;
     */
    /* JADX WARNING: Missing block: B:20:0x009f, code:
            debug.println("Exception while verifying CRL: " + r14.getMessage());
            r14.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:22:0x00c3, code:
            return java.util.Collections.emptySet();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Collection<X509CRL> verifyPossibleCRLs(Set<X509CRL> crls, X509Certificate cert, PublicKey prevKey, boolean signFlag, boolean[] reasonsMask, Set<TrustAnchor> anchors) throws CertPathValidatorException {
        try {
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            if (debug != null) {
                debug.println("RevocationChecker.verifyPossibleCRLs: Checking CRLDPs for " + certImpl.getSubjectX500Principal());
            }
            CRLDistributionPointsExtension ext = certImpl.getCRLDistributionPointsExtension();
            List<DistributionPoint> points;
            if (ext == null) {
                points = Collections.singletonList(new DistributionPoint(new GeneralNames().add(new GeneralName((X500Name) certImpl.getIssuerDN())), null, null));
            } else {
                points = ext.get(CRLDistributionPointsExtension.POINTS);
            }
            Set<X509CRL> results = new HashSet();
            for (DistributionPoint point : points) {
                for (X509CRL crl : crls) {
                    if (DistributionPointFetcher.verifyCRL(certImpl, point, crl, reasonsMask, signFlag, prevKey, null, this.params.sigProvider(), anchors, this.certStores, this.params.date())) {
                        results.-java_util_stream_Collectors-mthref-4(crl);
                    }
                }
                if (Arrays.equals(reasonsMask, ALL_REASONS)) {
                    break;
                }
            }
            return results;
        } catch (Exception e) {
        }
    }

    private void verifyWithSeparateSigningKey(X509Certificate cert, PublicKey prevKey, boolean signFlag, Set<X509Certificate> stackedCerts) throws CertPathValidatorException {
        String msg = "revocation status";
        if (debug != null) {
            debug.println("RevocationChecker.verifyWithSeparateSigningKey() ---checking " + msg + "...");
        }
        if (stackedCerts != null && stackedCerts.contains(cert)) {
            if (debug != null) {
                debug.println("RevocationChecker.verifyWithSeparateSigningKey() circular dependency");
            }
            throw new CertPathValidatorException("Could not determine revocation status", null, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
        } else if (signFlag) {
            buildToNewKey(cert, prevKey, stackedCerts);
        } else {
            buildToNewKey(cert, null, stackedCerts);
        }
    }

    private void buildToNewKey(X509Certificate currCert, PublicKey prevKey, Set<X509Certificate> stackedCerts) throws CertPathValidatorException {
        Set newAnchors;
        Throwable iape;
        if (debug != null) {
            debug.println("RevocationChecker.buildToNewKey() starting work");
        }
        Set<PublicKey> badKeys = new HashSet();
        if (prevKey != null) {
            badKeys.-java_util_stream_Collectors-mthref-4(prevKey);
        }
        X509CertSelector rejectKeySelector = new RejectKeySelector(badKeys);
        rejectKeySelector.setSubject(currCert.getIssuerX500Principal());
        rejectKeySelector.setKeyUsage(CRL_SIGN_USAGE);
        if (this.anchor == null) {
            newAnchors = this.params.trustAnchors();
        } else {
            newAnchors = Collections.singleton(this.anchor);
        }
        try {
            PKIXBuilderParameters pKIXBuilderParameters = new PKIXBuilderParameters(newAnchors, (CertSelector) rejectKeySelector);
            pKIXBuilderParameters.setInitialPolicies(this.params.initialPolicies());
            pKIXBuilderParameters.setCertStores(this.certStores);
            pKIXBuilderParameters.setExplicitPolicyRequired(this.params.explicitPolicyRequired());
            pKIXBuilderParameters.setPolicyMappingInhibited(this.params.policyMappingInhibited());
            pKIXBuilderParameters.setAnyPolicyInhibited(this.params.anyPolicyInhibited());
            pKIXBuilderParameters.setDate(this.params.date());
            pKIXBuilderParameters.setCertPathCheckers(this.params.getPKIXParameters().getCertPathCheckers());
            pKIXBuilderParameters.setSigProvider(this.params.sigProvider());
            pKIXBuilderParameters.setRevocationEnabled(false);
            if (Builder.USE_AIA) {
                X509CertImpl currCertImpl = null;
                try {
                    currCertImpl = X509CertImpl.toImpl(currCert);
                } catch (CertificateException ce) {
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
                                pKIXBuilderParameters.addCertStore(cs);
                            }
                        }
                    }
                }
            }
            try {
                CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
                while (true) {
                    PKIXCertPathBuilderResult cpbr;
                    PublicKey prevKey2;
                    boolean signFlag;
                    List<? extends Certificate> cpList;
                    while (true) {
                        Set<X509Certificate> stackedCerts2;
                        try {
                            stackedCerts2 = stackedCerts;
                            if (debug != null) {
                                debug.println("RevocationChecker.buildToNewKey() about to try build ...");
                            }
                            cpbr = (PKIXCertPathBuilderResult) builder.build(pKIXBuilderParameters);
                            if (debug != null) {
                                debug.println("RevocationChecker.buildToNewKey() about to check revocation ...");
                            }
                            if (stackedCerts2 == null) {
                                stackedCerts = new HashSet();
                            } else {
                                stackedCerts = stackedCerts2;
                            }
                            try {
                                stackedCerts.-java_util_stream_Collectors-mthref-4(currCert);
                                TrustAnchor ta = cpbr.getTrustAnchor();
                                prevKey2 = ta.getCAPublicKey();
                                if (prevKey2 == null) {
                                    prevKey2 = ta.getTrustedCert().getPublicKey();
                                }
                                signFlag = true;
                                cpList = cpbr.getCertPath().getCertificates();
                                try {
                                    break;
                                } catch (CertPathValidatorException e) {
                                    badKeys.-java_util_stream_Collectors-mthref-4(cpbr.getPublicKey());
                                }
                            } catch (CertPathValidatorException cpve) {
                                if (cpve.getReason() == BasicReason.REVOKED) {
                                    throw cpve;
                                }
                                badKeys.-java_util_stream_Collectors-mthref-4(newKey);
                            } catch (InvalidAlgorithmParameterException e2) {
                                iape = e2;
                            } catch (CertPathBuilderException e3) {
                            }
                        } catch (InvalidAlgorithmParameterException e4) {
                            iape = e4;
                            stackedCerts = stackedCerts2;
                            throw new CertPathValidatorException(iape);
                        } catch (CertPathBuilderException e5) {
                            stackedCerts = stackedCerts2;
                            throw new CertPathValidatorException("Could not determine revocation status", null, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
                        }
                    }
                    for (int i = cpList.size() - 1; i >= 0; i--) {
                        Object cert = (X509Certificate) cpList.get(i);
                        if (debug != null) {
                            debug.println("RevocationChecker.buildToNewKey() index " + i + " checking " + cert);
                        }
                        checkCRLs(cert, prevKey2, null, signFlag, true, stackedCerts, newAnchors);
                        signFlag = certCanSignCrl(cert);
                        prevKey2 = cert.getPublicKey();
                    }
                    if (debug != null) {
                        debug.println("RevocationChecker.buildToNewKey() got key " + cpbr.getPublicKey());
                    }
                    PublicKey newKey = cpbr.getPublicKey();
                    checkCRLs(currCert, newKey, cpList.isEmpty() ? null : (X509Certificate) cpList.get(0), true, false, null, this.params.trustAnchors());
                    return;
                }
            } catch (Throwable nsae) {
                throw new CertPathValidatorException(nsae);
            }
        } catch (Throwable iape2) {
            throw new RuntimeException(iape2);
        }
    }

    public RevocationChecker clone() {
        RevocationChecker copy = (RevocationChecker) super.clone();
        copy.softFailExceptions = new LinkedList(this.softFailExceptions);
        return copy;
    }
}
