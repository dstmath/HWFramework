package com.android.org.bouncycastle.jcajce.provider.asymmetric.util;

import java.security.spec.InvalidKeySpecException;

public class ExtendedInvalidKeySpecException extends InvalidKeySpecException {
    private Throwable cause;

    public ExtendedInvalidKeySpecException(String msg, Throwable cause2) {
        super(msg);
        this.cause = cause2;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
