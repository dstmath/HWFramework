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
import org.bouncycastle.util.Arrays;

public class X9Curve extends ASN1Object implements X9ObjectIdentifiers {
    private ECCurve curve;
    private ASN1ObjectIdentifier fieldIdentifier;
    private byte[] seed;

    public X9Curve(X9FieldID x9FieldID, BigInteger bigInteger, BigInteger bigInteger2, ASN1Sequence aSN1Sequence) {
        int i;
        int i2;
        int i3;
        ECCurve f2m;
        this.fieldIdentifier = null;
        this.fieldIdentifier = x9FieldID.getIdentifier();
        if (this.fieldIdentifier.equals((ASN1Primitive) prime_field)) {
            f2m = new ECCurve.Fp(((ASN1Integer) x9FieldID.getParameters()).getValue(), new BigInteger(1, ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(0)).getOctets()), new BigInteger(1, ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(1)).getOctets()), bigInteger, bigInteger2);
        } else if (this.fieldIdentifier.equals((ASN1Primitive) characteristic_two_field)) {
            ASN1Sequence instance = ASN1Sequence.getInstance(x9FieldID.getParameters());
            int intValueExact = ((ASN1Integer) instance.getObjectAt(0)).intValueExact();
            ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) instance.getObjectAt(1);
            if (aSN1ObjectIdentifier.equals((ASN1Primitive) tpBasis)) {
                i3 = ASN1Integer.getInstance(instance.getObjectAt(2)).intValueExact();
                i2 = 0;
                i = 0;
            } else if (aSN1ObjectIdentifier.equals((ASN1Primitive) ppBasis)) {
                ASN1Sequence instance2 = ASN1Sequence.getInstance(instance.getObjectAt(2));
                int intValueExact2 = ASN1Integer.getInstance(instance2.getObjectAt(0)).intValueExact();
                int intValueExact3 = ASN1Integer.getInstance(instance2.getObjectAt(1)).intValueExact();
                i = ASN1Integer.getInstance(instance2.getObjectAt(2)).intValueExact();
                i3 = intValueExact2;
                i2 = intValueExact3;
            } else {
                throw new IllegalArgumentException("This type of EC basis is not implemented");
            }
            f2m = new ECCurve.F2m(intValueExact, i3, i2, i, new BigInteger(1, ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(0)).getOctets()), new BigInteger(1, ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(1)).getOctets()), bigInteger, bigInteger2);
        } else {
            throw new IllegalArgumentException("This type of ECCurve is not implemented");
        }
        this.curve = f2m;
        if (aSN1Sequence.size() == 3) {
            this.seed = ((DERBitString) aSN1Sequence.getObjectAt(2)).getBytes();
        }
    }

    public X9Curve(ECCurve eCCurve) {
        this(eCCurve, null);
    }

    public X9Curve(ECCurve eCCurve, byte[] bArr) {
        this.fieldIdentifier = null;
        this.curve = eCCurve;
        this.seed = Arrays.clone(bArr);
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
        return Arrays.clone(this.seed);
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0061  */
    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        byte[] bArr;
        X9FieldElement x9FieldElement;
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(3);
        if (this.fieldIdentifier.equals((ASN1Primitive) prime_field)) {
            aSN1EncodableVector.add(new X9FieldElement(this.curve.getA()).toASN1Primitive());
            x9FieldElement = new X9FieldElement(this.curve.getB());
        } else {
            if (this.fieldIdentifier.equals((ASN1Primitive) characteristic_two_field)) {
                aSN1EncodableVector.add(new X9FieldElement(this.curve.getA()).toASN1Primitive());
                x9FieldElement = new X9FieldElement(this.curve.getB());
            }
            bArr = this.seed;
            if (bArr != null) {
                aSN1EncodableVector.add(new DERBitString(bArr));
            }
            return new DERSequence(aSN1EncodableVector);
        }
        aSN1EncodableVector.add(x9FieldElement.toASN1Primitive());
        bArr = this.seed;
        if (bArr != null) {
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
