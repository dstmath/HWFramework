package org.bouncycastle.cert;

public class CertException extends Exception {
    private Throwable cause;

    public CertException(String str) {
        super(str);
    }

    public CertException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
