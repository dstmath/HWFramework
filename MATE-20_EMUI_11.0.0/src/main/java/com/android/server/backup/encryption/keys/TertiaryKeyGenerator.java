package com.android.server.backup.encryption.keys;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class TertiaryKeyGenerator {
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_SIZE_BITS = 256;
    private final KeyGenerator mKeyGenerator;

    public TertiaryKeyGenerator(SecureRandom secureRandom) {
        try {
            this.mKeyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            this.mKeyGenerator.init(256, secureRandom);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Impossible condition: JCE thinks it does not support AES.", e);
        }
    }

    public SecretKey generate() {
        return this.mKeyGenerator.generateKey();
    }
}
