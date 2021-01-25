package org.bouncycastle.asn1.esf;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class CrlOcspRef extends ASN1Object {
    private CrlListID crlids;
    private OcspListID ocspids;
    private OtherRevRefs otherRev;

    private CrlOcspRef(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        while (objects.hasMoreElements()) {
            ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) objects.nextElement();
            int tagNo = aSN1TaggedObject.getTagNo();
            if (tagNo == 0) {
                this.crlids = CrlListID.getInstance(aSN1TaggedObject.getObject());
            } else if (tagNo == 1) {
                this.ocspids = OcspListID.getInstance(aSN1TaggedObject.getObject());
            } else if (tagNo == 2) {
                this.otherRev = OtherRevRefs.getInstance(aSN1TaggedObject.getObject());
            } else {
                throw new IllegalArgumentException("illegal tag");
            }
        }
    }

    public CrlOcspRef(CrlListID crlListID, OcspListID ocspListID, OtherRevRefs otherRevRefs) {
        this.crlids = crlListID;
        this.ocspids = ocspListID;
        this.otherRev = otherRevRefs;
    }

    public static CrlOcspRef getInstance(Object obj) {
        if (obj instanceof CrlOcspRef) {
            return (CrlOcspRef) obj;
        }
        if (obj != null) {
            return new CrlOcspRef(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public CrlListID getCrlids() {
        return this.crlids;
    }

    public OcspListID getOcspids() {
        return this.ocspids;
    }

    public OtherRevRefs getOtherRev() {
        return this.otherRev;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(3);
        CrlListID crlListID = this.crlids;
        if (crlListID != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, crlListID.toASN1Primitive()));
        }
        OcspListID ocspListID = this.ocspids;
        if (ocspListID != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 1, ocspListID.toASN1Primitive()));
        }
        OtherRevRefs otherRevRefs = this.otherRev;
        if (otherRevRefs != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 2, otherRevRefs.toASN1Primitive()));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
