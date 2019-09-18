package com.android.org.bouncycastle.jcajce.provider.util;

import javax.crypto.BadPaddingException;

public class BadBlockException extends BadPaddingException {
    private final Throwable cause;

    public BadBlockException(String msg, Throwable cause2) {
        super(msg);
        this.cause = cause2;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
