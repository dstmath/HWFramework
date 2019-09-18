package org.bouncycastle.cms;

import java.math.BigInteger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.selector.X509CertificateHolderSelector;
import org.bouncycastle.util.Selector;

public class SignerId implements Selector {
    private X509CertificateHolderSelector baseSelector;

    public SignerId(X500Name x500Name, BigInteger bigInteger) {
        this(x500Name, bigInteger, null);
    }

    public SignerId(X500Name x500Name, BigInteger bigInteger, byte[] bArr) {
        this(new X509CertificateHolderSelector(x500Name, bigInteger, bArr));
    }

    private SignerId(X509CertificateHolderSelector x509CertificateHolderSelector) {
        this.baseSelector = x509CertificateHolderSelector;
    }

    public SignerId(byte[] bArr) {
        this(null, null, bArr);
    }

    public Object clone() {
        return new SignerId(this.baseSelector);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SignerId)) {
            return false;
        }
        return this.baseSelector.equals(((SignerId) obj).baseSelector);
    }

    public X500Name getIssuer() {
        return this.baseSelector.getIssuer();
    }

    public BigInteger getSerialNumber() {
        return this.baseSelector.getSerialNumber();
    }

    public byte[] getSubjectKeyIdentifier() {
        return this.baseSelector.getSubjectKeyIdentifier();
    }

    public int hashCode() {
        return this.baseSelector.hashCode();
    }

    public boolean match(Object obj) {
        return obj instanceof SignerInformation ? ((SignerInformation) obj).getSID().equals(this) : this.baseSelector.match(obj);
    }
}
