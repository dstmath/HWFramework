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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.certpath.PKIX;
import sun.security.util.Debug;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;

class ForwardBuilder extends Builder {
    /* access modifiers changed from: private */
    public static final Debug debug = Debug.getInstance("certpath");
    private AdaptableX509CertSelector caSelector;
    private X509CertSelector caTargetSelector;
    private X509CertSelector eeSelector;
    private boolean searchAllCertStores = true;
    TrustAnchor trustAnchor;
    private final Set<TrustAnchor> trustAnchors;
    private final Set<X509Certificate> trustedCerts;
    private final Set<X500Principal> trustedSubjectDNs;

    static class PKIXCertComparator implements Comparator<X509Certificate> {
        static final String METHOD_NME = "PKIXCertComparator.compare()";
        private final X509CertSelector certSkidSelector;
        private final Set<X500Principal> trustedSubjectDNs;

        PKIXCertComparator(Set<X500Principal> trustedSubjectDNs2, X509CertImpl previousCert) throws IOException {
            this.trustedSubjectDNs = trustedSubjectDNs2;
            this.certSkidSelector = getSelector(previousCert);
        }

        private X509CertSelector getSelector(X509CertImpl previousCert) throws IOException {
            if (previousCert != null) {
                AuthorityKeyIdentifierExtension akidExt = previousCert.getAuthorityKeyIdentifierExtension();
                if (akidExt != null) {
                    byte[] skid = akidExt.getEncodedKeyIdentifier();
                    if (skid != null) {
                        X509CertSelector selector = new X509CertSelector();
                        selector.setSubjectKeyIdentifier(skid);
                        return selector;
                    }
                }
            }
            return null;
        }

        public int compare(X509Certificate oCert1, X509Certificate oCert2) {
            Iterator<X500Principal> it;
            X500Principal cIssuer1;
            if (oCert1.equals(oCert2)) {
                return 0;
            }
            int i = -1;
            if (this.certSkidSelector == null) {
                X509Certificate x509Certificate = oCert1;
                X509Certificate x509Certificate2 = oCert2;
            } else if (this.certSkidSelector.match(oCert1)) {
                return -1;
            } else {
                if (this.certSkidSelector.match(oCert2)) {
                    return 1;
                }
            }
            X500Principal cIssuer12 = oCert1.getIssuerX500Principal();
            X500Principal cIssuer2 = oCert2.getIssuerX500Principal();
            X500Name cIssuer1Name = X500Name.asX500Name(cIssuer12);
            X500Name cIssuer2Name = X500Name.asX500Name(cIssuer2);
            if (ForwardBuilder.debug != null) {
                Debug access$000 = ForwardBuilder.debug;
                access$000.println("PKIXCertComparator.compare() o1 Issuer:  " + cIssuer12);
                Debug access$0002 = ForwardBuilder.debug;
                access$0002.println("PKIXCertComparator.compare() o2 Issuer:  " + cIssuer2);
            }
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() MATCH TRUSTED SUBJECT TEST...");
            }
            boolean m1 = this.trustedSubjectDNs.contains(cIssuer12);
            boolean m2 = this.trustedSubjectDNs.contains(cIssuer2);
            if (ForwardBuilder.debug != null) {
                Debug access$0003 = ForwardBuilder.debug;
                access$0003.println("PKIXCertComparator.compare() m1: " + m1);
                Debug access$0004 = ForwardBuilder.debug;
                access$0004.println("PKIXCertComparator.compare() m2: " + m2);
            }
            if ((m1 && m2) || m1) {
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
                int distanceTto1 = Builder.distance(tSubjectName, cIssuer1Name, i);
                int distanceTto2 = Builder.distance(tSubjectName, cIssuer2Name, i);
                if (ForwardBuilder.debug != null) {
                    Debug access$0005 = ForwardBuilder.debug;
                    StringBuilder sb = new StringBuilder();
                    cIssuer1 = cIssuer12;
                    sb.append("PKIXCertComparator.compare() distanceTto1: ");
                    sb.append(distanceTto1);
                    access$0005.println(sb.toString());
                    Debug access$0006 = ForwardBuilder.debug;
                    access$0006.println("PKIXCertComparator.compare() distanceTto2: " + distanceTto2);
                } else {
                    cIssuer1 = cIssuer12;
                }
                if (distanceTto1 <= 0 && distanceTto2 <= 0) {
                    cIssuer12 = cIssuer1;
                    i = -1;
                } else if (distanceTto1 == distanceTto2) {
                    return -1;
                } else {
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
            }
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() NAMING ANCESTOR TEST...");
            }
            for (X500Principal tSubject2 : this.trustedSubjectDNs) {
                X500Name tSubjectName2 = X500Name.asX500Name(tSubject2);
                int distanceTto12 = Builder.distance(tSubjectName2, cIssuer1Name, Integer.MAX_VALUE);
                int distanceTto22 = Builder.distance(tSubjectName2, cIssuer2Name, Integer.MAX_VALUE);
                if (ForwardBuilder.debug != null) {
                    Debug access$0007 = ForwardBuilder.debug;
                    access$0007.println("PKIXCertComparator.compare() distanceTto1: " + distanceTto12);
                    Debug access$0008 = ForwardBuilder.debug;
                    access$0008.println("PKIXCertComparator.compare() distanceTto2: " + distanceTto22);
                }
                if (distanceTto12 >= 0) {
                    if (distanceTto22 < 0) {
                    }
                }
                if (distanceTto12 == distanceTto22) {
                    return -1;
                }
                if (distanceTto12 < 0 && distanceTto22 >= 0) {
                    return -1;
                }
                if (distanceTto12 >= 0 && distanceTto22 < 0) {
                    return 1;
                }
                if (distanceTto12 > distanceTto22) {
                    return -1;
                }
                return 1;
            }
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() SAME NAMESPACE AS TRUSTED TEST...");
            }
            Iterator<X500Principal> it2 = this.trustedSubjectDNs.iterator();
            while (it2.hasNext()) {
                X500Name tSubjectName3 = X500Name.asX500Name(it2.next());
                X500Name tAo1 = tSubjectName3.commonAncestor(cIssuer1Name);
                X500Name tAo2 = tSubjectName3.commonAncestor(cIssuer2Name);
                if (ForwardBuilder.debug != null) {
                    Debug access$0009 = ForwardBuilder.debug;
                    access$0009.println("PKIXCertComparator.compare() tAo1: " + String.valueOf((Object) tAo1));
                    Debug access$00010 = ForwardBuilder.debug;
                    access$00010.println("PKIXCertComparator.compare() tAo2: " + String.valueOf((Object) tAo2));
                }
                if (tAo1 == null && tAo2 == null) {
                    it = it2;
                } else if (tAo1 == null || tAo2 == null) {
                    return tAo1 == null ? 1 : -1;
                } else {
                    int hopsTto1 = Builder.hops(tSubjectName3, cIssuer1Name, Integer.MAX_VALUE);
                    int hopsTto2 = Builder.hops(tSubjectName3, cIssuer2Name, Integer.MAX_VALUE);
                    if (ForwardBuilder.debug != null) {
                        Debug access$00011 = ForwardBuilder.debug;
                        StringBuilder sb2 = new StringBuilder();
                        it = it2;
                        sb2.append("PKIXCertComparator.compare() hopsTto1: ");
                        sb2.append(hopsTto1);
                        access$00011.println(sb2.toString());
                        Debug access$00012 = ForwardBuilder.debug;
                        access$00012.println("PKIXCertComparator.compare() hopsTto2: " + hopsTto2);
                    } else {
                        it = it2;
                    }
                    if (hopsTto1 != hopsTto2) {
                        if (hopsTto1 > hopsTto2) {
                            return 1;
                        }
                        return -1;
                    }
                }
                it2 = it;
            }
            if (ForwardBuilder.debug != null) {
                ForwardBuilder.debug.println("PKIXCertComparator.compare() CERT ISSUER/SUBJECT COMPARISON TEST...");
            }
            X500Principal cSubject1 = oCert1.getSubjectX500Principal();
            X500Principal cSubject2 = oCert2.getSubjectX500Principal();
            X500Name cSubject1Name = X500Name.asX500Name(cSubject1);
            X500Name cSubject2Name = X500Name.asX500Name(cSubject2);
            if (ForwardBuilder.debug != null) {
                Debug access$00013 = ForwardBuilder.debug;
                access$00013.println("PKIXCertComparator.compare() o1 Subject: " + cSubject1);
                Debug access$00014 = ForwardBuilder.debug;
                access$00014.println("PKIXCertComparator.compare() o2 Subject: " + cSubject2);
            }
            int distanceStoI1 = Builder.distance(cSubject1Name, cIssuer1Name, Integer.MAX_VALUE);
            int distanceStoI2 = Builder.distance(cSubject2Name, cIssuer2Name, Integer.MAX_VALUE);
            if (ForwardBuilder.debug != null) {
                Debug access$00015 = ForwardBuilder.debug;
                access$00015.println("PKIXCertComparator.compare() distanceStoI1: " + distanceStoI1);
                Debug access$00016 = ForwardBuilder.debug;
                access$00016.println("PKIXCertComparator.compare() distanceStoI2: " + distanceStoI2);
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

    ForwardBuilder(PKIX.BuilderParams buildParams, boolean searchAllCertStores2) {
        super(buildParams);
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
        this.searchAllCertStores = searchAllCertStores2;
    }

    /* access modifiers changed from: package-private */
    public Collection<X509Certificate> getMatchingCerts(State currentState, List<CertStore> certStores) throws CertStoreException, CertificateException, IOException {
        if (debug != null) {
            debug.println("ForwardBuilder.getMatchingCerts()...");
        }
        ForwardState currState = (ForwardState) currentState;
        Set<X509Certificate> certs = new TreeSet<>((Comparator<? super X509Certificate>) new PKIXCertComparator(this.trustedSubjectDNs, currState.cert));
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
            this.caSelector.setValidityPeriod(currentState.cert.getNotBefore(), currentState.cert.getNotAfter());
            sel = this.caSelector;
        } else if (this.targetCertConstraints.getBasicConstraints() != -2) {
            if (debug != null) {
                debug.println("ForwardBuilder.getMatchingCACerts(): the target is a CA");
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
                    Debug debug2 = debug;
                    debug2.println("ForwardBuilder.getMatchingCACerts: found matching trust anchor.\n  SN: " + Debug.toHexString(trustedCert.getSerialNumber()) + "\n  Subject: " + trustedCert.getSubjectX500Principal() + "\n  Issuer: " + trustedCert.getIssuerX500Principal());
                }
                if (caCerts.add(trustedCert) && !this.searchAllCertStores) {
                    return;
                }
            }
        }
        sel.setCertificateValid(this.buildParams.date());
        sel.setBasicConstraints(currentState.traversedCACerts);
        if ((!currentState.isInitial() && this.buildParams.maxPathLength() != -1 && this.buildParams.maxPathLength() <= currentState.traversedCACerts) || !addMatchingCerts(sel, certStores, caCerts, this.searchAllCertStores) || this.searchAllCertStores) {
            if (!currentState.isInitial() && Builder.USE_AIA) {
                AuthorityInfoAccessExtension aiaExt = currentState.cert.getAuthorityInfoAccessExtension();
                if (aiaExt != null) {
                    getCerts(aiaExt, caCerts);
                }
            }
            if (debug != null) {
                Debug debug3 = debug;
                debug3.println("ForwardBuilder.getMatchingCACerts: found " + (caCerts.size() - initialSize) + " CA certs");
            }
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

    /* access modifiers changed from: package-private */
    public void verifyCert(X509Certificate cert, State currentState, List<X509Certificate> certPathList) throws GeneralSecurityException {
        if (debug != null) {
            Debug debug2 = debug;
            debug2.println("ForwardBuilder.verifyCert(SN: " + Debug.toHexString(cert.getSerialNumber()) + "\n  Issuer: " + cert.getIssuerX500Principal() + ")\n  Subject: " + cert.getSubjectX500Principal() + ")");
        }
        ForwardState currState = (ForwardState) currentState;
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
            Iterator<PKIXCertPathChecker> it = currState.forwardCheckers.iterator();
            while (it.hasNext()) {
                it.next().check(cert, unresCritExts);
            }
            for (PKIXCertPathChecker checker : this.buildParams.certPathCheckers()) {
                if (!checker.isForwardCheckingSupported()) {
                    Set<String> supportedExts = checker.getSupportedExtensions();
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
                    CertPathValidatorException certPathValidatorException = new CertPathValidatorException("Unrecognized critical extension(s)", null, null, -1, PKIXReason.UNRECOGNIZED_CRIT_EXT);
                    throw certPathValidatorException;
                }
            }
        }
        if (!currState.isInitial()) {
            if (!isTrustedCert) {
                if (cert.getBasicConstraints() != -1) {
                    KeyChecker.verifyCAKeyUsage(cert);
                } else {
                    throw new CertificateException("cert is NOT a CA cert");
                }
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

    /* access modifiers changed from: package-private */
    public boolean isPathCompleted(X509Certificate cert) {
        List<TrustAnchor> otherAnchors = new ArrayList<>();
        for (TrustAnchor anchor : this.trustAnchors) {
            if (anchor.getTrustedCert() == null) {
                X500Principal principal = anchor.getCA();
                PublicKey publicKey = anchor.getCAPublicKey();
                if (principal == null || publicKey == null || !principal.equals(cert.getSubjectX500Principal()) || !publicKey.equals(cert.getPublicKey())) {
                    otherAnchors.add(anchor);
                } else {
                    this.trustAnchor = anchor;
                    return true;
                }
            } else if (cert.equals(anchor.getTrustedCert())) {
                this.trustAnchor = anchor;
                return true;
            }
        }
        for (TrustAnchor anchor2 : otherAnchors) {
            X500Principal principal2 = anchor2.getCA();
            PublicKey publicKey2 = anchor2.getCAPublicKey();
            if (principal2 != null && principal2.equals(cert.getIssuerX500Principal()) && !PKIX.isDSAPublicKeyWithoutParams(publicKey2)) {
                try {
                    if (this.buildParams.sigProvider() != null) {
                        cert.verify(publicKey2, this.buildParams.sigProvider());
                    } else {
                        cert.verify(publicKey2);
                    }
                    this.trustAnchor = anchor2;
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
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void addCertToPath(X509Certificate cert, LinkedList<X509Certificate> certPathList) {
        certPathList.addFirst(cert);
    }

    /* access modifiers changed from: package-private */
    public void removeFinalCertFromPath(LinkedList<X509Certificate> certPathList) {
        certPathList.removeFirst();
    }
}
