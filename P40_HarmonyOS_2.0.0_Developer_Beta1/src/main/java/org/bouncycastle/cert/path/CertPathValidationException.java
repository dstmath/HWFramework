package org.bouncycastle.cert.path;

public class CertPathValidationException extends Exception {
    private final Exception cause;

    public CertPathValidationException(String str) {
        this(str, null);
    }

    public CertPathValidationException(String str, Exception exc) {
        super(str);
        this.cause = exc;
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this.cause;
    }
}
