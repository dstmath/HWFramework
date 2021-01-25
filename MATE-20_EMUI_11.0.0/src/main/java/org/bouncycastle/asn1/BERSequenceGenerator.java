package org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

public class BERSequenceGenerator extends BERGenerator {
    public BERSequenceGenerator(OutputStream outputStream) throws IOException {
        super(outputStream);
        writeBERHeader(48);
    }

    public BERSequenceGenerator(OutputStream outputStream, int i, boolean z) throws IOException {
        super(outputStream, i, z);
        writeBERHeader(48);
    }

    public void addObject(ASN1Encodable aSN1Encodable) throws IOException {
        aSN1Encodable.toASN1Primitive().encodeTo(this._out);
    }

    public void close() throws IOException {
        writeBEREnd();
    }
}
