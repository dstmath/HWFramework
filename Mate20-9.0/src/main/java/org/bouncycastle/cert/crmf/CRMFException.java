package org.bouncycastle.cert.crmf;

public class CRMFException extends Exception {
    private Throwable cause;

    public CRMFException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
