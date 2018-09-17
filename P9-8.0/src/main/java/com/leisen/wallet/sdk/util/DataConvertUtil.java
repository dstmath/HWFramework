package com.leisen.wallet.sdk.util;

import com.android.server.security.trustcircle.utils.ByteUtil;

public class DataConvertUtil {
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            String hv = Integer.toHexString(b & 255);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((byte) ((charToByte(hexChars[pos]) << 4) | charToByte(hexChars[pos + 1])));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) ByteUtil.HEX_TABLE.indexOf(c);
    }
}
