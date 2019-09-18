package org.bouncycastle.asn1.mozilla;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class SignedPublicKeyAndChallenge extends ASN1Object {
    private final ASN1Sequence pkacSeq;
    private final PublicKeyAndChallenge pubKeyAndChal;

    private SignedPublicKeyAndChallenge(ASN1Sequence aSN1Sequence) {
        this.pkacSeq = aSN1Sequence;
        this.pubKeyAndChal = PublicKeyAndChallenge.getInstance(aSN1Sequence.getObjectAt(0));
    }

    public static SignedPublicKeyAndChallenge getInstance(Object obj) {
        if (obj instanceof SignedPublicKeyAndChallenge) {
            return (SignedPublicKeyAndChallenge) obj;
        }
        if (obj != null) {
            return new SignedPublicKeyAndChallenge(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public PublicKeyAndChallenge getPublicKeyAndChallenge() {
        return this.pubKeyAndChal;
    }

    public DERBitString getSignature() {
        return DERBitString.getInstance(this.pkacSeq.getObjectAt(2));
    }

    public AlgorithmIdentifier getSignatureAlgorithm() {
        return AlgorithmIdentifier.getInstance(this.pkacSeq.getObjectAt(1));
    }

    public ASN1Primitive toASN1Primitive() {
        return this.pkacSeq;
    }
}
