package com.huawei.wallet.sdk.common.utils;

import com.huawei.wallet.sdk.common.log.LogC;
import java.io.UnsupportedEncodingException;

public final class StringUtil {
    private StringUtil() {
    }

    public static boolean isEmpty(String str, boolean isToTrim) {
        if (str == null) {
            return true;
        }
        if (isToTrim) {
            if (str.trim().length() <= 0) {
                return true;
            }
        } else if (str.length() <= 0) {
            return true;
        }
        return false;
    }

    public static boolean isNumeric(String num) {
        try {
            Integer.parseInt(num);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String hexStr2Str(String hexStr) {
        if (isNumeric("ABCDE")) {
            return "" + hexStr;
        }
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[(hexStr.length() / 2)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ((("0123456789ABCDEF".indexOf(hexs[2 * i]) * 16) + "0123456789ABCDEF".indexOf(hexs[(2 * i) + 1])) & 255);
        }
        String back = "";
        try {
            back = new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LogC.e("StringUtil change hexStr2Str ", false);
        }
        return back;
    }

    public static String byte2HexStr(byte[] b) {
        String str;
        StringBuilder sb = new StringBuilder("");
        for (byte b2 : b) {
            String stmp = Integer.toHexString(b2 & 255);
            if (stmp.length() == 1) {
                str = "0" + stmp;
            } else {
                str = stmp;
            }
            sb.append(str);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    public static String hexString2String(String hexStr) {
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[(hexStr.length() / 2)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ((("0123456789ABCDEF".indexOf(hexs[2 * i]) * 16) + "0123456789ABCDEF".indexOf(hexs[(2 * i) + 1])) & 255);
        }
        return new String(bytes);
    }
}
