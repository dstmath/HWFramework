package com.huawei.coauth.remotepin.util;

import android.support.annotation.NonNull;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AlgorithmHelper {
    private static final String ALGORITHM_PBDKF2 = "PBKDF2withHmacSHA256";
    private static final String ALGORITHM_PRNG = "SHA1PRNG";
    private static final int MAX_RANDOM_LEN = 1024;
    private static final int PBKDF2_HMAC_COUNT = 10000;
    private static final int PBKDF2_HMAC_KEY_LENGTH = 256;
    private static final String TAG = AlgorithmHelper.class.getSimpleName();

    private AlgorithmHelper() {
    }

    public static byte[] generateRandomBytes(int byteLength) {
        if (byteLength <= 0 || byteLength >= 1024) {
            return new byte[0];
        }
        byte[] result = new byte[byteLength];
        createSecureRandom().nextBytes(result);
        return result;
    }

    @NonNull
    public static byte[] pbkdf2(@NonNull byte[] password, @NonNull byte[] salt) {
        return pbkdf2(password, salt, 10000);
    }

    @NonNull
    public static byte[] pbkdf2(@NonNull byte[] password, @NonNull byte[] salt, int iterationCount) {
        byte[] result = new byte[0];
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM_PBDKF2);
            if (factory == null) {
                return new byte[0];
            }
            SecretKey key = factory.generateSecret(new PBEKeySpec(new String(password, StandardCharsets.UTF_8).toCharArray(), salt, iterationCount, 256));
            if (key == null) {
                return new byte[0];
            }
            result = key.getEncoded();
            if (result == null) {
                return new byte[0];
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            HwLog.e(TAG, "pkdf Encrypt no such algorithm exception");
        } catch (InvalidKeySpecException e2) {
            HwLog.e(TAG, "pkdf Encrypt invalid key exception");
        }
    }

    private static SecureRandom createSecureRandom() {
        SecureRandom result = null;
        try {
            result = SecureRandom.getInstance(ALGORITHM_PRNG);
        } catch (NoSuchAlgorithmException e) {
            HwLog.e(TAG, "No such Algorithm");
        }
        if (result == null) {
            result = new SecureRandom();
        }
        result.nextInt();
        return result;
    }
}
