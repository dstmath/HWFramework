package org.bouncycastle.asn1.ocsp;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.CRLReason;

public class RevokedInfo extends ASN1Object {
    private CRLReason revocationReason;
    private ASN1GeneralizedTime revocationTime;

    public RevokedInfo(ASN1GeneralizedTime aSN1GeneralizedTime, CRLReason cRLReason) {
        this.revocationTime = aSN1GeneralizedTime;
        this.revocationReason = cRLReason;
    }

    private RevokedInfo(ASN1Sequence aSN1Sequence) {
        this.revocationTime = ASN1GeneralizedTime.getInstance(aSN1Sequence.getObjectAt(0));
        if (aSN1Sequence.size() > 1) {
            this.revocationReason = CRLReason.getInstance(ASN1Enumerated.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(1), true));
        }
    }

    public static RevokedInfo getInstance(Object obj) {
        if (obj instanceof RevokedInfo) {
            return (RevokedInfo) obj;
        }
        if (obj != null) {
            return new RevokedInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static RevokedInfo getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public CRLReason getRevocationReason() {
        return this.revocationReason;
    }

    public ASN1GeneralizedTime getRevocationTime() {
        return this.revocationTime;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.revocationTime);
        if (this.revocationReason != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, this.revocationReason));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
