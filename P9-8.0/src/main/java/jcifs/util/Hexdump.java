package jcifs.util;

import java.io.PrintStream;

public class Hexdump {
    public static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String NL = System.getProperty("line.separator");
    private static final int NL_LENGTH = NL.length();
    private static final char[] SPACE_CHARS = new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};

    public static void hexdump(PrintStream ps, byte[] src, int srcIndex, int length) {
        if (length != 0) {
            int r;
            int s = length % 16;
            if (s == 0) {
                r = length / 16;
            } else {
                r = (length / 16) + 1;
            }
            char[] c = new char[((NL_LENGTH + 74) * r)];
            char[] d = new char[16];
            int si = 0;
            int ci = 0;
            do {
                toHexChars(si, c, ci, 5);
                ci += 5;
                int ci2 = ci + 1;
                c[ci] = ':';
                while (si != length) {
                    ci = ci2 + 1;
                    c[ci2] = ' ';
                    int i = src[srcIndex + si] & 255;
                    toHexChars(i, c, ci, 2);
                    ci += 2;
                    if (i < 0 || Character.isISOControl((char) i)) {
                        d[si % 16] = '.';
                    } else {
                        d[si % 16] = (char) i;
                    }
                    si++;
                    if (si % 16 == 0) {
                        break;
                    }
                    ci2 = ci;
                }
                int n = 16 - s;
                System.arraycopy(SPACE_CHARS, 0, c, ci2, n * 3);
                ci = ci2 + (n * 3);
                System.arraycopy(SPACE_CHARS, 0, d, s, n);
                ci2 = ci + 1;
                c[ci] = ' ';
                ci = ci2 + 1;
                c[ci2] = ' ';
                ci2 = ci + 1;
                c[ci] = '|';
                System.arraycopy(d, 0, c, ci2, 16);
                ci = ci2 + 16;
                ci2 = ci + 1;
                c[ci] = '|';
                NL.getChars(0, NL_LENGTH, c, ci2);
                ci = ci2 + NL_LENGTH;
            } while (si < length);
            ps.println(c);
        }
    }

    public static String toHexString(int val, int size) {
        char[] c = new char[size];
        toHexChars(val, c, 0, size);
        return new String(c);
    }

    public static String toHexString(long val, int size) {
        char[] c = new char[size];
        toHexChars(val, c, 0, size);
        return new String(c);
    }

    public static String toHexString(byte[] src, int srcIndex, int size) {
        int i;
        char[] c = new char[size];
        size = size % 2 == 0 ? size / 2 : (size / 2) + 1;
        int j = 0;
        for (int i2 = 0; i2 < size; i2++) {
            i = j + 1;
            c[j] = HEX_DIGITS[(src[i2] >> 4) & 15];
            if (i == c.length) {
                break;
            }
            j = i + 1;
            c[i] = HEX_DIGITS[src[i2] & 15];
        }
        i = j;
        return new String(c);
    }

    public static void toHexChars(int val, char[] dst, int dstIndex, int size) {
        while (size > 0) {
            int i = (dstIndex + size) - 1;
            if (i < dst.length) {
                dst[i] = HEX_DIGITS[val & 15];
            }
            if (val != 0) {
                val >>>= 4;
            }
            size--;
        }
    }

    public static void toHexChars(long val, char[] dst, int dstIndex, int size) {
        while (size > 0) {
            dst[(dstIndex + size) - 1] = HEX_DIGITS[(int) (15 & val)];
            if (val != 0) {
                val >>>= 4;
            }
            size--;
        }
    }
}
