package com.android.org.conscrypt;

import java.io.Serializable;
import java.security.SecureRandomSpi;

public class OpenSSLRandom extends SecureRandomSpi implements Serializable {
    private static final long serialVersionUID = 8506210602917522861L;

    protected void engineSetSeed(byte[] seed) {
        if (seed == null) {
            throw new NullPointerException("seed == null");
        }
    }

    protected void engineNextBytes(byte[] bytes) {
        NativeCrypto.RAND_bytes(bytes);
    }

    protected byte[] engineGenerateSeed(int numBytes) {
        byte[] output = new byte[numBytes];
        NativeCrypto.RAND_bytes(output);
        return output;
    }
}
