package org.bouncycastle.asn1.pkcs;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class CertBag extends ASN1Object {
    private ASN1ObjectIdentifier certId;
    private ASN1Encodable certValue;

    public CertBag(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        this.certId = aSN1ObjectIdentifier;
        this.certValue = aSN1Encodable;
    }

    private CertBag(ASN1Sequence aSN1Sequence) {
        this.certId = (ASN1ObjectIdentifier) aSN1Sequence.getObjectAt(0);
        this.certValue = ((DERTaggedObject) aSN1Sequence.getObjectAt(1)).getObject();
    }

    public static CertBag getInstance(Object obj) {
        if (obj instanceof CertBag) {
            return (CertBag) obj;
        }
        if (obj != null) {
            return new CertBag(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1ObjectIdentifier getCertId() {
        return this.certId;
    }

    public ASN1Encodable getCertValue() {
        return this.certValue;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.certId);
        aSN1EncodableVector.add(new DERTaggedObject(0, this.certValue));
        return new DERSequence(aSN1EncodableVector);
    }
}
