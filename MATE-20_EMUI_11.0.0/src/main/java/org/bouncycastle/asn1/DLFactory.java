package org.bouncycastle.asn1;

/* access modifiers changed from: package-private */
public class DLFactory {
    static final ASN1Sequence EMPTY_SEQUENCE = new DLSequence();
    static final ASN1Set EMPTY_SET = new DLSet();

    DLFactory() {
    }

    static ASN1Sequence createSequence(ASN1EncodableVector aSN1EncodableVector) {
        return aSN1EncodableVector.size() < 1 ? EMPTY_SEQUENCE : new DLSequence(aSN1EncodableVector);
    }

    static ASN1Set createSet(ASN1EncodableVector aSN1EncodableVector) {
        return aSN1EncodableVector.size() < 1 ? EMPTY_SET : new DLSet(aSN1EncodableVector);
    }
}
