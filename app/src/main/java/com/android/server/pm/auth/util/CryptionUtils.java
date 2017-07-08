package com.android.server.pm.auth.util;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;

public class CryptionUtils {
    public static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        return md.digest();
    }

    public static byte[] sign(byte[] content, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(content);
        return sign.sign();
    }

    public static boolean verify(byte[] content, PublicKey publicKey, byte[] signData) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(publicKey);
        sign.update(content);
        return sign.verify(signData);
    }

    public static PublicKey getPublicKey(String key) {
        PublicKey pKey = null;
        try {
            pKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Utils.stringToBytes(key)));
        } catch (Exception e) {
        }
        return pKey;
    }

    public static PublicKey getPublicKey(byte[] key) {
        PublicKey pKey = null;
        try {
            pKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key));
        } catch (Exception e) {
        }
        return pKey;
    }
}
