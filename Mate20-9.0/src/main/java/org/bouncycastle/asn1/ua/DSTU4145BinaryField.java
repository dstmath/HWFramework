package org.bouncycastle.asn1.ua;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class DSTU4145BinaryField extends ASN1Object {
    private int j;
    private int k;
    private int l;
    private int m;

    public DSTU4145BinaryField(int i, int i2) {
        this(i, i2, 0, 0);
    }

    public DSTU4145BinaryField(int i, int i2, int i3, int i4) {
        this.m = i;
        this.k = i2;
        this.j = i3;
        this.l = i4;
    }

    private DSTU4145BinaryField(ASN1Sequence aSN1Sequence) {
        this.m = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0)).getPositiveValue().intValue();
        if (aSN1Sequence.getObjectAt(1) instanceof ASN1Integer) {
            this.k = ((ASN1Integer) aSN1Sequence.getObjectAt(1)).getPositiveValue().intValue();
        } else if (aSN1Sequence.getObjectAt(1) instanceof ASN1Sequence) {
            ASN1Sequence instance = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(1));
            this.k = ASN1Integer.getInstance(instance.getObjectAt(0)).getPositiveValue().intValue();
            this.j = ASN1Integer.getInstance(instance.getObjectAt(1)).getPositiveValue().intValue();
            this.l = ASN1Integer.getInstance(instance.getObjectAt(2)).getPositiveValue().intValue();
        } else {
            throw new IllegalArgumentException("object parse error");
        }
    }

    public static DSTU4145BinaryField getInstance(Object obj) {
        if (obj instanceof DSTU4145BinaryField) {
            return (DSTU4145BinaryField) obj;
        }
        if (obj != null) {
            return new DSTU4145BinaryField(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public int getK1() {
        return this.k;
    }

    public int getK2() {
        return this.j;
    }

    public int getK3() {
        return this.l;
    }

    public int getM() {
        return this.m;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(new ASN1Integer((long) this.m));
        if (this.j == 0) {
            aSN1EncodableVector.add(new ASN1Integer((long) this.k));
        } else {
            ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
            aSN1EncodableVector2.add(new ASN1Integer((long) this.k));
            aSN1EncodableVector2.add(new ASN1Integer((long) this.j));
            aSN1EncodableVector2.add(new ASN1Integer((long) this.l));
            aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
