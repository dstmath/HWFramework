package org.bouncycastle.asn1.ess;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.PolicyInformation;

public class SigningCertificateV2 extends ASN1Object {
    ASN1Sequence certs;
    ASN1Sequence policies;

    private SigningCertificateV2(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() < 1 || aSN1Sequence.size() > 2) {
            throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
        }
        this.certs = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(0));
        if (aSN1Sequence.size() > 1) {
            this.policies = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(1));
        }
    }

    public SigningCertificateV2(ESSCertIDv2 eSSCertIDv2) {
        this.certs = new DERSequence(eSSCertIDv2);
    }

    public SigningCertificateV2(ESSCertIDv2[] eSSCertIDv2Arr) {
        this.certs = new DERSequence(eSSCertIDv2Arr);
    }

    public SigningCertificateV2(ESSCertIDv2[] eSSCertIDv2Arr, PolicyInformation[] policyInformationArr) {
        this.certs = new DERSequence(eSSCertIDv2Arr);
        if (policyInformationArr != null) {
            this.policies = new DERSequence(policyInformationArr);
        }
    }

    public static SigningCertificateV2 getInstance(Object obj) {
        if (obj == null || (obj instanceof SigningCertificateV2)) {
            return (SigningCertificateV2) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new SigningCertificateV2((ASN1Sequence) obj);
        }
        return null;
    }

    public ESSCertIDv2[] getCerts() {
        ESSCertIDv2[] eSSCertIDv2Arr = new ESSCertIDv2[this.certs.size()];
        for (int i = 0; i != this.certs.size(); i++) {
            eSSCertIDv2Arr[i] = ESSCertIDv2.getInstance(this.certs.getObjectAt(i));
        }
        return eSSCertIDv2Arr;
    }

    public PolicyInformation[] getPolicies() {
        ASN1Sequence aSN1Sequence = this.policies;
        if (aSN1Sequence == null) {
            return null;
        }
        PolicyInformation[] policyInformationArr = new PolicyInformation[aSN1Sequence.size()];
        for (int i = 0; i != this.policies.size(); i++) {
            policyInformationArr[i] = PolicyInformation.getInstance(this.policies.getObjectAt(i));
        }
        return policyInformationArr;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        aSN1EncodableVector.add(this.certs);
        ASN1Sequence aSN1Sequence = this.policies;
        if (aSN1Sequence != null) {
            aSN1EncodableVector.add(aSN1Sequence);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
