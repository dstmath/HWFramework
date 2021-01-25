package org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

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
    public abstract void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException;

    @Override // org.bouncycastle.asn1.ASN1Object
    public void encodeTo(OutputStream outputStream) throws IOException {
        ASN1OutputStream.create(outputStream).writeObject(this);
    }

    @Override // org.bouncycastle.asn1.ASN1Object
    public void encodeTo(OutputStream outputStream, String str) throws IOException {
        ASN1OutputStream.create(outputStream, str).writeObject(this);
    }

    /* access modifiers changed from: package-private */
    public abstract int encodedLength() throws IOException;

    @Override // org.bouncycastle.asn1.ASN1Object
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof ASN1Encodable) && asn1Equals(((ASN1Encodable) obj).toASN1Primitive());
    }

    public final boolean equals(ASN1Encodable aSN1Encodable) {
        return this == aSN1Encodable || (aSN1Encodable != null && asn1Equals(aSN1Encodable.toASN1Primitive()));
    }

    public final boolean equals(ASN1Primitive aSN1Primitive) {
        return this == aSN1Primitive || asn1Equals(aSN1Primitive);
    }

    @Override // org.bouncycastle.asn1.ASN1Object
    public abstract int hashCode();

    /* access modifiers changed from: package-private */
    public abstract boolean isConstructed();

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public final ASN1Primitive toASN1Primitive() {
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
