package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;

public class BERSet extends ASN1Set {
    public BERSet(ASN1Encodable obj) {
        super(obj);
    }

    public BERSet(ASN1EncodableVector v) {
        super(v, false);
    }

    public BERSet(ASN1Encodable[] a) {
        super(a, false);
    }

    int encodedLength() throws IOException {
        int length = 0;
        Enumeration e = getObjects();
        while (e.hasMoreElements()) {
            length += ((ASN1Encodable) e.nextElement()).toASN1Primitive().encodedLength();
        }
        return (length + 2) + 2;
    }

    void encode(ASN1OutputStream out) throws IOException {
        out.write(49);
        out.write(128);
        Enumeration e = getObjects();
        while (e.hasMoreElements()) {
            out.writeObject((ASN1Encodable) e.nextElement());
        }
        out.write(0);
        out.write(0);
    }
}
