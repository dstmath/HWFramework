package org.bouncycastle.asn1.esf;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class SigPolicyQualifiers extends ASN1Object {
    ASN1Sequence qualifiers;

    private SigPolicyQualifiers(ASN1Sequence aSN1Sequence) {
        this.qualifiers = aSN1Sequence;
    }

    public SigPolicyQualifiers(SigPolicyQualifierInfo[] sigPolicyQualifierInfoArr) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (SigPolicyQualifierInfo add : sigPolicyQualifierInfoArr) {
            aSN1EncodableVector.add(add);
        }
        this.qualifiers = new DERSequence(aSN1EncodableVector);
    }

    public static SigPolicyQualifiers getInstance(Object obj) {
        if (obj instanceof SigPolicyQualifiers) {
            return (SigPolicyQualifiers) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new SigPolicyQualifiers(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public SigPolicyQualifierInfo getInfoAt(int i) {
        return SigPolicyQualifierInfo.getInstance(this.qualifiers.getObjectAt(i));
    }

    public int size() {
        return this.qualifiers.size();
    }

    public ASN1Primitive toASN1Primitive() {
        return this.qualifiers;
    }
}
