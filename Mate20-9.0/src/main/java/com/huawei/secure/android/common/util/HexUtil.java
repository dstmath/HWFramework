package com.huawei.secure.android.common.util;

import android.text.TextUtils;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
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
            String hex = Integer.toHexString(255 & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String byteArray2HexStr(String sourceStr) {
        String resultStr = "";
        if (TextUtils.isEmpty(sourceStr)) {
            return resultStr;
        }
        try {
            resultStr = byteArray2HexStr(sourceStr.getBytes(AES.CHAR_ENCODING));
        } catch (UnsupportedEncodingException e) {
            LogsUtil.e(TAG, "byte array 2 hex string exception : " + e.getMessage(), true);
        }
        return resultStr;
    }

    public static byte[] hexStr2ByteArray(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String str2 = str.toUpperCase(Locale.US);
        byte[] bytes = new byte[(str2.length() / 2)];
        try {
            byte[] source = str2.getBytes(AES.CHAR_ENCODING);
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (((byte) (Byte.decode("0x" + new String(new byte[]{source[i * 2]}, AES.CHAR_ENCODING)).byteValue() << 4)) ^ Byte.decode("0x" + new String(new byte[]{source[(i * 2) + 1]}, AES.CHAR_ENCODING)).byteValue());
            }
        } catch (UnsupportedEncodingException | NumberFormatException e) {
            LogsUtil.e(TAG, "hex string 2 byte array exception : " + e.getMessage(), true);
        }
        return bytes;
    }
}
