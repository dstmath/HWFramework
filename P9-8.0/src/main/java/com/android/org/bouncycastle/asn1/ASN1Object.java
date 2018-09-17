package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Encodable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class ASN1Object implements ASN1Encodable, Encodable {
    public abstract ASN1Primitive toASN1Primitive();

    public byte[] getEncoded() throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        new ASN1OutputStream(bOut).writeObject(this);
        return bOut.toByteArray();
    }

    public byte[] getEncoded(String encoding) throws IOException {
        ByteArrayOutputStream bOut;
        if (encoding.equals(ASN1Encoding.DER)) {
            bOut = new ByteArrayOutputStream();
            new DEROutputStream(bOut).writeObject(this);
            return bOut.toByteArray();
        } else if (!encoding.equals(ASN1Encoding.DL)) {
            return getEncoded();
        } else {
            bOut = new ByteArrayOutputStream();
            new DLOutputStream(bOut).writeObject(this);
            return bOut.toByteArray();
        }
    }

    public int hashCode() {
        return toASN1Primitive().hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ASN1Encodable)) {
            return false;
        }
        return toASN1Primitive().equals(((ASN1Encodable) o).toASN1Primitive());
    }

    public ASN1Primitive toASN1Object() {
        return toASN1Primitive();
    }

    protected static boolean hasEncodedTagValue(Object obj, int tagValue) {
        return (obj instanceof byte[]) && ((byte[]) obj)[0] == tagValue;
    }
}
