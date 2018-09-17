package com.tencent.tcuser.util;

public class a {
    public static byte[] at(String str) {
        if (str == null || str.trim().length() <= 0) {
            return new byte[0];
        }
        int length = str.length() / 2;
        byte[] bArr = new byte[length];
        char[] toCharArray = str.toCharArray();
        for (int i = 0; i < length; i++) {
            int i2 = i * 2;
            bArr[i] = (byte) ((byte) ((b(toCharArray[i2]) << 4) | b(toCharArray[i2 + 1])));
        }
        return bArr;
    }

    public static byte au(String str) {
        if (str != null) {
            try {
                if (str.trim().length() > 0) {
                    return Byte.valueOf(str).byteValue();
                }
            } catch (Throwable th) {
                return (byte) -1;
            }
        }
        return (byte) -1;
    }

    public static int av(String str) {
        if (str != null) {
            try {
                if (str.trim().length() > 0) {
                    return Integer.valueOf(str).intValue();
                }
            } catch (Throwable th) {
                return -1;
            }
        }
        return -1;
    }

    public static long aw(String str) {
        if (str != null) {
            try {
                if (str.trim().length() > 0) {
                    return Long.valueOf(str).longValue();
                }
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    private static byte b(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static final String bytesToHexString(byte[] bArr) {
        if (bArr == null || bArr.length <= 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer(bArr.length);
        for (byte b : bArr) {
            String toHexString = Integer.toHexString(b & 255);
            if (toHexString.length() < 2) {
                stringBuffer.append(0);
            }
            stringBuffer.append(toHexString.toUpperCase());
        }
        return stringBuffer.toString();
    }
}
