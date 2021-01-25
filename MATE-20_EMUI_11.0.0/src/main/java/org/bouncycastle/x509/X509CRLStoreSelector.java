package org.bouncycastle.x509;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CRL;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Selector;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

public class X509CRLStoreSelector extends X509CRLSelector implements Selector {
    private X509AttributeCertificate attrCertChecking;
    private boolean completeCRLEnabled = false;
    private boolean deltaCRLIndicator = false;
    private byte[] issuingDistributionPoint = null;
    private boolean issuingDistributionPointEnabled = false;
    private BigInteger maxBaseCRLNumber = null;

    public static X509CRLStoreSelector getInstance(X509CRLSelector x509CRLSelector) {
        if (x509CRLSelector != null) {
            X509CRLStoreSelector x509CRLStoreSelector = new X509CRLStoreSelector();
            x509CRLStoreSelector.setCertificateChecking(x509CRLSelector.getCertificateChecking());
            x509CRLStoreSelector.setDateAndTime(x509CRLSelector.getDateAndTime());
            try {
                x509CRLStoreSelector.setIssuerNames(x509CRLSelector.getIssuerNames());
                x509CRLStoreSelector.setIssuers(x509CRLSelector.getIssuers());
                x509CRLStoreSelector.setMaxCRLNumber(x509CRLSelector.getMaxCRL());
                x509CRLStoreSelector.setMinCRLNumber(x509CRLSelector.getMinCRL());
                return x509CRLStoreSelector;
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("cannot create from null selector");
        }
    }

    @Override // java.security.cert.X509CRLSelector, java.security.cert.CRLSelector, java.lang.Object, org.bouncycastle.util.Selector
    public Object clone() {
        X509CRLStoreSelector instance = getInstance(this);
        instance.deltaCRLIndicator = this.deltaCRLIndicator;
        instance.completeCRLEnabled = this.completeCRLEnabled;
        instance.maxBaseCRLNumber = this.maxBaseCRLNumber;
        instance.attrCertChecking = this.attrCertChecking;
        instance.issuingDistributionPointEnabled = this.issuingDistributionPointEnabled;
        instance.issuingDistributionPoint = Arrays.clone(this.issuingDistributionPoint);
        return instance;
    }

    public X509AttributeCertificate getAttrCertificateChecking() {
        return this.attrCertChecking;
    }

    public byte[] getIssuingDistributionPoint() {
        return Arrays.clone(this.issuingDistributionPoint);
    }

    public BigInteger getMaxBaseCRLNumber() {
        return this.maxBaseCRLNumber;
    }

    public boolean isCompleteCRLEnabled() {
        return this.completeCRLEnabled;
    }

    public boolean isDeltaCRLIndicatorEnabled() {
        return this.deltaCRLIndicator;
    }

    public boolean isIssuingDistributionPointEnabled() {
        return this.issuingDistributionPointEnabled;
    }

    @Override // org.bouncycastle.util.Selector
    public boolean match(Object obj) {
        if (!(obj instanceof X509CRL)) {
            return false;
        }
        X509CRL x509crl = (X509CRL) obj;
        ASN1Integer aSN1Integer = null;
        try {
            byte[] extensionValue = x509crl.getExtensionValue(Extension.deltaCRLIndicator.getId());
            if (extensionValue != null) {
                aSN1Integer = ASN1Integer.getInstance(X509ExtensionUtil.fromExtensionValue(extensionValue));
            }
            if (isDeltaCRLIndicatorEnabled() && aSN1Integer == null) {
                return false;
            }
            if (isCompleteCRLEnabled() && aSN1Integer != null) {
                return false;
            }
            if (aSN1Integer != null && this.maxBaseCRLNumber != null && aSN1Integer.getPositiveValue().compareTo(this.maxBaseCRLNumber) == 1) {
                return false;
            }
            if (this.issuingDistributionPointEnabled) {
                byte[] extensionValue2 = x509crl.getExtensionValue(Extension.issuingDistributionPoint.getId());
                byte[] bArr = this.issuingDistributionPoint;
                if (bArr == null) {
                    if (extensionValue2 != null) {
                        return false;
                    }
                } else if (!Arrays.areEqual(extensionValue2, bArr)) {
                    return false;
                }
            }
            return super.match((CRL) x509crl);
        } catch (Exception e) {
            return false;
        }
    }

    @Override // java.security.cert.X509CRLSelector, java.security.cert.CRLSelector
    public boolean match(CRL crl) {
        return match((Object) crl);
    }

    public void setAttrCertificateChecking(X509AttributeCertificate x509AttributeCertificate) {
        this.attrCertChecking = x509AttributeCertificate;
    }

    public void setCompleteCRLEnabled(boolean z) {
        this.completeCRLEnabled = z;
    }

    public void setDeltaCRLIndicatorEnabled(boolean z) {
        this.deltaCRLIndicator = z;
    }

    public void setIssuingDistributionPoint(byte[] bArr) {
        this.issuingDistributionPoint = Arrays.clone(bArr);
    }

    public void setIssuingDistributionPointEnabled(boolean z) {
        this.issuingDistributionPointEnabled = z;
    }

    public void setMaxBaseCRLNumber(BigInteger bigInteger) {
        this.maxBaseCRLNumber = bigInteger;
    }
}
