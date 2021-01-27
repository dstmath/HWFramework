package org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.util.Encodable;

public abstract class ASN1Object implements ASN1Encodable, Encodable {
    protected static boolean hasEncodedTagValue(Object obj, int i) {
        return (obj instanceof byte[]) && ((byte[]) obj)[0] == i;
    }

    public void encodeTo(OutputStream outputStream) throws IOException {
        ASN1OutputStream.create(outputStream).writeObject(this);
    }

    public void encodeTo(OutputStream outputStream, String str) throws IOException {
        ASN1OutputStream.create(outputStream, str).writeObject(this);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ASN1Encodable)) {
            return false;
        }
        return toASN1Primitive().equals(((ASN1Encodable) obj).toASN1Primitive());
    }

    @Override // org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        encodeTo(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] getEncoded(String str) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        encodeTo(byteArrayOutputStream, str);
        return byteArrayOutputStream.toByteArray();
    }

    public int hashCode() {
        return toASN1Primitive().hashCode();
    }

    public ASN1Primitive toASN1Object() {
        return toASN1Primitive();
    }

    @Override // org.bouncycastle.asn1.ASN1Encodable
    public abstract ASN1Primitive toASN1Primitive();
}
