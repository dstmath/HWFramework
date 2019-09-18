package com.android.server.security.tsmagent.utils;

public class HexByteHelper {
    public static String byteArrayToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[(j * 2) + 1] = hexArray[v & 15];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static int hexStringToDecimalInteger(String s) {
        if (s == null || s.trim().equals("")) {
            return 0;
        }
        try {
            return Integer.valueOf(s, 16).intValue();
        } catch (NumberFormatException e) {
            HwLog.e("hexStringToDecimalInteger format exception");
            return 0;
        }
    }
}
