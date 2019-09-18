package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

public class DLOutputStream extends ASN1OutputStream {
    public DLOutputStream(OutputStream os) {
        super(os);
    }

    public void writeObject(ASN1Encodable obj) throws IOException {
        if (obj != null) {
            obj.toASN1Primitive().toDLObject().encode(this);
            return;
        }
        throw new IOException("null object detected");
    }
}
