package org.bouncycastle.asn1.x509;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class PrivateKeyUsagePeriod extends ASN1Object {
    private ASN1GeneralizedTime _notAfter;
    private ASN1GeneralizedTime _notBefore;

    private PrivateKeyUsagePeriod(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        while (objects.hasMoreElements()) {
            ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) objects.nextElement();
            if (aSN1TaggedObject.getTagNo() == 0) {
                this._notBefore = ASN1GeneralizedTime.getInstance(aSN1TaggedObject, false);
            } else if (aSN1TaggedObject.getTagNo() == 1) {
                this._notAfter = ASN1GeneralizedTime.getInstance(aSN1TaggedObject, false);
            }
        }
    }

    public static PrivateKeyUsagePeriod getInstance(Object obj) {
        if (obj instanceof PrivateKeyUsagePeriod) {
            return (PrivateKeyUsagePeriod) obj;
        }
        if (obj != null) {
            return new PrivateKeyUsagePeriod(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1GeneralizedTime getNotAfter() {
        return this._notAfter;
    }

    public ASN1GeneralizedTime getNotBefore() {
        return this._notBefore;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        ASN1GeneralizedTime aSN1GeneralizedTime = this._notBefore;
        if (aSN1GeneralizedTime != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, aSN1GeneralizedTime));
        }
        ASN1GeneralizedTime aSN1GeneralizedTime2 = this._notAfter;
        if (aSN1GeneralizedTime2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, aSN1GeneralizedTime2));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
