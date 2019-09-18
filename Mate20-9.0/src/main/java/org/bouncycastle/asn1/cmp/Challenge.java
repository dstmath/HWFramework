package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class Challenge extends ASN1Object {
    private ASN1OctetString challenge;
    private AlgorithmIdentifier owf;
    private ASN1OctetString witness;

    private Challenge(ASN1Sequence aSN1Sequence) {
        int i = 0;
        if (aSN1Sequence.size() == 3) {
            this.owf = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
            i = 1;
        }
        this.witness = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(i));
        this.challenge = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(i + 1));
    }

    public Challenge(AlgorithmIdentifier algorithmIdentifier, byte[] bArr, byte[] bArr2) {
        this.owf = algorithmIdentifier;
        this.witness = new DEROctetString(bArr);
        this.challenge = new DEROctetString(bArr2);
    }

    public Challenge(byte[] bArr, byte[] bArr2) {
        this(null, bArr, bArr2);
    }

    private void addOptional(ASN1EncodableVector aSN1EncodableVector, ASN1Encodable aSN1Encodable) {
        if (aSN1Encodable != null) {
            aSN1EncodableVector.add(aSN1Encodable);
        }
    }

    public static Challenge getInstance(Object obj) {
        if (obj instanceof Challenge) {
            return (Challenge) obj;
        }
        if (obj != null) {
            return new Challenge(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public byte[] getChallenge() {
        return this.challenge.getOctets();
    }

    public AlgorithmIdentifier getOwf() {
        return this.owf;
    }

    public byte[] getWitness() {
        return this.witness.getOctets();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        addOptional(aSN1EncodableVector, this.owf);
        aSN1EncodableVector.add(this.witness);
        aSN1EncodableVector.add(this.challenge);
        return new DERSequence(aSN1EncodableVector);
    }
}
