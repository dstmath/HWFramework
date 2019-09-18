package com.huawei.secure.android.common.encrypt.aes;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.secure.android.common.util.EncryptUtil;
import com.huawei.secure.android.common.util.HexUtil;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AesGcm {
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int AES_GCM_IV_LEN = 12;
    private static final int AES_GCM_KEY_LEN = 16;
    private static final String EMPTY = "";
    private static final String TAG = "AesGcm";
    private static final int TIMES = 2;

    private AesGcm() {
    }

    public static String encrypt(String content, String key) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(key) || !isBuildVersionHigherThan19()) {
            return "";
        }
        byte[] secretkey = HexUtil.hexStr2ByteArray(key);
        if (secretkey != null && secretkey.length >= 16) {
            return encrypt(content, secretkey);
        }
        Log.e(TAG, "key length is not right");
        return "";
    }

    @TargetApi(19)
    public static String encrypt(String content, byte[] key) {
        if (TextUtils.isEmpty(content) || key == null || key.length < 16 || !isBuildVersionHigherThan19()) {
            return "";
        }
        try {
            SecretKeySpec secretkey = new SecretKeySpec(key, AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            byte[] ivParameter = EncryptUtil.generateSecureRandom(12);
            cipher.init(1, secretkey, new IvParameterSpec(ivParameter));
            byte[] encrypted = cipher.doFinal(content.getBytes(AES.CHAR_ENCODING));
            String hexIv = HexUtil.byteArray2HexStr(ivParameter);
            String hexEncrypted = HexUtil.byteArray2HexStr(encrypted);
            return hexIv + hexEncrypted;
        } catch (Exception e) {
            Log.e(TAG, "GCM encrypt data error" + e.getMessage());
            return "";
        }
    }

    @TargetApi(19)
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

    @TargetApi(19)
    public static String decrypt(String content, byte[] key) {
        if (TextUtils.isEmpty(content) || key == null || key.length < 16) {
            return "";
        }
        try {
            SecretKeySpec secretkey = new SecretKeySpec(key, AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            String ivParameter = getIv(content);
            String encrypedWord = getEncryptWord(content);
            if (!TextUtils.isEmpty(ivParameter)) {
                if (!TextUtils.isEmpty(encrypedWord)) {
                    cipher.init(2, secretkey, new GCMParameterSpec(128, HexUtil.hexStr2ByteArray(ivParameter)));
                    return new String(cipher.doFinal(HexUtil.hexStr2ByteArray(encrypedWord)), AES.CHAR_ENCODING);
                }
            }
            Log.e(TAG, "ivParameter or encrypedWord is null");
            return "";
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e(TAG, "GCM decrypt data exception: " + e.getMessage());
            return "";
        }
    }

    private static String getIv(String src) {
        if (!TextUtils.isEmpty(src) && src.length() >= 24) {
            return src.substring(0, 24);
        }
        Log.e(TAG, "IV is invalid.");
        return "";
    }

    private static String getEncryptWord(String src) {
        if (TextUtils.isEmpty(src) || src.length() < 24) {
            return "";
        }
        return src.substring(24);
    }

    private static boolean isBuildVersionHigherThan19() {
        return Build.VERSION.SDK_INT >= 19;
    }
}
