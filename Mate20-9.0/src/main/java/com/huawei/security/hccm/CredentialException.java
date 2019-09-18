package com.huawei.security.hccm;

public class CredentialException extends Exception {
    public CredentialException() {
    }

    public CredentialException(String message) {
        super(message);
    }

    public CredentialException(String message, Throwable t) {
        super(message, t);
    }
}
