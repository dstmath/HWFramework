package com.android.server.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Sha {
    public static final String C1_KEY = "4135a1281fd5ede8fc53516fd2919dce65ba30f9c2db4dbced6f2cd9c471105a";

    public static byte[] hMac(byte[] key, byte[] data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        sha256_HMAC.init(new SecretKeySpec(key, "HmacSHA256"));
        return sha256_HMAC.doFinal(data);
    }

    public static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        return md.digest();
    }
}
