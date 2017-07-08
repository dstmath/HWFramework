package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;

class ReverseBuilder extends Builder {
    private Debug debug;
    private final Set<String> initPolicies;

    class PKIXCertComparator implements Comparator<X509Certificate> {
        private Debug debug;

        PKIXCertComparator() {
            this.debug = Debug.getInstance("certpath");
        }

        public int compare(X509Certificate cert1, X509Certificate cert2) {
            X500Principal targetSubject = ReverseBuilder.this.buildParams.targetSubject();
            if (cert1.getSubjectX500Principal().equals(targetSubject)) {
                return -1;
            }
            if (cert2.getSubjectX500Principal().equals(targetSubject)) {
                return 1;
            }
            try {
                X500Name targetSubjectName = X500Name.asX500Name(targetSubject);
                int targetDist1 = Builder.targetDistance(null, cert1, targetSubjectName);
                int targetDist2 = Builder.targetDistance(null, cert2, targetSubjectName);
                if (targetDist1 == targetDist2) {
                    return 0;
                }
                return (targetDist1 != -1 && targetDist1 < targetDist2) ? -1 : 1;
            } catch (IOException e) {
                if (this.debug != null) {
                    this.debug.println("IOException in call to Builder.targetDistance");
                    e.printStackTrace();
                }
                throw new ClassCastException("Invalid target subject distinguished name");
            }
        }
    }

    ReverseBuilder(BuilderParams buildParams) {
        super(buildParams);
        this.debug = Debug.getInstance("certpath");
        Set<String> initialPolicies = buildParams.initialPolicies();
        this.initPolicies = new HashSet();
        if (initialPolicies.isEmpty()) {
            this.initPolicies.add("2.5.29.32.0");
        } else {
            this.initPolicies.addAll(initialPolicies);
        }
    }

    Collection<X509Certificate> getMatchingCerts(State currState, List<CertStore> certStores) throws CertStoreException, CertificateException, IOException {
        ReverseState currentState = (ReverseState) currState;
        if (this.debug != null) {
            this.debug.println("In ReverseBuilder.getMatchingCerts.");
        }
        Collection<X509Certificate> certs = getMatchingEECerts(currentState, certStores);
        certs.addAll(getMatchingCACerts(currentState, certStores));
        return certs;
    }

    private Collection<X509Certificate> getMatchingEECerts(ReverseState currentState, List<CertStore> certStores) throws CertStoreException, CertificateException, IOException {
        X509CertSelector sel = (X509CertSelector) this.targetCertConstraints.clone();
        sel.setIssuer(currentState.subjectDN);
        sel.setCertificateValid(this.buildParams.date());
        if (currentState.explicitPolicy == 0) {
            sel.setPolicy(getMatchingPolicies());
        }
        sel.setBasicConstraints(-2);
        HashSet<X509Certificate> eeCerts = new HashSet();
        addMatchingCerts(sel, certStores, eeCerts, true);
        if (this.debug != null) {
            this.debug.println("ReverseBuilder.getMatchingEECerts got " + eeCerts.size() + " certs.");
        }
        return eeCerts;
    }

    private Collection<X509Certificate> getMatchingCACerts(ReverseState currentState, List<CertStore> certStores) throws CertificateException, CertStoreException, IOException {
        X509CertSelector sel = new X509CertSelector();
        sel.setIssuer(currentState.subjectDN);
        sel.setCertificateValid(this.buildParams.date());
        byte[] subject = this.targetCertConstraints.getSubjectAsBytes();
        if (subject != null) {
            sel.addPathToName(4, subject);
        } else {
            X509Certificate cert = this.targetCertConstraints.getCertificate();
            if (cert != null) {
                sel.addPathToName(4, cert.getSubjectX500Principal().getEncoded());
            }
        }
        if (currentState.explicitPolicy == 0) {
            sel.setPolicy(getMatchingPolicies());
        }
        sel.setBasicConstraints(0);
        ArrayList<X509Certificate> reverseCerts = new ArrayList();
        addMatchingCerts(sel, certStores, reverseCerts, true);
        Collections.sort(reverseCerts, new PKIXCertComparator());
        if (this.debug != null) {
            this.debug.println("ReverseBuilder.getMatchingCACerts got " + reverseCerts.size() + " certs.");
        }
        return reverseCerts;
    }

    void verifyCert(X509Certificate cert, State currState, List<X509Certificate> certPathList) throws GeneralSecurityException {
        if (this.debug != null) {
            this.debug.println("ReverseBuilder.verifyCert(SN: " + Debug.toHexString(cert.getSerialNumber()) + "\n  Subject: " + cert.getSubjectX500Principal() + ")");
        }
        ReverseState currentState = (ReverseState) currState;
        if (!currentState.isInitial()) {
            currentState.untrustedChecker.check(cert, Collections.emptySet());
            if (!(certPathList == null || certPathList.isEmpty())) {
                List<X509Certificate> reverseCertList = new ArrayList();
                for (X509Certificate c : certPathList) {
                    reverseCertList.add(0, c);
                }
                boolean policyMappingFound = false;
                for (X509Certificate cpListCert : reverseCertList) {
                    if (X509CertImpl.toImpl(cpListCert).getPolicyMappingsExtension() != null) {
                        policyMappingFound = true;
                    }
                    if (this.debug != null) {
                        this.debug.println("policyMappingFound = " + policyMappingFound);
                    }
                    if (cert.equals(cpListCert) && (this.buildParams.policyMappingInhibited() || !policyMappingFound)) {
                        if (this.debug != null) {
                            this.debug.println("loop detected!!");
                        }
                        throw new CertPathValidatorException("loop detected");
                    }
                }
            }
            boolean finalCert = cert.getSubjectX500Principal().equals(this.buildParams.targetSubject());
            boolean caCert = cert.getBasicConstraints() != -1;
            if (finalCert) {
                if (!this.targetCertConstraints.match(cert)) {
                    throw new CertPathValidatorException("target certificate constraints check failed");
                }
            } else if (!caCert) {
                throw new CertPathValidatorException("cert is NOT a CA cert");
            } else if (currentState.remainingCACerts > 0 || X509CertImpl.isSelfIssued(cert)) {
                KeyChecker.verifyCAKeyUsage(cert);
            } else {
                throw new CertPathValidatorException("pathLenConstraint violated, path too long", null, null, -1, PKIXReason.PATH_TOO_LONG);
            }
            if (this.buildParams.revocationEnabled() && currentState.revChecker != null) {
                currentState.revChecker.check(cert, Collections.emptySet());
            }
            if ((finalCert || !X509CertImpl.isSelfIssued(cert)) && currentState.nc != null) {
                try {
                    if (!currentState.nc.verify(cert)) {
                        throw new CertPathValidatorException("name constraints check failed", null, null, -1, PKIXReason.INVALID_NAME);
                    }
                } catch (Throwable ioe) {
                    throw new CertPathValidatorException(ioe);
                }
            }
            currentState.rootNode = PolicyChecker.processPolicies(currentState.certIndex, this.initPolicies, currentState.explicitPolicy, currentState.policyMapping, currentState.inhibitAnyPolicy, this.buildParams.policyQualifiersRejected(), currentState.rootNode, X509CertImpl.toImpl(cert), finalCert);
            Set<String> unresolvedCritExts = cert.getCriticalExtensionOIDs();
            if (unresolvedCritExts == null) {
                unresolvedCritExts = Collections.emptySet();
            }
            currentState.algorithmChecker.check((Certificate) cert, (Collection) unresolvedCritExts);
            for (PKIXCertPathChecker checker : currentState.userCheckers) {
                checker.check(cert, unresolvedCritExts);
            }
            if (!unresolvedCritExts.isEmpty()) {
                unresolvedCritExts.remove(PKIXExtensions.BasicConstraints_Id.toString());
                unresolvedCritExts.remove(PKIXExtensions.NameConstraints_Id.toString());
                unresolvedCritExts.remove(PKIXExtensions.CertificatePolicies_Id.toString());
                unresolvedCritExts.remove(PKIXExtensions.PolicyMappings_Id.toString());
                unresolvedCritExts.remove(PKIXExtensions.PolicyConstraints_Id.toString());
                unresolvedCritExts.remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
                unresolvedCritExts.remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
                unresolvedCritExts.remove(PKIXExtensions.KeyUsage_Id.toString());
                unresolvedCritExts.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
                if (!unresolvedCritExts.isEmpty()) {
                    throw new CertPathValidatorException("Unrecognized critical extension(s)", null, null, -1, PKIXReason.UNRECOGNIZED_CRIT_EXT);
                }
            }
            if (this.buildParams.sigProvider() != null) {
                cert.verify(currentState.pubKey, this.buildParams.sigProvider());
            } else {
                cert.verify(currentState.pubKey);
            }
        }
    }

    boolean isPathCompleted(X509Certificate cert) {
        return cert.getSubjectX500Principal().equals(this.buildParams.targetSubject());
    }

    void addCertToPath(X509Certificate cert, LinkedList<X509Certificate> certPathList) {
        certPathList.addLast(cert);
    }

    void removeFinalCertFromPath(LinkedList<X509Certificate> certPathList) {
        certPathList.removeLast();
    }
}
