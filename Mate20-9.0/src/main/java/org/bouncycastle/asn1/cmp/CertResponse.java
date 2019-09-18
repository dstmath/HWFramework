package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class CertResponse extends ASN1Object {
    private ASN1Integer certReqId;
    private CertifiedKeyPair certifiedKeyPair;
    private ASN1OctetString rspInfo;
    private PKIStatusInfo status;

    public CertResponse(ASN1Integer aSN1Integer, PKIStatusInfo pKIStatusInfo) {
        this(aSN1Integer, pKIStatusInfo, null, null);
    }

    public CertResponse(ASN1Integer aSN1Integer, PKIStatusInfo pKIStatusInfo, CertifiedKeyPair certifiedKeyPair2, ASN1OctetString aSN1OctetString) {
        if (aSN1Integer == null) {
            throw new IllegalArgumentException("'certReqId' cannot be null");
        } else if (pKIStatusInfo != null) {
            this.certReqId = aSN1Integer;
            this.status = pKIStatusInfo;
            this.certifiedKeyPair = certifiedKeyPair2;
            this.rspInfo = aSN1OctetString;
        } else {
            throw new IllegalArgumentException("'status' cannot be null");
        }
    }

    private CertResponse(ASN1Sequence aSN1Sequence) {
        ASN1Encodable aSN1Encodable;
        this.certReqId = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0));
        this.status = PKIStatusInfo.getInstance(aSN1Sequence.getObjectAt(1));
        if (aSN1Sequence.size() >= 3) {
            if (aSN1Sequence.size() == 3) {
                aSN1Encodable = aSN1Sequence.getObjectAt(2);
                if (!(aSN1Encodable instanceof ASN1OctetString)) {
                    this.certifiedKeyPair = CertifiedKeyPair.getInstance(aSN1Encodable);
                    return;
                }
            } else {
                this.certifiedKeyPair = CertifiedKeyPair.getInstance(aSN1Sequence.getObjectAt(2));
                aSN1Encodable = aSN1Sequence.getObjectAt(3);
            }
            this.rspInfo = ASN1OctetString.getInstance(aSN1Encodable);
        }
    }

    public static CertResponse getInstance(Object obj) {
        if (obj instanceof CertResponse) {
            return (CertResponse) obj;
        }
        if (obj != null) {
            return new CertResponse(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1Integer getCertReqId() {
        return this.certReqId;
    }

    public CertifiedKeyPair getCertifiedKeyPair() {
        return this.certifiedKeyPair;
    }

    public PKIStatusInfo getStatus() {
        return this.status;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.certReqId);
        aSN1EncodableVector.add(this.status);
        if (this.certifiedKeyPair != null) {
            aSN1EncodableVector.add(this.certifiedKeyPair);
        }
        if (this.rspInfo != null) {
            aSN1EncodableVector.add(this.rspInfo);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
