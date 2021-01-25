package org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

public class DLOutputStream extends ASN1OutputStream {
    public DLOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1OutputStream
    public ASN1OutputStream getDLSubStream() {
        return this;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1OutputStream
    public void writePrimitive(ASN1Primitive aSN1Primitive, boolean z) throws IOException {
        aSN1Primitive.toDLObject().encode(this, z);
    }
}
