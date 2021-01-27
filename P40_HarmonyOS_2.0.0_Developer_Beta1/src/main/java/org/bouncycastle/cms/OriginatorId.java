package org.bouncycastle.cms;

import java.math.BigInteger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Selector;

/* access modifiers changed from: package-private */
public class OriginatorId implements Selector {
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

    @Override // org.bouncycastle.util.Selector, java.lang.Object
    public Object clone() {
        return new OriginatorId(this.issuer, this.serialNumber, this.subjectKeyId);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof OriginatorId)) {
            return false;
        }
        OriginatorId originatorId = (OriginatorId) obj;
        return Arrays.areEqual(this.subjectKeyId, originatorId.subjectKeyId) && equalsObj(this.serialNumber, originatorId.serialNumber) && equalsObj(this.issuer, originatorId.issuer);
    }

    public X500Name getIssuer() {
        return this.issuer;
    }

    @Override // java.lang.Object
    public int hashCode() {
        int hashCode = Arrays.hashCode(this.subjectKeyId);
        BigInteger bigInteger = this.serialNumber;
        if (bigInteger != null) {
            hashCode ^= bigInteger.hashCode();
        }
        X500Name x500Name = this.issuer;
        return x500Name != null ? hashCode ^ x500Name.hashCode() : hashCode;
    }

    @Override // org.bouncycastle.util.Selector
    public boolean match(Object obj) {
        return false;
    }
}
