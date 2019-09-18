package org.bouncycastle.asn1.ess;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.PolicyInformation;

public class OtherSigningCertificate extends ASN1Object {
    ASN1Sequence certs;
    ASN1Sequence policies;

    private OtherSigningCertificate(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() < 1 || aSN1Sequence.size() > 2) {
            throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
        }
        this.certs = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(0));
        if (aSN1Sequence.size() > 1) {
            this.policies = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(1));
        }
    }

    public OtherSigningCertificate(OtherCertID otherCertID) {
        this.certs = new DERSequence((ASN1Encodable) otherCertID);
    }

    public static OtherSigningCertificate getInstance(Object obj) {
        if (obj instanceof OtherSigningCertificate) {
            return (OtherSigningCertificate) obj;
        }
        if (obj != null) {
            return new OtherSigningCertificate(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public OtherCertID[] getCerts() {
        OtherCertID[] otherCertIDArr = new OtherCertID[this.certs.size()];
        for (int i = 0; i != this.certs.size(); i++) {
            otherCertIDArr[i] = OtherCertID.getInstance(this.certs.getObjectAt(i));
        }
        return otherCertIDArr;
    }

    public PolicyInformation[] getPolicies() {
        if (this.policies == null) {
            return null;
        }
        PolicyInformation[] policyInformationArr = new PolicyInformation[this.policies.size()];
        for (int i = 0; i != this.policies.size(); i++) {
            policyInformationArr[i] = PolicyInformation.getInstance(this.policies.getObjectAt(i));
        }
        return policyInformationArr;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.certs);
        if (this.policies != null) {
            aSN1EncodableVector.add(this.policies);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
