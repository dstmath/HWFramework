package org.bouncycastle.cert.cmp;

public class CMPRuntimeException extends RuntimeException {
    private Throwable cause;

    public CMPRuntimeException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
