package org.bouncycastle.asn1.bc;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

public class ObjectStoreIntegrityCheck extends ASN1Object implements ASN1Choice {
    public static final int PBKD_MAC_CHECK = 0;
    private final ASN1Object integrityCheck;
    private final int type;

    private ObjectStoreIntegrityCheck(ASN1Encodable aSN1Encodable) {
        if ((aSN1Encodable instanceof ASN1Sequence) || (aSN1Encodable instanceof PbkdMacIntegrityCheck)) {
            this.type = 0;
            this.integrityCheck = PbkdMacIntegrityCheck.getInstance(aSN1Encodable);
            return;
        }
        throw new IllegalArgumentException("Unknown check object in integrity check.");
    }

    public ObjectStoreIntegrityCheck(PbkdMacIntegrityCheck pbkdMacIntegrityCheck) {
        this((ASN1Encodable) pbkdMacIntegrityCheck);
    }

    public static ObjectStoreIntegrityCheck getInstance(Object obj) {
        if (obj instanceof ObjectStoreIntegrityCheck) {
            return (ObjectStoreIntegrityCheck) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return new ObjectStoreIntegrityCheck((ASN1Encodable) ASN1Primitive.fromByteArray((byte[]) obj));
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to parse integrity check details.");
            }
        } else if (obj != null) {
            return new ObjectStoreIntegrityCheck((ASN1Encodable) obj);
        } else {
            return null;
        }
    }

    public ASN1Object getIntegrityCheck() {
        return this.integrityCheck;
    }

    public int getType() {
        return this.type;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.integrityCheck.toASN1Primitive();
    }
}
