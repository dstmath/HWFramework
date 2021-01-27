package org.bouncycastle.crypto.io;

public class InvalidCipherTextIOException extends CipherIOException {
    private static final long serialVersionUID = 1;

    public InvalidCipherTextIOException(String str, Throwable th) {
        super(str, th);
    }
}
