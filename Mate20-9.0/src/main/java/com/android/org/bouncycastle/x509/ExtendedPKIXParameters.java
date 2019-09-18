package com.android.org.bouncycastle.x509;

import com.android.org.bouncycastle.util.Selector;
import com.android.org.bouncycastle.util.Store;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExtendedPKIXParameters extends PKIXParameters {
    public static final int CHAIN_VALIDITY_MODEL = 1;
    public static final int PKIX_VALIDITY_MODEL = 0;
    private boolean additionalLocationsEnabled;
    private List additionalStores = new ArrayList();
    private Set attrCertCheckers = new HashSet();
    private Set necessaryACAttributes = new HashSet();
    private Set prohibitedACAttributes = new HashSet();
    private Selector selector;
    private List stores = new ArrayList();
    private Set trustedACIssuers = new HashSet();
    private boolean useDeltas = false;
    private int validityModel = 0;

    public ExtendedPKIXParameters(Set trustAnchors) throws InvalidAlgorithmParameterException {
        super(trustAnchors);
    }

    public static ExtendedPKIXParameters getInstance(PKIXParameters pkixParams) {
        try {
            ExtendedPKIXParameters params = new ExtendedPKIXParameters(pkixParams.getTrustAnchors());
            params.setParams(pkixParams);
            return params;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void setParams(PKIXParameters params) {
        Selector selector2;
        setDate(params.getDate());
        setCertPathCheckers(params.getCertPathCheckers());
        setCertStores(params.getCertStores());
        setAnyPolicyInhibited(params.isAnyPolicyInhibited());
        setExplicitPolicyRequired(params.isExplicitPolicyRequired());
        setPolicyMappingInhibited(params.isPolicyMappingInhibited());
        setRevocationEnabled(params.isRevocationEnabled());
        setInitialPolicies(params.getInitialPolicies());
        setPolicyQualifiersRejected(params.getPolicyQualifiersRejected());
        setSigProvider(params.getSigProvider());
        setTargetCertConstraints(params.getTargetCertConstraints());
        try {
            setTrustAnchors(params.getTrustAnchors());
            if (params instanceof ExtendedPKIXParameters) {
                ExtendedPKIXParameters _params = (ExtendedPKIXParameters) params;
                this.validityModel = _params.validityModel;
                this.useDeltas = _params.useDeltas;
                this.additionalLocationsEnabled = _params.additionalLocationsEnabled;
                if (_params.selector == null) {
                    selector2 = null;
                } else {
                    selector2 = (Selector) _params.selector.clone();
                }
                this.selector = selector2;
                this.stores = new ArrayList(_params.stores);
                this.additionalStores = new ArrayList(_params.additionalStores);
                this.trustedACIssuers = new HashSet(_params.trustedACIssuers);
                this.prohibitedACAttributes = new HashSet(_params.prohibitedACAttributes);
                this.necessaryACAttributes = new HashSet(_params.necessaryACAttributes);
                this.attrCertCheckers = new HashSet(_params.attrCertCheckers);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean isUseDeltasEnabled() {
        return this.useDeltas;
    }

    public void setUseDeltasEnabled(boolean useDeltas2) {
        this.useDeltas = useDeltas2;
    }

    public int getValidityModel() {
        return this.validityModel;
    }

    public void setCertStores(List stores2) {
        if (stores2 != null) {
            Iterator it = stores2.iterator();
            while (it.hasNext()) {
                addCertStore((CertStore) it.next());
            }
        }
    }

    public void setStores(List stores2) {
        if (stores2 == null) {
            this.stores = new ArrayList();
            return;
        }
        for (Object obj : stores2) {
            if (!(obj instanceof Store)) {
                throw new ClassCastException("All elements of list must be of type org.bouncycastle.util.Store.");
            }
        }
        this.stores = new ArrayList(stores2);
    }

    public void addStore(Store store) {
        if (store != null) {
            this.stores.add(store);
        }
    }

    public void addAdditionalStore(Store store) {
        if (store != null) {
            this.additionalStores.add(store);
        }
    }

    public void addAddionalStore(Store store) {
        addAdditionalStore(store);
    }

    public List getAdditionalStores() {
        return Collections.unmodifiableList(this.additionalStores);
    }

    public List getStores() {
        return Collections.unmodifiableList(new ArrayList(this.stores));
    }

    public void setValidityModel(int validityModel2) {
        this.validityModel = validityModel2;
    }

    public Object clone() {
        try {
            ExtendedPKIXParameters params = new ExtendedPKIXParameters(getTrustAnchors());
            params.setParams(this);
            return params;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean isAdditionalLocationsEnabled() {
        return this.additionalLocationsEnabled;
    }

    public void setAdditionalLocationsEnabled(boolean enabled) {
        this.additionalLocationsEnabled = enabled;
    }

    public Selector getTargetConstraints() {
        if (this.selector != null) {
            return (Selector) this.selector.clone();
        }
        return null;
    }

    public void setTargetConstraints(Selector selector2) {
        if (selector2 != null) {
            this.selector = (Selector) selector2.clone();
        } else {
            this.selector = null;
        }
    }

    public void setTargetCertConstraints(CertSelector selector2) {
        super.setTargetCertConstraints(selector2);
        if (selector2 != null) {
            this.selector = X509CertStoreSelector.getInstance((X509CertSelector) selector2);
        } else {
            this.selector = null;
        }
    }

    public Set getTrustedACIssuers() {
        return Collections.unmodifiableSet(this.trustedACIssuers);
    }

    public void setTrustedACIssuers(Set trustedACIssuers2) {
        if (trustedACIssuers2 == null) {
            this.trustedACIssuers.clear();
            return;
        }
        for (Object obj : trustedACIssuers2) {
            if (!(obj instanceof TrustAnchor)) {
                throw new ClassCastException("All elements of set must be of type " + TrustAnchor.class.getName() + ".");
            }
        }
        this.trustedACIssuers.clear();
        this.trustedACIssuers.addAll(trustedACIssuers2);
    }

    public Set getNecessaryACAttributes() {
        return Collections.unmodifiableSet(this.necessaryACAttributes);
    }

    public void setNecessaryACAttributes(Set necessaryACAttributes2) {
        if (necessaryACAttributes2 == null) {
            this.necessaryACAttributes.clear();
            return;
        }
        for (Object obj : necessaryACAttributes2) {
            if (!(obj instanceof String)) {
                throw new ClassCastException("All elements of set must be of type String.");
            }
        }
        this.necessaryACAttributes.clear();
        this.necessaryACAttributes.addAll(necessaryACAttributes2);
    }

    public Set getProhibitedACAttributes() {
        return Collections.unmodifiableSet(this.prohibitedACAttributes);
    }

    public void setProhibitedACAttributes(Set prohibitedACAttributes2) {
        if (prohibitedACAttributes2 == null) {
            this.prohibitedACAttributes.clear();
            return;
        }
        for (Object obj : prohibitedACAttributes2) {
            if (!(obj instanceof String)) {
                throw new ClassCastException("All elements of set must be of type String.");
            }
        }
        this.prohibitedACAttributes.clear();
        this.prohibitedACAttributes.addAll(prohibitedACAttributes2);
    }

    public Set getAttrCertCheckers() {
        return Collections.unmodifiableSet(this.attrCertCheckers);
    }

    public void setAttrCertCheckers(Set attrCertCheckers2) {
        if (attrCertCheckers2 == null) {
            this.attrCertCheckers.clear();
            return;
        }
        for (Object obj : attrCertCheckers2) {
            if (!(obj instanceof PKIXAttrCertChecker)) {
                throw new ClassCastException("All elements of set must be of type " + PKIXAttrCertChecker.class.getName() + ".");
            }
        }
        this.attrCertCheckers.clear();
        this.attrCertCheckers.addAll(attrCertCheckers2);
    }
}
