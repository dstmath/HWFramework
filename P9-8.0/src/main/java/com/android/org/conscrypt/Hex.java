package com.android.org.conscrypt;

public class Hex {
    private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private Hex() {
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] buf = new char[(bytes.length * 2)];
        int c = 0;
        for (byte b : bytes) {
            int i = c + 1;
            buf[c] = DIGITS[(b >> 4) & 15];
            c = i + 1;
            buf[i] = DIGITS[b & 15];
        }
        return new String(buf);
    }

    public static String intToHexString(int i, int minWidth) {
        char[] buf = new char[8];
        int cursor = 8;
        while (true) {
            cursor--;
            buf[cursor] = DIGITS[i & 15];
            i >>>= 4;
            if (i == 0 && 8 - cursor >= minWidth) {
                return new String(buf, cursor, 8 - cursor);
            }
        }
    }
}
