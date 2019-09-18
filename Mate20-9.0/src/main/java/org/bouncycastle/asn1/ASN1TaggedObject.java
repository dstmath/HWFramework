package org.bouncycastle.asn1;

import java.io.IOException;

public abstract class ASN1TaggedObject extends ASN1Primitive implements ASN1TaggedObjectParser {
    boolean empty = false;
    boolean explicit = true;
    ASN1Encodable obj = null;
    int tagNo;

    public ASN1TaggedObject(boolean z, int i, ASN1Encodable aSN1Encodable) {
        if (aSN1Encodable instanceof ASN1Choice) {
            this.explicit = true;
        } else {
            this.explicit = z;
        }
        this.tagNo = i;
        if (!this.explicit) {
            boolean z2 = aSN1Encodable.toASN1Primitive() instanceof ASN1Set;
        }
        this.obj = aSN1Encodable;
    }

    public static ASN1TaggedObject getInstance(Object obj2) {
        if (obj2 == null || (obj2 instanceof ASN1TaggedObject)) {
            return (ASN1TaggedObject) obj2;
        }
        if (obj2 instanceof byte[]) {
            try {
                return getInstance(fromByteArray((byte[]) obj2));
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to construct tagged object from byte[]: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("unknown object in getInstance: " + obj2.getClass().getName());
        }
    }

    public static ASN1TaggedObject getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        if (z) {
            return (ASN1TaggedObject) aSN1TaggedObject.getObject();
        }
        throw new IllegalArgumentException("implicitly tagged tagged object");
    }

    /* access modifiers changed from: package-private */
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1TaggedObject)) {
            return false;
        }
        ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) aSN1Primitive;
        if (this.tagNo != aSN1TaggedObject.tagNo || this.empty != aSN1TaggedObject.empty || this.explicit != aSN1TaggedObject.explicit) {
            return false;
        }
        if (this.obj == null) {
            if (aSN1TaggedObject.obj != null) {
                return false;
            }
        } else if (!this.obj.toASN1Primitive().equals(aSN1TaggedObject.obj.toASN1Primitive())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public abstract void encode(ASN1OutputStream aSN1OutputStream) throws IOException;

    public ASN1Primitive getLoadedObject() {
        return toASN1Primitive();
    }

    public ASN1Primitive getObject() {
        if (this.obj != null) {
            return this.obj.toASN1Primitive();
        }
        return null;
    }

    public ASN1Encodable getObjectParser(int i, boolean z) throws IOException {
        if (i == 4) {
            return ASN1OctetString.getInstance(this, z).parser();
        }
        switch (i) {
            case 16:
                return ASN1Sequence.getInstance(this, z).parser();
            case 17:
                return ASN1Set.getInstance(this, z).parser();
            default:
                if (z) {
                    return getObject();
                }
                throw new ASN1Exception("implicit tagging not implemented for tag: " + i);
        }
    }

    public int getTagNo() {
        return this.tagNo;
    }

    public int hashCode() {
        int i = this.tagNo;
        return this.obj != null ? i ^ this.obj.hashCode() : i;
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public boolean isExplicit() {
        return this.explicit;
    }

    /* access modifiers changed from: package-private */
    public ASN1Primitive toDERObject() {
        return new DERTaggedObject(this.explicit, this.tagNo, this.obj);
    }

    /* access modifiers changed from: package-private */
    public ASN1Primitive toDLObject() {
        return new DLTaggedObject(this.explicit, this.tagNo, this.obj);
    }

    public String toString() {
        return "[" + this.tagNo + "]" + this.obj;
    }
}
