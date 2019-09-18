package org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

public class BEROutputStream extends DEROutputStream {
    public BEROutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    public void writeObject(Object obj) throws IOException {
        ASN1Primitive aSN1Primitive;
        if (obj == null) {
            writeNull();
            return;
        }
        if (obj instanceof ASN1Primitive) {
            aSN1Primitive = (ASN1Primitive) obj;
        } else if (obj instanceof ASN1Encodable) {
            aSN1Primitive = ((ASN1Encodable) obj).toASN1Primitive();
        } else {
            throw new IOException("object not BEREncodable");
        }
        aSN1Primitive.encode(this);
    }
}
