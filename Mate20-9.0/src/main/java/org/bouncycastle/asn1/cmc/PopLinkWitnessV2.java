package org.bouncycastle.asn1.cmc;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.Arrays;

public class PopLinkWitnessV2 extends ASN1Object {
    private final AlgorithmIdentifier keyGenAlgorithm;
    private final AlgorithmIdentifier macAlgorithm;
    private final byte[] witness;

    private PopLinkWitnessV2(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 3) {
            this.keyGenAlgorithm = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
            this.macAlgorithm = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(1));
            this.witness = Arrays.clone(ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(2)).getOctets());
            return;
        }
        throw new IllegalArgumentException("incorrect sequence size");
    }

    public PopLinkWitnessV2(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) {
        this.keyGenAlgorithm = algorithmIdentifier;
        this.macAlgorithm = algorithmIdentifier2;
        this.witness = Arrays.clone(bArr);
    }

    public static PopLinkWitnessV2 getInstance(Object obj) {
        if (obj instanceof PopLinkWitnessV2) {
            return (PopLinkWitnessV2) obj;
        }
        if (obj != null) {
            return new PopLinkWitnessV2(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public AlgorithmIdentifier getKeyGenAlgorithm() {
        return this.keyGenAlgorithm;
    }

    public AlgorithmIdentifier getMacAlgorithm() {
        return this.macAlgorithm;
    }

    public byte[] getWitness() {
        return Arrays.clone(this.witness);
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.keyGenAlgorithm);
        aSN1EncodableVector.add(this.macAlgorithm);
        aSN1EncodableVector.add(new DEROctetString(getWitness()));
        return new DERSequence(aSN1EncodableVector);
    }
}
