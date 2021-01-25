package com.huawei.nb.security;

import com.huawei.nb.utils.file.FileUtils;
import com.huawei.nb.utils.logger.DSLog;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAEncryptUtils {
    private static final String CIPHER_ALGORITHM = "RSA/NONE/OAEPwithSHA-1andMGF1Padding";
    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final int MAX_DECRYPT_BLOCK = 256;
    private static final int MAX_ENCRYPT_BLOCK = 128;
    private static final String PRIVATE_KEY = "RSAPrivateKey";
    private static final String PUBLIC_KEY = "RSAPublicKey";

    public static Map<String, Object> generateKeyPair() {
        try {
            KeyPairGenerator instance = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            instance.initialize(KEY_SIZE, new SecureRandom());
            KeyPair generateKeyPair = instance.generateKeyPair();
            HashMap hashMap = new HashMap(2);
            hashMap.put(PUBLIC_KEY, (RSAPublicKey) generateKeyPair.getPublic());
            hashMap.put(PRIVATE_KEY, (RSAPrivateKey) generateKeyPair.getPrivate());
            return hashMap;
        } catch (RuntimeException | NoSuchAlgorithmException unused) {
            DSLog.e("Failed to generate key pair, error: NoSuchAlgorithmException.", new Object[0]);
            return null;
        }
    }

    public static String getPrivateKey(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        return encodeToString(((Key) map.get(PRIVATE_KEY)).getEncoded());
    }

    public static String getPublicKey(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        return encodeToString(((Key) map.get(PUBLIC_KEY)).getEncoded());
    }

    private static String encodeToString(byte[] bArr) {
        if (bArr != null) {
            return Base64.getEncoder().encodeToString(bArr);
        }
        return null;
    }

    private static byte[] decodeToByte(String str) {
        if (str != null) {
            return Base64.getDecoder().decode(str);
        }
        return null;
    }

    private static byte[] processByCipher(byte[] bArr, Cipher cipher, int i) {
        Throwable th;
        Object e;
        byte[] bArr2;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
            try {
                int length = bArr.length;
                int i2 = 0;
                while (true) {
                    int i3 = length - i2;
                    if (i3 > 0) {
                        if (i3 > i) {
                            bArr2 = cipher.doFinal(bArr, i2, i);
                        } else {
                            bArr2 = cipher.doFinal(bArr, i2, i3);
                        }
                        byteArrayOutputStream2.write(bArr2, 0, bArr2.length);
                        i2 += i;
                    } else {
                        byte[] byteArray = byteArrayOutputStream2.toByteArray();
                        FileUtils.closeCloseable(byteArrayOutputStream2);
                        return byteArray;
                    }
                }
            } catch (BadPaddingException | IllegalBlockSizeException e2) {
                e = e2;
                byteArrayOutputStream = byteArrayOutputStream2;
                try {
                    DSLog.e("Failed to process data by cipher, error: %s.", e.getClass().getSimpleName());
                    byte[] bArr3 = new byte[0];
                    FileUtils.closeCloseable(byteArrayOutputStream);
                    return bArr3;
                } catch (Throwable th2) {
                    th = th2;
                    FileUtils.closeCloseable(byteArrayOutputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                byteArrayOutputStream = byteArrayOutputStream2;
                FileUtils.closeCloseable(byteArrayOutputStream);
                throw th;
            }
        } catch (BadPaddingException | IllegalBlockSizeException e3) {
            e = e3;
            DSLog.e("Failed to process data by cipher, error: %s.", e.getClass().getSimpleName());
            byte[] bArr32 = new byte[0];
            FileUtils.closeCloseable(byteArrayOutputStream);
            return bArr32;
        }
    }

    private static byte[] encrypt(byte[] bArr, Key key) {
        if (bArr == null || bArr.length == 0 || key == null) {
            DSLog.e("Failed to encrypt data, error: invalid parameters.", new Object[0]);
            return new byte[0];
        }
        try {
            Cipher instance = Cipher.getInstance(CIPHER_ALGORITHM);
            instance.init(1, key);
            return processByCipher(bArr, instance, MAX_ENCRYPT_BLOCK);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            DSLog.e("Failed to encrypt data, error: %s.", e.getClass().getSimpleName());
            return new byte[0];
        }
    }

    private static byte[] decrypt(byte[] bArr, Key key) {
        if (bArr == null || bArr.length == 0 || key == null) {
            DSLog.e("Failed to decrypt data, error: invalid parameters.", new Object[0]);
            return new byte[0];
        }
        try {
            Cipher instance = Cipher.getInstance(CIPHER_ALGORITHM);
            instance.init(2, key);
            return processByCipher(bArr, instance, 256);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            DSLog.e("Failed to encrypt data, error: %s.", e.getClass().getSimpleName());
            return new byte[0];
        }
    }

    private static Key convertToPublicKey(String str) {
        byte[] decodeToByte = decodeToByte(str);
        if (decodeToByte == null || decodeToByte.length == 0) {
            DSLog.e("Failed to convert to public key, error: invalid parameter.", new Object[0]);
            return null;
        }
        try {
            return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(decodeToByte));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            DSLog.e("Failed to convert to public key, error: %s.", e.getClass().getSimpleName());
            return null;
        }
    }

    private static Key convertToPrivateKey(String str) {
        byte[] decodeToByte = decodeToByte(str);
        if (decodeToByte == null) {
            DSLog.e("Failed to convert to private key, error: invalid parameter.", new Object[0]);
            return null;
        }
        try {
            return KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decodeToByte));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            DSLog.e("Failed to convert to private key, error: %s.", e.getClass().getSimpleName());
            return null;
        }
    }

    private static byte[] decryptByPrivateKey(byte[] bArr, String str) {
        return decrypt(bArr, convertToPrivateKey(str));
    }

    private static byte[] encryptByPublicKey(byte[] bArr, String str) {
        return encrypt(bArr, convertToPublicKey(str));
    }

    public static String encryptString(String str, String str2) {
        if (str == null || str2 == null) {
            DSLog.e("Failed to encrypt given string, error: invalid parameter.", new Object[0]);
            return null;
        } else if (str.isEmpty()) {
            return str;
        } else {
            try {
                return encodeToString(encryptByPublicKey(str.getBytes("UTF-8"), str2));
            } catch (UnsupportedEncodingException unused) {
                DSLog.e("Failed to encrypt given string, error: unsupported encoding.", new Object[0]);
                return null;
            }
        }
    }

    public static String decryptString(String str, String str2) {
        if (str == null || str2 == null) {
            DSLog.e("Failed to decrypt given string, error: invalid parameter.", new Object[0]);
            return null;
        } else if (str.isEmpty()) {
            return str;
        } else {
            try {
                byte[] decryptByPrivateKey = decryptByPrivateKey(decodeToByte(str), str2);
                if (decryptByPrivateKey.length == 0) {
                    return null;
                }
                return new String(decryptByPrivateKey, "UTF-8");
            } catch (UnsupportedEncodingException unused) {
                DSLog.e("Failed to decrypt given string, error: unsupported encoding.", new Object[0]);
                return null;
            }
        }
    }

    public static String encryptBytesToString(byte[] bArr, String str) {
        return encodeToString(encryptByPublicKey(bArr, str));
    }

    public static byte[] decryptStringToBytes(String str, String str2) {
        return decryptByPrivateKey(decodeToByte(str), str2);
    }
}
