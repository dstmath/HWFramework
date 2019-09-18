package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;

public class CertificatePolicies extends ASN1Object {
    private final PolicyInformation[] policyInformation;

    private CertificatePolicies(ASN1Sequence aSN1Sequence) {
        this.policyInformation = new PolicyInformation[aSN1Sequence.size()];
        for (int i = 0; i != aSN1Sequence.size(); i++) {
            this.policyInformation[i] = PolicyInformation.getInstance(aSN1Sequence.getObjectAt(i));
        }
    }

    public CertificatePolicies(PolicyInformation policyInformation2) {
        this.policyInformation = new PolicyInformation[]{policyInformation2};
    }

    public CertificatePolicies(PolicyInformation[] policyInformationArr) {
        this.policyInformation = policyInformationArr;
    }

    public static CertificatePolicies fromExtensions(Extensions extensions) {
        return getInstance(extensions.getExtensionParsedValue(Extension.certificatePolicies));
    }

    public static CertificatePolicies getInstance(Object obj) {
        if (obj instanceof CertificatePolicies) {
            return (CertificatePolicies) obj;
        }
        if (obj != null) {
            return new CertificatePolicies(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static CertificatePolicies getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public PolicyInformation getPolicyInformation(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        for (int i = 0; i != this.policyInformation.length; i++) {
            if (aSN1ObjectIdentifier.equals(this.policyInformation[i].getPolicyIdentifier())) {
                return this.policyInformation[i];
            }
        }
        return null;
    }

    public PolicyInformation[] getPolicyInformation() {
        PolicyInformation[] policyInformationArr = new PolicyInformation[this.policyInformation.length];
        System.arraycopy(this.policyInformation, 0, policyInformationArr, 0, this.policyInformation.length);
        return policyInformationArr;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERSequence((ASN1Encodable[]) this.policyInformation);
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (PolicyInformation append : this.policyInformation) {
            if (stringBuffer.length() != 0) {
                stringBuffer.append(", ");
            }
            stringBuffer.append(append);
        }
        return "CertificatePolicies: [" + stringBuffer + "]";
    }
}
