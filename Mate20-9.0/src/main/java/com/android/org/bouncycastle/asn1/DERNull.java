package com.android.org.bouncycastle.asn1;

import java.io.IOException;

public class DERNull extends ASN1Null {
    public static final DERNull INSTANCE = new DERNull();
    private static final byte[] zeroBytes = new byte[0];

    protected DERNull() {
    }

    /* access modifiers changed from: package-private */
    public boolean isConstructed() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() {
        return 2;
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream out) throws IOException {
        out.writeEncoded(5, zeroBytes);
    }
}
