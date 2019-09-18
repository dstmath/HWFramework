package org.bouncycastle.asn1;

import java.io.IOException;

public class ASN1Exception extends IOException {
    private Throwable cause;

    ASN1Exception(String str) {
        super(str);
    }

    ASN1Exception(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
