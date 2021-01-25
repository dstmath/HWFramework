package com.android.server.backup.encryption.chunking;

import com.android.server.backup.encryption.chunk.ChunkHash;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class ChunkEncryptor {
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_NONCE_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BYTES = 16;
    private final SecretKey mSecretKey;
    private final SecureRandom mSecureRandom;

    public ChunkEncryptor(SecretKey secretKey, SecureRandom secureRandom) {
        this.mSecretKey = secretKey;
        this.mSecureRandom = secureRandom;
    }

    public EncryptedChunk encrypt(ChunkHash plaintextHash, byte[] plaintext) throws InvalidKeyException, IllegalBlockSizeException {
        byte[] nonce = generateNonce();
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(1, this.mSecretKey, new GCMParameterSpec(128, nonce));
            try {
                return EncryptedChunk.create(plaintextHash, nonce, cipher.doFinal(plaintext));
            } catch (BadPaddingException e) {
                throw new AssertionError("Impossible: threw BadPaddingException in encrypt mode.");
            }
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException e2) {
            throw new AssertionError(e2);
        }
    }

    private byte[] generateNonce() {
        byte[] nonce = new byte[12];
        this.mSecureRandom.nextBytes(nonce);
        return nonce;
    }
}
