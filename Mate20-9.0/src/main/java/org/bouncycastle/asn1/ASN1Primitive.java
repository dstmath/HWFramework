package org.bouncycastle.asn1;

import java.io.IOException;

public abstract class ASN1Primitive extends ASN1Object {
    ASN1Primitive() {
    }

    public static ASN1Primitive fromByteArray(byte[] bArr) throws IOException {
        ASN1InputStream aSN1InputStream = new ASN1InputStream(bArr);
        try {
            ASN1Primitive readObject = aSN1InputStream.readObject();
            if (aSN1InputStream.available() == 0) {
                return readObject;
            }
            throw new IOException("Extra data detected in stream");
        } catch (ClassCastException e) {
            throw new IOException("cannot recognise object in stream");
        }
    }

    /* access modifiers changed from: package-private */
    public abstract boolean asn1Equals(ASN1Primitive aSN1Primitive);

    /* access modifiers changed from: package-private */
    public abstract void encode(ASN1OutputStream aSN1OutputStream) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract int encodedLength() throws IOException;

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof ASN1Encodable) && asn1Equals(((ASN1Encodable) obj).toASN1Primitive());
    }

    public abstract int hashCode();

    /* access modifiers changed from: package-private */
    public abstract boolean isConstructed();

    public ASN1Primitive toASN1Primitive() {
        return this;
    }

    /* access modifiers changed from: package-private */
    public ASN1Primitive toDERObject() {
        return this;
    }

    /* access modifiers changed from: package-private */
    public ASN1Primitive toDLObject() {
        return this;
    }
}
