package com.android.org.bouncycastle.x509;

import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.x509.X509Extensions;
import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Selector;
import com.android.org.bouncycastle.x509.extension.X509ExtensionUtil;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CRL;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;

public class X509CRLStoreSelector extends X509CRLSelector implements Selector {
    private X509AttributeCertificate attrCertChecking;
    private boolean completeCRLEnabled = false;
    private boolean deltaCRLIndicator = false;
    private byte[] issuingDistributionPoint = null;
    private boolean issuingDistributionPointEnabled = false;
    private BigInteger maxBaseCRLNumber = null;

    public boolean isIssuingDistributionPointEnabled() {
        return this.issuingDistributionPointEnabled;
    }

    public void setIssuingDistributionPointEnabled(boolean issuingDistributionPointEnabled) {
        this.issuingDistributionPointEnabled = issuingDistributionPointEnabled;
    }

    public void setAttrCertificateChecking(X509AttributeCertificate attrCert) {
        this.attrCertChecking = attrCert;
    }

    public X509AttributeCertificate getAttrCertificateChecking() {
        return this.attrCertChecking;
    }

    public boolean match(Object obj) {
        if (!(obj instanceof X509CRL)) {
            return false;
        }
        X509CRL crl = (X509CRL) obj;
        ASN1Integer dci = null;
        try {
            byte[] bytes = crl.getExtensionValue(X509Extensions.DeltaCRLIndicator.getId());
            if (bytes != null) {
                dci = ASN1Integer.getInstance(X509ExtensionUtil.fromExtensionValue(bytes));
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
                byte[] idp = crl.getExtensionValue(X509Extensions.IssuingDistributionPoint.getId());
                if (this.issuingDistributionPoint == null) {
                    if (idp != null) {
                        return false;
                    }
                } else if (!Arrays.areEqual(idp, this.issuingDistributionPoint)) {
                    return false;
                }
            }
            return super.match((X509CRL) obj);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean match(CRL crl) {
        return match((Object) crl);
    }

    public boolean isDeltaCRLIndicatorEnabled() {
        return this.deltaCRLIndicator;
    }

    public void setDeltaCRLIndicatorEnabled(boolean deltaCRLIndicator) {
        this.deltaCRLIndicator = deltaCRLIndicator;
    }

    public static X509CRLStoreSelector getInstance(X509CRLSelector selector) {
        if (selector == null) {
            throw new IllegalArgumentException("cannot create from null selector");
        }
        X509CRLStoreSelector cs = new X509CRLStoreSelector();
        cs.setCertificateChecking(selector.getCertificateChecking());
        cs.setDateAndTime(selector.getDateAndTime());
        try {
            cs.setIssuerNames(selector.getIssuerNames());
            cs.setIssuers(selector.getIssuers());
            cs.setMaxCRLNumber(selector.getMaxCRL());
            cs.setMinCRLNumber(selector.getMinCRL());
            return cs;
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public Object clone() {
        X509CRLStoreSelector sel = getInstance(this);
        sel.deltaCRLIndicator = this.deltaCRLIndicator;
        sel.completeCRLEnabled = this.completeCRLEnabled;
        sel.maxBaseCRLNumber = this.maxBaseCRLNumber;
        sel.attrCertChecking = this.attrCertChecking;
        sel.issuingDistributionPointEnabled = this.issuingDistributionPointEnabled;
        sel.issuingDistributionPoint = Arrays.clone(this.issuingDistributionPoint);
        return sel;
    }

    public boolean isCompleteCRLEnabled() {
        return this.completeCRLEnabled;
    }

    public void setCompleteCRLEnabled(boolean completeCRLEnabled) {
        this.completeCRLEnabled = completeCRLEnabled;
    }

    public BigInteger getMaxBaseCRLNumber() {
        return this.maxBaseCRLNumber;
    }

    public void setMaxBaseCRLNumber(BigInteger maxBaseCRLNumber) {
        this.maxBaseCRLNumber = maxBaseCRLNumber;
    }

    public byte[] getIssuingDistributionPoint() {
        return Arrays.clone(this.issuingDistributionPoint);
    }

    public void setIssuingDistributionPoint(byte[] issuingDistributionPoint) {
        this.issuingDistributionPoint = Arrays.clone(issuingDistributionPoint);
    }
}
