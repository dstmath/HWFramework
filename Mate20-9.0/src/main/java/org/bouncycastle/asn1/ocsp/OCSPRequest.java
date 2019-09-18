package org.bouncycastle.asn1.ocsp;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class OCSPRequest extends ASN1Object {
    Signature optionalSignature;
    TBSRequest tbsRequest;

    private OCSPRequest(ASN1Sequence aSN1Sequence) {
        this.tbsRequest = TBSRequest.getInstance(aSN1Sequence.getObjectAt(0));
        if (aSN1Sequence.size() == 2) {
            this.optionalSignature = Signature.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(1), true);
        }
    }

    public OCSPRequest(TBSRequest tBSRequest, Signature signature) {
        this.tbsRequest = tBSRequest;
        this.optionalSignature = signature;
    }

    public static OCSPRequest getInstance(Object obj) {
        if (obj instanceof OCSPRequest) {
            return (OCSPRequest) obj;
        }
        if (obj != null) {
            return new OCSPRequest(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static OCSPRequest getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public Signature getOptionalSignature() {
        return this.optionalSignature;
    }

    public TBSRequest getTbsRequest() {
        return this.tbsRequest;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.tbsRequest);
        if (this.optionalSignature != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, this.optionalSignature));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
