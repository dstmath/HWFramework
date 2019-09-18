package org.bouncycastle.asn1.pkcs;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.util.Arrays;

public class MacData extends ASN1Object {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    DigestInfo digInfo;
    BigInteger iterationCount;
    byte[] salt;

    private MacData(ASN1Sequence aSN1Sequence) {
        this.digInfo = DigestInfo.getInstance(aSN1Sequence.getObjectAt(0));
        this.salt = Arrays.clone(((ASN1OctetString) aSN1Sequence.getObjectAt(1)).getOctets());
        this.iterationCount = aSN1Sequence.size() == 3 ? ((ASN1Integer) aSN1Sequence.getObjectAt(2)).getValue() : ONE;
    }

    public MacData(DigestInfo digestInfo, byte[] bArr, int i) {
        this.digInfo = digestInfo;
        this.salt = Arrays.clone(bArr);
        this.iterationCount = BigInteger.valueOf((long) i);
    }

    public static MacData getInstance(Object obj) {
        if (obj instanceof MacData) {
            return (MacData) obj;
        }
        if (obj != null) {
            return new MacData(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public BigInteger getIterationCount() {
        return this.iterationCount;
    }

    public DigestInfo getMac() {
        return this.digInfo;
    }

    public byte[] getSalt() {
        return Arrays.clone(this.salt);
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.digInfo);
        aSN1EncodableVector.add(new DEROctetString(this.salt));
        if (!this.iterationCount.equals(ONE)) {
            aSN1EncodableVector.add(new ASN1Integer(this.iterationCount));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
