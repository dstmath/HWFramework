package org.bouncycastle.asn1.pkcs;

import java.math.BigInteger;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class DHParameter extends ASN1Object {
    ASN1Integer g;
    ASN1Integer l;
    ASN1Integer p;

    public DHParameter(BigInteger bigInteger, BigInteger bigInteger2, int i) {
        this.p = new ASN1Integer(bigInteger);
        this.g = new ASN1Integer(bigInteger2);
        this.l = i != 0 ? new ASN1Integer((long) i) : null;
    }

    private DHParameter(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        this.p = ASN1Integer.getInstance(objects.nextElement());
        this.g = ASN1Integer.getInstance(objects.nextElement());
        this.l = objects.hasMoreElements() ? (ASN1Integer) objects.nextElement() : null;
    }

    public static DHParameter getInstance(Object obj) {
        if (obj instanceof DHParameter) {
            return (DHParameter) obj;
        }
        if (obj != null) {
            return new DHParameter(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public BigInteger getG() {
        return this.g.getPositiveValue();
    }

    public BigInteger getL() {
        if (this.l == null) {
            return null;
        }
        return this.l.getPositiveValue();
    }

    public BigInteger getP() {
        return this.p.getPositiveValue();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.p);
        aSN1EncodableVector.add(this.g);
        if (getL() != null) {
            aSN1EncodableVector.add(this.l);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
