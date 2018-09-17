package com.android.org.bouncycastle.asn1;

import java.io.IOException;

public class DERNull extends ASN1Null {
    public static final DERNull INSTANCE = new DERNull();
    private static final byte[] zeroBytes = new byte[0];

    protected DERNull() {
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        return 2;
    }

    void encode(ASN1OutputStream out) throws IOException {
        out.writeEncoded(5, zeroBytes);
    }
}
