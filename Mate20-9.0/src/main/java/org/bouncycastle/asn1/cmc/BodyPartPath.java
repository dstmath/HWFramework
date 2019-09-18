package org.bouncycastle.asn1.cmc;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;

public class BodyPartPath extends ASN1Object {
    private final BodyPartID[] bodyPartIDs;

    private BodyPartPath(ASN1Sequence aSN1Sequence) {
        this.bodyPartIDs = Utils.toBodyPartIDArray(aSN1Sequence);
    }

    public BodyPartPath(BodyPartID bodyPartID) {
        this.bodyPartIDs = new BodyPartID[]{bodyPartID};
    }

    public BodyPartPath(BodyPartID[] bodyPartIDArr) {
        this.bodyPartIDs = Utils.clone(bodyPartIDArr);
    }

    public static BodyPartPath getInstance(Object obj) {
        if (obj instanceof BodyPartPath) {
            return (BodyPartPath) obj;
        }
        if (obj != null) {
            return new BodyPartPath(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static BodyPartPath getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public BodyPartID[] getBodyPartIDs() {
        return Utils.clone(this.bodyPartIDs);
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERSequence((ASN1Encodable[]) this.bodyPartIDs);
    }
}
