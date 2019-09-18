package com.android.org.bouncycastle.jcajce;

import com.android.org.bouncycastle.asn1.x509.GeneralName;
import com.android.org.bouncycastle.jcajce.PKIXCertStoreSelector;
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

        public Builder(PKIXParameters baseParameters2) {
            this.baseParameters = (PKIXParameters) baseParameters2.clone();
            CertSelector constraints = baseParameters2.getTargetCertConstraints();
            if (constraints != null) {
                this.targetConstraints = new PKIXCertStoreSelector.Builder(constraints).build();
            }
            Date checkDate = baseParameters2.getDate();
            this.date = checkDate == null ? new Date() : checkDate;
            this.revocationEnabled = baseParameters2.isRevocationEnabled();
            this.trustAnchors = baseParameters2.getTrustAnchors();
        }

        public Builder(PKIXExtendedParameters baseParameters2) {
            this.baseParameters = baseParameters2.baseParameters;
            this.date = baseParameters2.date;
            this.targetConstraints = baseParameters2.targetConstraints;
            this.extraCertStores = new ArrayList(baseParameters2.extraCertStores);
            this.namedCertificateStoreMap = new HashMap(baseParameters2.namedCertificateStoreMap);
            this.extraCRLStores = new ArrayList(baseParameters2.extraCRLStores);
            this.namedCRLStoreMap = new HashMap(baseParameters2.namedCRLStoreMap);
            this.useDeltas = baseParameters2.useDeltas;
            this.validityModel = baseParameters2.validityModel;
            this.revocationEnabled = baseParameters2.isRevocationEnabled();
            this.trustAnchors = baseParameters2.getTrustAnchors();
        }

        public Builder addCertificateStore(PKIXCertStore store) {
            this.extraCertStores.add(store);
            return this;
        }

        public Builder addNamedCertificateStore(GeneralName issuerAltName, PKIXCertStore store) {
            this.namedCertificateStoreMap.put(issuerAltName, store);
            return this;
        }

        public Builder addCRLStore(PKIXCRLStore store) {
            this.extraCRLStores.add(store);
            return this;
        }

        public Builder addNamedCRLStore(GeneralName issuerAltName, PKIXCRLStore store) {
            this.namedCRLStoreMap.put(issuerAltName, store);
            return this;
        }

        public Builder setTargetConstraints(PKIXCertStoreSelector selector) {
            this.targetConstraints = selector;
            return this;
        }

        public Builder setUseDeltasEnabled(boolean useDeltas2) {
            this.useDeltas = useDeltas2;
            return this;
        }

        public Builder setValidityModel(int validityModel2) {
            this.validityModel = validityModel2;
            return this;
        }

        public Builder setTrustAnchor(TrustAnchor trustAnchor) {
            this.trustAnchors = Collections.singleton(trustAnchor);
            return this;
        }

        public Builder setTrustAnchors(Set<TrustAnchor> trustAnchors2) {
            this.trustAnchors = trustAnchors2;
            return this;
        }

        public void setRevocationEnabled(boolean revocationEnabled2) {
            this.revocationEnabled = revocationEnabled2;
        }

        public PKIXExtendedParameters build() {
            return new PKIXExtendedParameters(this);
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

    public List<PKIXCertStore> getCertificateStores() {
        return this.extraCertStores;
    }

    public Map<GeneralName, PKIXCertStore> getNamedCertificateStoreMap() {
        return this.namedCertificateStoreMap;
    }

    public List<PKIXCRLStore> getCRLStores() {
        return this.extraCRLStores;
    }

    public Map<GeneralName, PKIXCRLStore> getNamedCRLStoreMap() {
        return this.namedCRLStoreMap;
    }

    public Date getDate() {
        return new Date(this.date.getTime());
    }

    public boolean isUseDeltasEnabled() {
        return this.useDeltas;
    }

    public int getValidityModel() {
        return this.validityModel;
    }

    public Object clone() {
        return this;
    }

    public PKIXCertStoreSelector getTargetConstraints() {
        return this.targetConstraints;
    }

    public Set getTrustAnchors() {
        return this.trustAnchors;
    }

    public Set getInitialPolicies() {
        return this.baseParameters.getInitialPolicies();
    }

    public String getSigProvider() {
        return this.baseParameters.getSigProvider();
    }

    public boolean isExplicitPolicyRequired() {
        return this.baseParameters.isExplicitPolicyRequired();
    }

    public boolean isAnyPolicyInhibited() {
        return this.baseParameters.isAnyPolicyInhibited();
    }

    public boolean isPolicyMappingInhibited() {
        return this.baseParameters.isPolicyMappingInhibited();
    }

    public List getCertPathCheckers() {
        return this.baseParameters.getCertPathCheckers();
    }

    public List<CertStore> getCertStores() {
        return this.baseParameters.getCertStores();
    }

    public boolean isRevocationEnabled() {
        return this.revocationEnabled;
    }
}
