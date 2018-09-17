package java.security;

import java.io.Serializable;

public abstract class SecureRandomSpi implements Serializable {
    private static final long serialVersionUID = -2991854161009191830L;

    protected abstract byte[] engineGenerateSeed(int i);

    protected abstract void engineNextBytes(byte[] bArr);

    protected abstract void engineSetSeed(byte[] bArr);
}
