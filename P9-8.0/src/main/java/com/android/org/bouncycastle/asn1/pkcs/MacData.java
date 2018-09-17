package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.x509.DigestInfo;
import com.android.org.bouncycastle.util.Arrays;
import java.math.BigInteger;

public class MacData extends ASN1Object {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    DigestInfo digInfo;
    BigInteger iterationCount;
    byte[] salt;

    public static MacData getInstance(Object obj) {
        if (obj instanceof MacData) {
            return (MacData) obj;
        }
        if (obj != null) {
            return new MacData(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    private MacData(ASN1Sequence seq) {
        this.digInfo = DigestInfo.getInstance(seq.getObjectAt(0));
        this.salt = Arrays.clone(((ASN1OctetString) seq.getObjectAt(1)).getOctets());
        if (seq.size() == 3) {
            this.iterationCount = ((ASN1Integer) seq.getObjectAt(2)).getValue();
        } else {
            this.iterationCount = ONE;
        }
    }

    public MacData(DigestInfo digInfo, byte[] salt, int iterationCount) {
        this.digInfo = digInfo;
        this.salt = Arrays.clone(salt);
        this.iterationCount = BigInteger.valueOf((long) iterationCount);
    }

    public DigestInfo getMac() {
        return this.digInfo;
    }

    public byte[] getSalt() {
        return Arrays.clone(this.salt);
    }

    public BigInteger getIterationCount() {
        return this.iterationCount;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.digInfo);
        v.add(new DEROctetString(this.salt));
        if (!this.iterationCount.equals(ONE)) {
            v.add(new ASN1Integer(this.iterationCount));
        }
        return new DERSequence(v);
    }
}
