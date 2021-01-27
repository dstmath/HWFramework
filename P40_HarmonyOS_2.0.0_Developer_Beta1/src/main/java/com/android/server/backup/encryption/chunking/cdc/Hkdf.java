package com.android.server.backup.encryption.chunking.cdc;

import com.android.internal.util.Preconditions;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class Hkdf {
    private static final String AES = "AES";
    private static final byte[] CONSTANT_01 = {1};
    private static final String HmacSHA256 = "HmacSHA256";

    static byte[] hkdf(byte[] masterKey, byte[] salt, byte[] data) throws InvalidKeyException {
        Preconditions.checkNotNull(masterKey, "HKDF requires master key to be set.");
        Preconditions.checkNotNull(salt, "HKDF requires a salt.");
        Preconditions.checkNotNull(data, "No data provided to HKDF.");
        return hkdfSha256Expand(hkdfSha256Extract(masterKey, salt), data);
    }

    private Hkdf() {
    }

    private static byte[] hkdfSha256Extract(byte[] inputKeyMaterial, byte[] salt) throws InvalidKeyException {
        try {
            Mac sha256 = Mac.getInstance(HmacSHA256);
            sha256.init(new SecretKeySpec(salt, AES));
            return sha256.doFinal(inputKeyMaterial);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static byte[] hkdfSha256Expand(byte[] pseudoRandomKey, byte[] info) throws InvalidKeyException {
        try {
            Mac sha256 = Mac.getInstance(HmacSHA256);
            sha256.init(new SecretKeySpec(pseudoRandomKey, AES));
            sha256.update(info);
            sha256.update(CONSTANT_01);
            return sha256.doFinal();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }
}
