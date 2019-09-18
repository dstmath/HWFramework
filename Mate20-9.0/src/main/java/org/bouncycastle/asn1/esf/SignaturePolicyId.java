package org.bouncycastle.asn1.esf;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class SignaturePolicyId extends ASN1Object {
    private OtherHashAlgAndValue sigPolicyHash;
    private ASN1ObjectIdentifier sigPolicyId;
    private SigPolicyQualifiers sigPolicyQualifiers;

    public SignaturePolicyId(ASN1ObjectIdentifier aSN1ObjectIdentifier, OtherHashAlgAndValue otherHashAlgAndValue) {
        this(aSN1ObjectIdentifier, otherHashAlgAndValue, null);
    }

    public SignaturePolicyId(ASN1ObjectIdentifier aSN1ObjectIdentifier, OtherHashAlgAndValue otherHashAlgAndValue, SigPolicyQualifiers sigPolicyQualifiers2) {
        this.sigPolicyId = aSN1ObjectIdentifier;
        this.sigPolicyHash = otherHashAlgAndValue;
        this.sigPolicyQualifiers = sigPolicyQualifiers2;
    }

    private SignaturePolicyId(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 2 || aSN1Sequence.size() == 3) {
            this.sigPolicyId = ASN1ObjectIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
            this.sigPolicyHash = OtherHashAlgAndValue.getInstance(aSN1Sequence.getObjectAt(1));
            if (aSN1Sequence.size() == 3) {
                this.sigPolicyQualifiers = SigPolicyQualifiers.getInstance(aSN1Sequence.getObjectAt(2));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
    }

    public static SignaturePolicyId getInstance(Object obj) {
        if (obj instanceof SignaturePolicyId) {
            return (SignaturePolicyId) obj;
        }
        if (obj != null) {
            return new SignaturePolicyId(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public OtherHashAlgAndValue getSigPolicyHash() {
        return this.sigPolicyHash;
    }

    public ASN1ObjectIdentifier getSigPolicyId() {
        return new ASN1ObjectIdentifier(this.sigPolicyId.getId());
    }

    public SigPolicyQualifiers getSigPolicyQualifiers() {
        return this.sigPolicyQualifiers;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.sigPolicyId);
        aSN1EncodableVector.add(this.sigPolicyHash);
        if (this.sigPolicyQualifiers != null) {
            aSN1EncodableVector.add(this.sigPolicyQualifiers);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
