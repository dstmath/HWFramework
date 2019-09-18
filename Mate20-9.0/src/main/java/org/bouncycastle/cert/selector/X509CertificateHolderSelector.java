package org.bouncycastle.cert.selector;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Selector;

public class X509CertificateHolderSelector implements Selector {
    private X500Name issuer;
    private BigInteger serialNumber;
    private byte[] subjectKeyId;

    public X509CertificateHolderSelector(X500Name x500Name, BigInteger bigInteger) {
        this(x500Name, bigInteger, null);
    }

    public X509CertificateHolderSelector(X500Name x500Name, BigInteger bigInteger, byte[] bArr) {
        this.issuer = x500Name;
        this.serialNumber = bigInteger;
        this.subjectKeyId = bArr;
    }

    public X509CertificateHolderSelector(byte[] bArr) {
        this(null, null, bArr);
    }

    private boolean equalsObj(Object obj, Object obj2) {
        return obj != null ? obj.equals(obj2) : obj2 == null;
    }

    public Object clone() {
        return new X509CertificateHolderSelector(this.issuer, this.serialNumber, this.subjectKeyId);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof X509CertificateHolderSelector)) {
            return false;
        }
        X509CertificateHolderSelector x509CertificateHolderSelector = (X509CertificateHolderSelector) obj;
        if (Arrays.areEqual(this.subjectKeyId, x509CertificateHolderSelector.subjectKeyId) && equalsObj(this.serialNumber, x509CertificateHolderSelector.serialNumber) && equalsObj(this.issuer, x509CertificateHolderSelector.issuer)) {
            z = true;
        }
        return z;
    }

    public X500Name getIssuer() {
        return this.issuer;
    }

    public BigInteger getSerialNumber() {
        return this.serialNumber;
    }

    public byte[] getSubjectKeyIdentifier() {
        return Arrays.clone(this.subjectKeyId);
    }

    public int hashCode() {
        int hashCode = Arrays.hashCode(this.subjectKeyId);
        if (this.serialNumber != null) {
            hashCode ^= this.serialNumber.hashCode();
        }
        return this.issuer != null ? hashCode ^ this.issuer.hashCode() : hashCode;
    }

    public boolean match(Object obj) {
        byte[] bArr;
        byte[] bArr2;
        boolean z = false;
        if (obj instanceof X509CertificateHolder) {
            X509CertificateHolder x509CertificateHolder = (X509CertificateHolder) obj;
            if (getSerialNumber() != null) {
                IssuerAndSerialNumber issuerAndSerialNumber = new IssuerAndSerialNumber(x509CertificateHolder.toASN1Structure());
                if (issuerAndSerialNumber.getName().equals(this.issuer) && issuerAndSerialNumber.getSerialNumber().getValue().equals(this.serialNumber)) {
                    z = true;
                }
                return z;
            }
            if (this.subjectKeyId != null) {
                Extension extension = x509CertificateHolder.getExtension(Extension.subjectKeyIdentifier);
                if (extension == null) {
                    bArr = this.subjectKeyId;
                    bArr2 = MSOutlookKeyIdCalculator.calculateKeyId(x509CertificateHolder.getSubjectPublicKeyInfo());
                } else {
                    bArr2 = ASN1OctetString.getInstance(extension.getParsedValue()).getOctets();
                    bArr = this.subjectKeyId;
                }
            }
            return false;
        }
        if (obj instanceof byte[]) {
            bArr = this.subjectKeyId;
            bArr2 = (byte[]) obj;
        }
        return false;
        return Arrays.areEqual(bArr, bArr2);
    }
}
