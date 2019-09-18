package com.huawei.secure.android.common.encrypt.keystore.rsa;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import com.huawei.wallet.sdk.common.utils.crypto.SHA_256;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class RSAEncryptKS {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String EMPTY = "";
    private static final int KEY_LENGTH = 2048;
    private static final String RSA_MODE_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String TAG = "RSAEncryptKS";

    public static String encrypt(String alias, String plaintext) {
        if (TextUtils.isEmpty(alias) || TextUtils.isEmpty(plaintext)) {
            Log.e(TAG, "alias or content is null");
            return "";
        } else if (!isBuildVersionHigherThan22()) {
            Log.e(TAG, "sdk version is too low");
            return "";
        } else {
            try {
                PublicKey publicKey = getPublicKey(alias);
                if (publicKey == null) {
                    Log.e(TAG, "Public key is null");
                    return "";
                }
                Cipher cipher = Cipher.getInstance(RSA_MODE_OAEP);
                cipher.init(1, publicKey, new OAEPParameterSpec(SHA_256.ALGORITHM_SHA256, "MGF1", new MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT));
                return Base64.encodeToString(cipher.doFinal(plaintext.getBytes()), 0);
            } catch (GeneralSecurityException e) {
                Log.e(TAG, "RSA encrypt exception : " + e.getMessage());
                return "";
            }
        }
    }

    public static String decrpyt(String alias, String encrypted) {
        if (TextUtils.isEmpty(alias) || TextUtils.isEmpty(encrypted)) {
            Log.e(TAG, "alias or encrypted content is null");
            return "";
        } else if (!isBuildVersionHigherThan22()) {
            Log.e(TAG, "sdk version is too low");
            return "";
        } else {
            try {
                PrivateKey privateKey = getPrivateKey(alias);
                if (privateKey == null) {
                    Log.e(TAG, "Private key is null");
                    return "";
                }
                Cipher cipher = Cipher.getInstance(RSA_MODE_OAEP);
                cipher.init(2, privateKey, new OAEPParameterSpec(SHA_256.ALGORITHM_SHA256, "MGF1", new MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT));
                return new String(cipher.doFinal(Base64.decode(encrypted, 0)), AES.CHAR_ENCODING);
            } catch (UnsupportedEncodingException | GeneralSecurityException e) {
                Log.e(TAG, "RSA decrypt exception : " + e.getMessage());
                return "";
            }
        }
    }

    @TargetApi(23)
    private static KeyPair generateKeyPair(String alias) {
        try {
            Log.i(TAG, "generate key pair.");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEY_STORE);
            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(alias, 2).setDigests(new String[]{SHA_256.ALGORITHM_SHA256, "SHA-512"}).setEncryptionPaddings(new String[]{"OAEPPadding"}).setKeySize(KEY_LENGTH).build());
            return keyPairGenerator.generateKeyPair();
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "generateKeyPair exception: " + e.getMessage());
            return null;
        }
    }

    private static PublicKey getPublicKey(String alias) {
        Certificate certificate = loadCertificate(alias);
        if (certificate == null) {
            generateKeyPair(alias);
            certificate = loadCertificate(alias);
        }
        if (certificate != null) {
            return certificate.getPublicKey();
        }
        return null;
    }

    private static Certificate loadCertificate(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            return keyStore.getCertificate(alias);
        } catch (IOException | GeneralSecurityException e) {
            Log.e(TAG, "load public key exception : " + e.getMessage());
            return null;
        }
    }

    private static PrivateKey getPrivateKey(String alias) {
        if (loadCertificate(alias) == null) {
            generateKeyPair(alias);
        }
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            return (PrivateKey) keyStore.getKey(alias, null);
        } catch (IOException | GeneralSecurityException e) {
            Log.e(TAG, "get private key exception : " + e.getMessage());
            return null;
        }
    }

    private static boolean isBuildVersionHigherThan22() {
        return Build.VERSION.SDK_INT >= 23;
    }
}
