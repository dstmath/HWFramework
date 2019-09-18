package com.huawei.secure.android.common.encrypt.hash;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.secure.android.common.util.EncryptUtil;
import com.huawei.secure.android.common.util.HexUtil;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2 {
    private static final String EMPTY = "";
    private static final int HASH_BYTE_SIZE = 32;
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int PBKDF2_ITERATIONS_TIMES = 10000;
    private static final int PBKDF2_MIN_ITERATIONS_TIMES = 1000;
    private static final int SALT_LEN = 8;
    private static final String TAG = "PBKDF2";

    public static String pbkdf2Encrypt(String password) {
        return pbkdf2Encrypt(password, 10000);
    }

    public static String pbkdf2Encrypt(String password, int iterations) {
        if (TextUtils.isEmpty(password) || iterations < 1000) {
            Log.e(TAG, "pwd is null or iterations times is not enough.");
            return "";
        }
        byte[] salt = EncryptUtil.generateSecureRandom(8);
        byte[] hash = pbkdf2(password.toCharArray(), salt, iterations, 256);
        return HexUtil.byteArray2HexStr(salt) + HexUtil.byteArray2HexStr(hash);
    }

    public static boolean validatePassword(String password, String encryptPassword) {
        return validatePassword(password, encryptPassword, 10000);
    }

    public static boolean validatePassword(String password, String encryptPassword, int iterations) {
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(encryptPassword) || encryptPassword.length() < 16) {
            return false;
        }
        return isByteArrayEqual(pbkdf2(password.toCharArray(), HexUtil.hexStr2ByteArray(encryptPassword.substring(0, 16)), iterations, 256), HexUtil.hexStr2ByteArray(encryptPassword.substring(16)));
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) {
        try {
            return SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(new PBEKeySpec(password, salt, iterations, bytes)).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, e.getMessage());
            return new byte[0];
        }
    }

    private static boolean isByteArrayEqual(byte[] realHash, byte[] tempHash) {
        boolean z = false;
        if (realHash == null || tempHash == null) {
            return false;
        }
        int diff = realHash.length ^ tempHash.length;
        int i = 0;
        while (i < realHash.length && i < tempHash.length) {
            diff |= realHash[i] ^ tempHash[i];
            i++;
        }
        if (diff == 0) {
            z = true;
        }
        return z;
    }
}
