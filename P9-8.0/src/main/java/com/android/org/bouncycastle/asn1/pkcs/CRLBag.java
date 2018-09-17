package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;

public class CRLBag extends ASN1Object {
    private ASN1ObjectIdentifier crlId;
    private ASN1Encodable crlValue;

    private CRLBag(ASN1Sequence seq) {
        this.crlId = (ASN1ObjectIdentifier) seq.getObjectAt(0);
        this.crlValue = ((ASN1TaggedObject) seq.getObjectAt(1)).getObject();
    }

    public static CRLBag getInstance(Object o) {
        if (o instanceof CRLBag) {
            return (CRLBag) o;
        }
        if (o != null) {
            return new CRLBag(ASN1Sequence.getInstance(o));
        }
        return null;
    }

    public CRLBag(ASN1ObjectIdentifier crlId, ASN1Encodable crlValue) {
        this.crlId = crlId;
        this.crlValue = crlValue;
    }

    public ASN1ObjectIdentifier getCrlId() {
        return this.crlId;
    }

    public ASN1Encodable getCrlValue() {
        return this.crlValue;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.crlId);
        v.add(new DERTaggedObject(0, this.crlValue));
        return new DERSequence(v);
    }
}
