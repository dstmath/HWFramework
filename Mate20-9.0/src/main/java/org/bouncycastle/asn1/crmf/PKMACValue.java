package org.bouncycastle.asn1.crmf;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cmp.CMPObjectIdentifiers;
import org.bouncycastle.asn1.cmp.PBMParameter;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class PKMACValue extends ASN1Object {
    private AlgorithmIdentifier algId;
    private DERBitString value;

    private PKMACValue(ASN1Sequence aSN1Sequence) {
        this.algId = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
        this.value = DERBitString.getInstance(aSN1Sequence.getObjectAt(1));
    }

    public PKMACValue(PBMParameter pBMParameter, DERBitString dERBitString) {
        this(new AlgorithmIdentifier(CMPObjectIdentifiers.passwordBasedMac, pBMParameter), dERBitString);
    }

    public PKMACValue(AlgorithmIdentifier algorithmIdentifier, DERBitString dERBitString) {
        this.algId = algorithmIdentifier;
        this.value = dERBitString;
    }

    public static PKMACValue getInstance(Object obj) {
        if (obj instanceof PKMACValue) {
            return (PKMACValue) obj;
        }
        if (obj != null) {
            return new PKMACValue(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static PKMACValue getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public AlgorithmIdentifier getAlgId() {
        return this.algId;
    }

    public DERBitString getValue() {
        return this.value;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.algId);
        aSN1EncodableVector.add(this.value);
        return new DERSequence(aSN1EncodableVector);
    }
}
