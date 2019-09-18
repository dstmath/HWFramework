package com.huawei.zxing.resultdispatch;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {
    public static String Encrypt(String strSrc, String encName) {
        byte[] bt = strSrc.getBytes();
        if (encName != null) {
            try {
                if (encName.equals("")) {
                }
                MessageDigest md = MessageDigest.getInstance(encName);
                md.update(bt);
                return bytes2Hex(md.digest());
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
        }
        encName = "SHA-256";
        MessageDigest md2 = MessageDigest.getInstance(encName);
        md2.update(bt);
        return bytes2Hex(md2.digest());
    }

    public static String bytes2Hex(byte[] bts) {
        String des = "";
        for (byte b : bts) {
            if (Integer.toHexString(b & 255).length() == 1) {
                des = des + "0";
            }
            des = des + tmp;
        }
        return des;
    }
}
