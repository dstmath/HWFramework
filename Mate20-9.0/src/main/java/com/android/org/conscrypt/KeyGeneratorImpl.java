package com.android.org.conscrypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public abstract class KeyGeneratorImpl extends KeyGeneratorSpi {
    private final String algorithm;
    private int keySizeBits;
    protected SecureRandom secureRandom;

    public static final class AES extends KeyGeneratorImpl {
        public AES() {
            super("AES", 128);
        }

        /* access modifiers changed from: protected */
        public void checkKeySize(int keySize) {
            if (keySize != 128 && keySize != 192 && keySize != 256) {
                throw new InvalidParameterException("Key size must be either 128, 192, or 256 bits");
            }
        }
    }

    public static final class ARC4 extends KeyGeneratorImpl {
        public ARC4() {
            super("ARC4", 128);
        }

        /* access modifiers changed from: protected */
        public void checkKeySize(int keySize) {
            if (keySize < 40 || 2048 < keySize) {
                throw new InvalidParameterException("Key size must be between 40 and 2048 bits");
            }
        }
    }

    public static final class ChaCha20 extends KeyGeneratorImpl {
        public ChaCha20() {
            super("ChaCha20", PSKKeyManager.MAX_KEY_LENGTH_BYTES);
        }

        /* access modifiers changed from: protected */
        public void checkKeySize(int keySize) {
            if (keySize != 256) {
                throw new InvalidParameterException("Key size must be 256 bits");
            }
        }
    }

    public static final class DESEDE extends KeyGeneratorImpl {
        public DESEDE() {
            super("DESEDE", 192);
        }

        /* access modifiers changed from: protected */
        public void checkKeySize(int keySize) {
            if (keySize != 112 && keySize != 168) {
                throw new InvalidParameterException("Key size must be either 112 or 168 bits");
            }
        }

        /* access modifiers changed from: protected */
        public byte[] doKeyGeneration(int keyBytes) {
            byte[] keyData = new byte[24];
            this.secureRandom.nextBytes(keyData);
            for (int i = 0; i < keyData.length; i++) {
                if (Integer.bitCount(keyData[i]) % 2 == 0) {
                    keyData[i] = (byte) (keyData[i] ^ 1);
                }
            }
            if (keyBytes == 14) {
                System.arraycopy(keyData, 0, keyData, 16, 8);
            }
            return keyData;
        }
    }

    public static final class HmacMD5 extends KeyGeneratorImpl {
        public HmacMD5() {
            super("HmacMD5", 128);
        }
    }

    public static final class HmacSHA1 extends KeyGeneratorImpl {
        public HmacSHA1() {
            super("HmacSHA1", 160);
        }
    }

    public static final class HmacSHA224 extends KeyGeneratorImpl {
        public HmacSHA224() {
            super("HmacSHA224", 224);
        }
    }

    public static final class HmacSHA256 extends KeyGeneratorImpl {
        public HmacSHA256() {
            super("HmacSHA256", PSKKeyManager.MAX_KEY_LENGTH_BYTES);
        }
    }

    public static final class HmacSHA384 extends KeyGeneratorImpl {
        public HmacSHA384() {
            super("HmacSHA384", 384);
        }
    }

    public static final class HmacSHA512 extends KeyGeneratorImpl {
        public HmacSHA512() {
            super("HmacSHA512", 512);
        }
    }

    private KeyGeneratorImpl(String algorithm2, int defaultKeySizeBits) {
        this.algorithm = algorithm2;
        this.keySizeBits = defaultKeySizeBits;
    }

    /* access modifiers changed from: protected */
    public void checkKeySize(int keySize) {
        if (keySize <= 0) {
            throw new InvalidParameterException("Key size must be positive");
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(SecureRandom secureRandom2) {
        this.secureRandom = secureRandom2;
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec params, SecureRandom secureRandom2) throws InvalidAlgorithmParameterException {
        if (params == null) {
            throw new InvalidAlgorithmParameterException("No params provided");
        }
        throw new InvalidAlgorithmParameterException("Unknown param type: " + params.getClass().getName());
    }

    /* access modifiers changed from: protected */
    public void engineInit(int keySize, SecureRandom secureRandom2) {
        checkKeySize(keySize);
        this.keySizeBits = keySize;
        this.secureRandom = secureRandom2;
    }

    /* access modifiers changed from: protected */
    public byte[] doKeyGeneration(int keyBytes) {
        byte[] keyData = new byte[keyBytes];
        this.secureRandom.nextBytes(keyData);
        return keyData;
    }

    /* access modifiers changed from: protected */
    public SecretKey engineGenerateKey() {
        if (this.secureRandom == null) {
            this.secureRandom = new SecureRandom();
        }
        return new SecretKeySpec(doKeyGeneration((this.keySizeBits + 7) / 8), this.algorithm);
    }
}
