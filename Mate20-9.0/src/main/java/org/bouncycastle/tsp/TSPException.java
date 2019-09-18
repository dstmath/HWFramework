package org.bouncycastle.tsp;

public class TSPException extends Exception {
    Throwable underlyingException;

    public TSPException(String str) {
        super(str);
    }

    public TSPException(String str, Throwable th) {
        super(str);
        this.underlyingException = th;
    }

    public Throwable getCause() {
        return this.underlyingException;
    }

    public Exception getUnderlyingException() {
        return (Exception) this.underlyingException;
    }
}
