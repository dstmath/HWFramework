package com.huawei.secure.android.common.encrypt.aes;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.secure.android.common.util.EncryptUtil;
import com.huawei.secure.android.common.util.HexUtil;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AesCbc {
    private static final int AES_128_CBC_IV_LEN = 16;
    private static final int AES_128_CBC_KEY_LEN = 16;
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_CBC_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String EMPTY = "";
    private static final String TAG = "AesCbc";

    private AesCbc() {
    }

    public static String encrypt(String content, String key) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(key)) {
            return "";
        }
        byte[] secretkey = HexUtil.hexStr2ByteArray(key);
        if (secretkey != null && secretkey.length >= 16) {
            return encrypt(content, secretkey);
        }
        Log.e(TAG, "key length is not right");
        return "";
    }

    public static String encrypt(String content, byte[] key) {
        if (TextUtils.isEmpty(content) || key == null || key.length < 16) {
            return "";
        }
        try {
            SecretKeySpec secretkey = new SecretKeySpec(key, AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] ivParameter = EncryptUtil.generateSecureRandom(16);
            cipher.init(1, secretkey, new IvParameterSpec(ivParameter));
            return mixIvAndEncryWord(HexUtil.byteArray2HexStr(ivParameter), HexUtil.byteArray2HexStr(cipher.doFinal(content.getBytes(AES.CHAR_ENCODING))));
        } catch (RuntimeException e) {
            Log.e(TAG, " cbc encrypt data error" + e.getMessage());
            return "";
        } catch (Exception e2) {
            Log.e(TAG, " cbc encrypt data error" + e2.getMessage());
            return "";
        }
    }

    public static String decrypt(String content, String key) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(key)) {
            return "";
        }
        byte[] secretkey = HexUtil.hexStr2ByteArray(key);
        if (secretkey != null && secretkey.length >= 16) {
            return decrypt(content, secretkey);
        }
        Log.e(TAG, "key length is not right");
        return "";
    }

    public static String decrypt(String content, byte[] key) {
        if (TextUtils.isEmpty(content) || key == null || key.length < 16) {
            return "";
        }
        try {
            SecretKeySpec secretkey = new SecretKeySpec(key, AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            String ivParameter = getIv(content);
            String encrypedWord = getEncryptWord(content);
            if (!TextUtils.isEmpty(ivParameter)) {
                if (!TextUtils.isEmpty(encrypedWord)) {
                    cipher.init(2, secretkey, new IvParameterSpec(HexUtil.hexStr2ByteArray(ivParameter)));
                    return new String(cipher.doFinal(HexUtil.hexStr2ByteArray(encrypedWord)), AES.CHAR_ENCODING);
                }
            }
            Log.e(TAG, "ivParameter or encrypedWord is null");
            return "";
        } catch (Exception e) {
            Log.e(TAG, " cbc decrypt data error" + e.getMessage());
            return "";
        }
    }

    private static String mixIvAndEncryWord(String iv, String cryptWord) {
        if (TextUtils.isEmpty(iv) || TextUtils.isEmpty(cryptWord)) {
            return "";
        }
        try {
            return cryptWord.substring(0, 6) + iv.substring(0, 6) + cryptWord.substring(6, 10) + iv.substring(6, 16) + cryptWord.substring(10, 16) + iv.substring(16) + cryptWord.substring(16);
        } catch (Exception e) {
            Log.e(TAG, "mix exception: " + e.getMessage());
            return "";
        }
    }

    private static String getIv(String src) {
        if (TextUtils.isEmpty(src)) {
            return "";
        }
        try {
            return src.substring(6, 12) + src.substring(16, 26) + src.substring(32, 48);
        } catch (Exception e) {
            Log.e(TAG, "getIv exception : " + e.getMessage());
            return "";
        }
    }

    private static String getEncryptWord(String src) {
        if (TextUtils.isEmpty(src)) {
            return "";
        }
        try {
            return src.substring(0, 6) + src.substring(12, 16) + src.substring(26, 32) + src.substring(48);
        } catch (Exception e) {
            Log.e(TAG, "get encryptword exception : " + e.getMessage());
            return "";
        }
    }
}
