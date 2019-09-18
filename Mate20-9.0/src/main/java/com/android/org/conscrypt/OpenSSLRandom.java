package com.android.org.conscrypt;

import java.io.Serializable;
import java.security.SecureRandomSpi;

public final class OpenSSLRandom extends SecureRandomSpi implements Serializable {
    private static final long serialVersionUID = 8506210602917522861L;

    /* access modifiers changed from: protected */
    public void engineSetSeed(byte[] seed) {
        if (seed == null) {
            throw new NullPointerException("seed == null");
        }
    }

    /* access modifiers changed from: protected */
    public void engineNextBytes(byte[] bytes) {
        NativeCrypto.RAND_bytes(bytes);
    }

    /* access modifiers changed from: protected */
    public byte[] engineGenerateSeed(int numBytes) {
        byte[] output = new byte[numBytes];
        NativeCrypto.RAND_bytes(output);
        return output;
    }
}
