package com.android.server.utils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2WithHmacSHA1 {
    public static final String C3_KEY = "b720a52ffe5e9e9b8ef5da107b17452b74a2abb770bfc36708cc623f710bea1f";

    public static byte[] generateStorngPasswordHash(String mainKey, String salt, int iterations) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec(mainKey.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), iterations, 256)).getEncoded();
    }
}
