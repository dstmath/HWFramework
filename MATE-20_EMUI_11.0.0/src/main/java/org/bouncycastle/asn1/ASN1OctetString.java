package org.bouncycastle.asn1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

public abstract class ASN1OctetString extends ASN1Primitive implements ASN1OctetStringParser {
    byte[] string;

    public ASN1OctetString(byte[] bArr) {
        if (bArr != null) {
            this.string = bArr;
            return;
        }
        throw new NullPointerException("'string' cannot be null");
    }

    public static ASN1OctetString getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1OctetString)) {
            return (ASN1OctetString) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return getInstance(fromByteArray((byte[]) obj));
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to construct OCTET STRING from byte[]: " + e.getMessage());
            }
        } else {
            if (obj instanceof ASN1Encodable) {
                ASN1Primitive aSN1Primitive = ((ASN1Encodable) obj).toASN1Primitive();
                if (aSN1Primitive instanceof ASN1OctetString) {
                    return (ASN1OctetString) aSN1Primitive;
                }
            }
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static ASN1OctetString getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        if (!z) {
            ASN1Primitive object = aSN1TaggedObject.getObject();
            if (aSN1TaggedObject.isExplicit()) {
                ASN1OctetString instance = getInstance(object);
                return aSN1TaggedObject instanceof BERTaggedObject ? new BEROctetString(new ASN1OctetString[]{instance}) : (ASN1OctetString) new BEROctetString(new ASN1OctetString[]{instance}).toDLObject();
            } else if (object instanceof ASN1OctetString) {
                ASN1OctetString aSN1OctetString = (ASN1OctetString) object;
                return aSN1TaggedObject instanceof BERTaggedObject ? aSN1OctetString : (ASN1OctetString) aSN1OctetString.toDLObject();
            } else if (object instanceof ASN1Sequence) {
                ASN1Sequence aSN1Sequence = (ASN1Sequence) object;
                return aSN1TaggedObject instanceof BERTaggedObject ? BEROctetString.fromSequence(aSN1Sequence) : (ASN1OctetString) BEROctetString.fromSequence(aSN1Sequence).toDLObject();
            } else {
                throw new IllegalArgumentException("unknown object in getInstance: " + aSN1TaggedObject.getClass().getName());
            }
        } else if (aSN1TaggedObject.isExplicit()) {
            return getInstance(aSN1TaggedObject.getObject());
        } else {
            throw new IllegalArgumentException("object implicit - explicit expected.");
        }
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1OctetString)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((ASN1OctetString) aSN1Primitive).string);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public abstract void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException;

    @Override // org.bouncycastle.asn1.InMemoryRepresentable
    public ASN1Primitive getLoadedObject() {
        return toASN1Primitive();
    }

    @Override // org.bouncycastle.asn1.ASN1OctetStringParser
    public InputStream getOctetStream() {
        return new ByteArrayInputStream(this.string);
    }

    public byte[] getOctets() {
        return this.string;
    }

    @Override // org.bouncycastle.asn1.ASN1Primitive, org.bouncycastle.asn1.ASN1Object
    public int hashCode() {
        return Arrays.hashCode(getOctets());
    }

    public ASN1OctetStringParser parser() {
        return this;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDERObject() {
        return new DEROctetString(this.string);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDLObject() {
        return new DEROctetString(this.string);
    }

    public String toString() {
        return "#" + Strings.fromByteArray(Hex.encode(this.string));
    }
}
