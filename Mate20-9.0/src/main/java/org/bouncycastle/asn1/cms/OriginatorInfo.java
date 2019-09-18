package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class OriginatorInfo extends ASN1Object {
    private ASN1Set certs;
    private ASN1Set crls;

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x005b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0026, code lost:
        r3.crls = org.bouncycastle.asn1.ASN1Set.getInstance(r4, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x002c, code lost:
        return;
     */
    private OriginatorInfo(ASN1Sequence aSN1Sequence) {
        ASN1TaggedObject aSN1TaggedObject;
        switch (aSN1Sequence.size()) {
            case 0:
                break;
            case 1:
                aSN1TaggedObject = (ASN1TaggedObject) aSN1Sequence.getObjectAt(0);
                switch (aSN1TaggedObject.getTagNo()) {
                    case 0:
                        this.certs = ASN1Set.getInstance(aSN1TaggedObject, false);
                        break;
                    case 1:
                        break;
                    default:
                        throw new IllegalArgumentException("Bad tag in OriginatorInfo: " + aSN1TaggedObject.getTagNo());
                }
            case 2:
                this.certs = ASN1Set.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(0), false);
                aSN1TaggedObject = (ASN1TaggedObject) aSN1Sequence.getObjectAt(1);
                break;
            default:
                throw new IllegalArgumentException("OriginatorInfo too big");
        }
    }

    public OriginatorInfo(ASN1Set aSN1Set, ASN1Set aSN1Set2) {
        this.certs = aSN1Set;
        this.crls = aSN1Set2;
    }

    public static OriginatorInfo getInstance(Object obj) {
        if (obj instanceof OriginatorInfo) {
            return (OriginatorInfo) obj;
        }
        if (obj != null) {
            return new OriginatorInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static OriginatorInfo getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public ASN1Set getCRLs() {
        return this.crls;
    }

    public ASN1Set getCertificates() {
        return this.certs;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (this.certs != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.certs));
        }
        if (this.crls != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.crls));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
