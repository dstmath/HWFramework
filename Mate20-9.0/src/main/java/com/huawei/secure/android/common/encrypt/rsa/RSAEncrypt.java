package com.huawei.secure.android.common.encrypt.rsa;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.huawei.secure.android.common.util.EncryptUtil;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;

public class RSAEncrypt {
    private static final String ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String CHARSET = "UTF-8";
    private static final String EMPTY = "";
    private static final String TAG = "RSAEncrypt";

    public static String encrypt(String data, String publicKeyStr) {
        if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(publicKeyStr)) {
            return encrypt(data, EncryptUtil.getPublicKey(publicKeyStr));
        }
        Log.e(TAG, "content or public key is null");
        return "";
    }

    public static String encrypt(String data, PublicKey publicKey) {
        if (TextUtils.isEmpty(data) || publicKey == null) {
            Log.e(TAG, "content or PublicKey is null");
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(1, publicKey);
            return Base64.encodeToString(cipher.doFinal(data.getBytes("UTF-8")), 0);
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e(TAG, "RSA encrypt exception : " + e.getMessage());
            return "";
        }
    }

    public static String decrypt(String data, String privateKeyStr) {
        if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(privateKeyStr)) {
            return decrypt(data, EncryptUtil.getPrivateKey(privateKeyStr));
        }
        Log.e(TAG, "content or private key is null");
        return "";
    }

    public static String decrypt(String data, PrivateKey privateKey) {
        if (TextUtils.isEmpty(data) || privateKey == null) {
            Log.e(TAG, "content or privateKey is null");
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(2, privateKey);
            return new String(cipher.doFinal(Base64.decode(data, 0)), "UTF-8");
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e(TAG, "RSA decrypt exception : " + e.getMessage());
            return "";
        }
    }
}
