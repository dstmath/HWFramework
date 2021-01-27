package com.huawei.server.security.pwdprotect.utils;

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
import java.util.Optional;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils {
    private static final String AES = "AES";
    private static final String CBC_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int MAX_ENCRYPT_BLOCK = 117;
    private static final String RSA = "RSA";
    private static final String RSA_CIPHER_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String TAG = "PwdProtectService";

    public static Optional<KeyPair> generateRsaKeyPair(int keyLength) {
        if (keyLength <= 0) {
            return Optional.empty();
        }
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(keyLength);
            return Optional.ofNullable(keyPairGenerator.genKeyPair());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "generateRsaKeyPair: Generate rsa KeyPair failed!");
            return Optional.empty();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0045, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004a, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004b, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004e, code lost:
        throw r4;
     */
    public static byte[] encryptData(byte[] data, PublicKey publicKey) {
        byte[] cachedCipherText;
        if (data == null || data.length == 0 || publicKey == null) {
            Log.e(TAG, "encryptData: Invalid input!");
            return new byte[0];
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Cipher cipher = Cipher.getInstance(RSA_CIPHER_ALGORITHM);
            cipher.init(1, publicKey);
            int plainTextLen = data.length;
            int offSetLen = 0;
            int encryptNumbers = 0;
            while (plainTextLen - offSetLen > 0) {
                if (plainTextLen - offSetLen > MAX_ENCRYPT_BLOCK) {
                    cachedCipherText = cipher.doFinal(data, offSetLen, MAX_ENCRYPT_BLOCK);
                } else {
                    cachedCipherText = cipher.doFinal(data, offSetLen, plainTextLen - offSetLen);
                }
                outputStream.write(cachedCipherText, 0, cachedCipherText.length);
                encryptNumbers++;
                offSetLen = encryptNumbers * MAX_ENCRYPT_BLOCK;
            }
            byte[] cachedCipherText2 = outputStream.toByteArray();
            try {
                outputStream.close();
                return cachedCipherText2;
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                Log.e(TAG, "encryptData: RSA encryptData error, invalid cipher parameter!");
                return new byte[0];
            } catch (InvalidKeyException | IllegalBlockSizeException e2) {
                Log.e(TAG, "encryptData: RSA encryptData error, invalid key parameter!");
                return new byte[0];
            }
        } catch (BadPaddingException e3) {
            Log.e(TAG, "encryptData: RSA encryptData error!");
            return new byte[0];
        } catch (IOException e4) {
            Log.e(TAG, "encryptData: RSA encryptData IOException error!");
            return new byte[0];
        }
    }

    public static byte[] decryptData(byte[] encryptedData, PrivateKey privateKey) {
        if (encryptedData == null || encryptedData.length == 0 || privateKey == null) {
            Log.e(TAG, "decryptData: Invalid input!");
            return new byte[0];
        }
        try {
            Cipher cipher = Cipher.getInstance(RSA_CIPHER_ALGORITHM);
            cipher.init(2, privateKey);
            return cipher.doFinal(encryptedData);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Log.e(TAG, "decryptData: RSA decryptData error, invalid cipher parameter!");
            return new byte[0];
        } catch (InvalidKeyException | IllegalBlockSizeException e2) {
            Log.e(TAG, "decryptData: RSA decryptData error, invalid key parameter!");
            return new byte[0];
        } catch (BadPaddingException e3) {
            Log.e(TAG, "decryptData: RSA decryptData error!");
            return new byte[0];
        }
    }

    public static byte[] encodeWithAes(byte[] plainText, byte[] key, byte[] ivParameter) {
        if (plainText == null || key == null || ivParameter == null) {
            Log.e(TAG, "encodeWithAes: Invalid input, the input is null!");
            return new byte[0];
        } else if (plainText.length == 0 || key.length == 0 || ivParameter.length == 0) {
            Log.e(TAG, "encodeWithAes: Invalid input, the length of input is 0!");
            return new byte[0];
        } else {
            try {
                IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParameter);
                Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
                cipher.init(1, new SecretKeySpec(key, AES), ivParameterSpec);
                return cipher.doFinal(plainText);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                Log.e(TAG, "encodeWithAes: AES Encrypt failed, invalid cipher parameter!");
                return new byte[0];
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException e2) {
                Log.e(TAG, "encodeWithAes: AES Encrypt failed, invalid key parameter!");
                return new byte[0];
            } catch (BadPaddingException e3) {
                Log.e(TAG, "encodeWithAes: AES Encrypt failed!");
                return new byte[0];
            }
        }
    }

    public static byte[] decodeWithAes(byte[] decodedText, byte[] key, byte[] ivParameter) {
        if (decodedText == null || key == null || ivParameter == null) {
            Log.e(TAG, "decodeWithAes: Invalid input, the input is null!");
            return new byte[0];
        } else if (decodedText.length == 0 || key.length == 0 || ivParameter.length == 0) {
            Log.e(TAG, "decodeWithAes: Invalid input, the length of input is 0!");
            return new byte[0];
        } else {
            try {
                IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParameter);
                Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
                cipher.init(2, new SecretKeySpec(key, AES), ivParameterSpec);
                return cipher.doFinal(decodedText);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                Log.e(TAG, "decodeWithAes: AES Decode failed, invalid cipher parameter!");
                return new byte[0];
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException e2) {
                Log.e(TAG, "decodeWithAes: AES Decode failed, invalid key parameter!");
                return new byte[0];
            } catch (BadPaddingException e3) {
                Log.e(TAG, "decodeWithAes: Failed, the decrypted data is not bounded by the appropriate padding bytes");
                return new byte[0];
            }
        }
    }

    public static Optional<PublicKey> getPublicKey(byte[] keyBytes) {
        if (keyBytes == null || keyBytes.length == 0) {
            Log.e(TAG, "getPublicKey: The publicKey is invalid");
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(keyBytes)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "getPublicKey: GetPublicKey error!");
            return Optional.empty();
        }
    }

    public static Optional<PrivateKey> getPrivateKey(byte[] keyBytes) {
        if (keyBytes == null || keyBytes.length == 0) {
            Log.e(TAG, "getPrivateKey: The privateKey is invalid");
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(keyBytes)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "getPrivateKey: GetPrivateKey error!");
            return Optional.empty();
        }
    }
}
