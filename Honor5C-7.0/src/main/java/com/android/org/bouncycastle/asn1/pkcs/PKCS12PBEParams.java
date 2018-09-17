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

public class PKCS12PBEParams extends ASN1Object {
    ASN1Integer iterations;
    ASN1OctetString iv;

    public PKCS12PBEParams(byte[] salt, int iterations) {
        this.iv = new DEROctetString(salt);
        this.iterations = new ASN1Integer((long) iterations);
    }

    private PKCS12PBEParams(ASN1Sequence seq) {
        this.iv = (ASN1OctetString) seq.getObjectAt(0);
        this.iterations = ASN1Integer.getInstance(seq.getObjectAt(1));
    }

    public static PKCS12PBEParams getInstance(Object obj) {
        if (obj instanceof PKCS12PBEParams) {
            return (PKCS12PBEParams) obj;
        }
        if (obj != null) {
            return new PKCS12PBEParams(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public BigInteger getIterations() {
        return this.iterations.getValue();
    }

    public byte[] getIV() {
        return this.iv.getOctets();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.iv);
        v.add(this.iterations);
        return new DERSequence(v);
    }
}
