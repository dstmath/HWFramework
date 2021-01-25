package org.bouncycastle.cmc;

public class CMCException extends Exception {
    private final Throwable cause;

    public CMCException(String str) {
        this(str, null);
    }

    public CMCException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this.cause;
    }
}
