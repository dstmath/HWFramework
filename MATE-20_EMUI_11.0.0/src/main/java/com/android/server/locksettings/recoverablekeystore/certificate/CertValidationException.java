package com.android.server.locksettings.recoverablekeystore.certificate;

public class CertValidationException extends Exception {
    public CertValidationException(String message) {
        super(message);
    }

    public CertValidationException(Exception cause) {
        super(cause);
    }

    public CertValidationException(String message, Exception cause) {
        super(message, cause);
    }
}
