package com.huawei.coauth.remotepin.util;

import android.support.annotation.NonNull;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AlgorithmHelper {
    private static final String ALGORITHM_HMAC = "HmacSHA256";
    private static final String ALGORITHM_PBDKF2 = "PBKDF2withHmacSHA256";
    private static final String ALGORITHM_PRNG = "SHA1PRNG";
    private static final String ALGORITHM_SHA256 = "SHA-256";
    private static final byte[] BIG_TWO = {2};
    private static final int MAX_RANDOM_LEN = 1024;
    private static final int PBKDF2_HMAC_COUNT = 10000;
    private static final int PBKDF2_HMAC_KEY_LENGTH = 256;
    private static final byte[] P_VALUE = {-1, -1, -1, -1, -1, -1, -1, -1, -55, 15, -38, -94, 33, 104, -62, 52, -60, -58, 98, -117, Byte.MIN_VALUE, -36, 28, -47, 41, 2, 78, 8, -118, 103, -52, 116, 2, 11, -66, -90, 59, 19, -101, 34, 81, 74, 8, 121, -114, 52, 4, -35, -17, -107, 25, -77, -51, 58, 67, 27, 48, 43, 10, 109, -14, 95, 20, 55, 79, -31, 53, 109, 109, 81, -62, 69, -28, -123, -75, 118, 98, 94, 126, -58, -12, 76, 66, -23, -90, 55, -19, 107, 11, -1, 92, -74, -12, 6, -73, -19, -18, 56, 107, -5, 90, -119, -97, -91, -82, -97, 36, 17, 124, 75, 31, -26, 73, 40, 102, 81, -20, -28, 91, 61, -62, 0, 124, -72, -95, 99, -65, 5, -104, -38, 72, 54, 28, 85, -45, -102, 105, 22, 63, -88, -3, 36, -49, 95, -125, 101, 93, 35, -36, -93, -83, -106, 28, 98, -13, 86, 32, -123, 82, -69, -98, -43, 41, 7, 112, -106, -106, 109, 103, 12, 53, 78, 74, -68, -104, 4, -15, 116, 108, 8, -54, 24, 33, 124, 50, -112, 94, 70, 46, 54, -50, 59, -29, -98, 119, 44, 24, 14, -122, 3, -101, 39, -125, -94, -20, 7, -94, -113, -75, -59, 93, -16, 111, 76, 82, -55, -34, 43, -53, -10, -107, 88, 23, 24, 57, -107, 73, 124, -22, -107, 106, -27, 21, -46, 38, 24, -104, -6, 5, 16, 21, 114, -114, 90, -118, -86, -60, 45, -83, 51, 23, 13, 4, 80, 122, 51, -88, 85, 33, -85, -33, 28, -70, 100, -20, -5, -123, 4, 88, -37, -17, 10, -118, -22, 113, 87, 93, 6, 12, 125, -77, -105, 15, -123, -90, -31, -28, -57, -85, -11, -82, -116, -37, 9, 51, -41, 30, -116, -108, -32, 74, 37, 97, -99, -50, -29, -46, 38, 26, -46, -18, 107, -15, 47, -6, 6, -39, -118, 8, 100, -40, 118, 2, 115, 62, -56, 106, 100, 82, 31, 43, 24, 23, 123, 32, 12, -69, -31, 23, 87, 122, 97, 93, 108, 119, 9, -120, -64, -70, -39, 70, -30, 8, -30, 79, -96, 116, -27, -85, 49, 67, -37, 91, -4, -32, -3, 16, -114, 75, -126, -47, 32, -87, 58, -46, -54, -1, -1, -1, -1, -1, -1, -1, -1};
    private static final int SIGN_POS = 1;
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

    public static byte[] squareModP(byte[] base) {
        return squareMod(base, P_VALUE);
    }

    public static byte[] powerModP(byte[] base, byte[] exponent) {
        return powerMod(base, exponent, P_VALUE);
    }

    @NonNull
    public static byte[] getHmac(@NonNull byte[] data, @NonNull byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            if (mac == null) {
                return new byte[0];
            }
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            HwLog.e(TAG, "getHmac no such algorithm exception");
            return new byte[0];
        } catch (InvalidKeyException e2) {
            HwLog.e(TAG, "getHmac invalid key exception");
            return new byte[0];
        }
    }

    @NonNull
    public static byte[] getSha256(@NonNull byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            HwLog.e(TAG, "getSha256 failed");
            return new byte[0];
        }
    }

    private static byte[] squareMod(byte[] base, byte[] mod) {
        return powerMod(base, BIG_TWO, mod);
    }

    private static byte[] powerMod(byte[] base, byte[] exponent, byte[] mod) {
        return new BigInteger(1, base).modPow(new BigInteger(1, exponent), new BigInteger(1, mod)).toByteArray();
    }

    public static byte[] mergeBytes(byte[] bytes1, byte[] bytes2) {
        if (bytes1 == null || bytes2 == null) {
            HwLog.e(TAG, "param is null, mergeBytes fail");
            return new byte[0];
        }
        byte[] bytes = new byte[(bytes1.length + bytes2.length)];
        System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
        return bytes;
    }
}
