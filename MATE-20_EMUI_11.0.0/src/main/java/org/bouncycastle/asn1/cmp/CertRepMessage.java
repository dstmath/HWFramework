package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class CertRepMessage extends ASN1Object {
    private ASN1Sequence caPubs;
    private ASN1Sequence response;

    private CertRepMessage(ASN1Sequence aSN1Sequence) {
        int i = 1;
        if (aSN1Sequence.size() > 1) {
            this.caPubs = ASN1Sequence.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(0), true);
        } else {
            i = 0;
        }
        this.response = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(i));
    }

    public CertRepMessage(CMPCertificate[] cMPCertificateArr, CertResponse[] certResponseArr) {
        if (certResponseArr != null) {
            if (cMPCertificateArr != null) {
                this.caPubs = new DERSequence(cMPCertificateArr);
            }
            this.response = new DERSequence(certResponseArr);
            return;
        }
        throw new IllegalArgumentException("'response' cannot be null");
    }

    public static CertRepMessage getInstance(Object obj) {
        if (obj instanceof CertRepMessage) {
            return (CertRepMessage) obj;
        }
        if (obj != null) {
            return new CertRepMessage(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public CMPCertificate[] getCaPubs() {
        ASN1Sequence aSN1Sequence = this.caPubs;
        if (aSN1Sequence == null) {
            return null;
        }
        CMPCertificate[] cMPCertificateArr = new CMPCertificate[aSN1Sequence.size()];
        for (int i = 0; i != cMPCertificateArr.length; i++) {
            cMPCertificateArr[i] = CMPCertificate.getInstance(this.caPubs.getObjectAt(i));
        }
        return cMPCertificateArr;
    }

    public CertResponse[] getResponse() {
        CertResponse[] certResponseArr = new CertResponse[this.response.size()];
        for (int i = 0; i != certResponseArr.length; i++) {
            certResponseArr[i] = CertResponse.getInstance(this.response.getObjectAt(i));
        }
        return certResponseArr;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        ASN1Sequence aSN1Sequence = this.caPubs;
        if (aSN1Sequence != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 1, aSN1Sequence));
        }
        aSN1EncodableVector.add(this.response);
        return new DERSequence(aSN1EncodableVector);
    }
}
