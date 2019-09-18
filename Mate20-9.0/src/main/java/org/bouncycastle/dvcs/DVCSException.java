package org.bouncycastle.dvcs;

public class DVCSException extends Exception {
    private static final long serialVersionUID = 389345256020131488L;
    private Throwable cause;

    public DVCSException(String str) {
        super(str);
    }

    public DVCSException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
