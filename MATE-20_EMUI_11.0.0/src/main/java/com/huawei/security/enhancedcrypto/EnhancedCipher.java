package com.huawei.security.enhancedcrypto;

import android.support.annotation.NonNull;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

public class EnhancedCipher {
    public static final int DECRYPT_MODE = 2;
    public static final int ENCRYPT_MODE = 1;
    private EnhancedCipherSpi mCipherSpi;

    private EnhancedCipher(EnhancedCipherSpi cipherSpi) {
        this.mCipherSpi = cipherSpi;
    }

    public static EnhancedCipher getInstance(String transformation) throws NoSuchAlgorithmException {
        if ("AES/GCM/NoPadding".equals(transformation)) {
            return new EnhancedCipher(new AesGcmEnhancedCipherSpi());
        }
        throw new NoSuchAlgorithmException("Unsupported transformation: " + transformation);
    }

    public void init(int opmode, @NonNull byte[] key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.mCipherSpi.engineInit(opmode, key, algorithmParameterSpec);
    }

    public void updateAad(@NonNull byte[] input, int inputOffset, int inputLen) {
        this.mCipherSpi.engineUpdateAad(input, inputOffset, inputLen);
    }

    public int doFinal(@NonNull byte[] input, int inputOffset, int inputLen, @NonNull byte[] output, int outputOffset) {
        return this.mCipherSpi.engineDoFinal(input, inputOffset, inputLen, output, outputOffset);
    }
}
