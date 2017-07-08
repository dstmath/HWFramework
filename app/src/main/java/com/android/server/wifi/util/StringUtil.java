package com.android.server.wifi.util;

import com.google.protobuf.nano.Extension;

public class StringUtil {
    static final byte ASCII_PRINTABLE_MAX = (byte) 126;
    static final byte ASCII_PRINTABLE_MIN = (byte) 32;

    public static boolean isAsciiPrintable(byte[] byteArray) {
        if (byteArray == null) {
            return true;
        }
        for (byte b : byteArray) {
            switch (b) {
                case Extension.TYPE_FIXED32 /*7*/:
                case Extension.TYPE_STRING /*9*/:
                case Extension.TYPE_GROUP /*10*/:
                case Extension.TYPE_MESSAGE /*11*/:
                case Extension.TYPE_BYTES /*12*/:
                    break;
                default:
                    if (b >= 32 && b <= 126) {
                        break;
                    }
                    return false;
                    break;
            }
        }
        return true;
    }
}
