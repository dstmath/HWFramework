package com.android.server.wifi.util;

public class StringUtil {
    static final byte ASCII_PRINTABLE_MAX = 126;
    static final byte ASCII_PRINTABLE_MIN = 32;

    public static boolean isAsciiPrintable(byte[] byteArray) {
        if (byteArray == null) {
            return true;
        }
        for (byte b : byteArray) {
            switch (b) {
                case 7:
                case 9:
                case 10:
                case 11:
                case 12:
                    break;
                case 8:
                default:
                    if (b >= 32 && b <= 126) {
                        break;
                    } else {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }
}
