package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

public class DEROutputStream extends ASN1OutputStream {
    public DEROutputStream(OutputStream os) {
        super(os);
    }

    public void writeObject(ASN1Encodable obj) throws IOException {
        if (obj != null) {
            obj.toASN1Primitive().toDERObject().encode(this);
            return;
        }
        throw new IOException("null object detected");
    }

    /* access modifiers changed from: package-private */
    public ASN1OutputStream getDERSubStream() {
        return this;
    }

    /* access modifiers changed from: package-private */
    public ASN1OutputStream getDLSubStream() {
        return this;
    }
}
