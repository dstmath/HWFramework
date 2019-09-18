package com.android.org.bouncycastle.x509;

import java.security.cert.CertificateEncodingException;

class ExtCertificateEncodingException extends CertificateEncodingException {
    Throwable cause;

    ExtCertificateEncodingException(String message, Throwable cause2) {
        super(message);
        this.cause = cause2;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
