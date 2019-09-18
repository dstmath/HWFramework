package com.huawei.secure.android.common.encrypt.rsa;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.huawei.secure.android.common.util.EncryptUtil;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class RSASign {
    private static final String ALGORITHM = "SHA256WithRSA";
    private static final String CHARSET = "UTF-8";
    private static final String EMPTY = "";
    private static final String TAG = "RSASign";

    public static String sign(String content, String privateKey) {
        if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(privateKey)) {
            return sign(content, EncryptUtil.getPrivateKey(privateKey));
        }
        Log.e(TAG, "sign content or key is null");
        return "";
    }

    public static String sign(String content, PrivateKey privateKey) {
        if (TextUtils.isEmpty(content) || privateKey == null) {
            Log.e(TAG, "content or key is null");
            return "";
        }
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(privateKey);
            signature.update(content.getBytes("UTF-8"));
            return Base64.encodeToString(signature.sign(), 0);
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e(TAG, "sign exception: " + e.getMessage());
            return "";
        }
    }

    public static boolean verifySign(String content, String signVal, String publicKey) {
        if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(publicKey) && !TextUtils.isEmpty(signVal)) {
            return verifySign(content, signVal, EncryptUtil.getPublicKey(publicKey));
        }
        Log.e(TAG, "content or public key or sign value is null");
        return false;
    }

    public static boolean verifySign(String content, String signVal, PublicKey publicKey) {
        if (TextUtils.isEmpty(content) || publicKey == null || TextUtils.isEmpty(signVal)) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(content.getBytes("UTF-8"));
            return signature.verify(Base64.decode(signVal, 0));
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e(TAG, "check sign exception: " + e.getMessage());
            return false;
        }
    }
}
