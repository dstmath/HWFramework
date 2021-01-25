package org.bouncycastle.crypto;

public class MaxBytesExceededException extends RuntimeCryptoException {
    public MaxBytesExceededException() {
    }

    public MaxBytesExceededException(String str) {
        super(str);
    }
}
