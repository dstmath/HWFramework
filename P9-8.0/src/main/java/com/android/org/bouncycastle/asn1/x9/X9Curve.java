package com.android.org.bouncycastle.asn1.x9;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.math.ec.ECAlgorithms;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECCurve.F2m;
import com.android.org.bouncycastle.math.ec.ECCurve.Fp;
import java.math.BigInteger;

public class X9Curve extends ASN1Object implements X9ObjectIdentifiers {
    private ECCurve curve;
    private ASN1ObjectIdentifier fieldIdentifier = null;
    private byte[] seed;

    public X9Curve(ECCurve curve) {
        this.curve = curve;
        this.seed = null;
        setFieldIdentifier();
    }

    public X9Curve(ECCurve curve, byte[] seed) {
        this.curve = curve;
        this.seed = seed;
        setFieldIdentifier();
    }

    public X9Curve(X9FieldID fieldID, ASN1Sequence seq) {
        this.fieldIdentifier = fieldID.getIdentifier();
        if (this.fieldIdentifier.equals(prime_field)) {
            BigInteger p = ((ASN1Integer) fieldID.getParameters()).getValue();
            this.curve = new Fp(p, new X9FieldElement(p, (ASN1OctetString) seq.getObjectAt(0)).getValue().toBigInteger(), new X9FieldElement(p, (ASN1OctetString) seq.getObjectAt(1)).getValue().toBigInteger());
        } else if (this.fieldIdentifier.equals(characteristic_two_field)) {
            int k1;
            ASN1Sequence parameters = ASN1Sequence.getInstance(fieldID.getParameters());
            int m = ((ASN1Integer) parameters.getObjectAt(0)).getValue().intValue();
            ASN1ObjectIdentifier representation = (ASN1ObjectIdentifier) parameters.getObjectAt(1);
            int k2 = 0;
            int k3 = 0;
            if (representation.equals(tpBasis)) {
                k1 = ASN1Integer.getInstance(parameters.getObjectAt(2)).getValue().intValue();
            } else {
                if (representation.equals(ppBasis)) {
                    ASN1Sequence pentanomial = ASN1Sequence.getInstance(parameters.getObjectAt(2));
                    k1 = ASN1Integer.getInstance(pentanomial.getObjectAt(0)).getValue().intValue();
                    k2 = ASN1Integer.getInstance(pentanomial.getObjectAt(1)).getValue().intValue();
                    k3 = ASN1Integer.getInstance(pentanomial.getObjectAt(2)).getValue().intValue();
                } else {
                    throw new IllegalArgumentException("This type of EC basis is not implemented");
                }
            }
            X9FieldElement x9A = new X9FieldElement(m, k1, k2, k3, (ASN1OctetString) seq.getObjectAt(0));
            X9FieldElement x9B = new X9FieldElement(m, k1, k2, k3, (ASN1OctetString) seq.getObjectAt(1));
            this.curve = new F2m(m, k1, k2, k3, x9A.getValue().toBigInteger(), x9B.getValue().toBigInteger());
        } else {
            throw new IllegalArgumentException("This type of ECCurve is not implemented");
        }
        if (seq.size() == 3) {
            this.seed = ((DERBitString) seq.getObjectAt(2)).getBytes();
        }
    }

    private void setFieldIdentifier() {
        if (ECAlgorithms.isFpCurve(this.curve)) {
            this.fieldIdentifier = prime_field;
        } else if (ECAlgorithms.isF2mCurve(this.curve)) {
            this.fieldIdentifier = characteristic_two_field;
        } else {
            throw new IllegalArgumentException("This type of ECCurve is not implemented");
        }
    }

    public ECCurve getCurve() {
        return this.curve;
    }

    public byte[] getSeed() {
        return this.seed;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        if (this.fieldIdentifier.equals(prime_field)) {
            v.add(new X9FieldElement(this.curve.getA()).toASN1Primitive());
            v.add(new X9FieldElement(this.curve.getB()).toASN1Primitive());
        } else if (this.fieldIdentifier.equals(characteristic_two_field)) {
            v.add(new X9FieldElement(this.curve.getA()).toASN1Primitive());
            v.add(new X9FieldElement(this.curve.getB()).toASN1Primitive());
        }
        if (this.seed != null) {
            v.add(new DERBitString(this.seed));
        }
        return new DERSequence(v);
    }
}
