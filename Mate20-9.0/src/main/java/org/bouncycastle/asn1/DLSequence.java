package org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;

public class DLSequence extends ASN1Sequence {
    private int bodyLength = -1;

    public DLSequence() {
    }

    public DLSequence(ASN1Encodable aSN1Encodable) {
        super(aSN1Encodable);
    }

    public DLSequence(ASN1EncodableVector aSN1EncodableVector) {
        super(aSN1EncodableVector);
    }

    public DLSequence(ASN1Encodable[] aSN1EncodableArr) {
        super(aSN1EncodableArr);
    }

    private int getBodyLength() throws IOException {
        if (this.bodyLength < 0) {
            int i = 0;
            Enumeration objects = getObjects();
            while (objects.hasMoreElements()) {
                i += ((ASN1Encodable) objects.nextElement()).toASN1Primitive().toDLObject().encodedLength();
            }
            this.bodyLength = i;
        }
        return this.bodyLength;
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream aSN1OutputStream) throws IOException {
        ASN1OutputStream dLSubStream = aSN1OutputStream.getDLSubStream();
        int bodyLength2 = getBodyLength();
        aSN1OutputStream.write(48);
        aSN1OutputStream.writeLength(bodyLength2);
        Enumeration objects = getObjects();
        while (objects.hasMoreElements()) {
            dLSubStream.writeObject((ASN1Encodable) objects.nextElement());
        }
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() throws IOException {
        int bodyLength2 = getBodyLength();
        return 1 + StreamUtil.calculateBodyLength(bodyLength2) + bodyLength2;
    }
}
