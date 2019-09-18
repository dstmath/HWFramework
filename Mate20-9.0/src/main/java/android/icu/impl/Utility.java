package android.icu.impl;

import android.icu.impl.locale.LanguageTag;
import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterEnums;
import android.icu.text.Replaceable;
import android.icu.text.UTF16;
import android.icu.text.UnicodeMatcher;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Utility {
    private static final char APOSTROPHE = '\'';
    private static final char BACKSLASH = '\\';
    static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char ESCAPE = 'ꖥ';
    static final byte ESCAPE_BYTE = -91;
    static final char[] HEX_DIGIT = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int MAGIC_UNSIGNED = Integer.MIN_VALUE;
    private static final char[] UNESCAPE_MAP = {'a', 7, 'b', 8, 'e', 27, 'f', 12, 'n', 10, 'r', 13, 't', 9, 'v', 11};

    public static final boolean arrayEquals(Object[] source, Object target) {
        boolean z = true;
        if (source == null) {
            if (target != null) {
                z = false;
            }
            return z;
        } else if (!(target instanceof Object[])) {
            return false;
        } else {
            Object[] targ = (Object[]) target;
            if (source.length != targ.length || !arrayRegionMatches(source, 0, targ, 0, source.length)) {
                z = false;
            }
            return z;
        }
    }

    public static final boolean arrayEquals(int[] source, Object target) {
        boolean z = true;
        if (source == null) {
            if (target != null) {
                z = false;
            }
            return z;
        } else if (!(target instanceof int[])) {
            return false;
        } else {
            int[] targ = (int[]) target;
            if (source.length != targ.length || !arrayRegionMatches(source, 0, targ, 0, source.length)) {
                z = false;
            }
            return z;
        }
    }

    public static final boolean arrayEquals(double[] source, Object target) {
        boolean z = true;
        if (source == null) {
            if (target != null) {
                z = false;
            }
            return z;
        } else if (!(target instanceof double[])) {
            return false;
        } else {
            double[] targ = (double[]) target;
            if (source.length != targ.length || !arrayRegionMatches(source, 0, targ, 0, source.length)) {
                z = false;
            }
            return z;
        }
    }

    public static final boolean arrayEquals(byte[] source, Object target) {
        boolean z = true;
        if (source == null) {
            if (target != null) {
                z = false;
            }
            return z;
        } else if (!(target instanceof byte[])) {
            return false;
        } else {
            byte[] targ = (byte[]) target;
            if (source.length != targ.length || !arrayRegionMatches(source, 0, targ, 0, source.length)) {
                z = false;
            }
            return z;
        }
    }

    public static final boolean arrayEquals(Object source, Object target) {
        if (source == null) {
            return target == null;
        } else if (source instanceof Object[]) {
            return arrayEquals((Object[]) source, target);
        } else {
            if (source instanceof int[]) {
                return arrayEquals((int[]) source, target);
            }
            if (source instanceof double[]) {
                return arrayEquals((double[]) source, target);
            }
            if (source instanceof byte[]) {
                return arrayEquals((byte[]) source, target);
            }
            return source.equals(target);
        }
    }

    public static final boolean arrayRegionMatches(Object[] source, int sourceStart, Object[] target, int targetStart, int len) {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (!arrayEquals(source[i], target[i + delta])) {
                return false;
            }
        }
        return true;
    }

    public static final boolean arrayRegionMatches(char[] source, int sourceStart, char[] target, int targetStart, int len) {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (source[i] != target[i + delta]) {
                return false;
            }
        }
        return true;
    }

    public static final boolean arrayRegionMatches(int[] source, int sourceStart, int[] target, int targetStart, int len) {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (source[i] != target[i + delta]) {
                return false;
            }
        }
        return true;
    }

    public static final boolean arrayRegionMatches(double[] source, int sourceStart, double[] target, int targetStart, int len) {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (source[i] != target[i + delta]) {
                return false;
            }
        }
        return true;
    }

    public static final boolean arrayRegionMatches(byte[] source, int sourceStart, byte[] target, int targetStart, int len) {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (source[i] != target[i + delta]) {
                return false;
            }
        }
        return true;
    }

    public static final boolean sameObjects(Object a, Object b) {
        return a == b;
    }

    public static final boolean objectEquals(Object a, Object b) {
        if (a == null) {
            if (b == null) {
                return true;
            }
            return false;
        } else if (b == null) {
            return false;
        } else {
            return a.equals(b);
        }
    }

    public static <T extends Comparable<T>> int checkCompare(T a, T b) {
        if (a == null) {
            return b == null ? 0 : -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }

    public static int checkHash(Object a) {
        if (a == null) {
            return 0;
        }
        return a.hashCode();
    }

    public static final String arrayToRLEString(int[] a) {
        StringBuilder buffer = new StringBuilder();
        appendInt(buffer, a.length);
        int runValue = a[0];
        int runLength = 1;
        for (int i = 1; i < a.length; i++) {
            int s = a[i];
            if (s != runValue || runLength >= 65535) {
                encodeRun(buffer, runValue, runLength);
                runValue = s;
                runLength = 1;
            } else {
                runLength++;
            }
        }
        encodeRun(buffer, runValue, runLength);
        return buffer.toString();
    }

    public static final String arrayToRLEString(short[] a) {
        StringBuilder buffer = new StringBuilder();
        buffer.append((char) (a.length >> 16));
        buffer.append((char) a.length);
        short runValue = a[0];
        int runLength = 1;
        for (int i = 1; i < a.length; i++) {
            short s = a[i];
            if (s != runValue || runLength >= 65535) {
                encodeRun(buffer, runValue, runLength);
                runValue = s;
                runLength = 1;
            } else {
                runLength++;
            }
        }
        encodeRun(buffer, runValue, runLength);
        return buffer.toString();
    }

    public static final String arrayToRLEString(char[] a) {
        StringBuilder buffer = new StringBuilder();
        buffer.append((char) (a.length >> 16));
        buffer.append((char) a.length);
        char runValue = a[0];
        int runLength = 1;
        for (int i = 1; i < a.length; i++) {
            char s = a[i];
            if (s != runValue || runLength >= 65535) {
                encodeRun(buffer, (short) runValue, runLength);
                runValue = s;
                runLength = 1;
            } else {
                runLength++;
            }
        }
        encodeRun(buffer, (short) runValue, runLength);
        return buffer.toString();
    }

    public static final String arrayToRLEString(byte[] a) {
        StringBuilder buffer = new StringBuilder();
        buffer.append((char) (a.length >> 16));
        buffer.append((char) a.length);
        byte runValue = a[0];
        int runLength = 1;
        byte[] state = new byte[2];
        for (int i = 1; i < a.length; i++) {
            byte b = a[i];
            if (b != runValue || runLength >= 255) {
                encodeRun(buffer, runValue, runLength, state);
                runValue = b;
                runLength = 1;
            } else {
                runLength++;
            }
        }
        encodeRun(buffer, runValue, runLength, state);
        if (state[0] != 0) {
            appendEncodedByte(buffer, (byte) 0, state);
        }
        return buffer.toString();
    }

    private static final <T extends Appendable> void encodeRun(T buffer, int value, int length) {
        if (length < 4) {
            for (int j = 0; j < length; j++) {
                if (value == 42405) {
                    appendInt(buffer, value);
                }
                appendInt(buffer, value);
            }
            return;
        }
        if (length == 42405) {
            if (value == 42405) {
                appendInt(buffer, 42405);
            }
            appendInt(buffer, value);
            length--;
        }
        appendInt(buffer, 42405);
        appendInt(buffer, length);
        appendInt(buffer, value);
    }

    private static final <T extends Appendable> void appendInt(T buffer, int value) {
        try {
            buffer.append((char) (value >>> 16));
            buffer.append((char) (65535 & value));
        } catch (IOException e) {
            throw new IllegalIcuArgumentException((Throwable) e);
        }
    }

    private static final <T extends Appendable> void encodeRun(T buffer, short value, int length) {
        char valueChar = (char) value;
        if (length < 4) {
            for (int j = 0; j < length; j++) {
                if (valueChar == 42405) {
                    try {
                        buffer.append(ESCAPE);
                    } catch (IOException e) {
                        throw new IllegalIcuArgumentException((Throwable) e);
                    }
                }
                buffer.append(valueChar);
            }
            return;
        }
        if (length == 42405) {
            if (valueChar == 42405) {
                buffer.append(ESCAPE);
            }
            buffer.append(valueChar);
            length--;
        }
        buffer.append(ESCAPE);
        buffer.append((char) length);
        buffer.append(valueChar);
    }

    private static final <T extends Appendable> void encodeRun(T buffer, byte value, int length, byte[] state) {
        if (length < 4) {
            for (int j = 0; j < length; j++) {
                if (value == -91) {
                    appendEncodedByte(buffer, ESCAPE_BYTE, state);
                }
                appendEncodedByte(buffer, value, state);
            }
            return;
        }
        if (((byte) length) == -91) {
            if (value == -91) {
                appendEncodedByte(buffer, ESCAPE_BYTE, state);
            }
            appendEncodedByte(buffer, value, state);
            length--;
        }
        appendEncodedByte(buffer, ESCAPE_BYTE, state);
        appendEncodedByte(buffer, (byte) length, state);
        appendEncodedByte(buffer, value, state);
    }

    private static final <T extends Appendable> void appendEncodedByte(T buffer, byte value, byte[] state) {
        try {
            if (state[0] != 0) {
                buffer.append((char) ((state[1] << 8) | (value & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED)));
                state[0] = 0;
                return;
            }
            state[0] = 1;
            state[1] = value;
        } catch (IOException e) {
            throw new IllegalIcuArgumentException((Throwable) e);
        }
    }

    public static final int[] RLEStringToIntArray(String s) {
        int length = getInt(s, 0);
        int[] array = new int[length];
        int ai = 0;
        int c = 1;
        int maxI = s.length() / 2;
        while (ai < length && c < maxI) {
            int i = c + 1;
            int i2 = getInt(s, c);
            if (i2 == 42405) {
                int i3 = i + 1;
                int c2 = getInt(s, i);
                if (c2 == 42405) {
                    array[ai] = c2;
                    ai++;
                    c = i3;
                } else {
                    int runLength = c2;
                    int i4 = i3 + 1;
                    int i5 = getInt(s, i3);
                    int ai2 = ai;
                    int j = 0;
                    while (j < runLength) {
                        array[ai2] = i5;
                        j++;
                        ai2++;
                    }
                    c = i4;
                    ai = ai2;
                }
            } else {
                array[ai] = i2;
                c = i;
                ai++;
            }
        }
        if (ai == length && c == maxI) {
            return array;
        }
        throw new IllegalStateException("Bad run-length encoded int array");
    }

    static final int getInt(String s, int i) {
        return s.charAt((2 * i) + 1) | (s.charAt(2 * i) << 16);
    }

    public static final short[] RLEStringToShortArray(String s) {
        int ai;
        int length = (s.charAt(0) << 16) | s.charAt(1);
        short[] array = new short[length];
        int ai2 = 0;
        int i = 2;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == 42405) {
                i++;
                int c2 = s.charAt(i);
                if (c2 == 42405) {
                    ai = ai2 + 1;
                    array[ai2] = (short) c2;
                } else {
                    int runLength = c2;
                    i++;
                    short runValue = (short) s.charAt(i);
                    int ai3 = ai2;
                    int j = 0;
                    while (j < runLength) {
                        array[ai3] = runValue;
                        j++;
                        ai3++;
                    }
                    ai2 = ai3;
                    i++;
                }
            } else {
                ai = ai2 + 1;
                array[ai2] = (short) c;
            }
            ai2 = ai;
            i++;
        }
        if (ai2 == length) {
            return array;
        }
        throw new IllegalStateException("Bad run-length encoded short array");
    }

    public static final char[] RLEStringToCharArray(String s) {
        int ai;
        int length = (s.charAt(0) << 16) | s.charAt(1);
        char[] array = new char[length];
        int ai2 = 0;
        int i = 2;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == 42405) {
                i++;
                char c2 = s.charAt(i);
                if (c2 == 42405) {
                    ai = ai2 + 1;
                    array[ai2] = c2;
                } else {
                    int runLength = c2;
                    i++;
                    char runValue = s.charAt(i);
                    int ai3 = ai2;
                    int j = 0;
                    while (j < runLength) {
                        array[ai3] = runValue;
                        j++;
                        ai3++;
                    }
                    ai2 = ai3;
                    i++;
                }
            } else {
                ai = ai2 + 1;
                array[ai2] = c;
            }
            ai2 = ai;
            i++;
        }
        if (ai2 == length) {
            return array;
        }
        throw new IllegalStateException("Bad run-length encoded short array");
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public static final byte[] RLEStringToByteArray(String s) {
        int i;
        byte b;
        int ai;
        int length = (s.charAt(0) << 16) | s.charAt(1);
        byte[] array = new byte[length];
        int node = 0;
        int i2 = 2;
        int runLength = 0;
        char c = 0;
        boolean nextChar = true;
        int node2 = 0;
        while (node2 < length) {
            if (nextChar) {
                i = i2 + 1;
                c = s.charAt(i2);
                b = (byte) (c >> 8);
                nextChar = false;
            } else {
                nextChar = true;
                i = i2;
                b = (byte) (c & 255);
            }
            switch (node) {
                case 0:
                    if (b == -91) {
                        node = 1;
                        continue;
                    } else {
                        ai = node2 + 1;
                        array[node2] = b;
                    }
                case 1:
                    if (b != -91) {
                        runLength = b;
                        if (runLength < 0) {
                            runLength += 256;
                        }
                        node = 2;
                        break;
                    } else {
                        array[node2] = ESCAPE_BYTE;
                        node = 0;
                        node2++;
                        continue;
                    }
                case 2:
                    ai = node2;
                    int j = 0;
                    while (j < runLength) {
                        array[ai] = b;
                        j++;
                        ai++;
                    }
                    node = 0;
                    node2 = ai;
                    break;
            }
            node2 = ai;
            i2 = i;
        }
        if (node != 0) {
            throw new IllegalStateException("Bad run-length encoded byte array");
        } else if (i2 == s.length()) {
            return array;
        } else {
            throw new IllegalStateException("Excess data in RLE byte array string");
        }
    }

    public static final String formatForSource(String s) {
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            if (i > 0) {
                buffer.append('+');
                buffer.append(LINE_SEPARATOR);
            }
            buffer.append("        \"");
            int count = 11;
            while (i < s.length() && count < 80) {
                int i2 = i + 1;
                char c = s.charAt(i);
                if (c < ' ' || c == '\"' || c == '\\') {
                    if (c == 10) {
                        buffer.append("\\n");
                        count += 2;
                    } else if (c == 9) {
                        buffer.append("\\t");
                        count += 2;
                    } else if (c == 13) {
                        buffer.append("\\r");
                        count += 2;
                    } else {
                        buffer.append('\\');
                        buffer.append(HEX_DIGIT[(c & 448) >> 6]);
                        buffer.append(HEX_DIGIT[(c & '8') >> 3]);
                        buffer.append(HEX_DIGIT[c & 7]);
                        count += 4;
                    }
                } else if (c <= '~') {
                    buffer.append(c);
                    count++;
                } else {
                    buffer.append("\\u");
                    buffer.append(HEX_DIGIT[(61440 & c) >> 12]);
                    buffer.append(HEX_DIGIT[(c & 3840) >> 8]);
                    buffer.append(HEX_DIGIT[(c & 240) >> 4]);
                    buffer.append(HEX_DIGIT[c & 15]);
                    count += 6;
                }
                i = i2;
            }
            buffer.append('\"');
        }
        return buffer.toString();
    }

    public static final String format1ForSource(String s) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("\"");
        int i = 0;
        while (i < s.length()) {
            int i2 = i + 1;
            char c = s.charAt(i);
            if (c < ' ' || c == '\"' || c == '\\') {
                if (c == 10) {
                    buffer.append("\\n");
                } else if (c == 9) {
                    buffer.append("\\t");
                } else if (c == 13) {
                    buffer.append("\\r");
                } else {
                    buffer.append('\\');
                    buffer.append(HEX_DIGIT[(c & 448) >> 6]);
                    buffer.append(HEX_DIGIT[(c & '8') >> 3]);
                    buffer.append(HEX_DIGIT[c & 7]);
                }
            } else if (c <= '~') {
                buffer.append(c);
            } else {
                buffer.append("\\u");
                buffer.append(HEX_DIGIT[(61440 & c) >> 12]);
                buffer.append(HEX_DIGIT[(c & 3840) >> 8]);
                buffer.append(HEX_DIGIT[(c & 240) >> 4]);
                buffer.append(HEX_DIGIT[c & 15]);
            }
            i = i2;
        }
        buffer.append('\"');
        return buffer.toString();
    }

    public static final String escape(String s) {
        StringBuilder buf = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int c = Character.codePointAt(s, i);
            i += UTF16.getCharCount(c);
            if (c < 32 || c > 127) {
                boolean four = c <= 65535;
                buf.append(four ? "\\u" : "\\U");
                buf.append(hex((long) c, four ? 4 : 8));
            } else if (c == 92) {
                buf.append("\\\\");
            } else {
                buf.append((char) c);
            }
        }
        return buf.toString();
    }

    public static int unescapeAt(String s, int[] offset16) {
        int result = 0;
        int n = 0;
        int minDig = 0;
        int maxDig = 0;
        int bitsPerDigit = 4;
        boolean braces = false;
        int offset = offset16[0];
        int length = s.length();
        if (offset < 0 || offset >= length) {
            return -1;
        }
        int c = Character.codePointAt(s, offset);
        int offset2 = offset + UTF16.getCharCount(c);
        if (c == 85) {
            maxDig = 8;
            minDig = 8;
        } else if (c == 117) {
            maxDig = 4;
            minDig = 4;
        } else if (c != 120) {
            int dig = UCharacter.digit(c, 8);
            if (dig >= 0) {
                minDig = 1;
                maxDig = 3;
                n = 1;
                bitsPerDigit = 3;
                result = dig;
            }
        } else {
            minDig = 1;
            if (offset2 >= length || UTF16.charAt(s, offset2) != 123) {
                maxDig = 2;
            } else {
                offset2++;
                braces = true;
                maxDig = 8;
            }
        }
        if (minDig != 0) {
            while (offset2 < length && n < maxDig) {
                c = UTF16.charAt(s, offset2);
                int dig2 = UCharacter.digit(c, bitsPerDigit == 3 ? 8 : 16);
                if (dig2 < 0) {
                    break;
                }
                result = (result << bitsPerDigit) | dig2;
                offset2 += UTF16.getCharCount(c);
                n++;
            }
            if (n < minDig) {
                return -1;
            }
            if (braces) {
                if (c != 125) {
                    return -1;
                }
                offset2++;
            }
            if (result < 0 || result >= 1114112) {
                return -1;
            }
            if (offset2 < length && UTF16.isLeadSurrogate((char) result)) {
                int ahead = offset2 + 1;
                int c2 = s.charAt(offset2);
                if (c2 == 92 && ahead < length) {
                    int[] o = {ahead};
                    c2 = unescapeAt(s, o);
                    ahead = o[0];
                }
                if (UTF16.isTrailSurrogate((char) c2)) {
                    offset2 = ahead;
                    result = Character.toCodePoint((char) result, (char) c2);
                }
            }
            offset16[0] = offset2;
            return result;
        }
        int i = 0;
        while (i < UNESCAPE_MAP.length) {
            if (c == UNESCAPE_MAP[i]) {
                offset16[0] = offset2;
                return UNESCAPE_MAP[i + 1];
            } else if (c < UNESCAPE_MAP[i]) {
                break;
            } else {
                i += 2;
            }
        }
        if (c != 99 || offset2 >= length) {
            offset16[0] = offset2;
            return c;
        }
        int c3 = UTF16.charAt(s, offset2);
        offset16[0] = UTF16.getCharCount(c3) + offset2;
        return 31 & c3;
    }

    public static String unescape(String s) {
        StringBuilder buf = new StringBuilder();
        int[] pos = new int[1];
        int i = 0;
        while (i < s.length()) {
            int i2 = i + 1;
            char c = s.charAt(i);
            if (c == '\\') {
                pos[0] = i2;
                int e = unescapeAt(s, pos);
                if (e >= 0) {
                    buf.appendCodePoint(e);
                    i2 = pos[0];
                } else {
                    throw new IllegalArgumentException("Invalid escape sequence " + s.substring(i2 - 1, Math.min(i2 + 8, s.length())));
                }
            } else {
                buf.append(c);
            }
            i = i2;
        }
        return buf.toString();
    }

    public static String unescapeLeniently(String s) {
        StringBuilder buf = new StringBuilder();
        int[] pos = new int[1];
        int i = 0;
        while (i < s.length()) {
            int i2 = i + 1;
            char c = s.charAt(i);
            if (c == '\\') {
                pos[0] = i2;
                int e = unescapeAt(s, pos);
                if (e < 0) {
                    buf.append(c);
                } else {
                    buf.appendCodePoint(e);
                    i2 = pos[0];
                }
            } else {
                buf.append(c);
            }
            i = i2;
        }
        return buf.toString();
    }

    public static String hex(long ch) {
        return hex(ch, 4);
    }

    public static String hex(long i, int places) {
        if (i == Long.MIN_VALUE) {
            return "-8000000000000000";
        }
        boolean negative = i < 0;
        if (negative) {
            i = -i;
        }
        String result = Long.toString(i, 16).toUpperCase(Locale.ENGLISH);
        if (result.length() < places) {
            result = "0000000000000000".substring(result.length(), places) + result;
        }
        if (!negative) {
            return result;
        }
        return '-' + result;
    }

    public static String hex(CharSequence s) {
        return ((StringBuilder) hex(s, 4, ",", true, new StringBuilder())).toString();
    }

    public static <S extends CharSequence, U extends CharSequence, T extends Appendable> T hex(S s, int width, U separator, boolean useCodePoints, T result) {
        int i = 0;
        if (useCodePoints) {
            while (i < s.length()) {
                try {
                    int cp = Character.codePointAt(s, i);
                    if (i != 0) {
                        result.append(separator);
                    }
                    result.append(hex((long) cp, width));
                    i += UTF16.getCharCount(cp);
                } catch (IOException e) {
                    throw new IllegalIcuArgumentException((Throwable) e);
                }
            }
        } else {
            while (i < s.length()) {
                if (i != 0) {
                    result.append(separator);
                }
                result.append(hex((long) s.charAt(i), width));
                i++;
            }
        }
        return result;
    }

    public static String hex(byte[] o, int start, int end, String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (i != 0) {
                result.append(separator);
            }
            result.append(hex((long) o[i]));
        }
        return result.toString();
    }

    public static <S extends CharSequence> String hex(S s, int width, S separator) {
        return ((StringBuilder) hex(s, width, separator, true, new StringBuilder())).toString();
    }

    public static void split(String s, char divider, String[] output) {
        int last = 0;
        int current = 0;
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == divider) {
                output[current] = s.substring(last, i);
                last = i + 1;
                current++;
            }
            i++;
        }
        output[current] = s.substring(last, i);
        for (int current2 = current + 1; current2 < output.length; current2++) {
            output[current2] = "";
        }
    }

    public static String[] split(String s, char divider) {
        int last = 0;
        ArrayList<String> output = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == divider) {
                output.add(s.substring(last, i));
                last = i + 1;
            }
            i++;
        }
        output.add(s.substring(last, i));
        return (String[]) output.toArray(new String[output.size()]);
    }

    public static int lookup(String source, String[] target) {
        for (int i = 0; i < target.length; i++) {
            if (source.equals(target[i])) {
                return i;
            }
        }
        return -1;
    }

    public static boolean parseChar(String id, int[] pos, char ch) {
        int start = pos[0];
        pos[0] = PatternProps.skipWhiteSpace(id, pos[0]);
        if (pos[0] == id.length() || id.charAt(pos[0]) != ch) {
            pos[0] = start;
            return false;
        }
        pos[0] = pos[0] + 1;
        return true;
    }

    public static int parsePattern(String rule, int pos, int limit, String pattern, int[] parsedInts) {
        int[] p = new int[1];
        int intCount = 0;
        int pos2 = pos;
        for (int i = 0; i < pattern.length(); i++) {
            char cpat = pattern.charAt(i);
            if (cpat != ' ') {
                if (cpat == '#') {
                    p[0] = pos2;
                    int intCount2 = intCount + 1;
                    parsedInts[intCount] = parseInteger(rule, p, limit);
                    if (p[0] == pos2) {
                        return -1;
                    }
                    pos2 = p[0];
                    intCount = intCount2;
                } else if (cpat != '~') {
                    if (pos2 >= limit) {
                        return -1;
                    }
                    int pos3 = pos2 + 1;
                    if (((char) UCharacter.toLowerCase(rule.charAt(pos2))) != cpat) {
                        return -1;
                    }
                    pos2 = pos3;
                }
            } else if (pos2 >= limit) {
                return -1;
            } else {
                int pos4 = pos2 + 1;
                if (!PatternProps.isWhiteSpace(rule.charAt(pos2))) {
                    return -1;
                }
                pos2 = pos4;
            }
            pos2 = PatternProps.skipWhiteSpace(rule, pos2);
        }
        return pos2;
    }

    public static int parsePattern(String pat, Replaceable text, int index, int limit) {
        int ipat = 0;
        if (0 == pat.length()) {
            return index;
        }
        int cpat = Character.codePointAt(pat, 0);
        while (index < limit) {
            int c = text.char32At(index);
            if (cpat == 126) {
                if (PatternProps.isWhiteSpace(c)) {
                    index += UTF16.getCharCount(c);
                } else {
                    ipat++;
                    if (ipat == pat.length()) {
                        return index;
                    }
                }
            } else if (c != cpat) {
                return -1;
            } else {
                int n = UTF16.getCharCount(c);
                index += n;
                ipat += n;
                if (ipat == pat.length()) {
                    return index;
                }
            }
            cpat = UTF16.charAt(pat, ipat);
        }
        return -1;
    }

    public static int parseInteger(String rule, int[] pos, int limit) {
        int count = 0;
        int value = 0;
        int p = pos[0];
        int radix = 10;
        if (rule.regionMatches(true, p, "0x", 0, 2)) {
            p += 2;
            radix = 16;
        } else if (p < limit && rule.charAt(p) == '0') {
            p++;
            count = 1;
            radix = 8;
        }
        while (true) {
            if (p >= limit) {
                break;
            }
            int p2 = p + 1;
            int d = UCharacter.digit(rule.charAt(p), radix);
            if (d < 0) {
                p = p2 - 1;
                break;
            }
            count++;
            int v = (value * radix) + d;
            if (v <= value) {
                return 0;
            }
            value = v;
            p = p2;
        }
        if (count > 0) {
            pos[0] = p;
        }
        return value;
    }

    public static String parseUnicodeIdentifier(String str, int[] pos) {
        StringBuilder buf = new StringBuilder();
        int p = pos[0];
        while (p < str.length()) {
            int ch = Character.codePointAt(str, p);
            if (buf.length() != 0) {
                if (!UCharacter.isUnicodeIdentifierPart(ch)) {
                    break;
                }
                buf.appendCodePoint(ch);
            } else if (!UCharacter.isUnicodeIdentifierStart(ch)) {
                return null;
            } else {
                buf.appendCodePoint(ch);
            }
            p += UTF16.getCharCount(ch);
        }
        pos[0] = p;
        return buf.toString();
    }

    private static <T extends Appendable> void recursiveAppendNumber(T result, int n, int radix, int minDigits) {
        try {
            int digit = n % radix;
            if (n >= radix || minDigits > 1) {
                recursiveAppendNumber(result, n / radix, radix, minDigits - 1);
            }
            result.append(DIGITS[digit]);
        } catch (IOException e) {
            throw new IllegalIcuArgumentException((Throwable) e);
        }
    }

    public static <T extends Appendable> T appendNumber(T result, int n, int radix, int minDigits) {
        if (radix < 2 || radix > 36) {
            throw new IllegalArgumentException("Illegal radix " + radix);
        }
        int abs = n;
        if (n < 0) {
            abs = -n;
            try {
                result.append(LanguageTag.SEP);
            } catch (IOException e) {
                throw new IllegalIcuArgumentException((Throwable) e);
            }
        }
        recursiveAppendNumber(result, abs, radix, minDigits);
        return result;
    }

    public static int parseNumber(String text, int[] pos, int radix) {
        int n = 0;
        int p = pos[0];
        while (p < text.length()) {
            int d = UCharacter.digit(Character.codePointAt(text, p), radix);
            if (d < 0) {
                break;
            }
            n = (radix * n) + d;
            if (n < 0) {
                return -1;
            }
            p++;
        }
        if (p == pos[0]) {
            return -1;
        }
        pos[0] = p;
        return n;
    }

    public static boolean isUnprintable(int c) {
        return c < 32 || c > 126;
    }

    public static <T extends Appendable> boolean escapeUnprintable(T result, int c) {
        try {
            if (!isUnprintable(c)) {
                return false;
            }
            result.append('\\');
            if ((-65536 & c) != 0) {
                result.append('U');
                result.append(DIGITS[(c >> 28) & 15]);
                result.append(DIGITS[(c >> 24) & 15]);
                result.append(DIGITS[(c >> 20) & 15]);
                result.append(DIGITS[(c >> 16) & 15]);
            } else {
                result.append('u');
            }
            result.append(DIGITS[(c >> 12) & 15]);
            result.append(DIGITS[(c >> 8) & 15]);
            result.append(DIGITS[(c >> 4) & 15]);
            result.append(DIGITS[15 & c]);
            return true;
        } catch (IOException e) {
            throw new IllegalIcuArgumentException((Throwable) e);
        }
    }

    public static int quotedIndexOf(String text, int start, int limit, String setOfChars) {
        int i = start;
        while (i < limit) {
            char c = text.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '\'') {
                do {
                    i++;
                    if (i >= limit) {
                        break;
                    }
                } while (text.charAt(i) != '\'');
            } else if (setOfChars.indexOf(c) >= 0) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static void appendToRule(StringBuffer rule, int c, boolean isLiteral, boolean escapeUnprintable, StringBuffer quoteBuf) {
        if (isLiteral || (escapeUnprintable && isUnprintable(c))) {
            if (quoteBuf.length() > 0) {
                while (quoteBuf.length() >= 2 && quoteBuf.charAt(0) == '\'' && quoteBuf.charAt(1) == '\'') {
                    rule.append('\\');
                    rule.append('\'');
                    quoteBuf.delete(0, 2);
                }
                int trailingCount = 0;
                while (quoteBuf.length() >= 2 && quoteBuf.charAt(quoteBuf.length() - 2) == '\'' && quoteBuf.charAt(quoteBuf.length() - 1) == '\'') {
                    quoteBuf.setLength(quoteBuf.length() - 2);
                    trailingCount++;
                }
                if (quoteBuf.length() > 0) {
                    rule.append('\'');
                    rule.append(quoteBuf);
                    rule.append('\'');
                    quoteBuf.setLength(0);
                }
                while (true) {
                    int trailingCount2 = trailingCount - 1;
                    if (trailingCount <= 0) {
                        break;
                    }
                    rule.append('\\');
                    rule.append('\'');
                    trailingCount = trailingCount2;
                }
            }
            if (c == -1) {
                return;
            }
            if (c == 32) {
                if (rule.length() > 0 && rule.charAt(len - 1) != ' ') {
                    rule.append(' ');
                }
            } else if (!escapeUnprintable || !escapeUnprintable(rule, c)) {
                rule.appendCodePoint(c);
            }
        } else if (quoteBuf.length() == 0 && (c == 39 || c == 92)) {
            rule.append('\\');
            rule.append((char) c);
        } else if (quoteBuf.length() > 0 || ((c >= 33 && c <= 126 && ((c < 48 || c > 57) && ((c < 65 || c > 90) && (c < 97 || c > 122)))) || PatternProps.isWhiteSpace(c))) {
            quoteBuf.appendCodePoint(c);
            if (c == 39) {
                quoteBuf.append((char) c);
            }
        } else {
            rule.appendCodePoint(c);
        }
    }

    public static void appendToRule(StringBuffer rule, String text, boolean isLiteral, boolean escapeUnprintable, StringBuffer quoteBuf) {
        for (int i = 0; i < text.length(); i++) {
            appendToRule(rule, (int) text.charAt(i), isLiteral, escapeUnprintable, quoteBuf);
        }
    }

    public static void appendToRule(StringBuffer rule, UnicodeMatcher matcher, boolean escapeUnprintable, StringBuffer quoteBuf) {
        if (matcher != null) {
            appendToRule(rule, matcher.toPattern(escapeUnprintable), true, escapeUnprintable, quoteBuf);
        }
    }

    public static final int compareUnsigned(int source, int target) {
        int source2 = source - Integer.MIN_VALUE;
        int target2 = target - Integer.MIN_VALUE;
        if (source2 < target2) {
            return -1;
        }
        if (source2 > target2) {
            return 1;
        }
        return 0;
    }

    public static final byte highBit(int n) {
        if (n <= 0) {
            return -1;
        }
        byte bit = 0;
        if (n >= 65536) {
            n >>= 16;
            bit = (byte) (0 + 16);
        }
        if (n >= 256) {
            n >>= 8;
            bit = (byte) (bit + 8);
        }
        if (n >= 16) {
            n >>= 4;
            bit = (byte) (bit + 4);
        }
        if (n >= 4) {
            n >>= 2;
            bit = (byte) (bit + 2);
        }
        if (n >= 2) {
            int n2 = n >> 1;
            bit = (byte) (bit + 1);
        }
        return bit;
    }

    public static String valueOf(int[] source) {
        StringBuilder result = new StringBuilder(source.length);
        for (int appendCodePoint : source) {
            result.appendCodePoint(appendCodePoint);
        }
        return result.toString();
    }

    public static String repeat(String s, int count) {
        if (count <= 0) {
            return "";
        }
        if (count == 1) {
            return s;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(s);
        }
        return result.toString();
    }

    public static String[] splitString(String src, String target) {
        return src.split("\\Q" + target + "\\E");
    }

    public static String[] splitWhitespace(String src) {
        return src.split("\\s+");
    }

    public static String fromHex(String string, int minLength, String separator) {
        return fromHex(string, minLength, Pattern.compile(separator != null ? separator : "\\s+"));
    }

    public static String fromHex(String string, int minLength, Pattern separator) {
        StringBuilder buffer = new StringBuilder();
        String[] parts = separator.split(string);
        int length = parts.length;
        int i = 0;
        while (i < length) {
            String part = parts[i];
            if (part.length() >= minLength) {
                buffer.appendCodePoint(Integer.parseInt(part, 16));
                i++;
            } else {
                throw new IllegalArgumentException("code point too short: " + part);
            }
        }
        return buffer.toString();
    }

    public static boolean equals(Object a, Object b) {
        return a == b || !(a == null || b == null || !a.equals(b));
    }

    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }

    public static int hashCode(Object o) {
        if (o == null) {
            return 0;
        }
        return o.hashCode();
    }

    public static String toString(Object o) {
        return o == null ? "null" : o.toString();
    }
}
