package com.huawei.zxing.resultdispatch;

import android.telephony.MSimTelephonyConstants;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String Encrypt(String strSrc, String encName) {
        byte[] bt = strSrc.getBytes();
        if (encName != null) {
            try {
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
        }
        encName = "SHA-256";
        MessageDigest md = MessageDigest.getInstance(encName);
        md.update(bt);
        return bytes2Hex(md.digest());
    }

    public static String bytes2Hex(byte[] bts) {
        String des = MSimTelephonyConstants.MY_RADIO_PLATFORM;
        for (byte b : bts) {
            String tmp = Integer.toHexString(b & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN);
            if (tmp.length() == 1) {
                des = des + "0";
            }
            des = des + tmp;
        }
        return des;
    }
}
