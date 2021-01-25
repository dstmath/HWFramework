package org.bouncycastle.asn1.esf;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class CommitmentTypeQualifier extends ASN1Object {
    private ASN1ObjectIdentifier commitmentTypeIdentifier;
    private ASN1Encodable qualifier;

    public CommitmentTypeQualifier(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this(aSN1ObjectIdentifier, null);
    }

    public CommitmentTypeQualifier(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        this.commitmentTypeIdentifier = aSN1ObjectIdentifier;
        this.qualifier = aSN1Encodable;
    }

    private CommitmentTypeQualifier(ASN1Sequence aSN1Sequence) {
        this.commitmentTypeIdentifier = (ASN1ObjectIdentifier) aSN1Sequence.getObjectAt(0);
        if (aSN1Sequence.size() > 1) {
            this.qualifier = aSN1Sequence.getObjectAt(1);
        }
    }

    public static CommitmentTypeQualifier getInstance(Object obj) {
        if (obj instanceof CommitmentTypeQualifier) {
            return (CommitmentTypeQualifier) obj;
        }
        if (obj != null) {
            return new CommitmentTypeQualifier(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1ObjectIdentifier getCommitmentTypeIdentifier() {
        return this.commitmentTypeIdentifier;
    }

    public ASN1Encodable getQualifier() {
        return this.qualifier;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        aSN1EncodableVector.add(this.commitmentTypeIdentifier);
        ASN1Encodable aSN1Encodable = this.qualifier;
        if (aSN1Encodable != null) {
            aSN1EncodableVector.add(aSN1Encodable);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
