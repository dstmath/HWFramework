package org.bouncycastle.pkcs;

import java.io.IOException;

public class PKCSIOException extends IOException {
    private Throwable cause;

    public PKCSIOException(String str) {
        super(str);
    }

    public PKCSIOException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this.cause;
    }
}
