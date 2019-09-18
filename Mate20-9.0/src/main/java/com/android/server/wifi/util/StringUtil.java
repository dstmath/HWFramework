package com.android.server.wifi.util;

public class StringUtil {
    static final byte ASCII_PRINTABLE_MAX = 126;
    static final byte ASCII_PRINTABLE_MIN = 32;

    public static boolean isAsciiPrintable(byte[] byteArray) {
        if (byteArray == null) {
            return true;
        }
        for (byte b : byteArray) {
            if (b != 7) {
                switch (b) {
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                        continue;
                    default:
                        if (b >= 32 && b <= 126) {
                            break;
                        } else {
                            return false;
                        }
                        break;
                }
            }
        }
        return true;
    }

    public static String safeDisplayBssid(String srcBssid) {
        if (srcBssid == null) {
            return "null";
        }
        int len = srcBssid.length();
        if (len < 12) {
            return "Can not display bssid";
        }
        return srcBssid.substring(0, 6) + "**:**" + srcBssid.substring(len - 6, len);
    }
}
