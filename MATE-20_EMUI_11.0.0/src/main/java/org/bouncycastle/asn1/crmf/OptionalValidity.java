package org.bouncycastle.asn1.crmf;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.Time;

public class OptionalValidity extends ASN1Object {
    private Time notAfter;
    private Time notBefore;

    private OptionalValidity(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        while (objects.hasMoreElements()) {
            ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) objects.nextElement();
            int tagNo = aSN1TaggedObject.getTagNo();
            Time instance = Time.getInstance(aSN1TaggedObject, true);
            if (tagNo == 0) {
                this.notBefore = instance;
            } else {
                this.notAfter = instance;
            }
        }
    }

    public OptionalValidity(Time time, Time time2) {
        if (time == null && time2 == null) {
            throw new IllegalArgumentException("at least one of notBefore/notAfter must not be null.");
        }
        this.notBefore = time;
        this.notAfter = time2;
    }

    public static OptionalValidity getInstance(Object obj) {
        if (obj instanceof OptionalValidity) {
            return (OptionalValidity) obj;
        }
        if (obj != null) {
            return new OptionalValidity(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public Time getNotAfter() {
        return this.notAfter;
    }

    public Time getNotBefore() {
        return this.notBefore;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        Time time = this.notBefore;
        if (time != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, time));
        }
        Time time2 = this.notAfter;
        if (time2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 1, time2));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
