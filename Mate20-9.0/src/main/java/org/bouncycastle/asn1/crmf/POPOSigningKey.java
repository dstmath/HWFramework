package org.bouncycastle.asn1.crmf;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class POPOSigningKey extends ASN1Object {
    private AlgorithmIdentifier algorithmIdentifier;
    private POPOSigningKeyInput poposkInput;
    private DERBitString signature;

    private POPOSigningKey(ASN1Sequence aSN1Sequence) {
        int i = 0;
        if (aSN1Sequence.getObjectAt(0) instanceof ASN1TaggedObject) {
            ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) aSN1Sequence.getObjectAt(0);
            if (aSN1TaggedObject.getTagNo() == 0) {
                this.poposkInput = POPOSigningKeyInput.getInstance(aSN1TaggedObject.getObject());
                i = 1;
            } else {
                throw new IllegalArgumentException("Unknown POPOSigningKeyInput tag: " + aSN1TaggedObject.getTagNo());
            }
        }
        this.algorithmIdentifier = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(i));
        this.signature = DERBitString.getInstance(aSN1Sequence.getObjectAt(i + 1));
    }

    public POPOSigningKey(POPOSigningKeyInput pOPOSigningKeyInput, AlgorithmIdentifier algorithmIdentifier2, DERBitString dERBitString) {
        this.poposkInput = pOPOSigningKeyInput;
        this.algorithmIdentifier = algorithmIdentifier2;
        this.signature = dERBitString;
    }

    public static POPOSigningKey getInstance(Object obj) {
        if (obj instanceof POPOSigningKey) {
            return (POPOSigningKey) obj;
        }
        if (obj != null) {
            return new POPOSigningKey(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static POPOSigningKey getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return this.algorithmIdentifier;
    }

    public POPOSigningKeyInput getPoposkInput() {
        return this.poposkInput;
    }

    public DERBitString getSignature() {
        return this.signature;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (this.poposkInput != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.poposkInput));
        }
        aSN1EncodableVector.add(this.algorithmIdentifier);
        aSN1EncodableVector.add(this.signature);
        return new DERSequence(aSN1EncodableVector);
    }
}
