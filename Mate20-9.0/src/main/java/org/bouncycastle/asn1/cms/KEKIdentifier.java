package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;

public class KEKIdentifier extends ASN1Object {
    private ASN1GeneralizedTime date;
    private ASN1OctetString keyIdentifier;
    private OtherKeyAttribute other;

    private KEKIdentifier(ASN1Sequence aSN1Sequence) {
        ASN1Encodable objectAt;
        this.keyIdentifier = (ASN1OctetString) aSN1Sequence.getObjectAt(0);
        switch (aSN1Sequence.size()) {
            case 1:
                return;
            case 2:
                if (!(aSN1Sequence.getObjectAt(1) instanceof ASN1GeneralizedTime)) {
                    objectAt = aSN1Sequence.getObjectAt(1);
                    break;
                } else {
                    this.date = (ASN1GeneralizedTime) aSN1Sequence.getObjectAt(1);
                    return;
                }
            case 3:
                this.date = (ASN1GeneralizedTime) aSN1Sequence.getObjectAt(1);
                objectAt = aSN1Sequence.getObjectAt(2);
                break;
            default:
                throw new IllegalArgumentException("Invalid KEKIdentifier");
        }
        this.other = OtherKeyAttribute.getInstance(objectAt);
    }

    public KEKIdentifier(byte[] bArr, ASN1GeneralizedTime aSN1GeneralizedTime, OtherKeyAttribute otherKeyAttribute) {
        this.keyIdentifier = new DEROctetString(bArr);
        this.date = aSN1GeneralizedTime;
        this.other = otherKeyAttribute;
    }

    public static KEKIdentifier getInstance(Object obj) {
        if (obj == null || (obj instanceof KEKIdentifier)) {
            return (KEKIdentifier) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new KEKIdentifier((ASN1Sequence) obj);
        }
        throw new IllegalArgumentException("Invalid KEKIdentifier: " + obj.getClass().getName());
    }

    public static KEKIdentifier getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public ASN1GeneralizedTime getDate() {
        return this.date;
    }

    public ASN1OctetString getKeyIdentifier() {
        return this.keyIdentifier;
    }

    public OtherKeyAttribute getOther() {
        return this.other;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.keyIdentifier);
        if (this.date != null) {
            aSN1EncodableVector.add(this.date);
        }
        if (this.other != null) {
            aSN1EncodableVector.add(this.other);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
