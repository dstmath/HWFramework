package org.bouncycastle.operator;

public class RuntimeOperatorException extends RuntimeException {
    private Throwable cause;

    public RuntimeOperatorException(String str) {
        super(str);
    }

    public RuntimeOperatorException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this.cause;
    }
}
