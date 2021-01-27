package org.bouncycastle.x509;

import org.bouncycastle.util.Selector;

public class X509CertPairStoreSelector implements Selector {
    private X509CertificatePair certPair;
    private X509CertStoreSelector forwardSelector;
    private X509CertStoreSelector reverseSelector;

    @Override // org.bouncycastle.util.Selector, java.lang.Object
    public Object clone() {
        X509CertPairStoreSelector x509CertPairStoreSelector = new X509CertPairStoreSelector();
        x509CertPairStoreSelector.certPair = this.certPair;
        X509CertStoreSelector x509CertStoreSelector = this.forwardSelector;
        if (x509CertStoreSelector != null) {
            x509CertPairStoreSelector.setForwardSelector((X509CertStoreSelector) x509CertStoreSelector.clone());
        }
        X509CertStoreSelector x509CertStoreSelector2 = this.reverseSelector;
        if (x509CertStoreSelector2 != null) {
            x509CertPairStoreSelector.setReverseSelector((X509CertStoreSelector) x509CertStoreSelector2.clone());
        }
        return x509CertPairStoreSelector;
    }

    public X509CertificatePair getCertPair() {
        return this.certPair;
    }

    public X509CertStoreSelector getForwardSelector() {
        return this.forwardSelector;
    }

    public X509CertStoreSelector getReverseSelector() {
        return this.reverseSelector;
    }

    @Override // org.bouncycastle.util.Selector
    public boolean match(Object obj) {
        try {
            if (!(obj instanceof X509CertificatePair)) {
                return false;
            }
            X509CertificatePair x509CertificatePair = (X509CertificatePair) obj;
            if (this.forwardSelector != null && !this.forwardSelector.match((Object) x509CertificatePair.getForward())) {
                return false;
            }
            if (this.reverseSelector != null && !this.reverseSelector.match((Object) x509CertificatePair.getReverse())) {
                return false;
            }
            if (this.certPair != null) {
                return this.certPair.equals(obj);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setCertPair(X509CertificatePair x509CertificatePair) {
        this.certPair = x509CertificatePair;
    }

    public void setForwardSelector(X509CertStoreSelector x509CertStoreSelector) {
        this.forwardSelector = x509CertStoreSelector;
    }

    public void setReverseSelector(X509CertStoreSelector x509CertStoreSelector) {
        this.reverseSelector = x509CertStoreSelector;
    }
}
