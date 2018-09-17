package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X500Name;
import sun.util.logging.PlatformLogger;

class ForwardBuilder extends Builder {
    private static final Debug debug = null;
    private AdaptableX509CertSelector caSelector;
    private X509CertSelector caTargetSelector;
    private Comparator<X509Certificate> comparator;
    private X509CertSelector eeSelector;
    private boolean searchAllCertStores;
    TrustAnchor trustAnchor;
    private final Set<TrustAnchor> trustAnchors;
    private final Set<X509Certificate> trustedCerts;
    private final Set<X500Principal> trustedSubjectDNs;

    static class PKIXCertComparator implements Comparator<X509Certificate> {
        static final String METHOD_NME = "PKIXCertComparator.compare()";
        private final Set<X500Principal> trustedSubjectDNs;

        PKIXCertComparator(Set<X500Principal> trustedSubjectDNs) {
            this.trustedSubjectDNs = trustedSubjectDNs;
        }

        public int compare(X509Certificate oCert1, X509Certificate oCert2) {
            if (oCert1.equals(oCert2)) {
                return 0;
            }
            Object cIssuer1 = oCert1.getIssuerX500Principal();
            Object cIssuer2 = oCert2.getIssuerX500Principal();
            X500Name cIssuer1Name = X500Name.asX500Name(cIssuer1);
            X500Name cIssuer2Name = X500Name.asX500Name(cIssuer2);
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() o1 Issuer:  " + cIssuer1);
                ForwardBuilder.debug.println("PKIXCertComparator.compare() o2 Issuer:  " + cIssuer2);
            }
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() MATCH TRUSTED SUBJECT TEST...");
            }
            boolean m1 = this.trustedSubjectDNs.contains(cIssuer1);
            boolean m2 = this.trustedSubjectDNs.contains(cIssuer2);
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() m1: " + m1);
                ForwardBuilder.debug.println("PKIXCertComparator.compare() m2: " + m2);
            }
            if (m1 && m2) {
                return -1;
            }
            if (m1) {
                return -1;
            }
            if (m2) {
                return 1;
            }
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() NAMING DESCENDANT TEST...");
            }
            for (X500Principal tSubject : this.trustedSubjectDNs) {
                X500Name tSubjectName = X500Name.asX500Name(tSubject);
                int distanceTto1 = Builder.distance(tSubjectName, cIssuer1Name, -1);
                int distanceTto2 = Builder.distance(tSubjectName, cIssuer2Name, -1);
                if (ForwardBuilder.debug != null) {
                    ForwardBuilder.debug.println("PKIXCertComparator.compare() distanceTto1: " + distanceTto1);
                    ForwardBuilder.debug.println("PKIXCertComparator.compare() distanceTto2: " + distanceTto2);
                }
                if (distanceTto1 <= 0) {
                    if (distanceTto2 > 0) {
                    }
                }
                if (distanceTto1 == distanceTto2) {
                    return -1;
                }
                if (distanceTto1 > 0 && distanceTto2 <= 0) {
                    return -1;
                }
                if (distanceTto1 <= 0 && distanceTto2 > 0) {
                    return 1;
                }
                if (distanceTto1 < distanceTto2) {
                    return -1;
                }
                return 1;
            }
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() NAMING ANCESTOR TEST...");
            }
            for (X500Principal tSubject2 : this.trustedSubjectDNs) {
                tSubjectName = X500Name.asX500Name(tSubject2);
                distanceTto1 = Builder.distance(tSubjectName, cIssuer1Name, PlatformLogger.OFF);
                distanceTto2 = Builder.distance(tSubjectName, cIssuer2Name, PlatformLogger.OFF);
                if (ForwardBuilder.debug != null) {
                    ForwardBuilder.debug.println("PKIXCertComparator.compare() distanceTto1: " + distanceTto1);
                    ForwardBuilder.debug.println("PKIXCertComparator.compare() distanceTto2: " + distanceTto2);
                }
                if (distanceTto1 >= 0) {
                    if (distanceTto2 < 0) {
                    }
                }
                if (distanceTto1 == distanceTto2) {
                    return -1;
                }
                if (distanceTto1 < 0 && distanceTto2 >= 0) {
                    return -1;
                }
                if (distanceTto1 >= 0 && distanceTto2 < 0) {
                    return 1;
                }
                if (distanceTto1 > distanceTto2) {
                    return -1;
                }
                return 1;
            }
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() SAME NAMESPACE AS TRUSTED TEST...");
            }
            for (X500Principal tSubject22 : this.trustedSubjectDNs) {
                tSubjectName = X500Name.asX500Name(tSubject22);
                Object tAo1 = tSubjectName.commonAncestor(cIssuer1Name);
                Object tAo2 = tSubjectName.commonAncestor(cIssuer2Name);
                if (ForwardBuilder.debug != null) {
                    ForwardBuilder.debug.println("PKIXCertComparator.compare() tAo1: " + String.valueOf(tAo1));
                    ForwardBuilder.debug.println("PKIXCertComparator.compare() tAo2: " + String.valueOf(tAo2));
                }
                if (tAo1 != null || tAo2 != null) {
                    if (tAo1 != null && tAo2 != null) {
                        int hopsTto1 = Builder.hops(tSubjectName, cIssuer1Name, PlatformLogger.OFF);
                        int hopsTto2 = Builder.hops(tSubjectName, cIssuer2Name, PlatformLogger.OFF);
                        if (ForwardBuilder.debug != null) {
                            ForwardBuilder.debug.println("PKIXCertComparator.compare() hopsTto1: " + hopsTto1);
                            ForwardBuilder.debug.println("PKIXCertComparator.compare() hopsTto2: " + hopsTto2);
                        }
                        if (hopsTto1 != hopsTto2) {
                            if (hopsTto1 > hopsTto2) {
                                return 1;
                            }
                            return -1;
                        }
                    } else if (tAo1 == null) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() CERT ISSUER/SUBJECT COMPARISON TEST...");
            }
            Object cSubject1 = oCert1.getSubjectX500Principal();
            Object cSubject2 = oCert2.getSubjectX500Principal();
            X500Name cSubject1Name = X500Name.asX500Name(cSubject1);
            X500Name cSubject2Name = X500Name.asX500Name(cSubject2);
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() o1 Subject: " + cSubject1);
                ForwardBuilder.debug.println("PKIXCertComparator.compare() o2 Subject: " + cSubject2);
            }
            int distanceStoI1 = Builder.distance(cSubject1Name, cIssuer1Name, PlatformLogger.OFF);
            int distanceStoI2 = Builder.distance(cSubject2Name, cIssuer2Name, PlatformLogger.OFF);
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() distanceStoI1: " + distanceStoI1);
                ForwardBuilder.debug.println("PKIXCertComparator.compare() distanceStoI2: " + distanceStoI2);
            }
            if (distanceStoI2 > distanceStoI1) {
                return -1;
            }
            if (distanceStoI2 < distanceStoI1) {
                return 1;
            }
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() no tests matched; RETURN 0");
            }
            return -1;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.ForwardBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.ForwardBuilder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.ForwardBuilder.<clinit>():void");
    }

    ForwardBuilder(BuilderParams buildParams, boolean searchAllCertStores) {
        super(buildParams);
        this.searchAllCertStores = true;
        this.trustAnchors = buildParams.trustAnchors();
        this.trustedCerts = new HashSet(this.trustAnchors.size());
        this.trustedSubjectDNs = new HashSet(this.trustAnchors.size());
        for (TrustAnchor anchor : this.trustAnchors) {
            X509Certificate trustedCert = anchor.getTrustedCert();
            if (trustedCert != null) {
                this.trustedCerts.add(trustedCert);
                this.trustedSubjectDNs.add(trustedCert.getSubjectX500Principal());
            } else {
                this.trustedSubjectDNs.add(anchor.getCA());
            }
        }
        this.comparator = new PKIXCertComparator(this.trustedSubjectDNs);
        this.searchAllCertStores = searchAllCertStores;
    }

    Collection<X509Certificate> getMatchingCerts(State currentState, List<CertStore> certStores) throws CertStoreException, CertificateException, IOException {
        if (debug != null) {
            debug.println("ForwardBuilder.getMatchingCerts()...");
        }
        ForwardState currState = (ForwardState) currentState;
        Set<X509Certificate> certs = new TreeSet(this.comparator);
        if (currState.isInitial()) {
            getMatchingEECerts(currState, certStores, certs);
        }
        getMatchingCACerts(currState, certStores, certs);
        return certs;
    }

    private void getMatchingEECerts(ForwardState currentState, List<CertStore> certStores, Collection<X509Certificate> eeCerts) throws IOException {
        if (debug != null) {
            debug.println("ForwardBuilder.getMatchingEECerts()...");
        }
        if (this.eeSelector == null) {
            this.eeSelector = (X509CertSelector) this.targetCertConstraints.clone();
            this.eeSelector.setCertificateValid(this.buildParams.date());
            if (this.buildParams.explicitPolicyRequired()) {
                this.eeSelector.setPolicy(getMatchingPolicies());
            }
            this.eeSelector.setBasicConstraints(-2);
        }
        addMatchingCerts(this.eeSelector, certStores, eeCerts, this.searchAllCertStores);
    }

    private void getMatchingCACerts(ForwardState currentState, List<CertStore> certStores, Collection<X509Certificate> caCerts) throws IOException {
        X509CertSelector sel;
        AuthorityInfoAccessExtension aiaExt;
        if (debug != null) {
            debug.println("ForwardBuilder.getMatchingCACerts()...");
        }
        int initialSize = caCerts.size();
        if (!currentState.isInitial()) {
            if (this.caSelector == null) {
                this.caSelector = new AdaptableX509CertSelector();
                if (this.buildParams.explicitPolicyRequired()) {
                    this.caSelector.setPolicy(getMatchingPolicies());
                }
            }
            this.caSelector.setSubject(currentState.issuerDN);
            CertPathHelper.setPathToNames(this.caSelector, currentState.subjectNamesTraversed);
            this.caSelector.parseAuthorityKeyIdentifierExtension(currentState.cert.getAuthorityKeyIdentifierExtension());
            this.caSelector.setValidityPeriod(currentState.cert.getNotBefore(), currentState.cert.getNotAfter());
            sel = this.caSelector;
        } else if (this.targetCertConstraints.getBasicConstraints() != -2) {
            if (debug != null) {
                debug.println("ForwardBuilder.getMatchingCACerts(): ca is target");
            }
            if (this.caTargetSelector == null) {
                this.caTargetSelector = (X509CertSelector) this.targetCertConstraints.clone();
                if (this.buildParams.explicitPolicyRequired()) {
                    this.caTargetSelector.setPolicy(getMatchingPolicies());
                }
            }
            sel = this.caTargetSelector;
        } else {
            return;
        }
        sel.setBasicConstraints(-1);
        for (X509Certificate trustedCert : this.trustedCerts) {
            if (sel.match(trustedCert)) {
                if (debug != null) {
                    debug.println("ForwardBuilder.getMatchingCACerts: found matching trust anchor");
                }
                if (caCerts.add(trustedCert) && !this.searchAllCertStores) {
                    return;
                }
            }
        }
        sel.setCertificateValid(this.buildParams.date());
        sel.setBasicConstraints(currentState.traversedCACerts);
        if (!(currentState.isInitial() || this.buildParams.maxPathLength() == -1)) {
            if (this.buildParams.maxPathLength() > currentState.traversedCACerts) {
            }
            if (!currentState.isInitial() && Builder.USE_AIA) {
                aiaExt = currentState.cert.getAuthorityInfoAccessExtension();
                if (aiaExt != null) {
                    getCerts(aiaExt, caCerts);
                }
            }
            if (debug != null) {
                debug.println("ForwardBuilder.getMatchingCACerts: found " + (caCerts.size() - initialSize) + " CA certs");
            }
        }
        if (addMatchingCerts(sel, certStores, caCerts, this.searchAllCertStores) && !this.searchAllCertStores) {
            return;
        }
        aiaExt = currentState.cert.getAuthorityInfoAccessExtension();
        if (aiaExt != null) {
            getCerts(aiaExt, caCerts);
        }
        if (debug != null) {
            debug.println("ForwardBuilder.getMatchingCACerts: found " + (caCerts.size() - initialSize) + " CA certs");
        }
    }

    private boolean getCerts(AuthorityInfoAccessExtension aiaExt, Collection<X509Certificate> certs) {
        if (!Builder.USE_AIA) {
            return false;
        }
        List<AccessDescription> adList = aiaExt.getAccessDescriptions();
        if (adList == null || adList.isEmpty()) {
            return false;
        }
        boolean add = false;
        for (AccessDescription ad : adList) {
            CertStore cs = URICertStore.getInstance(ad);
            if (cs != null) {
                try {
                    if (certs.addAll(cs.getCertificates(this.caSelector))) {
                        add = true;
                        if (!this.searchAllCertStores) {
                            return true;
                        }
                    } else {
                        continue;
                    }
                } catch (CertStoreException cse) {
                    if (debug != null) {
                        debug.println("exception getting certs from CertStore:");
                        cse.printStackTrace();
                    }
                }
            }
        }
        return add;
    }

    void verifyCert(X509Certificate cert, State currentState, List<X509Certificate> certPathList) throws GeneralSecurityException {
        if (debug != null) {
            debug.println("ForwardBuilder.verifyCert(SN: " + Debug.toHexString(cert.getSerialNumber()) + "\n  Issuer: " + cert.getIssuerX500Principal() + ")" + "\n  Subject: " + cert.getSubjectX500Principal() + ")");
        }
        ForwardState currState = (ForwardState) currentState;
        currState.untrustedChecker.check(cert, Collections.emptySet());
        if (certPathList != null) {
            for (X509Certificate cpListCert : certPathList) {
                if (cert.equals(cpListCert)) {
                    if (debug != null) {
                        debug.println("loop detected!!");
                    }
                    throw new CertPathValidatorException("loop detected");
                }
            }
        }
        boolean isTrustedCert = this.trustedCerts.contains(cert);
        if (!isTrustedCert) {
            Set<String> unresCritExts = cert.getCriticalExtensionOIDs();
            if (unresCritExts == null) {
                unresCritExts = Collections.emptySet();
            }
            for (PKIXCertPathChecker checker : currState.forwardCheckers) {
                checker.check(cert, unresCritExts);
            }
            for (PKIXCertPathChecker checker2 : this.buildParams.certPathCheckers()) {
                if (!checker2.isForwardCheckingSupported()) {
                    Set<String> supportedExts = checker2.getSupportedExtensions();
                    if (supportedExts != null) {
                        unresCritExts.removeAll(supportedExts);
                    }
                }
            }
            if (!unresCritExts.isEmpty()) {
                unresCritExts.remove(PKIXExtensions.BasicConstraints_Id.toString());
                unresCritExts.remove(PKIXExtensions.NameConstraints_Id.toString());
                unresCritExts.remove(PKIXExtensions.CertificatePolicies_Id.toString());
                unresCritExts.remove(PKIXExtensions.PolicyMappings_Id.toString());
                unresCritExts.remove(PKIXExtensions.PolicyConstraints_Id.toString());
                unresCritExts.remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
                unresCritExts.remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
                unresCritExts.remove(PKIXExtensions.KeyUsage_Id.toString());
                unresCritExts.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
                if (!unresCritExts.isEmpty()) {
                    throw new CertPathValidatorException("Unrecognized critical extension(s)", null, null, -1, PKIXReason.UNRECOGNIZED_CRIT_EXT);
                }
            }
        }
        if (!currState.isInitial()) {
            if (!isTrustedCert) {
                if (cert.getBasicConstraints() == -1) {
                    throw new CertificateException("cert is NOT a CA cert");
                }
                KeyChecker.verifyCAKeyUsage(cert);
            }
            if (!currState.keyParamsNeeded()) {
                if (this.buildParams.sigProvider() != null) {
                    currState.cert.verify(cert.getPublicKey(), this.buildParams.sigProvider());
                } else {
                    currState.cert.verify(cert.getPublicKey());
                }
            }
        }
    }

    boolean isPathCompleted(X509Certificate cert) {
        for (TrustAnchor anchor : this.trustAnchors) {
            if (anchor.getTrustedCert() == null) {
                X500Principal principal = anchor.getCA();
                PublicKey publicKey = anchor.getCAPublicKey();
                if (principal != null && publicKey != null && principal.equals(cert.getSubjectX500Principal()) && publicKey.equals(cert.getPublicKey())) {
                    this.trustAnchor = anchor;
                    return true;
                } else if (!(principal == null || !principal.equals(cert.getIssuerX500Principal()) || PKIX.isDSAPublicKeyWithoutParams(publicKey))) {
                    try {
                        if (this.buildParams.sigProvider() != null) {
                            cert.verify(publicKey, this.buildParams.sigProvider());
                        } else {
                            cert.verify(publicKey);
                        }
                        this.trustAnchor = anchor;
                        return true;
                    } catch (InvalidKeyException e) {
                        if (debug != null) {
                            debug.println("ForwardBuilder.isPathCompleted() invalid DSA key found");
                        }
                    } catch (GeneralSecurityException e2) {
                        if (debug != null) {
                            debug.println("ForwardBuilder.isPathCompleted() unexpected exception");
                            e2.printStackTrace();
                        }
                    }
                }
            } else if (cert.equals(anchor.getTrustedCert())) {
                this.trustAnchor = anchor;
                return true;
            }
        }
        return false;
    }

    void addCertToPath(X509Certificate cert, LinkedList<X509Certificate> certPathList) {
        certPathList.addFirst(cert);
    }

    void removeFinalCertFromPath(LinkedList<X509Certificate> certPathList) {
        certPathList.removeFirst();
    }
}
