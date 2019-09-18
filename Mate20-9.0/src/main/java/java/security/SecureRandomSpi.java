package java.security;

import java.io.Serializable;

public abstract class SecureRandomSpi implements Serializable {
    private static final long serialVersionUID = -2991854161009191830L;

    /* access modifiers changed from: protected */
    public abstract byte[] engineGenerateSeed(int i);

    /* access modifiers changed from: protected */
    public abstract void engineNextBytes(byte[] bArr);

    /* access modifiers changed from: protected */
    public abstract void engineSetSeed(byte[] bArr);
}
