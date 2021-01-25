package com.huawei.server.security.pwdprotect.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.util.Log;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class DeviceEncryptUtils {
    private static final String AES_MODE_CBC = "AES/CBC/PKCS7Padding";
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String HMACMODE = "HmacSHA256";
    private static final String KEY_ALIAS_AES = "com_huawei_securitymgr_aes_alias";
    private static final String KEY_ALIAS_HMAC = "com_huawei_securitymgr_hmac_alias";
    private static final String TAG = "PwdProtectService";

    public static byte[] deviceEncode(byte[] plainText) {
        try {
            Cipher cipher = Cipher.getInstance(AES_MODE_CBC);
            KeyStore keyStore = KeyStore.getInstance(AndroidKeyStore);
            keyStore.load(null);
            cipher.init(1, (SecretKey) keyStore.getKey(KEY_ALIAS_AES, null));
            return StringUtils.byteMerger(cipher.getIV(), cipher.doFinal(plainText));
        } catch (IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Log.e(TAG, "deviceEncode: failed" + e.getMessage());
            return new byte[0];
        }
    }

    public static byte[] deviceDecode(byte[] decodedText, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_MODE_CBC);
            KeyStore keyStore = KeyStore.getInstance(AndroidKeyStore);
            keyStore.load(null);
            cipher.init(2, (SecretKey) keyStore.getKey(KEY_ALIAS_AES, null), new IvParameterSpec(iv));
            return cipher.doFinal(decodedText);
        } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Log.e(TAG, "deviceDecode: failed" + e.getMessage());
            return new byte[0];
        }
    }

    public static SecretKey createKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", AndroidKeyStore);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS_AES, 3).setBlockModes("CBC").setEncryptionPaddings("PKCS7Padding").build());
            return keyGenerator.generateKey();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            Log.e(TAG, "createKey: failed" + e.getMessage());
            return null;
        }
    }

    public static byte[] hmacSign(byte[] decodedText) {
        byte[] encodedBytes = new byte[0];
        try {
            KeyStore keyStore = KeyStore.getInstance(AndroidKeyStore);
            keyStore.load(null);
            Mac mac = Mac.getInstance(HMACMODE);
            mac.init((SecretKey) keyStore.getKey(KEY_ALIAS_HMAC, null));
            return mac.doFinal(decodedText);
        } catch (IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException e) {
            Log.e(TAG, "hmacSign failed" + e.getMessage());
            return encodedBytes;
        }
    }

    public static SecretKey createHmacKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(HMACMODE, AndroidKeyStore);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS_HMAC, 4).build());
            return keyGenerator.generateKey();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            Log.e(TAG, "createKey: failed" + e.getMessage());
            return null;
        }
    }
}
