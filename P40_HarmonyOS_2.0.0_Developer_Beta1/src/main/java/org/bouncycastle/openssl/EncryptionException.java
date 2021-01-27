package org.bouncycastle.openssl;

public class EncryptionException extends PEMException {
    private Throwable cause;

    public EncryptionException(String str) {
        super(str);
    }

    public EncryptionException(String str, Throwable th) {
        super(str);
        this.cause = th;
    }

    @Override // org.bouncycastle.openssl.PEMException, java.lang.Throwable
    public Throwable getCause() {
        return this.cause;
    }
}
