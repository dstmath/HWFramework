package com.android.org.bouncycastle.util.io.pem;

import java.io.IOException;

public class PemGenerationException extends IOException {
    private Throwable cause;

    public PemGenerationException(String message, Throwable cause2) {
        super(message);
        this.cause = cause2;
    }

    public PemGenerationException(String message) {
        super(message);
    }

    public Throwable getCause() {
        return this.cause;
    }
}
