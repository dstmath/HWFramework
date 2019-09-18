package com.huawei.device.connectivitychrlog;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHAUtils {
    private static final String SHA256 = "SHA-256";
    private static final String UTF8 = "UTF-8";

    private static byte[] sha(String source) {
        try {
            return MessageDigest.getInstance(SHA256).digest(source.getBytes(UTF8));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String shaBase64(String source) {
        if (source == null) {
            return null;
        }
        return Base64Coder.encode(sha(source));
    }
}
