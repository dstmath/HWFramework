package com.android.server.security.pwdprotect.utils;

import android.util.Log;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class HashUtils {
    private static final int CRYPTO_ALGORITHM_KEY_SIZE = 256;
    private static final String HASH_SHA256 = "SHA-256";
    private static final String HMAC_MD5 = "HmacMD5";
    private static final int PBKDF2_HASH_ROUNDS = 5000;
    private static final String PBKDF2_HmacSHA1 = "PBKDF2WithHmacSHA1";
    private static final String TAG = "PwdProtectService";

    public static byte[] encryHmacSha256(byte[] data, byte[] key) {
        if (data == null || key == null) {
            return new byte[0];
        }
        try {
            Mac mac = Mac.getInstance(HMAC_MD5);
            mac.init(new SecretKeySpec(key, HMAC_MD5));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "encryptedHmacSha256 no such algorithm exception");
            return new byte[0];
        } catch (InvalidKeyException e2) {
            Log.e(TAG, "encryptedHmacSha256 invalid key exception");
            return new byte[0];
        }
    }

    public static byte[] calHash256(byte[] data) {
        if (data == null) {
            return new byte[0];
        }
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_SHA256);
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "calculate sha256 failed");
            return new byte[0];
        }
    }

    public static byte[] pkdfEncry(String password, byte[] salt) {
        if (password == null) {
            return new byte[0];
        }
        byte[] key_result = new byte[1024];
        try {
            return SecretKeyFactory.getInstance(PBKDF2_HmacSHA1).generateSecret(new PBEKeySpec(password.toCharArray(), salt, PBKDF2_HASH_ROUNDS, 256)).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "pkdfEncry no such algorithm exception");
            return key_result;
        } catch (InvalidKeySpecException e2) {
            Log.e(TAG, "pkdfEncry invalid key exception");
            return key_result;
        }
    }
}
