package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;

public class DERSet extends ASN1Set {
    private int bodyLength = -1;

    public DERSet(ASN1Encodable obj) {
        super(obj);
    }

    public DERSet(ASN1EncodableVector v) {
        super(v, true);
    }

    public DERSet(ASN1Encodable[] a) {
        super(a, true);
    }

    DERSet(ASN1EncodableVector v, boolean doSort) {
        super(v, doSort);
    }

    private int getBodyLength() throws IOException {
        if (this.bodyLength < 0) {
            int length = 0;
            Enumeration e = getObjects();
            while (e.hasMoreElements()) {
                length += ((ASN1Encodable) e.nextElement()).toASN1Primitive().toDERObject().encodedLength();
            }
            this.bodyLength = length;
        }
        return this.bodyLength;
    }

    int encodedLength() throws IOException {
        int length = getBodyLength();
        return (StreamUtil.calculateBodyLength(length) + 1) + length;
    }

    void encode(ASN1OutputStream out) throws IOException {
        ASN1OutputStream dOut = out.getDERSubStream();
        int length = getBodyLength();
        out.write(49);
        out.writeLength(length);
        Enumeration e = getObjects();
        while (e.hasMoreElements()) {
            dOut.writeObject((ASN1Encodable) e.nextElement());
        }
    }
}
