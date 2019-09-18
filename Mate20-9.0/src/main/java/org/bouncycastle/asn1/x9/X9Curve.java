package org.bouncycastle.asn1.x9;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;

public class X9Curve extends ASN1Object implements X9ObjectIdentifiers {
    private ECCurve curve;
    private ASN1ObjectIdentifier fieldIdentifier;
    private byte[] seed;

    /* JADX WARNING: type inference failed for: r2v25, types: [org.bouncycastle.math.ec.ECCurve] */
    /* JADX WARNING: type inference failed for: r7v9, types: [org.bouncycastle.math.ec.ECCurve$F2m] */
    /* JADX WARNING: type inference failed for: r6v12, types: [org.bouncycastle.math.ec.ECCurve$Fp] */
    /* JADX WARNING: Multi-variable type inference failed */
    public X9Curve(X9FieldID x9FieldID, BigInteger bigInteger, BigInteger bigInteger2, ASN1Sequence aSN1Sequence) {
        int i;
        int i2;
        int i3;
        ? r2;
        ASN1Sequence aSN1Sequence2 = aSN1Sequence;
        this.fieldIdentifier = null;
        this.fieldIdentifier = x9FieldID.getIdentifier();
        if (this.fieldIdentifier.equals(prime_field)) {
            ECCurve.Fp fp = new ECCurve.Fp(((ASN1Integer) x9FieldID.getParameters()).getValue(), new BigInteger(1, ASN1OctetString.getInstance(aSN1Sequence2.getObjectAt(0)).getOctets()), new BigInteger(1, ASN1OctetString.getInstance(aSN1Sequence2.getObjectAt(1)).getOctets()), bigInteger, bigInteger2);
            r2 = fp;
        } else if (this.fieldIdentifier.equals(characteristic_two_field)) {
            ASN1Sequence instance = ASN1Sequence.getInstance(x9FieldID.getParameters());
            int intValue = ((ASN1Integer) instance.getObjectAt(0)).getValue().intValue();
            ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) instance.getObjectAt(1);
            if (aSN1ObjectIdentifier.equals(tpBasis)) {
                i3 = ASN1Integer.getInstance(instance.getObjectAt(2)).getValue().intValue();
                i2 = 0;
                i = 0;
            } else if (aSN1ObjectIdentifier.equals(ppBasis)) {
                ASN1Sequence instance2 = ASN1Sequence.getInstance(instance.getObjectAt(2));
                int intValue2 = ASN1Integer.getInstance(instance2.getObjectAt(0)).getValue().intValue();
                int intValue3 = ASN1Integer.getInstance(instance2.getObjectAt(1)).getValue().intValue();
                i = ASN1Integer.getInstance(instance2.getObjectAt(2)).getValue().intValue();
                i3 = intValue2;
                i2 = intValue3;
            } else {
                throw new IllegalArgumentException("This type of EC basis is not implemented");
            }
            ECCurve.F2m f2m = new ECCurve.F2m(intValue, i3, i2, i, new BigInteger(1, ASN1OctetString.getInstance(aSN1Sequence2.getObjectAt(0)).getOctets()), new BigInteger(1, ASN1OctetString.getInstance(aSN1Sequence2.getObjectAt(1)).getOctets()), bigInteger, bigInteger2);
            r2 = f2m;
        } else {
            throw new IllegalArgumentException("This type of ECCurve is not implemented");
        }
        this.curve = r2;
        if (aSN1Sequence.size() == 3) {
            this.seed = ((DERBitString) aSN1Sequence2.getObjectAt(2)).getBytes();
        }
    }

    public X9Curve(X9FieldID x9FieldID, ASN1Sequence aSN1Sequence) {
        this(x9FieldID, null, null, aSN1Sequence);
    }

    public X9Curve(ECCurve eCCurve) {
        this.fieldIdentifier = null;
        this.curve = eCCurve;
        this.seed = null;
        setFieldIdentifier();
    }

    public X9Curve(ECCurve eCCurve, byte[] bArr) {
        this.fieldIdentifier = null;
        this.curve = eCCurve;
        this.seed = bArr;
        setFieldIdentifier();
    }

    private void setFieldIdentifier() {
        ASN1ObjectIdentifier aSN1ObjectIdentifier;
        if (ECAlgorithms.isFpCurve(this.curve)) {
            aSN1ObjectIdentifier = prime_field;
        } else if (ECAlgorithms.isF2mCurve(this.curve)) {
            aSN1ObjectIdentifier = characteristic_two_field;
        } else {
            throw new IllegalArgumentException("This type of ECCurve is not implemented");
        }
        this.fieldIdentifier = aSN1ObjectIdentifier;
    }

    public ECCurve getCurve() {
        return this.curve;
    }

    public byte[] getSeed() {
        return this.seed;
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0060  */
    public ASN1Primitive toASN1Primitive() {
        X9FieldElement x9FieldElement;
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (this.fieldIdentifier.equals(prime_field)) {
            aSN1EncodableVector.add(new X9FieldElement(this.curve.getA()).toASN1Primitive());
            x9FieldElement = new X9FieldElement(this.curve.getB());
        } else {
            if (this.fieldIdentifier.equals(characteristic_two_field)) {
                aSN1EncodableVector.add(new X9FieldElement(this.curve.getA()).toASN1Primitive());
                x9FieldElement = new X9FieldElement(this.curve.getB());
            }
            if (this.seed != null) {
                aSN1EncodableVector.add(new DERBitString(this.seed));
            }
            return new DERSequence(aSN1EncodableVector);
        }
        aSN1EncodableVector.add(x9FieldElement.toASN1Primitive());
        if (this.seed != null) {
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
