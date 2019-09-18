package org.bouncycastle.util.encoders;

public class DecoderException extends IllegalStateException {
    private Throwable cause;

    DecoderException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
