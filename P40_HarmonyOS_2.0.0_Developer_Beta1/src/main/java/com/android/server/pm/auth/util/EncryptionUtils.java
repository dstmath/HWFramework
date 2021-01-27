package com.android.server.pm.auth.util;

import android.text.TextUtils;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class EncryptionUtils {
    private static final String TAG = "HwCertificationManager";

    private EncryptionUtils() {
    }

    public static byte[] sha256(byte[] bytes) throws NoSuchAlgorithmException {
        if (bytes == null) {
            return new byte[0];
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(bytes);
        return md.digest();
    }

    public static byte[] sha384(byte[] bytes) throws NoSuchAlgorithmException {
        if (bytes == null) {
            return new byte[0];
        }
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        md.update(bytes);
        return md.digest();
    }

    public static byte[] sign(byte[] bytes, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (bytes == null || privateKey == null) {
            return new byte[0];
        }
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(bytes);
        return signature.sign();
    }

    public static boolean verify(byte[] content, PublicKey publicKey, byte[] bytes) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (content == null || publicKey == null || bytes == null) {
            return false;
        }
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(content);
        return signature.verify(bytes);
    }

    public static boolean verifySignatureV3(byte[] content, PublicKey publicKey, byte[] bytes) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (content == null || publicKey == null || bytes == null) {
            return false;
        }
        Signature signature = Signature.getInstance("SHA384withRSA");
        signature.initVerify(publicKey);
        signature.update(content);
        return signature.verify(bytes);
    }

    public static PublicKey getPublicKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Utils.stringToHexBytes(key)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            HwAuthLogger.error("HwCertificationManager", "getPublicKey by key failed!");
            return null;
        }
    }

    public static PublicKey getPublicKey(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            HwAuthLogger.error("HwCertificationManager", "getPublicKey failed!");
            return null;
        }
    }
}
