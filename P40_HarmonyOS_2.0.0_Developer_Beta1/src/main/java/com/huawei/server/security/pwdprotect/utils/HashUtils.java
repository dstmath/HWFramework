package com.huawei.server.security.pwdprotect.utils;

import android.text.TextUtils;
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
    private static final int KEY_LENGTH = 1024;
    private static final int PBKDF2_HASH_ROUNDS = 5000;
    private static final String PBKDF2_HMAC_SHA1 = "PBKDF2WithHmacSHA1";
    private static final String TAG = "PwdProtectService";

    public static byte[] calculateHashWithHmacSha256(byte[] data, byte[] key) {
        if (data == null || key == null) {
            return new byte[0];
        }
        try {
            Mac mac = Mac.getInstance(HMAC_MD5);
            mac.init(new SecretKeySpec(key, HMAC_MD5));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "calculateHashWithHmacSha256: No such algorithm exception!");
            return new byte[0];
        } catch (InvalidKeyException e2) {
            Log.e(TAG, "calculateHashWithHmacSha256: Invalid key exception!");
            return new byte[0];
        }
    }

    public static byte[] calculateHashWithSha256(byte[] data) {
        if (data == null) {
            return new byte[0];
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_SHA256);
            messageDigest.update(data);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "calculateHashWithSha256: Calculate sha256 failed!");
            return new byte[0];
        }
    }

    public static byte[] calculateHashWithPbkdf(String password, byte[] salt) {
        if (TextUtils.isEmpty(password)) {
            return new byte[0];
        }
        if (salt == null || salt.length == 0) {
            return new byte[0];
        }
        byte[] keyResult = new byte[1024];
        try {
            return SecretKeyFactory.getInstance(PBKDF2_HMAC_SHA1).generateSecret(new PBEKeySpec(password.toCharArray(), salt, PBKDF2_HASH_ROUNDS, 256)).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "calculateHashWithPbkdf: No such algorithm exception!");
            return keyResult;
        } catch (InvalidKeySpecException e2) {
            Log.e(TAG, "calculateHashWithPbkdf: Invalid key exception!");
            return keyResult;
        }
    }
}
