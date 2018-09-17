package com.android.org.bouncycastle.crypto;

public class InvalidCipherTextException extends CryptoException {
    public InvalidCipherTextException(String message) {
        super(message);
    }

    public InvalidCipherTextException(String message, Throwable cause) {
        super(message, cause);
    }
}
