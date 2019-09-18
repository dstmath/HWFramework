package com.huawei.secure.android.common.encrypt.keystore.rsa;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.huawei.wallet.sdk.common.utils.crypto.SHA_256;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Signature;

public class RSASignKS {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String EMPTY = "";
    private static final int KEY_LENGTH = 2048;
    private static final String RSA_MODE_SIGN = "SHA256withRSA/PSS";
    private static final String TAG = "RSASignKS";

    public static String sign(String alias, String data) {
        if (TextUtils.isEmpty(alias) || TextUtils.isEmpty(data)) {
            Log.e(TAG, "alias or content is null");
            return "";
        } else if (!isBuildVersionHigherThan22()) {
            Log.e(TAG, "sdk version is too low");
            return "";
        } else {
            try {
                KeyStore.Entry entry = loadEntry(alias);
                if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                    Log.e(TAG, "Not an instance of a PrivateKeyEntry");
                    return "";
                }
                Signature signature = Signature.getInstance(RSA_MODE_SIGN);
                signature.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
                signature.update(data.getBytes());
                return Base64.encodeToString(signature.sign(), 0);
            } catch (GeneralSecurityException e) {
                Log.e(TAG, "encrypt exception : " + e.getMessage());
                return "";
            }
        }
    }

    public static boolean verifySign(String alias, String data, String signValue) {
        if (TextUtils.isEmpty(alias) || TextUtils.isEmpty(data) || TextUtils.isEmpty(signValue)) {
            Log.e(TAG, "alias or content or sign value is null");
            return false;
        }
        boolean result = false;
        if (!isBuildVersionHigherThan22()) {
            Log.e(TAG, "sdk version is too low");
            return false;
        }
        try {
            KeyStore.Entry entry = loadEntry(alias);
            if (entry != null) {
                if (entry instanceof KeyStore.PrivateKeyEntry) {
                    Signature signature = Signature.getInstance(RSA_MODE_SIGN);
                    signature.initVerify(((KeyStore.PrivateKeyEntry) entry).getCertificate());
                    signature.update(data.getBytes());
                    result = signature.verify(Base64.decode(signValue, 0));
                    return result;
                }
            }
            Log.e(TAG, "Not an instance of a PrivateKeyEntry");
            return false;
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "verify sign exception : " + e.getMessage());
        }
    }

    @TargetApi(23)
    private static KeyPair generateKeyPair(String alias) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEY_STORE);
            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(alias, 12).setDigests(new String[]{SHA_256.ALGORITHM_SHA256, "SHA-512"}).setSignaturePaddings(new String[]{"PSS"}).setKeySize(KEY_LENGTH).build());
            return keyPairGenerator.generateKeyPair();
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "generate keypair exception: " + e.getMessage());
            return null;
        }
    }

    private static KeyStore.Entry loadEntry(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            KeyStore.Entry entry = keyStore.getEntry(alias, null);
            if (entry != null) {
                return entry;
            }
            generateKeyPair(alias);
            return keyStore.getEntry(alias, null);
        } catch (IOException | GeneralSecurityException e) {
            Log.e(TAG, "load entry exception : " + e.getMessage());
            return null;
        }
    }

    private static boolean isBuildVersionHigherThan22() {
        return Build.VERSION.SDK_INT >= 23;
    }
}
