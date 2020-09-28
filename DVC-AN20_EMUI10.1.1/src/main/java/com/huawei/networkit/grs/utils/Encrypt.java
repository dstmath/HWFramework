package com.huawei.networkit.grs.utils;

import android.text.TextUtils;
import com.huawei.internal.telephony.PhoneConstantsEx;
import com.huawei.internal.telephony.ProxyControllerEx;
import com.huawei.networkit.grs.common.Logger;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class Encrypt {
    public static final String ALGORITHM_SHA256 = "SHA-256";
    private static final int DEVIDE_NUMBER = 2;
    private static final int NUMBER = 255;
    private static final Pattern REG_PATTERN = Pattern.compile("[0-9]*[a-z|A-Z]*[一-龥]*");
    private static final String TAG = Encrypt.class.getSimpleName();

    public static String encryptBySHA256(String strSrc) {
        return encrypt(strSrc, ALGORITHM_SHA256);
    }

    private static String encrypt(String strSrc, String algorithm) {
        try {
            try {
                return bytes2Hex(MessageDigest.getInstance(algorithm).digest(strSrc.getBytes("UTF-8")));
            } catch (NoSuchAlgorithmException e) {
                Logger.w(TAG, "encrypt NoSuchAlgorithmException");
                return null;
            }
        } catch (UnsupportedEncodingException e2) {
            Logger.w(TAG, "encrypt UnsupportedEncodingException");
            return null;
        }
    }

    private static String bytes2Hex(byte[] bts) {
        StringBuffer des = new StringBuffer();
        for (byte b : bts) {
            String tmp = Integer.toHexString(b & 255);
            if (tmp.length() == 1) {
                des.append(ProxyControllerEx.MODEM_0);
            }
            des.append(tmp);
        }
        return des.toString();
    }

    public static String formatWithStar(String logStr) {
        if (TextUtils.isEmpty(logStr)) {
            return logStr;
        }
        if (logStr.length() == 1) {
            return PhoneConstantsEx.APN_TYPE_ALL;
        }
        StringBuffer retStr = new StringBuffer();
        int count = 1;
        for (int i = 0; i < logStr.length(); i++) {
            String charAt = logStr.charAt(i) + "";
            if (REG_PATTERN.matcher(charAt).matches()) {
                if (count % 2 == 0) {
                    charAt = PhoneConstantsEx.APN_TYPE_ALL;
                }
                count++;
            }
            retStr.append(charAt);
        }
        return retStr.toString();
    }
}
