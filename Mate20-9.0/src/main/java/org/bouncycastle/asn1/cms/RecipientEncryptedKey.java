package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;

public class RecipientEncryptedKey extends ASN1Object {
    private ASN1OctetString encryptedKey;
    private KeyAgreeRecipientIdentifier identifier;

    private RecipientEncryptedKey(ASN1Sequence aSN1Sequence) {
        this.identifier = KeyAgreeRecipientIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
        this.encryptedKey = (ASN1OctetString) aSN1Sequence.getObjectAt(1);
    }

    public RecipientEncryptedKey(KeyAgreeRecipientIdentifier keyAgreeRecipientIdentifier, ASN1OctetString aSN1OctetString) {
        this.identifier = keyAgreeRecipientIdentifier;
        this.encryptedKey = aSN1OctetString;
    }

    public static RecipientEncryptedKey getInstance(Object obj) {
        if (obj instanceof RecipientEncryptedKey) {
            return (RecipientEncryptedKey) obj;
        }
        if (obj != null) {
            return new RecipientEncryptedKey(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static RecipientEncryptedKey getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public ASN1OctetString getEncryptedKey() {
        return this.encryptedKey;
    }

    public KeyAgreeRecipientIdentifier getIdentifier() {
        return this.identifier;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.identifier);
        aSN1EncodableVector.add(this.encryptedKey);
        return new DERSequence(aSN1EncodableVector);
    }
}
