package org.bouncycastle.asn1.bc;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class ObjectStore extends ASN1Object {
    private final ObjectStoreIntegrityCheck integrityCheck;
    private final ASN1Encodable storeData;

    private ObjectStore(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 2) {
            ASN1Encodable objectAt = aSN1Sequence.getObjectAt(0);
            if (!(objectAt instanceof EncryptedObjectStoreData) && !(objectAt instanceof ObjectStoreData)) {
                ASN1Sequence instance = ASN1Sequence.getInstance(objectAt);
                objectAt = instance.size() == 2 ? EncryptedObjectStoreData.getInstance(instance) : ObjectStoreData.getInstance(instance);
            }
            this.storeData = objectAt;
            this.integrityCheck = ObjectStoreIntegrityCheck.getInstance(aSN1Sequence.getObjectAt(1));
            return;
        }
        throw new IllegalArgumentException("malformed sequence");
    }

    public ObjectStore(EncryptedObjectStoreData encryptedObjectStoreData, ObjectStoreIntegrityCheck objectStoreIntegrityCheck) {
        this.storeData = encryptedObjectStoreData;
        this.integrityCheck = objectStoreIntegrityCheck;
    }

    public ObjectStore(ObjectStoreData objectStoreData, ObjectStoreIntegrityCheck objectStoreIntegrityCheck) {
        this.storeData = objectStoreData;
        this.integrityCheck = objectStoreIntegrityCheck;
    }

    public static ObjectStore getInstance(Object obj) {
        if (obj instanceof ObjectStore) {
            return (ObjectStore) obj;
        }
        if (obj != null) {
            return new ObjectStore(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ObjectStoreIntegrityCheck getIntegrityCheck() {
        return this.integrityCheck;
    }

    public ASN1Encodable getStoreData() {
        return this.storeData;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.storeData);
        aSN1EncodableVector.add(this.integrityCheck);
        return new DERSequence(aSN1EncodableVector);
    }
}
