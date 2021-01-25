package com.android.server.backup.encryption.chunking;

import com.android.server.backup.encryption.chunk.ChunkHash;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class ChunkHasher {
    private static final String MAC_ALGORITHM = "HmacSHA256";
    private final SecretKey mSecretKey;

    public ChunkHasher(SecretKey secretKey) {
        this.mSecretKey = secretKey;
    }

    public ChunkHash computeHash(byte[] plaintext) throws InvalidKeyException {
        try {
            Mac mac = Mac.getInstance(MAC_ALGORITHM);
            mac.init(this.mSecretKey);
            return new ChunkHash(mac.doFinal(plaintext));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }
}
