package jcifs.util;

import java.io.PrintStream;

public class Hexdump {
    public static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String NL = System.getProperty("line.separator");
    private static final int NL_LENGTH = NL.length();
    private static final char[] SPACE_CHARS = {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};

    public static void hexdump(PrintStream ps, byte[] src, int srcIndex, int length) {
        int r;
        int ci;
        if (length != 0) {
            int s = length % 16;
            if (s == 0) {
                r = length / 16;
            } else {
                r = (length / 16) + 1;
            }
            char[] c = new char[((NL_LENGTH + 74) * r)];
            char[] d = new char[16];
            int si = 0;
            int ci2 = 0;
            do {
                toHexChars(si, c, ci2, 5);
                int ci3 = ci2 + 5;
                int ci4 = ci3 + 1;
                c[ci3] = ':';
                while (true) {
                    if (si != length) {
                        int ci5 = ci4 + 1;
                        c[ci4] = ' ';
                        int i = src[srcIndex + si] & 255;
                        toHexChars(i, c, ci5, 2);
                        ci = ci5 + 2;
                        if (i < 0 || Character.isISOControl((char) i)) {
                            d[si % 16] = '.';
                        } else {
                            d[si % 16] = (char) i;
                        }
                        si++;
                        if (si % 16 == 0) {
                            break;
                        }
                        ci4 = ci;
                    } else {
                        int n = 16 - s;
                        System.arraycopy(SPACE_CHARS, 0, c, ci4, n * 3);
                        ci = ci4 + (n * 3);
                        System.arraycopy(SPACE_CHARS, 0, d, s, n);
                        break;
                    }
                }
                int ci6 = ci + 1;
                c[ci] = ' ';
                int ci7 = ci6 + 1;
                c[ci6] = ' ';
                int ci8 = ci7 + 1;
                c[ci7] = '|';
                System.arraycopy(d, 0, c, ci8, 16);
                int ci9 = ci8 + 16;
                int ci10 = ci9 + 1;
                c[ci9] = '|';
                NL.getChars(0, NL_LENGTH, c, ci10);
                ci2 = ci10 + NL_LENGTH;
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
        char[] c = new char[size];
        int size2 = size % 2 == 0 ? size / 2 : (size / 2) + 1;
        int i = 0;
        int j = 0;
        while (true) {
            if (i >= size2) {
                break;
            }
            int j2 = j + 1;
            c[j] = HEX_DIGITS[(src[i] >> 4) & 15];
            if (j2 == c.length) {
                break;
            }
            j = j2 + 1;
            c[j2] = HEX_DIGITS[src[i] & 15];
            i++;
        }
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
