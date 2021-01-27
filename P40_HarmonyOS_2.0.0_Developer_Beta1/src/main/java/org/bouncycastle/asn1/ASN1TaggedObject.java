package org.bouncycastle.asn1;

import java.io.IOException;

public abstract class ASN1TaggedObject extends ASN1Primitive implements ASN1TaggedObjectParser {
    final boolean explicit;
    final ASN1Encodable obj;
    final int tagNo;

    public ASN1TaggedObject(boolean z, int i, ASN1Encodable aSN1Encodable) {
        if (aSN1Encodable != null) {
            this.tagNo = i;
            this.explicit = z || (aSN1Encodable instanceof ASN1Choice);
            this.obj = aSN1Encodable;
            return;
        }
        throw new NullPointerException("'obj' cannot be null");
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
            return getInstance(aSN1TaggedObject.getObject());
        }
        throw new IllegalArgumentException("implicitly tagged tagged object");
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1TaggedObject)) {
            return false;
        }
        ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) aSN1Primitive;
        if (this.tagNo != aSN1TaggedObject.tagNo || this.explicit != aSN1TaggedObject.explicit) {
            return false;
        }
        ASN1Primitive aSN1Primitive2 = this.obj.toASN1Primitive();
        ASN1Primitive aSN1Primitive3 = aSN1TaggedObject.obj.toASN1Primitive();
        return aSN1Primitive2 == aSN1Primitive3 || aSN1Primitive2.asn1Equals(aSN1Primitive3);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public abstract void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException;

    @Override // org.bouncycastle.asn1.InMemoryRepresentable
    public ASN1Primitive getLoadedObject() {
        return toASN1Primitive();
    }

    public ASN1Primitive getObject() {
        return this.obj.toASN1Primitive();
    }

    @Override // org.bouncycastle.asn1.ASN1TaggedObjectParser
    public ASN1Encodable getObjectParser(int i, boolean z) throws IOException {
        if (i == 4) {
            return ASN1OctetString.getInstance(this, z).parser();
        }
        if (i == 16) {
            return ASN1Sequence.getInstance(this, z).parser();
        }
        if (i == 17) {
            return ASN1Set.getInstance(this, z).parser();
        }
        if (z) {
            return getObject();
        }
        throw new ASN1Exception("implicit tagging not implemented for tag: " + i);
    }

    @Override // org.bouncycastle.asn1.ASN1TaggedObjectParser
    public int getTagNo() {
        return this.tagNo;
    }

    @Override // org.bouncycastle.asn1.ASN1Primitive, org.bouncycastle.asn1.ASN1Object
    public int hashCode() {
        return (this.tagNo ^ (this.explicit ? 15 : 240)) ^ this.obj.toASN1Primitive().hashCode();
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isExplicit() {
        return this.explicit;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDERObject() {
        return new DERTaggedObject(this.explicit, this.tagNo, this.obj);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDLObject() {
        return new DLTaggedObject(this.explicit, this.tagNo, this.obj);
    }

    public String toString() {
        return "[" + this.tagNo + "]" + this.obj;
    }
}
