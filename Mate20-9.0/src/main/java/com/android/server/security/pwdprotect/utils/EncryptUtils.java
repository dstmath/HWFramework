package com.android.server.security.pwdprotect.utils;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils {
    private static final String CBC_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int MAX_ENCRYPT_BLOCK = 117;
    private static String RSA = "RSA";
    private static final String TAG = "PwdProtectService";

    public static KeyPair generateRSAKeyPair(int keyLength) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
            kpg.initialize(keyLength);
            return kpg.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "generate RSA KeyPair failed");
            return null;
        }
    }

    public static byte[] encryptData(byte[] data, PublicKey publicKey) {
        byte[] cache;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(1, publicKey);
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            int i = 0;
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > 117) {
                    cache = cipher.doFinal(data, offSet, 117);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * 117;
            }
            byte[] encryptedData = out.toByteArray();
            out.close();
            return encryptedData;
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Log.e(TAG, "RSA encryptData error" + e.getMessage());
            return new byte[0];
        }
    }

    public static byte[] decryptData(byte[] encryptedData, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(2, privateKey);
            return cipher.doFinal(encryptedData);
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Log.e(TAG, "RSA decryptData error" + e.getMessage());
            return new byte[0];
        }
    }

    public static byte[] aesCbcEncode(byte[] plainText, byte[] key, byte[] IVParameter) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IVParameter);
            Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
            cipher.init(1, new SecretKeySpec(key, "AES"), ivParameterSpec);
            return cipher.doFinal(plainText);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Log.e(TAG, "AES Encrypt failed");
            return new byte[0];
        }
    }

    public static byte[] aesCbcDecode(byte[] decodedText, byte[] key, byte[] IVParameter) {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(IVParameter);
        try {
            Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
            cipher.init(2, new SecretKeySpec(key, "AES"), ivParameterSpec);
            return cipher.doFinal(decodedText);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Log.e(TAG, "AES Decode failed");
            return new byte[0];
        }
    }

    public static PublicKey getPublicKey(byte[] keyBytes) {
        try {
            return KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "getPublicKey error" + e.getMessage());
            return null;
        }
    }

    public static PrivateKey getPrivateKey(byte[] keyBytes) {
        try {
            return KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "getPrivateKey error" + e.getMessage());
            return null;
        }
    }
}
