package sun.security.provider.certpath;

import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;

class PKIX {
    private static final Debug debug = Debug.getInstance("certpath");

    static class ValidatorParams {
        private Set<TrustAnchor> anchors;
        private CertPath certPath;
        private List<X509Certificate> certs;
        private List<PKIXCertPathChecker> checkers;
        private CertSelector constraints;
        private Date date;
        private boolean gotConstraints;
        private boolean gotDate;
        private final PKIXParameters params;
        private Set<String> policies;
        private List<CertStore> stores;

        ValidatorParams(CertPath cp, PKIXParameters params) throws InvalidAlgorithmParameterException {
            this(params);
            if (cp.getType().equals("X.509") || (cp.getType().equals("X509") ^ 1) == 0) {
                this.certPath = cp;
                return;
            }
            throw new InvalidAlgorithmParameterException("inappropriate CertPath type specified, must be X.509 or X509");
        }

        ValidatorParams(PKIXParameters params) throws InvalidAlgorithmParameterException {
            this.anchors = params.getTrustAnchors();
            for (TrustAnchor anchor : this.anchors) {
                if (anchor.getNameConstraints() != null) {
                    throw new InvalidAlgorithmParameterException("name constraints in trust anchor not supported");
                }
            }
            this.params = params;
        }

        CertPath certPath() {
            return this.certPath;
        }

        void setCertPath(CertPath cp) {
            this.certPath = cp;
        }

        List<X509Certificate> certificates() {
            if (this.certs == null) {
                if (this.certPath == null) {
                    this.certs = Collections.emptyList();
                } else {
                    List<X509Certificate> xc = new ArrayList(this.certPath.getCertificates());
                    Collections.reverse(xc);
                    this.certs = xc;
                }
            }
            return this.certs;
        }

        List<PKIXCertPathChecker> certPathCheckers() {
            if (this.checkers == null) {
                this.checkers = this.params.getCertPathCheckers();
            }
            return this.checkers;
        }

        List<CertStore> certStores() {
            if (this.stores == null) {
                this.stores = this.params.getCertStores();
            }
            return this.stores;
        }

        Date date() {
            if (!this.gotDate) {
                this.date = this.params.getDate();
                if (this.date == null) {
                    this.date = new Date();
                }
                this.gotDate = true;
            }
            return this.date;
        }

        Set<String> initialPolicies() {
            if (this.policies == null) {
                this.policies = this.params.getInitialPolicies();
            }
            return this.policies;
        }

        CertSelector targetCertConstraints() {
            if (!this.gotConstraints) {
                this.constraints = this.params.getTargetCertConstraints();
                this.gotConstraints = true;
            }
            return this.constraints;
        }

        Set<TrustAnchor> trustAnchors() {
            return this.anchors;
        }

        boolean revocationEnabled() {
            return this.params.isRevocationEnabled();
        }

        boolean policyMappingInhibited() {
            return this.params.isPolicyMappingInhibited();
        }

        boolean explicitPolicyRequired() {
            return this.params.isExplicitPolicyRequired();
        }

        boolean policyQualifiersRejected() {
            return this.params.getPolicyQualifiersRejected();
        }

        String sigProvider() {
            return this.params.getSigProvider();
        }

        boolean anyPolicyInhibited() {
            return this.params.isAnyPolicyInhibited();
        }

        PKIXParameters getPKIXParameters() {
            return this.params;
        }
    }

    static class BuilderParams extends ValidatorParams {
        private PKIXBuilderParameters params;
        private List<CertStore> stores;
        private X500Principal targetSubject;

        BuilderParams(PKIXBuilderParameters params) throws InvalidAlgorithmParameterException {
            super(params);
            checkParams(params);
        }

        private void checkParams(PKIXBuilderParameters params) throws InvalidAlgorithmParameterException {
            if (targetCertConstraints() instanceof X509CertSelector) {
                this.params = params;
                this.targetSubject = getTargetSubject(certStores(), (X509CertSelector) targetCertConstraints());
                return;
            }
            throw new InvalidAlgorithmParameterException("the targetCertConstraints parameter must be an X509CertSelector");
        }

        List<CertStore> certStores() {
            if (this.stores == null) {
                this.stores = new ArrayList(this.params.getCertStores());
                Collections.sort(this.stores, new CertStoreComparator());
            }
            return this.stores;
        }

        int maxPathLength() {
            return this.params.getMaxPathLength();
        }

        PKIXBuilderParameters params() {
            return this.params;
        }

        X500Principal targetSubject() {
            return this.targetSubject;
        }

        private static X500Principal getTargetSubject(List<CertStore> stores, X509CertSelector sel) throws InvalidAlgorithmParameterException {
            X500Principal subject = sel.getSubject();
            if (subject != null) {
                return subject;
            }
            X509Certificate cert = sel.getCertificate();
            if (cert != null) {
                subject = cert.getSubjectX500Principal();
            }
            if (subject != null) {
                return subject;
            }
            for (CertStore store : stores) {
                try {
                    Collection<? extends Certificate> certs = store.getCertificates(sel);
                    if (!certs.isEmpty()) {
                        return ((X509Certificate) certs.iterator().next()).getSubjectX500Principal();
                    }
                } catch (Object e) {
                    if (PKIX.debug != null) {
                        PKIX.debug.println("BuilderParams.getTargetSubjectDN: non-fatal exception retrieving certs: " + e);
                        e.printStackTrace();
                    }
                }
            }
            throw new InvalidAlgorithmParameterException("Could not determine unique target subject");
        }
    }

    private static class CertStoreComparator implements Comparator<CertStore> {
        /* synthetic */ CertStoreComparator(CertStoreComparator -this0) {
            this();
        }

        private CertStoreComparator() {
        }

        public int compare(CertStore store1, CertStore store2) {
            if (store1.getType().equals("Collection") || (store1.getCertStoreParameters() instanceof CollectionCertStoreParameters)) {
                return -1;
            }
            return 1;
        }
    }

    static class CertStoreTypeException extends CertStoreException {
        private static final long serialVersionUID = 7463352639238322556L;
        private final String type;

        CertStoreTypeException(String type, CertStoreException cse) {
            super(cse.getMessage(), cse.getCause());
            this.type = type;
        }

        String getType() {
            return this.type;
        }
    }

    private PKIX() {
    }

    static boolean isDSAPublicKeyWithoutParams(PublicKey publicKey) {
        if ((publicKey instanceof DSAPublicKey) && ((DSAPublicKey) publicKey).getParams() == null) {
            return true;
        }
        return false;
    }

    static ValidatorParams checkParams(CertPath cp, CertPathParameters params) throws InvalidAlgorithmParameterException {
        if (params instanceof PKIXParameters) {
            return new ValidatorParams(cp, (PKIXParameters) params);
        }
        throw new InvalidAlgorithmParameterException("inappropriate params, must be an instance of PKIXParameters");
    }

    static BuilderParams checkBuilderParams(CertPathParameters params) throws InvalidAlgorithmParameterException {
        if (params instanceof PKIXBuilderParameters) {
            return new BuilderParams((PKIXBuilderParameters) params);
        }
        throw new InvalidAlgorithmParameterException("inappropriate params, must be an instance of PKIXBuilderParameters");
    }
}
