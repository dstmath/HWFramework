package com.huawei.secure.android.common.encrypt.keystore.aes;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.secure.android.common.util.HexUtil;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class AesGcmKS {
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int AES_GCM_IV_LEN = 12;
    private static final int AES_GCM_KEY_BIT_LEN = 256;
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String EMPTY = "";
    private static final String TAG = "AesGcmKS";

    @TargetApi(19)
    public static String encrypt(String alias, String content) {
        if (TextUtils.isEmpty(alias) || TextUtils.isEmpty(content)) {
            Log.e(TAG, "alias or encrypt content is null");
            return "";
        } else if (!isBuildVersionHigherThan22()) {
            Log.e(TAG, "sdk version is too low");
            return "";
        } else {
            byte[] result = new byte[0];
            try {
                Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
                SecretKey secretKey = generateKey(alias);
                if (secretKey == null) {
                    Log.e(TAG, "Encrypt secret key is null");
                    return "";
                }
                cipher.init(1, secretKey);
                byte[] encryptBytes = cipher.doFinal(content.getBytes(AES.CHAR_ENCODING));
                byte[] iv = cipher.getIV();
                if (iv != null) {
                    if (iv.length == 12) {
                        result = Arrays.copyOf(iv, iv.length + encryptBytes.length);
                        System.arraycopy(encryptBytes, 0, result, iv.length, encryptBytes.length);
                        return HexUtil.byteArray2HexStr(result);
                    }
                }
                Log.e(TAG, "IV is invalid.");
                return "";
            } catch (UnsupportedEncodingException | GeneralSecurityException e) {
                Log.e(TAG, "Encrypt exception: " + e.getMessage());
            }
        }
    }

    @TargetApi(19)
    public static String decrypt(String alias, String content) {
        if (TextUtils.isEmpty(alias) || TextUtils.isEmpty(content)) {
            Log.e(TAG, "alias or encrypt content is null");
            return "";
        } else if (!isBuildVersionHigherThan22()) {
            Log.e(TAG, "sdk version is too low");
            return "";
        } else {
            byte[] data = HexUtil.hexStr2ByteArray(content);
            if (data == null || data.length <= 12) {
                Log.e(TAG, "Decrypt source data is invalid.");
                return "";
            }
            byte[] decryptedData = new byte[0];
            try {
                SecretKey secretKey = generateKey(alias);
                if (secretKey == null) {
                    Log.e(TAG, "Decrypt secret key is null");
                    return "";
                }
                byte[] iv = Arrays.copyOf(data, 12);
                Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
                cipher.init(2, secretKey, new GCMParameterSpec(128, iv));
                decryptedData = cipher.doFinal(data, 12, data.length - 12);
                try {
                    return new String(decryptedData, AES.CHAR_ENCODING);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "unreachable UnsupportedEncodingException");
                    return "";
                }
            } catch (GeneralSecurityException e2) {
                Log.e(TAG, "Decrypt exception: " + e2.getMessage());
            }
        }
    }

    @TargetApi(23)
    private static SecretKey generateKey(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            Key key = keyStore.getKey(alias, null);
            if (key != null && (key instanceof SecretKey)) {
                return (SecretKey) key;
            }
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEY_STORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(alias, 3).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).setKeySize(AES_GCM_KEY_BIT_LEN).build());
            return keyGenerator.generateKey();
        } catch (RuntimeException e) {
            Log.e(TAG, "generateKey RuntimeException");
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "Generate key exception");
            return null;
        }
    }

    private static boolean isBuildVersionHigherThan22() {
        return Build.VERSION.SDK_INT >= 23;
    }
}
