package com.android.contacts.hap.numbermark.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
    private static final String ALGORITHM_AES = "AES";
    private static final String CIPHER_FORM = "AES/CBC/PKCS5Padding";

    public static String encrypt4AES(String source, String key) {
        try {
            SecretKeySpec keySepc = new SecretKeySpec(key.getBytes("UTF-8"), ALGORITHM_AES);
            Cipher cipher = Cipher.getInstance(CIPHER_FORM);
            byte[] ivBytes = new byte[16];
            new SecureRandom().nextBytes(ivBytes);
            cipher.init(1, keySepc, new IvParameterSpec(ivBytes));
            return Base64.encode(ivBytes) + Base64.encode(cipher.doFinal(source.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e2) {
        } catch (InvalidKeyException e3) {
        } catch (InvalidAlgorithmParameterException e4) {
        } catch (IllegalBlockSizeException e5) {
        } catch (BadPaddingException e6) {
        } catch (UnsupportedEncodingException e7) {
        }
        return null;
    }

    public static String decrypt4AES(String content, String key) {
        try {
            String ivEncodeStr = content.substring(0, 24);
            byte[] decryptFrom = Base64.decode(content.substring(24));
            if (decryptFrom == null) {
                return null;
            }
            IvParameterSpec zeroIv = new IvParameterSpec(Base64.decode(ivEncodeStr));
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("UTF-8"), ALGORITHM_AES);
            Cipher cipher = Cipher.getInstance(CIPHER_FORM);
            cipher.init(2, keySpec, zeroIv);
            return new String(cipher.doFinal(decryptFrom), "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchPaddingException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvalidKeyException e3) {
            return null;
        } catch (UnsupportedEncodingException e4) {
            e4.printStackTrace();
            return null;
        } catch (IllegalBlockSizeException e5) {
            e5.printStackTrace();
            return null;
        } catch (BadPaddingException e6) {
            e6.printStackTrace();
            return null;
        } catch (Exception e7) {
            e7.printStackTrace();
            return null;
        }
    }
}
