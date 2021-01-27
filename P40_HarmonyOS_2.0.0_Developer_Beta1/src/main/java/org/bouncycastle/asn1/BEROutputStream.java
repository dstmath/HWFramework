package org.bouncycastle.asn1;

import java.io.OutputStream;

public class BEROutputStream extends ASN1OutputStream {
    public BEROutputStream(OutputStream outputStream) {
        super(outputStream);
    }
}
