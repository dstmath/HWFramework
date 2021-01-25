package org.bouncycastle.asn1.bc;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;

public class ObjectStoreIntegrityCheck extends ASN1Object implements ASN1Choice {
    public static final int PBKD_MAC_CHECK = 0;
    public static final int SIG_CHECK = 1;
    private final ASN1Object integrityCheck;
    private final int type;

    private ObjectStoreIntegrityCheck(ASN1Encodable aSN1Encodable) {
        ASN1Object aSN1Object;
        if ((aSN1Encodable instanceof ASN1Sequence) || (aSN1Encodable instanceof PbkdMacIntegrityCheck)) {
            this.type = 0;
            aSN1Object = PbkdMacIntegrityCheck.getInstance(aSN1Encodable);
        } else if (aSN1Encodable instanceof ASN1TaggedObject) {
            this.type = 1;
            aSN1Object = SignatureCheck.getInstance(((ASN1TaggedObject) aSN1Encodable).getObject());
        } else {
            throw new IllegalArgumentException("Unknown check object in integrity check.");
        }
        this.integrityCheck = aSN1Object;
    }

    public ObjectStoreIntegrityCheck(PbkdMacIntegrityCheck pbkdMacIntegrityCheck) {
        this((ASN1Encodable) pbkdMacIntegrityCheck);
    }

    public ObjectStoreIntegrityCheck(SignatureCheck signatureCheck) {
        this(new DERTaggedObject(0, signatureCheck));
    }

    public static ObjectStoreIntegrityCheck getInstance(Object obj) {
        if (obj instanceof ObjectStoreIntegrityCheck) {
            return (ObjectStoreIntegrityCheck) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return new ObjectStoreIntegrityCheck(ASN1Primitive.fromByteArray((byte[]) obj));
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

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1Object aSN1Object = this.integrityCheck;
        return aSN1Object instanceof SignatureCheck ? new DERTaggedObject(0, aSN1Object) : aSN1Object.toASN1Primitive();
    }
}
