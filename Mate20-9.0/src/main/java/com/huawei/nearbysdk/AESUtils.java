package com.huawei.nearbysdk;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
    public static final String CIPHER_ALGORITHM = "AES/CBC/NoPadding";
    public static final String CIPHER_ALGORITHM_PADDING = "AES/CBC/PKCS5Padding";

    public static byte[] encrypt(byte[] plain, byte[] key, byte[] iv, boolean padding) throws Exception {
        if (key == null || key.length != 16 || iv == null || iv.length != 16) {
            throw new Exception("key or iv error");
        }
        SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");
        String cipherType = CIPHER_ALGORITHM;
        if (padding) {
            cipherType = CIPHER_ALGORITHM_PADDING;
        }
        Cipher cipher = Cipher.getInstance(cipherType);
        cipher.init(1, sKeySpec, new IvParameterSpec(iv));
        return cipher.doFinal(plain);
    }

    public static byte[] encrypt(byte[] crypt, byte[] key, byte[] iv) throws Exception {
        return encrypt(crypt, key, iv, false);
    }

    public static byte[] decrypt(byte[] crypt, byte[] key, byte[] iv, boolean padding) throws Exception {
        if (key == null || key.length != 16 || iv == null || iv.length != 16) {
            throw new Exception("key or iv error");
        }
        SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");
        sKeySpec.getClass().getSimpleName();
        String cipherType = CIPHER_ALGORITHM;
        if (padding) {
            cipherType = CIPHER_ALGORITHM_PADDING;
        }
        Cipher cipher = Cipher.getInstance(cipherType);
        cipher.init(2, sKeySpec, new IvParameterSpec(iv));
        return cipher.doFinal(crypt);
    }

    public static byte[] decrypt(byte[] crypt, byte[] key, byte[] iv) throws Exception {
        return decrypt(crypt, key, iv, false);
    }
}
