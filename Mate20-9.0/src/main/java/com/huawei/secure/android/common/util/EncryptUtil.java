package com.huawei.secure.android.common.util;

import android.util.Base64;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class EncryptUtil {
    private static final String RSA_ALGORITHM = "RSA";
    private static final int RSA_KEY_LENGTH = 256;
    private static final String TAG = "EncryptUtil";

    public static byte[] generateSecureRandom(int len) {
        try {
            byte[] ivParameter = new byte[len];
            new SecureRandom().nextBytes(ivParameter);
            return ivParameter;
        } catch (Exception e) {
            LogsUtil.e(TAG, "generate secure random error" + e.getMessage(), true);
            return new byte[0];
        }
    }

    public static RSAPublicKey getPublicKey(String publicKeyStr) {
        try {
            byte[] publicKey = Base64.decode(publicKeyStr, 0);
            if (publicKey.length < RSA_KEY_LENGTH) {
                LogsUtil.e(TAG, "key length is not correct.", true);
                return null;
            }
            try {
                return (RSAPublicKey) KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(new X509EncodedKeySpec(publicKey));
            } catch (GeneralSecurityException e) {
                LogsUtil.e(TAG, "load Key Exception:" + e.getMessage(), true);
                return null;
            }
        } catch (IllegalArgumentException e2) {
            LogsUtil.e(TAG, "base64 decode IllegalArgumentException", true);
            return null;
        }
    }

    public static PrivateKey getPrivateKey(String privateKeyStr) {
        try {
            byte[] privatekey = Base64.decode(privateKeyStr, 0);
            if (privatekey.length < RSA_KEY_LENGTH) {
                LogsUtil.e(TAG, "key length is not correct.", true);
                return null;
            }
            try {
                return KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(privatekey));
            } catch (GeneralSecurityException e) {
                LogsUtil.e(TAG, "load Key Exception:" + e.getMessage(), true);
                return null;
            }
        } catch (IllegalArgumentException e2) {
            LogsUtil.e(TAG, "base64 decode IllegalArgumentException", true);
            return null;
        }
    }
}
