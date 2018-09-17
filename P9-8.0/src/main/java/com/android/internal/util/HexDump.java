package com.android.internal.util;

import android.text.format.DateFormat;

public class HexDump {
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', DateFormat.CAPITAL_AM_PM, 'B', 'C', 'D', DateFormat.DAY, 'F'};
    private static final char[] HEX_LOWER_CASE_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', DateFormat.AM_PM, 'b', 'c', DateFormat.DATE, 'e', 'f'};

    public static String dumpHexString(byte[] array) {
        return dumpHexString(array, 0, array.length);
    }

    public static String dumpHexString(byte[] array, int offset, int length) {
        StringBuilder result = new StringBuilder();
        byte[] line = new byte[16];
        int lineIndex = 0;
        result.append("\n0x");
        result.append(toHexString(offset));
        int i = offset;
        while (i < offset + length) {
            if (lineIndex == 16) {
                result.append(" ");
                int j = 0;
                while (j < 16) {
                    if (line[j] <= (byte) 32 || line[j] >= (byte) 126) {
                        result.append(".");
                    } else {
                        result.append(new String(line, j, 1));
                    }
                    j++;
                }
                result.append("\n0x");
                result.append(toHexString(i));
                lineIndex = 0;
            }
            byte b = array[i];
            result.append(" ");
            result.append(HEX_DIGITS[(b >>> 4) & 15]);
            result.append(HEX_DIGITS[b & 15]);
            int lineIndex2 = lineIndex + 1;
            line[lineIndex] = b;
            i++;
            lineIndex = lineIndex2;
        }
        if (lineIndex != 16) {
            int count = ((16 - lineIndex) * 3) + 1;
            for (i = 0; i < count; i++) {
                result.append(" ");
            }
            i = 0;
            while (i < lineIndex) {
                if (line[i] <= (byte) 32 || line[i] >= (byte) 126) {
                    result.append(".");
                } else {
                    result.append(new String(line, i, 1));
                }
                i++;
            }
        }
        return result.toString();
    }

    public static String toHexString(byte b) {
        return toHexString(toByteArray(b));
    }

    public static String toHexString(byte[] array) {
        return toHexString(array, 0, array.length, true);
    }

    public static String toHexString(byte[] array, boolean upperCase) {
        return toHexString(array, 0, array.length, upperCase);
    }

    public static String toHexString(byte[] array, int offset, int length) {
        return toHexString(array, offset, length, true);
    }

    public static String toHexString(byte[] array, int offset, int length, boolean upperCase) {
        char[] digits = upperCase ? HEX_DIGITS : HEX_LOWER_CASE_DIGITS;
        char[] buf = new char[(length * 2)];
        int bufIndex = 0;
        for (int i = offset; i < offset + length; i++) {
            byte b = array[i];
            int i2 = bufIndex + 1;
            buf[bufIndex] = digits[(b >>> 4) & 15];
            bufIndex = i2 + 1;
            buf[i2] = digits[b & 15];
        }
        return new String(buf);
    }

    public static String toHexString(int i) {
        return toHexString(toByteArray(i));
    }

    public static byte[] toByteArray(byte b) {
        return new byte[]{b};
    }

    public static byte[] toByteArray(int i) {
        return new byte[]{(byte) (i & 255), (byte) ((i >> 8) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 24) & 255)};
    }

    private static int toByte(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= DateFormat.CAPITAL_AM_PM && c <= 'F') {
            return (c - 65) + 10;
        }
        if (c >= DateFormat.AM_PM && c <= 'f') {
            return (c - 97) + 10;
        }
        throw new RuntimeException("Invalid hex char '" + c + "'");
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] buffer = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            buffer[i / 2] = (byte) ((toByte(hexString.charAt(i)) << 4) | toByte(hexString.charAt(i + 1)));
        }
        return buffer;
    }

    public static StringBuilder appendByteAsHex(StringBuilder sb, byte b, boolean upperCase) {
        char[] digits = upperCase ? HEX_DIGITS : HEX_LOWER_CASE_DIGITS;
        sb.append(digits[(b >> 4) & 15]);
        sb.append(digits[b & 15]);
        return sb;
    }
}
