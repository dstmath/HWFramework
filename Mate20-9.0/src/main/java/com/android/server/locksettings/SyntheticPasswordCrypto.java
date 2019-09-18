package com.android.server.locksettings;

import android.security.keystore.KeyProtection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SyntheticPasswordCrypto {
    private static final int AES_KEY_LENGTH = 32;
    private static final byte[] APPLICATION_ID_PERSONALIZATION = "application-id".getBytes();
    private static final int PROFILE_KEY_IV_SIZE = 12;
    private static final int USER_AUTHENTICATION_VALIDITY = 15;

    private static byte[] decrypt(SecretKey key, byte[] blob) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        if (blob == null) {
            return null;
        }
        byte[] iv = Arrays.copyOfRange(blob, 0, 12);
        byte[] ciphertext = Arrays.copyOfRange(blob, 12, blob.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, key, new GCMParameterSpec(128, iv));
        return cipher.doFinal(ciphertext);
    }

    private static byte[] encrypt(SecretKey key, byte[] blob) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (blob == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(1, key);
        byte[] ciphertext = cipher.doFinal(blob);
        byte[] iv = cipher.getIV();
        if (iv.length == 12) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(iv);
            outputStream.write(ciphertext);
            return outputStream.toByteArray();
        }
        throw new RuntimeException("Invalid iv length: " + iv.length);
    }

    public static byte[] encrypt(byte[] keyBytes, byte[] personalisation, byte[] message) {
        try {
            return encrypt(new SecretKeySpec(Arrays.copyOf(personalisedHash(personalisation, keyBytes), 32), "AES"), message);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decrypt(byte[] keyBytes, byte[] personalisation, byte[] ciphertext) {
        try {
            return decrypt(new SecretKeySpec(Arrays.copyOf(personalisedHash(personalisation, keyBytes), 32), "AES"), ciphertext);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decryptBlobV1(String keyAlias, byte[] blob, byte[] applicationId) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return decrypt((SecretKey) keyStore.getKey(keyAlias, null), decrypt(applicationId, APPLICATION_ID_PERSONALIZATION, blob));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to decrypt blob", e);
        }
    }

    public static byte[] decryptBlob(String keyAlias, byte[] blob, byte[] applicationId) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return decrypt(applicationId, APPLICATION_ID_PERSONALIZATION, decrypt((SecretKey) keyStore.getKey(keyAlias, null), blob));
        } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to decrypt blob", e);
        }
    }

    public static byte[] createBlob(String keyAlias, byte[] data, byte[] applicationId, long sid, boolean managedProfile) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyProtection.Builder builder = new KeyProtection.Builder(2).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).setCriticalToDeviceEncryption(true);
            if (sid != 0) {
                builder.setUserAuthenticationRequired(true).setBoundToSpecificSecureUserId(sid).setUserAuthenticationValidityDurationSeconds(15);
            }
            keyStore.setEntry(keyAlias, new KeyStore.SecretKeyEntry(secretKey), builder.build());
            return encrypt(secretKey, encrypt(applicationId, APPLICATION_ID_PERSONALIZATION, data));
        } catch (IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to encrypt blob", e);
        }
    }

    public static byte[] createBlob(String keyAlias, byte[] data, byte[] applicationId, long sid) {
        return createBlob(keyAlias, data, applicationId, sid, false);
    }

    public static void destroyBlobKey(String keyAlias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry(keyAlias);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
    }

    protected static byte[] personalisedHash(byte[] personalisation, byte[]... message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            if (personalisation.length <= 128) {
                digest.update(Arrays.copyOf(personalisation, 128));
                for (byte[] data : message) {
                    digest.update(data);
                }
                return digest.digest();
            }
            throw new RuntimeException("Personalisation too long");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException for SHA-512", e);
        }
    }
}
