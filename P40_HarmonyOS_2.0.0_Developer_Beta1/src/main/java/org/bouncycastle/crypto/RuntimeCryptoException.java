package org.bouncycastle.crypto;

public class RuntimeCryptoException extends RuntimeException {
    public RuntimeCryptoException() {
    }

    public RuntimeCryptoException(String str) {
        super(str);
    }
}
