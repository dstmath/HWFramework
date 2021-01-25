package com.huawei.zxing.resultdispatch;

import com.huawei.internal.telephony.ProxyControllerEx;
import com.huawei.networkit.grs.utils.Encrypt;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x000e, code lost:
        if (r6.equals("") != false) goto L_0x0010;
     */
    public static String Encrypt(String strSrc, String encName) {
        byte[] bt = strSrc.getBytes();
        if (encName != null) {
            try {
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
        }
        encName = Encrypt.ALGORITHM_SHA256;
        MessageDigest md = MessageDigest.getInstance(encName);
        md.update(bt);
        return bytes2Hex(md.digest());
    }

    public static String bytes2Hex(byte[] bts) {
        String des = "";
        for (byte b : bts) {
            String tmp = Integer.toHexString(b & 255);
            if (tmp.length() == 1) {
                des = des + ProxyControllerEx.MODEM_0;
            }
            des = des + tmp;
        }
        return des;
    }
}
