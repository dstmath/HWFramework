package com.android.org.bouncycastle.util.encoders;

public class DecoderException extends IllegalStateException {
    private Throwable cause;

    DecoderException(String msg, Throwable cause2) {
        super(msg);
        this.cause = cause2;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
