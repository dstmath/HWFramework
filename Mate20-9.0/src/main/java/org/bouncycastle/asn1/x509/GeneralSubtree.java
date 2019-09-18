package org.bouncycastle.asn1.x509;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class GeneralSubtree extends ASN1Object {
    private static final BigInteger ZERO = BigInteger.valueOf(0);
    private GeneralName base;
    private ASN1Integer maximum;
    private ASN1Integer minimum;

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00b5, code lost:
        r4.maximum = org.bouncycastle.asn1.ASN1Integer.getInstance(r5, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00bb, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00c2, code lost:
        return;
     */
    private GeneralSubtree(ASN1Sequence aSN1Sequence) {
        ASN1TaggedObject aSN1TaggedObject;
        this.base = GeneralName.getInstance(aSN1Sequence.getObjectAt(0));
        switch (aSN1Sequence.size()) {
            case 1:
                break;
            case 2:
                aSN1TaggedObject = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(1));
                switch (aSN1TaggedObject.getTagNo()) {
                    case 0:
                        this.minimum = ASN1Integer.getInstance(aSN1TaggedObject, false);
                        break;
                    case 1:
                        break;
                    default:
                        throw new IllegalArgumentException("Bad tag number: " + aSN1TaggedObject.getTagNo());
                }
            case 3:
                ASN1TaggedObject instance = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(1));
                if (instance.getTagNo() == 0) {
                    this.minimum = ASN1Integer.getInstance(instance, false);
                    aSN1TaggedObject = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(2));
                    if (aSN1TaggedObject.getTagNo() != 1) {
                        throw new IllegalArgumentException("Bad tag number for 'maximum': " + aSN1TaggedObject.getTagNo());
                    }
                } else {
                    throw new IllegalArgumentException("Bad tag number for 'minimum': " + instance.getTagNo());
                }
                break;
            default:
                throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
        }
    }

    public GeneralSubtree(GeneralName generalName) {
        this(generalName, null, null);
    }

    public GeneralSubtree(GeneralName generalName, BigInteger bigInteger, BigInteger bigInteger2) {
        this.base = generalName;
        if (bigInteger2 != null) {
            this.maximum = new ASN1Integer(bigInteger2);
        }
        this.minimum = bigInteger == null ? null : new ASN1Integer(bigInteger);
    }

    public static GeneralSubtree getInstance(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj instanceof GeneralSubtree ? (GeneralSubtree) obj : new GeneralSubtree(ASN1Sequence.getInstance(obj));
    }

    public static GeneralSubtree getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return new GeneralSubtree(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public GeneralName getBase() {
        return this.base;
    }

    public BigInteger getMaximum() {
        if (this.maximum == null) {
            return null;
        }
        return this.maximum.getValue();
    }

    public BigInteger getMinimum() {
        return this.minimum == null ? ZERO : this.minimum.getValue();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.base);
        if (this.minimum != null && !this.minimum.getValue().equals(ZERO)) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.minimum));
        }
        if (this.maximum != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.maximum));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
