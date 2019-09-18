package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class KeyTransRecipientInfo extends ASN1Object {
    private ASN1OctetString encryptedKey;
    private AlgorithmIdentifier keyEncryptionAlgorithm;
    private RecipientIdentifier rid;
    private ASN1Integer version;

    public KeyTransRecipientInfo(ASN1Sequence aSN1Sequence) {
        this.version = (ASN1Integer) aSN1Sequence.getObjectAt(0);
        this.rid = RecipientIdentifier.getInstance(aSN1Sequence.getObjectAt(1));
        this.keyEncryptionAlgorithm = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(2));
        this.encryptedKey = (ASN1OctetString) aSN1Sequence.getObjectAt(3);
    }

    public KeyTransRecipientInfo(RecipientIdentifier recipientIdentifier, AlgorithmIdentifier algorithmIdentifier, ASN1OctetString aSN1OctetString) {
        this.version = recipientIdentifier.toASN1Primitive() instanceof ASN1TaggedObject ? new ASN1Integer(2) : new ASN1Integer(0);
        this.rid = recipientIdentifier;
        this.keyEncryptionAlgorithm = algorithmIdentifier;
        this.encryptedKey = aSN1OctetString;
    }

    public static KeyTransRecipientInfo getInstance(Object obj) {
        if (obj instanceof KeyTransRecipientInfo) {
            return (KeyTransRecipientInfo) obj;
        }
        if (obj != null) {
            return new KeyTransRecipientInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1OctetString getEncryptedKey() {
        return this.encryptedKey;
    }

    public AlgorithmIdentifier getKeyEncryptionAlgorithm() {
        return this.keyEncryptionAlgorithm;
    }

    public RecipientIdentifier getRecipientIdentifier() {
        return this.rid;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.rid);
        aSN1EncodableVector.add(this.keyEncryptionAlgorithm);
        aSN1EncodableVector.add(this.encryptedKey);
        return new DERSequence(aSN1EncodableVector);
    }
}
