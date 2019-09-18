package com.android.org.conscrypt;

public final class Hex {
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private Hex() {
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] buf = new char[(bytes.length * 2)];
        int c = 0;
        for (byte b : bytes) {
            int c2 = c + 1;
            buf[c] = DIGITS[(b >> 4) & 15];
            c = c2 + 1;
            buf[c2] = DIGITS[b & 15];
        }
        return new String(buf);
    }

    public static String intToHexString(int i, int minWidth) {
        char[] buf = new char[8];
        int i2 = i;
        int cursor = 8;
        while (true) {
            cursor--;
            buf[cursor] = DIGITS[i2 & 15];
            int i3 = i2 >>> 4;
            i2 = i3;
            if (i3 == 0 && 8 - cursor >= minWidth) {
                return new String(buf, cursor, 8 - cursor);
            }
        }
    }
}
