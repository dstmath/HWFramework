package org.bouncycastle.cms;

import java.math.BigInteger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.selector.X509CertificateHolderSelector;

public class KeyTransRecipientId extends RecipientId {
    private X509CertificateHolderSelector baseSelector;

    public KeyTransRecipientId(X500Name x500Name, BigInteger bigInteger) {
        this(x500Name, bigInteger, null);
    }

    public KeyTransRecipientId(X500Name x500Name, BigInteger bigInteger, byte[] bArr) {
        this(new X509CertificateHolderSelector(x500Name, bigInteger, bArr));
    }

    private KeyTransRecipientId(X509CertificateHolderSelector x509CertificateHolderSelector) {
        super(0);
        this.baseSelector = x509CertificateHolderSelector;
    }

    public KeyTransRecipientId(byte[] bArr) {
        this(null, null, bArr);
    }

    public Object clone() {
        return new KeyTransRecipientId(this.baseSelector);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof KeyTransRecipientId)) {
            return false;
        }
        return this.baseSelector.equals(((KeyTransRecipientId) obj).baseSelector);
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
        return obj instanceof KeyTransRecipientInformation ? ((KeyTransRecipientInformation) obj).getRID().equals(this) : this.baseSelector.match(obj);
    }
}
