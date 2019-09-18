package org.bouncycastle.asn1.bc;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class EncryptedObjectStoreData extends ASN1Object {
    private final ASN1OctetString encryptedContent;
    private final AlgorithmIdentifier encryptionAlgorithm;

    private EncryptedObjectStoreData(ASN1Sequence aSN1Sequence) {
        this.encryptionAlgorithm = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
        this.encryptedContent = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(1));
    }

    public EncryptedObjectStoreData(AlgorithmIdentifier algorithmIdentifier, byte[] bArr) {
        this.encryptionAlgorithm = algorithmIdentifier;
        this.encryptedContent = new DEROctetString(bArr);
    }

    public static EncryptedObjectStoreData getInstance(Object obj) {
        if (obj instanceof EncryptedObjectStoreData) {
            return (EncryptedObjectStoreData) obj;
        }
        if (obj != null) {
            return new EncryptedObjectStoreData(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1OctetString getEncryptedContent() {
        return this.encryptedContent;
    }

    public AlgorithmIdentifier getEncryptionAlgorithm() {
        return this.encryptionAlgorithm;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.encryptionAlgorithm);
        aSN1EncodableVector.add(this.encryptedContent);
        return new DERSequence(aSN1EncodableVector);
    }
}
