package com.huawei.server.security.pwdprotect.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class DeviceEncryptUtils {
    private static final String AES_MODE_CBC = ("AES" + File.separator + "CBC" + File.separator + "PKCS7Padding");
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String HMAC_MODE = "HmacSHA256";
    private static final String KEY_ALIAS_AES = "com_huawei_securitymgr_aes_alias";
    private static final String KEY_ALIAS_HMAC = "com_huawei_securitymgr_hmac_alias";
    private static final String TAG = "PwdProtectService";

    public static byte[] encodeWithDeviceKey(byte[] plainText) {
        if (plainText == null) {
            Log.e(TAG, "encodeWithDeviceKey: plainText is null!");
            return new byte[0];
        }
        try {
            Cipher cipher = Cipher.getInstance(AES_MODE_CBC);
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            cipher.init(1, keyStore.getKey(KEY_ALIAS_AES, null));
            return StringUtils.concatByteArrays(cipher.getIV(), cipher.doFinal(plainText));
        } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Log.e(TAG, "encodeWithDeviceKey: Failed, algorithm parameter is invalid!");
            return new byte[0];
        } catch (InvalidKeyException | KeyStoreException | UnrecoverableKeyException e2) {
            Log.e(TAG, "encodeWithDeviceKey: Failed, key parameter is invalid!");
            return new byte[0];
        } catch (IOException | CertificateException e3) {
            Log.e(TAG, "encodeWithDeviceKey: Failed!");
            return new byte[0];
        }
    }

    public static byte[] decodeWithDeviceKey(byte[] decodedText, byte[] iv) {
        if (decodedText == null || iv == null) {
            Log.e(TAG, "decodeWithDeviceKey: decodedText or iv is null!");
            return new byte[0];
        }
        try {
            Cipher cipher = Cipher.getInstance(AES_MODE_CBC);
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            cipher.init(2, keyStore.getKey(KEY_ALIAS_AES, null), new IvParameterSpec(iv));
            return cipher.doFinal(decodedText);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | BadPaddingException | NoSuchPaddingException e) {
            Log.e(TAG, "decodeWithDeviceKey: Algorithm parameter is invalid!");
            return new byte[0];
        } catch (InvalidKeyException | KeyStoreException | UnrecoverableKeyException e2) {
            Log.e(TAG, "decodeWithDeviceKey: Failed, secret key is invalid!");
            return new byte[0];
        } catch (IOException | CertificateException | IllegalBlockSizeException e3) {
            Log.e(TAG, "decodeWithDeviceKey: Failed!");
            return new byte[0];
        }
    }

    public static Optional<SecretKey> generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEY_STORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS_AES, 3).setBlockModes("CBC").setEncryptionPaddings("PKCS7Padding").build());
            return Optional.ofNullable(keyGenerator.generateKey());
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            Log.e(TAG, "generateSecretKey: Failed!");
            return Optional.empty();
        }
    }

    public static byte[] signWithHmac(byte[] decodedText) {
        if (decodedText == null) {
            Log.e(TAG, "signWithHmac: Failed, decodedText is null!");
            return new byte[0];
        }
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            Key secretKey = keyStore.getKey(KEY_ALIAS_HMAC, null);
            Mac mac = Mac.getInstance(HMAC_MODE);
            mac.init(secretKey);
            return mac.doFinal(decodedText);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            Log.e(TAG, "signWithHmac: Failed, algorithm parameter is invalid!");
            return new byte[0];
        } catch (InvalidKeyException | KeyStoreException | UnrecoverableKeyException e2) {
            Log.e(TAG, "signWithHmac: Failed, key parameter is invalid!");
            return new byte[0];
        } catch (IOException e3) {
            Log.e(TAG, "signWithHmac: Failed, IOException!");
            return new byte[0];
        }
    }

    public static Optional<SecretKey> generateHmacKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(HMAC_MODE, ANDROID_KEY_STORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS_HMAC, 4).build());
            return Optional.ofNullable(keyGenerator.generateKey());
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            Log.e(TAG, "generateHmacKey: Failed!");
            return Optional.empty();
        }
    }
}
