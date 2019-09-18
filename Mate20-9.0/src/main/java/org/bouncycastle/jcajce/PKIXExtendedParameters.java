package org.bouncycastle.jcajce;

import java.security.cert.CertPathParameters;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.jcajce.PKIXCertStoreSelector;

public class PKIXExtendedParameters implements CertPathParameters {
    public static final int CHAIN_VALIDITY_MODEL = 1;
    public static final int PKIX_VALIDITY_MODEL = 0;
    /* access modifiers changed from: private */
    public final PKIXParameters baseParameters;
    /* access modifiers changed from: private */
    public final Date date;
    /* access modifiers changed from: private */
    public final List<PKIXCRLStore> extraCRLStores;
    /* access modifiers changed from: private */
    public final List<PKIXCertStore> extraCertStores;
    /* access modifiers changed from: private */
    public final Map<GeneralName, PKIXCRLStore> namedCRLStoreMap;
    /* access modifiers changed from: private */
    public final Map<GeneralName, PKIXCertStore> namedCertificateStoreMap;
    private final boolean revocationEnabled;
    /* access modifiers changed from: private */
    public final PKIXCertStoreSelector targetConstraints;
    private final Set<TrustAnchor> trustAnchors;
    /* access modifiers changed from: private */
    public final boolean useDeltas;
    /* access modifiers changed from: private */
    public final int validityModel;

    public static class Builder {
        /* access modifiers changed from: private */
        public final PKIXParameters baseParameters;
        /* access modifiers changed from: private */
        public final Date date;
        /* access modifiers changed from: private */
        public List<PKIXCRLStore> extraCRLStores = new ArrayList();
        /* access modifiers changed from: private */
        public List<PKIXCertStore> extraCertStores = new ArrayList();
        /* access modifiers changed from: private */
        public Map<GeneralName, PKIXCRLStore> namedCRLStoreMap = new HashMap();
        /* access modifiers changed from: private */
        public Map<GeneralName, PKIXCertStore> namedCertificateStoreMap = new HashMap();
        /* access modifiers changed from: private */
        public boolean revocationEnabled;
        /* access modifiers changed from: private */
        public PKIXCertStoreSelector targetConstraints;
        /* access modifiers changed from: private */
        public Set<TrustAnchor> trustAnchors;
        /* access modifiers changed from: private */
        public boolean useDeltas = false;
        /* access modifiers changed from: private */
        public int validityModel = 0;

        public Builder(PKIXParameters pKIXParameters) {
            this.baseParameters = (PKIXParameters) pKIXParameters.clone();
            CertSelector targetCertConstraints = pKIXParameters.getTargetCertConstraints();
            if (targetCertConstraints != null) {
                this.targetConstraints = new PKIXCertStoreSelector.Builder(targetCertConstraints).build();
            }
            this.date = pKIXParameters.getDate() == null ? new Date() : pKIXParameters.getDate();
            this.revocationEnabled = pKIXParameters.isRevocationEnabled();
            this.trustAnchors = pKIXParameters.getTrustAnchors();
        }

        public Builder(PKIXExtendedParameters pKIXExtendedParameters) {
            this.baseParameters = pKIXExtendedParameters.baseParameters;
            this.date = pKIXExtendedParameters.date;
            this.targetConstraints = pKIXExtendedParameters.targetConstraints;
            this.extraCertStores = new ArrayList(pKIXExtendedParameters.extraCertStores);
            this.namedCertificateStoreMap = new HashMap(pKIXExtendedParameters.namedCertificateStoreMap);
            this.extraCRLStores = new ArrayList(pKIXExtendedParameters.extraCRLStores);
            this.namedCRLStoreMap = new HashMap(pKIXExtendedParameters.namedCRLStoreMap);
            this.useDeltas = pKIXExtendedParameters.useDeltas;
            this.validityModel = pKIXExtendedParameters.validityModel;
            this.revocationEnabled = pKIXExtendedParameters.isRevocationEnabled();
            this.trustAnchors = pKIXExtendedParameters.getTrustAnchors();
        }

        public Builder addCRLStore(PKIXCRLStore pKIXCRLStore) {
            this.extraCRLStores.add(pKIXCRLStore);
            return this;
        }

        public Builder addCertificateStore(PKIXCertStore pKIXCertStore) {
            this.extraCertStores.add(pKIXCertStore);
            return this;
        }

        public Builder addNamedCRLStore(GeneralName generalName, PKIXCRLStore pKIXCRLStore) {
            this.namedCRLStoreMap.put(generalName, pKIXCRLStore);
            return this;
        }

        public Builder addNamedCertificateStore(GeneralName generalName, PKIXCertStore pKIXCertStore) {
            this.namedCertificateStoreMap.put(generalName, pKIXCertStore);
            return this;
        }

        public PKIXExtendedParameters build() {
            return new PKIXExtendedParameters(this);
        }

        public void setRevocationEnabled(boolean z) {
            this.revocationEnabled = z;
        }

        public Builder setTargetConstraints(PKIXCertStoreSelector pKIXCertStoreSelector) {
            this.targetConstraints = pKIXCertStoreSelector;
            return this;
        }

        public Builder setTrustAnchor(TrustAnchor trustAnchor) {
            this.trustAnchors = Collections.singleton(trustAnchor);
            return this;
        }

        public Builder setTrustAnchors(Set<TrustAnchor> set) {
            this.trustAnchors = set;
            return this;
        }

        public Builder setUseDeltasEnabled(boolean z) {
            this.useDeltas = z;
            return this;
        }

        public Builder setValidityModel(int i) {
            this.validityModel = i;
            return this;
        }
    }

    private PKIXExtendedParameters(Builder builder) {
        this.baseParameters = builder.baseParameters;
        this.date = builder.date;
        this.extraCertStores = Collections.unmodifiableList(builder.extraCertStores);
        this.namedCertificateStoreMap = Collections.unmodifiableMap(new HashMap(builder.namedCertificateStoreMap));
        this.extraCRLStores = Collections.unmodifiableList(builder.extraCRLStores);
        this.namedCRLStoreMap = Collections.unmodifiableMap(new HashMap(builder.namedCRLStoreMap));
        this.targetConstraints = builder.targetConstraints;
        this.revocationEnabled = builder.revocationEnabled;
        this.useDeltas = builder.useDeltas;
        this.validityModel = builder.validityModel;
        this.trustAnchors = Collections.unmodifiableSet(builder.trustAnchors);
    }

    public Object clone() {
        return this;
    }

    public List<PKIXCRLStore> getCRLStores() {
        return this.extraCRLStores;
    }

    public List getCertPathCheckers() {
        return this.baseParameters.getCertPathCheckers();
    }

    public List<CertStore> getCertStores() {
        return this.baseParameters.getCertStores();
    }

    public List<PKIXCertStore> getCertificateStores() {
        return this.extraCertStores;
    }

    public Date getDate() {
        return new Date(this.date.getTime());
    }

    public Set getInitialPolicies() {
        return this.baseParameters.getInitialPolicies();
    }

    public Map<GeneralName, PKIXCRLStore> getNamedCRLStoreMap() {
        return this.namedCRLStoreMap;
    }

    public Map<GeneralName, PKIXCertStore> getNamedCertificateStoreMap() {
        return this.namedCertificateStoreMap;
    }

    public String getSigProvider() {
        return this.baseParameters.getSigProvider();
    }

    public PKIXCertStoreSelector getTargetConstraints() {
        return this.targetConstraints;
    }

    public Set getTrustAnchors() {
        return this.trustAnchors;
    }

    public int getValidityModel() {
        return this.validityModel;
    }

    public boolean isAnyPolicyInhibited() {
        return this.baseParameters.isAnyPolicyInhibited();
    }

    public boolean isExplicitPolicyRequired() {
        return this.baseParameters.isExplicitPolicyRequired();
    }

    public boolean isPolicyMappingInhibited() {
        return this.baseParameters.isPolicyMappingInhibited();
    }

    public boolean isRevocationEnabled() {
        return this.revocationEnabled;
    }

    public boolean isUseDeltasEnabled() {
        return this.useDeltas;
    }
}
