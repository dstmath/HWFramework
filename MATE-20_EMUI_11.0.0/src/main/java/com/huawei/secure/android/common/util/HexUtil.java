package com.huawei.secure.android.common.util;

import android.text.TextUtils;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public final class HexUtil {
    private static final String EMPTY = "";
    private static final String TAG = "HexUtil";

    private HexUtil() {
    }

    public static String byteArray2HexStr(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 255);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String byteArray2HexStr(String sourceStr) {
        if (TextUtils.isEmpty(sourceStr)) {
            return "";
        }
        try {
            return byteArray2HexStr(sourceStr.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LogsUtil.e(TAG, "byte array 2 hex string exception : " + e.getMessage(), true);
            return "";
        }
    }

    public static byte[] hexStr2ByteArray(String str) {
        if (TextUtils.isEmpty(str)) {
            return new byte[0];
        }
        String str2 = str.toUpperCase(Locale.ENGLISH);
        byte[] bytes = new byte[(str2.length() / 2)];
        try {
            byte[] source = str2.getBytes("UTF-8");
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (((byte) (Byte.decode("0x" + new String(new byte[]{source[i * 2]}, "UTF-8")).byteValue() << 4)) ^ Byte.decode("0x" + new String(new byte[]{source[(i * 2) + 1]}, "UTF-8")).byteValue());
            }
        } catch (UnsupportedEncodingException | NumberFormatException e) {
            LogsUtil.e(TAG, "hex string 2 byte array exception : " + e.getMessage(), true);
        }
        return bytes;
    }
}
