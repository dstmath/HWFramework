package com.android.org.bouncycastle.jce.exception;

import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderException;

public class ExtCertPathBuilderException extends CertPathBuilderException implements ExtException {
    private Throwable cause;

    public ExtCertPathBuilderException(String message, Throwable cause2) {
        super(message);
        this.cause = cause2;
    }

    public ExtCertPathBuilderException(String msg, Throwable cause2, CertPath certPath, int index) {
        super(msg, cause2);
        this.cause = cause2;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
