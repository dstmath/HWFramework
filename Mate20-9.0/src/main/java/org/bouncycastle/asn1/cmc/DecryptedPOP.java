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

public class DecryptedPOP extends ASN1Object {
    private final BodyPartID bodyPartID;
    private final byte[] thePOP;
    private final AlgorithmIdentifier thePOPAlgID;

    private DecryptedPOP(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 3) {
            this.bodyPartID = BodyPartID.getInstance(aSN1Sequence.getObjectAt(0));
            this.thePOPAlgID = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(1));
            this.thePOP = Arrays.clone(ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(2)).getOctets());
            return;
        }
        throw new IllegalArgumentException("incorrect sequence size");
    }

    public DecryptedPOP(BodyPartID bodyPartID2, AlgorithmIdentifier algorithmIdentifier, byte[] bArr) {
        this.bodyPartID = bodyPartID2;
        this.thePOPAlgID = algorithmIdentifier;
        this.thePOP = Arrays.clone(bArr);
    }

    public static DecryptedPOP getInstance(Object obj) {
        if (obj instanceof DecryptedPOP) {
            return (DecryptedPOP) obj;
        }
        if (obj != null) {
            return new DecryptedPOP(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public BodyPartID getBodyPartID() {
        return this.bodyPartID;
    }

    public byte[] getThePOP() {
        return Arrays.clone(this.thePOP);
    }

    public AlgorithmIdentifier getThePOPAlgID() {
        return this.thePOPAlgID;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.bodyPartID);
        aSN1EncodableVector.add(this.thePOPAlgID);
        aSN1EncodableVector.add(new DEROctetString(this.thePOP));
        return new DERSequence(aSN1EncodableVector);
    }
}
