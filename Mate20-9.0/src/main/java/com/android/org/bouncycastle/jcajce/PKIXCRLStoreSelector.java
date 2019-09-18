package com.android.org.bouncycastle.jcajce;

import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.x509.Extension;
import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Selector;
import java.math.BigInteger;
import java.security.cert.CRL;
import java.security.cert.CRLSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class PKIXCRLStoreSelector<T extends CRL> implements Selector<T> {
    /* access modifiers changed from: private */
    public final CRLSelector baseSelector;
    private final boolean completeCRLEnabled;
    private final boolean deltaCRLIndicator;
    private final byte[] issuingDistributionPoint;
    private final boolean issuingDistributionPointEnabled;
    private final BigInteger maxBaseCRLNumber;

    public static class Builder {
        /* access modifiers changed from: private */
        public final CRLSelector baseSelector;
        /* access modifiers changed from: private */
        public boolean completeCRLEnabled = false;
        /* access modifiers changed from: private */
        public boolean deltaCRLIndicator = false;
        /* access modifiers changed from: private */
        public byte[] issuingDistributionPoint = null;
        /* access modifiers changed from: private */
        public boolean issuingDistributionPointEnabled = false;
        /* access modifiers changed from: private */
        public BigInteger maxBaseCRLNumber = null;

        public Builder(CRLSelector crlSelector) {
            this.baseSelector = (CRLSelector) crlSelector.clone();
        }

        public Builder setCompleteCRLEnabled(boolean completeCRLEnabled2) {
            this.completeCRLEnabled = completeCRLEnabled2;
            return this;
        }

        public Builder setDeltaCRLIndicatorEnabled(boolean deltaCRLIndicator2) {
            this.deltaCRLIndicator = deltaCRLIndicator2;
            return this;
        }

        public void setMaxBaseCRLNumber(BigInteger maxBaseCRLNumber2) {
            this.maxBaseCRLNumber = maxBaseCRLNumber2;
        }

        public void setIssuingDistributionPointEnabled(boolean issuingDistributionPointEnabled2) {
            this.issuingDistributionPointEnabled = issuingDistributionPointEnabled2;
        }

        public void setIssuingDistributionPoint(byte[] issuingDistributionPoint2) {
            this.issuingDistributionPoint = Arrays.clone(issuingDistributionPoint2);
        }

        public PKIXCRLStoreSelector<? extends CRL> build() {
            return new PKIXCRLStoreSelector<>(this);
        }
    }

    private static class SelectorClone extends X509CRLSelector {
        private final PKIXCRLStoreSelector selector;

        SelectorClone(PKIXCRLStoreSelector selector2) {
            this.selector = selector2;
            if (selector2.baseSelector instanceof X509CRLSelector) {
                X509CRLSelector baseSelector = (X509CRLSelector) selector2.baseSelector;
                setCertificateChecking(baseSelector.getCertificateChecking());
                setDateAndTime(baseSelector.getDateAndTime());
                setIssuers(baseSelector.getIssuers());
                setMinCRLNumber(baseSelector.getMinCRL());
                setMaxCRLNumber(baseSelector.getMaxCRL());
            }
        }

        public boolean match(CRL crl) {
            if (this.selector == null) {
                return crl != null;
            }
            return this.selector.match(crl);
        }
    }

    private PKIXCRLStoreSelector(Builder baseBuilder) {
        this.baseSelector = baseBuilder.baseSelector;
        this.deltaCRLIndicator = baseBuilder.deltaCRLIndicator;
        this.completeCRLEnabled = baseBuilder.completeCRLEnabled;
        this.maxBaseCRLNumber = baseBuilder.maxBaseCRLNumber;
        this.issuingDistributionPoint = baseBuilder.issuingDistributionPoint;
        this.issuingDistributionPointEnabled = baseBuilder.issuingDistributionPointEnabled;
    }

    public boolean isIssuingDistributionPointEnabled() {
        return this.issuingDistributionPointEnabled;
    }

    public boolean match(CRL obj) {
        if (!(obj instanceof X509CRL)) {
            return this.baseSelector.match(obj);
        }
        X509CRL crl = (X509CRL) obj;
        ASN1Integer dci = null;
        try {
            byte[] bytes = crl.getExtensionValue(Extension.deltaCRLIndicator.getId());
            if (bytes != null) {
                dci = ASN1Integer.getInstance(ASN1OctetString.getInstance(bytes).getOctets());
            }
            if (isDeltaCRLIndicatorEnabled() && dci == null) {
                return false;
            }
            if (isCompleteCRLEnabled() && dci != null) {
                return false;
            }
            if (dci != null && this.maxBaseCRLNumber != null && dci.getPositiveValue().compareTo(this.maxBaseCRLNumber) == 1) {
                return false;
            }
            if (this.issuingDistributionPointEnabled) {
                byte[] idp = crl.getExtensionValue(Extension.issuingDistributionPoint.getId());
                if (this.issuingDistributionPoint == null) {
                    if (idp != null) {
                        return false;
                    }
                } else if (!Arrays.areEqual(idp, this.issuingDistributionPoint)) {
                    return false;
                }
            }
            return this.baseSelector.match(obj);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDeltaCRLIndicatorEnabled() {
        return this.deltaCRLIndicator;
    }

    public Object clone() {
        return this;
    }

    public boolean isCompleteCRLEnabled() {
        return this.completeCRLEnabled;
    }

    public BigInteger getMaxBaseCRLNumber() {
        return this.maxBaseCRLNumber;
    }

    public byte[] getIssuingDistributionPoint() {
        return Arrays.clone(this.issuingDistributionPoint);
    }

    public X509Certificate getCertificateChecking() {
        if (this.baseSelector instanceof X509CRLSelector) {
            return ((X509CRLSelector) this.baseSelector).getCertificateChecking();
        }
        return null;
    }

    public static Collection<? extends CRL> getCRLs(PKIXCRLStoreSelector selector, CertStore certStore) throws CertStoreException {
        return certStore.getCRLs(new SelectorClone(selector));
    }
}
