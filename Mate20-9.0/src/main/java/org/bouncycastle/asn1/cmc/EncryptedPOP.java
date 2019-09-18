package org.bouncycastle.asn1.cmc;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.Arrays;

public class EncryptedPOP extends ASN1Object {
    private final ContentInfo cms;
    private final TaggedRequest request;
    private final AlgorithmIdentifier thePOPAlgID;
    private final byte[] witness;
    private final AlgorithmIdentifier witnessAlgID;

    private EncryptedPOP(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 5) {
            this.request = TaggedRequest.getInstance(aSN1Sequence.getObjectAt(0));
            this.cms = ContentInfo.getInstance(aSN1Sequence.getObjectAt(1));
            this.thePOPAlgID = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(2));
            this.witnessAlgID = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(3));
            this.witness = Arrays.clone(ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(4)).getOctets());
            return;
        }
        throw new IllegalArgumentException("incorrect sequence size");
    }

    public EncryptedPOP(TaggedRequest taggedRequest, ContentInfo contentInfo, AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) {
        this.request = taggedRequest;
        this.cms = contentInfo;
        this.thePOPAlgID = algorithmIdentifier;
        this.witnessAlgID = algorithmIdentifier2;
        this.witness = Arrays.clone(bArr);
    }

    public static EncryptedPOP getInstance(Object obj) {
        if (obj instanceof EncryptedPOP) {
            return (EncryptedPOP) obj;
        }
        if (obj != null) {
            return new EncryptedPOP(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ContentInfo getCms() {
        return this.cms;
    }

    public TaggedRequest getRequest() {
        return this.request;
    }

    public AlgorithmIdentifier getThePOPAlgID() {
        return this.thePOPAlgID;
    }

    public byte[] getWitness() {
        return Arrays.clone(this.witness);
    }

    public AlgorithmIdentifier getWitnessAlgID() {
        return this.witnessAlgID;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.request);
        aSN1EncodableVector.add(this.cms);
        aSN1EncodableVector.add(this.thePOPAlgID);
        aSN1EncodableVector.add(this.witnessAlgID);
        aSN1EncodableVector.add(new DEROctetString(this.witness));
        return new DERSequence(aSN1EncodableVector);
    }
}
