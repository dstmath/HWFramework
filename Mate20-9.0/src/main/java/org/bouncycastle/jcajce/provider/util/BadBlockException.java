package org.bouncycastle.jcajce.provider.util;

import javax.crypto.BadPaddingException;

public class BadBlockException extends BadPaddingException {
    private final Throwable cause;

    public BadBlockException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
