package org.bouncycastle.cms;

import java.math.BigInteger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Selector;

class OriginatorId implements Selector {
    private X500Name issuer;
    private BigInteger serialNumber;
    private byte[] subjectKeyId;

    public OriginatorId(X500Name x500Name, BigInteger bigInteger) {
        setIssuerAndSerial(x500Name, bigInteger);
    }

    public OriginatorId(X500Name x500Name, BigInteger bigInteger, byte[] bArr) {
        setIssuerAndSerial(x500Name, bigInteger);
        setSubjectKeyID(bArr);
    }

    public OriginatorId(byte[] bArr) {
        setSubjectKeyID(bArr);
    }

    private boolean equalsObj(Object obj, Object obj2) {
        return obj != null ? obj.equals(obj2) : obj2 == null;
    }

    private void setIssuerAndSerial(X500Name x500Name, BigInteger bigInteger) {
        this.issuer = x500Name;
        this.serialNumber = bigInteger;
    }

    private void setSubjectKeyID(byte[] bArr) {
        this.subjectKeyId = bArr;
    }

    public Object clone() {
        return new OriginatorId(this.issuer, this.serialNumber, this.subjectKeyId);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof OriginatorId)) {
            return false;
        }
        OriginatorId originatorId = (OriginatorId) obj;
        if (Arrays.areEqual(this.subjectKeyId, originatorId.subjectKeyId) && equalsObj(this.serialNumber, originatorId.serialNumber) && equalsObj(this.issuer, originatorId.issuer)) {
            z = true;
        }
        return z;
    }

    public X500Name getIssuer() {
        return this.issuer;
    }

    public int hashCode() {
        int hashCode = Arrays.hashCode(this.subjectKeyId);
        if (this.serialNumber != null) {
            hashCode ^= this.serialNumber.hashCode();
        }
        return this.issuer != null ? hashCode ^ this.issuer.hashCode() : hashCode;
    }

    public boolean match(Object obj) {
        return false;
    }
}
