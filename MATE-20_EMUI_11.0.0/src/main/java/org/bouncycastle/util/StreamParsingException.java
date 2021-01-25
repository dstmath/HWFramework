package org.bouncycastle.util;

public class StreamParsingException extends Exception {
    Throwable _e;

    public StreamParsingException(String str, Throwable th) {
        super(str);
        this._e = th;
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this._e;
    }
}
