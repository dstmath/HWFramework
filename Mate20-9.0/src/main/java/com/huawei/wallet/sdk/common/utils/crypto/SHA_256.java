package com.huawei.wallet.sdk.common.utils.crypto;

import android.text.TextUtils;
import com.huawei.wallet.sdk.common.log.LogC;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA_256 {
    public static final String ALGORITHM_SHA256 = "SHA-256";

    public static String encrypt(String strSrc, String encName) {
        try {
            byte[] bt = strSrc.getBytes(AES.CHAR_ENCODING);
            try {
                if (TextUtils.isEmpty(encName)) {
                    encName = ALGORITHM_SHA256;
                }
                MessageDigest md = MessageDigest.getInstance(encName);
                md.update(bt);
                return bytes2Hex(md.digest());
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
        } catch (UnsupportedEncodingException e2) {
            LogC.e("encrypt error.", false);
            return null;
        }
    }

    public static String bytes2Hex(byte[] bts) {
        StringBuffer des = new StringBuffer();
        for (byte b : bts) {
            String tmp = Integer.toHexString(b & 255);
            if (tmp.length() == 1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }
}
