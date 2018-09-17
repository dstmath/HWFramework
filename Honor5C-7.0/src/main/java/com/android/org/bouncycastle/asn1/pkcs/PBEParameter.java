package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.DERSequence;
import java.math.BigInteger;

public class PBEParameter extends ASN1Object {
    ASN1Integer iterations;
    ASN1OctetString salt;

    public PBEParameter(byte[] salt, int iterations) {
        if (salt.length != 8) {
            throw new IllegalArgumentException("salt length must be 8");
        }
        this.salt = new DEROctetString(salt);
        this.iterations = new ASN1Integer((long) iterations);
    }

    private PBEParameter(ASN1Sequence seq) {
        this.salt = (ASN1OctetString) seq.getObjectAt(0);
        this.iterations = (ASN1Integer) seq.getObjectAt(1);
    }

    public static PBEParameter getInstance(Object obj) {
        if (obj instanceof PBEParameter) {
            return (PBEParameter) obj;
        }
        if (obj != null) {
            return new PBEParameter(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public BigInteger getIterationCount() {
        return this.iterations.getValue();
    }

    public byte[] getSalt() {
        return this.salt.getOctets();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.salt);
        v.add(this.iterations);
        return new DERSequence(v);
    }
}
