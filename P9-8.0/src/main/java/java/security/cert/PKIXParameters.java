package java.security.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PKIXParameters implements CertPathParameters {
    private boolean anyPolicyInhibited = false;
    private List<PKIXCertPathChecker> certPathCheckers;
    private CertSelector certSelector;
    private List<CertStore> certStores;
    private Date date;
    private boolean explicitPolicyRequired = false;
    private boolean policyMappingInhibited = false;
    private boolean policyQualifiersRejected = true;
    private boolean revocationEnabled = true;
    private String sigProvider;
    private Set<String> unmodInitialPolicies;
    private Set<TrustAnchor> unmodTrustAnchors;

    public PKIXParameters(Set<TrustAnchor> trustAnchors) throws InvalidAlgorithmParameterException {
        setTrustAnchors(trustAnchors);
        this.unmodInitialPolicies = Collections.emptySet();
        this.certPathCheckers = new ArrayList();
        this.certStores = new ArrayList();
    }

    public PKIXParameters(KeyStore keystore) throws KeyStoreException, InvalidAlgorithmParameterException {
        if (keystore == null) {
            throw new NullPointerException("the keystore parameter must be non-null");
        }
        Set<TrustAnchor> hashSet = new HashSet();
        Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = (String) aliases.nextElement();
            if (keystore.isCertificateEntry(alias)) {
                Certificate cert = keystore.getCertificate(alias);
                if (cert instanceof X509Certificate) {
                    hashSet.add(new TrustAnchor((X509Certificate) cert, null));
                }
            }
        }
        setTrustAnchors(hashSet);
        this.unmodInitialPolicies = Collections.emptySet();
        this.certPathCheckers = new ArrayList();
        this.certStores = new ArrayList();
    }

    public Set<TrustAnchor> getTrustAnchors() {
        return this.unmodTrustAnchors;
    }

    public void setTrustAnchors(Set<TrustAnchor> trustAnchors) throws InvalidAlgorithmParameterException {
        if (trustAnchors == null) {
            throw new NullPointerException("the trustAnchors parameters must be non-null");
        } else if (trustAnchors.isEmpty()) {
            throw new InvalidAlgorithmParameterException("the trustAnchors parameter must be non-empty");
        } else {
            for (Object obj : trustAnchors) {
                if (!(obj instanceof TrustAnchor)) {
                    throw new ClassCastException("all elements of set must be of type java.security.cert.TrustAnchor");
                }
            }
            this.unmodTrustAnchors = Collections.unmodifiableSet(new HashSet((Collection) trustAnchors));
        }
    }

    public Set<String> getInitialPolicies() {
        return this.unmodInitialPolicies;
    }

    public void setInitialPolicies(Set<String> initialPolicies) {
        if (initialPolicies != null) {
            for (Object obj : initialPolicies) {
                if (!(obj instanceof String)) {
                    throw new ClassCastException("all elements of set must be of type java.lang.String");
                }
            }
            this.unmodInitialPolicies = Collections.unmodifiableSet(new HashSet((Collection) initialPolicies));
            return;
        }
        this.unmodInitialPolicies = Collections.emptySet();
    }

    public void setCertStores(List<CertStore> stores) {
        if (stores == null) {
            this.certStores = new ArrayList();
            return;
        }
        for (Object obj : stores) {
            if (!(obj instanceof CertStore)) {
                throw new ClassCastException("all elements of list must be of type java.security.cert.CertStore");
            }
        }
        this.certStores = new ArrayList((Collection) stores);
    }

    public void addCertStore(CertStore store) {
        if (store != null) {
            this.certStores.add(store);
        }
    }

    public List<CertStore> getCertStores() {
        return Collections.unmodifiableList(new ArrayList(this.certStores));
    }

    public void setRevocationEnabled(boolean val) {
        this.revocationEnabled = val;
    }

    public boolean isRevocationEnabled() {
        return this.revocationEnabled;
    }

    public void setExplicitPolicyRequired(boolean val) {
        this.explicitPolicyRequired = val;
    }

    public boolean isExplicitPolicyRequired() {
        return this.explicitPolicyRequired;
    }

    public void setPolicyMappingInhibited(boolean val) {
        this.policyMappingInhibited = val;
    }

    public boolean isPolicyMappingInhibited() {
        return this.policyMappingInhibited;
    }

    public void setAnyPolicyInhibited(boolean val) {
        this.anyPolicyInhibited = val;
    }

    public boolean isAnyPolicyInhibited() {
        return this.anyPolicyInhibited;
    }

    public void setPolicyQualifiersRejected(boolean qualifiersRejected) {
        this.policyQualifiersRejected = qualifiersRejected;
    }

    public boolean getPolicyQualifiersRejected() {
        return this.policyQualifiersRejected;
    }

    public Date getDate() {
        if (this.date == null) {
            return null;
        }
        return (Date) this.date.clone();
    }

    public void setDate(Date date) {
        if (date != null) {
            this.date = (Date) date.clone();
        }
    }

    public void setCertPathCheckers(List<PKIXCertPathChecker> checkers) {
        if (checkers != null) {
            List<PKIXCertPathChecker> tmpList = new ArrayList();
            for (PKIXCertPathChecker checker : checkers) {
                tmpList.add((PKIXCertPathChecker) checker.clone());
            }
            this.certPathCheckers = tmpList;
            return;
        }
        this.certPathCheckers = new ArrayList();
    }

    public List<PKIXCertPathChecker> getCertPathCheckers() {
        List<PKIXCertPathChecker> tmpList = new ArrayList();
        for (PKIXCertPathChecker ck : this.certPathCheckers) {
            tmpList.add((PKIXCertPathChecker) ck.clone());
        }
        return Collections.unmodifiableList(tmpList);
    }

    public void addCertPathChecker(PKIXCertPathChecker checker) {
        if (checker != null) {
            this.certPathCheckers.add((PKIXCertPathChecker) checker.clone());
        }
    }

    public String getSigProvider() {
        return this.sigProvider;
    }

    public void setSigProvider(String sigProvider) {
        this.sigProvider = sigProvider;
    }

    public CertSelector getTargetCertConstraints() {
        if (this.certSelector != null) {
            return (CertSelector) this.certSelector.clone();
        }
        return null;
    }

    public void setTargetCertConstraints(CertSelector selector) {
        if (selector != null) {
            this.certSelector = (CertSelector) selector.clone();
        } else {
            this.certSelector = null;
        }
    }

    public Object clone() {
        try {
            PKIXParameters copy = (PKIXParameters) super.clone();
            if (this.certStores != null) {
                copy.certStores = new ArrayList(this.certStores);
            }
            if (this.certPathCheckers != null) {
                copy.certPathCheckers = new ArrayList(this.certPathCheckers.size());
                for (PKIXCertPathChecker checker : this.certPathCheckers) {
                    copy.certPathCheckers.add((PKIXCertPathChecker) checker.clone());
                }
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");
        if (this.unmodTrustAnchors != null) {
            sb.append("  Trust Anchors: " + this.unmodTrustAnchors.toString() + "\n");
        }
        if (this.unmodInitialPolicies != null) {
            if (this.unmodInitialPolicies.isEmpty()) {
                sb.append("  Initial Policy OIDs: any\n");
            } else {
                sb.append("  Initial Policy OIDs: [" + this.unmodInitialPolicies.toString() + "]\n");
            }
        }
        sb.append("  Validity Date: " + String.valueOf(this.date) + "\n");
        sb.append("  Signature Provider: " + String.valueOf(this.sigProvider) + "\n");
        sb.append("  Default Revocation Enabled: " + this.revocationEnabled + "\n");
        sb.append("  Explicit Policy Required: " + this.explicitPolicyRequired + "\n");
        sb.append("  Policy Mapping Inhibited: " + this.policyMappingInhibited + "\n");
        sb.append("  Any Policy Inhibited: " + this.anyPolicyInhibited + "\n");
        sb.append("  Policy Qualifiers Rejected: " + this.policyQualifiersRejected + "\n");
        sb.append("  Target Cert Constraints: " + String.valueOf(this.certSelector) + "\n");
        if (this.certPathCheckers != null) {
            sb.append("  Certification Path Checkers: [" + this.certPathCheckers.toString() + "]\n");
        }
        if (this.certStores != null) {
            sb.append("  CertStores: [" + this.certStores.toString() + "]\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
