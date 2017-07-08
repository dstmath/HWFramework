package com.tencent.tcuser.util;

/* compiled from: Unknown */
public class a {
    public static byte bt(String str) {
        if (str != null) {
            try {
                if (str.trim().length() > 0) {
                    return Byte.valueOf(str).byteValue();
                }
            } catch (Exception e) {
                return (byte) -1;
            }
        }
        return (byte) -1;
    }

    public static int bu(String str) {
        if (str != null) {
            try {
                if (str.trim().length() > 0) {
                    return Integer.valueOf(str).intValue();
                }
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }
}
