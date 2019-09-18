package org.bouncycastle.cert.cmp;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DEROutputStream;

class CMPUtil {
    CMPUtil() {
    }

    static void derEncodeToStream(ASN1Encodable aSN1Encodable, OutputStream outputStream) {
        DEROutputStream dEROutputStream = new DEROutputStream(outputStream);
        try {
            dEROutputStream.writeObject(aSN1Encodable);
            dEROutputStream.close();
        } catch (IOException e) {
            throw new CMPRuntimeException("unable to DER encode object: " + e.getMessage(), e);
        }
    }
}
