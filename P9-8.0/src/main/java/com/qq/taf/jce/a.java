package com.qq.taf.jce;

public class a {
    private static final char[] mE = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static final byte[] mF = new byte[0];

    public static String c(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        char[] buf = new char[(bytes.length * 2)];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            buf[(i * 2) + 1] = (char) mE[b & 15];
            buf[(i * 2) + 0] = (char) mE[((byte) (b >>> 4)) & 15];
        }
        return new String(buf);
    }

    public static byte a(char ch) {
        if (ch >= '0' && ch <= '9') {
            return (byte) (ch - 48);
        }
        if (ch >= 'a' && ch <= 'f') {
            return (byte) ((ch - 97) + 10);
        }
        if (ch >= 'A' && ch <= 'F') {
            return (byte) ((ch - 65) + 10);
        }
        return (byte) 0;
    }

    public static byte[] E(String str) {
        if (str == null || str.equals("")) {
            return mF;
        }
        byte[] bytes = new byte[(str.length() / 2)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ((byte) ((a(str.charAt(i * 2)) * 16) + a(str.charAt((i * 2) + 1))));
        }
        return bytes;
    }
}
