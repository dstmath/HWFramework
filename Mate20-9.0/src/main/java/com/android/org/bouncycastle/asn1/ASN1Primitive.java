package com.android.org.bouncycastle.asn1;

import java.io.IOException;

public abstract class ASN1Primitive extends ASN1Object {
    /* access modifiers changed from: package-private */
    public abstract boolean asn1Equals(ASN1Primitive aSN1Primitive);

    /* access modifiers changed from: package-private */
    public abstract void encode(ASN1OutputStream aSN1OutputStream) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract int encodedLength() throws IOException;

    public abstract int hashCode();

    /* access modifiers changed from: package-private */
    public abstract boolean isConstructed();

    ASN1Primitive() {
    }

    public static ASN1Primitive fromByteArray(byte[] data) throws IOException {
        ASN1InputStream aIn = new ASN1InputStream(data);
        try {
            ASN1Primitive o = aIn.readObject();
            if (aIn.available() == 0) {
                return o;
            }
            throw new IOException("Extra data detected in stream");
        } catch (ClassCastException e) {
            throw new IOException("cannot recognise object in stream");
        }
    }

    public final boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof ASN1Encodable) || !asn1Equals(((ASN1Encodable) o).toASN1Primitive())) {
            z = false;
        }
        return z;
    }

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
