package org.bouncycastle.cert.crmf;

public class CRMFRuntimeException extends RuntimeException {
    private Throwable cause;

    public CRMFRuntimeException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this.cause;
    }
}
