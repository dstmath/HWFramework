package android.icu.impl;

import android.icu.impl.locale.LanguageTag;
import android.icu.lang.UCharacter;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.Replaceable;
import android.icu.text.UTF16;
import android.icu.text.UnicodeMatcher;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Utility {
    private static final char APOSTROPHE = '\'';
    private static final char BACKSLASH = '\\';
    static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char ESCAPE = 'ꖥ';
    static final byte ESCAPE_BYTE = (byte) -91;
    static final char[] HEX_DIGIT = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int MAGIC_UNSIGNED = Integer.MIN_VALUE;
    private static final char[] UNESCAPE_MAP = new char[]{'a', 7, 'b', 8, 'e', 27, 'f', 12, 'n', 10, 'r', 13, 't', 9, 'v', 11};

    public static final boolean arrayEquals(Object[] source, Object target) {
        boolean z = false;
        if (source == null) {
            if (target == null) {
                z = true;
            }
            return z;
        } else if (!(target instanceof Object[])) {
            return false;
        } else {
            Object[] targ = (Object[]) target;
            if (source.length == targ.length) {
                z = arrayRegionMatches(source, 0, targ, 0, source.length);
            }
            return z;
        }
    }

    public static final boolean arrayEquals(int[] source, Object target) {
        boolean z = false;
        if (source == null) {
            if (target == null) {
                z = true;
            }
            return z;
        } else if (!(target instanceof int[])) {
            return false;
        } else {
            int[] targ = (int[]) target;
            if (source.length == targ.length) {
                z = arrayRegionMatches(source, 0, targ, 0, source.length);
            }
            return z;
        }
    }

    public static final boolean arrayEquals(double[] source, Object target) {
        boolean z = false;
        if (source == null) {
            if (target == null) {
                z = true;
            }
            return z;
        } else if (!(target instanceof double[])) {
            return false;
        } else {
            double[] targ = (double[]) target;
            if (source.length == targ.length) {
                z = arrayRegionMatches(source, 0, targ, 0, source.length);
            }
            return z;
        }
    }

    public static final boolean arrayEquals(byte[] source, Object target) {
        boolean z = false;
        if (source == null) {
            if (target == null) {
                z = true;
            }
            return z;
        } else if (!(target instanceof byte[])) {
            return false;
        } else {
            byte[] targ = (byte[]) target;
            if (source.length == targ.length) {
                z = arrayRegionMatches(source, 0, targ, 0, source.length);
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
        } else if (b != null) {
            return a.equals(b);
        } else {
            return false;
        }
    }

    public static <T extends Comparable<T>> int checkCompare(T a, T b) {
        return a == null ? b == null ? 0 : -1 : b == null ? 1 : a.compareTo(b);
    }

    public static int checkHash(Object a) {
        return a == null ? 0 : a.hashCode();
    }

    public static final String arrayToRLEString(int[] a) {
        Appendable buffer = new StringBuilder();
        appendInt(buffer, a.length);
        int runValue = a[0];
        int runLength = 1;
        for (int i = 1; i < a.length; i++) {
            int s = a[i];
            if (s != runValue || runLength >= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
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
        Appendable buffer = new StringBuilder();
        buffer.append((char) (a.length >> 16));
        buffer.append((char) a.length);
        short runValue = a[0];
        int runLength = 1;
        for (int i = 1; i < a.length; i++) {
            short s = a[i];
            if (s != runValue || runLength >= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
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
        Appendable buffer = new StringBuilder();
        buffer.append((char) (a.length >> 16));
        buffer.append((char) a.length);
        char runValue = a[0];
        int runLength = 1;
        for (int i = 1; i < a.length; i++) {
            char s = a[i];
            if (s != runValue || runLength >= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
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
        if (state[0] != (byte) 0) {
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
            buffer.append((char) (DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH & value));
        } catch (Throwable e) {
            throw new IllegalIcuArgumentException(e);
        }
    }

    private static final <T extends Appendable> void encodeRun(T buffer, short value, int length) {
        char valueChar = (char) value;
        if (length < 4) {
            for (int j = 0; j < length; j++) {
                if (valueChar == ESCAPE) {
                    try {
                        buffer.append(ESCAPE);
                    } catch (Throwable e) {
                        throw new IllegalIcuArgumentException(e);
                    }
                }
                buffer.append(valueChar);
            }
            return;
        }
        if (length == 42405) {
            if (valueChar == ESCAPE) {
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
                if (value == ESCAPE_BYTE) {
                    appendEncodedByte(buffer, ESCAPE_BYTE, state);
                }
                appendEncodedByte(buffer, value, state);
            }
            return;
        }
        if (((byte) length) == ESCAPE_BYTE) {
            if (value == ESCAPE_BYTE) {
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
            if (state[0] != (byte) 0) {
                buffer.append((char) ((state[1] << 8) | (value & 255)));
                state[0] = (byte) 0;
                return;
            }
            state[0] = (byte) 1;
            state[1] = value;
        } catch (Throwable e) {
            throw new IllegalIcuArgumentException(e);
        }
    }

    public static final int[] RLEStringToIntArray(String s) {
        int length = getInt(s, 0);
        int[] array = new int[length];
        int maxI = s.length() / 2;
        int i = 1;
        int ai = 0;
        while (ai < length && i < maxI) {
            int ai2;
            int i2 = i + 1;
            int c = getInt(s, i);
            if (c == 42405) {
                i = i2 + 1;
                c = getInt(s, i2);
                if (c == 42405) {
                    ai2 = ai + 1;
                    array[ai] = c;
                    i2 = i;
                } else {
                    int runLength = c;
                    i2 = i + 1;
                    int runValue = getInt(s, i);
                    int j = 0;
                    while (j < c) {
                        ai2 = ai + 1;
                        array[ai] = runValue;
                        j++;
                        ai = ai2;
                    }
                    ai2 = ai;
                }
            } else {
                ai2 = ai + 1;
                array[ai] = c;
            }
            i = i2;
            ai = ai2;
        }
        if (ai == length && i == maxI) {
            return array;
        }
        throw new IllegalStateException("Bad run-length encoded int array");
    }

    static final int getInt(String s, int i) {
        return (s.charAt(i * 2) << 16) | s.charAt((i * 2) + 1);
    }

    public static final short[] RLEStringToShortArray(String s) {
        int length = (s.charAt(0) << 16) | s.charAt(1);
        short[] array = new short[length];
        int ai = 0;
        int i = 2;
        while (i < s.length()) {
            int ai2;
            char c = s.charAt(i);
            if (c == ESCAPE) {
                i++;
                c = s.charAt(i);
                if (c == ESCAPE) {
                    ai2 = ai + 1;
                    array[ai] = (short) c;
                } else {
                    char runLength = c;
                    i++;
                    short runValue = (short) s.charAt(i);
                    char j = 0;
                    ai2 = ai;
                    while (j < runLength) {
                        ai = ai2 + 1;
                        array[ai2] = runValue;
                        j++;
                        ai2 = ai;
                    }
                }
            } else {
                ai2 = ai + 1;
                array[ai] = (short) c;
            }
            ai = ai2;
            i++;
        }
        if (ai == length) {
            return array;
        }
        throw new IllegalStateException("Bad run-length encoded short array");
    }

    public static final char[] RLEStringToCharArray(String s) {
        int length = (s.charAt(0) << 16) | s.charAt(1);
        char[] array = new char[length];
        int ai = 0;
        int i = 2;
        while (i < s.length()) {
            int ai2;
            char c = s.charAt(i);
            if (c == ESCAPE) {
                i++;
                c = s.charAt(i);
                if (c == ESCAPE) {
                    ai2 = ai + 1;
                    array[ai] = c;
                } else {
                    char runLength = c;
                    i++;
                    char runValue = s.charAt(i);
                    char j = 0;
                    ai2 = ai;
                    while (j < runLength) {
                        ai = ai2 + 1;
                        array[ai2] = runValue;
                        j++;
                        ai2 = ai;
                    }
                }
            } else {
                ai2 = ai + 1;
                array[ai] = c;
            }
            ai = ai2;
            i++;
        }
        if (ai == length) {
            return array;
        }
        throw new IllegalStateException("Bad run-length encoded short array");
    }

    public static final byte[] RLEStringToByteArray(String s) {
        int length = (s.charAt(0) << 16) | s.charAt(1);
        byte[] array = new byte[length];
        boolean nextChar = true;
        int c = 0;
        int node = 0;
        int runLength = 0;
        int ai = 0;
        int i = 2;
        while (ai < length) {
            int i2;
            byte b;
            int ai2;
            if (nextChar) {
                i2 = i + 1;
                c = s.charAt(i);
                b = (byte) (c >> 8);
                nextChar = false;
            } else {
                b = (byte) (c & 255);
                nextChar = true;
                i2 = i;
            }
            switch (node) {
                case 0:
                    if (b != ESCAPE_BYTE) {
                        ai2 = ai + 1;
                        array[ai] = b;
                        break;
                    }
                    node = 1;
                    ai2 = ai;
                    break;
                case 1:
                    if (b != ESCAPE_BYTE) {
                        runLength = b;
                        if (runLength < (byte) 0) {
                            runLength += 256;
                        }
                        node = 2;
                        ai2 = ai;
                        break;
                    }
                    ai2 = ai + 1;
                    array[ai] = ESCAPE_BYTE;
                    node = 0;
                    break;
                case 2:
                    int j = 0;
                    while (j < runLength) {
                        ai2 = ai + 1;
                        array[ai] = b;
                        j++;
                        ai = ai2;
                    }
                    node = 0;
                    ai2 = ai;
                    break;
                default:
                    ai2 = ai;
                    break;
            }
            ai = ai2;
            i = i2;
        }
        if (node != 0) {
            throw new IllegalStateException("Bad run-length encoded byte array");
        } else if (i == s.length()) {
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
                buffer.append('+').append(LINE_SEPARATOR);
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
                        buffer.append(HEX_DIGIT[(c & 56) >> 3]);
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
                    buffer.append(HEX_DIGIT[(c & 56) >> 3]);
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
                boolean four = c <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
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
        int dig;
        char c = Character.codePointAt(s, offset);
        offset += UTF16.getCharCount(c);
        switch (c) {
            case 'U':
                maxDig = 8;
                minDig = 8;
                break;
            case 'u':
                maxDig = 4;
                minDig = 4;
                break;
            case 'x':
                minDig = 1;
                if (offset < length && UTF16.charAt(s, offset) == 123) {
                    offset++;
                    braces = true;
                    maxDig = 8;
                    break;
                }
                maxDig = 2;
                break;
                break;
            default:
                dig = UCharacter.digit(c, 8);
                if (dig >= 0) {
                    minDig = 1;
                    maxDig = 3;
                    n = 1;
                    bitsPerDigit = 3;
                    result = dig;
                    break;
                }
                break;
        }
        int c2;
        if (minDig != 0) {
            while (offset < length && n < maxDig) {
                c2 = UTF16.charAt(s, offset);
                dig = UCharacter.digit(c2, bitsPerDigit == 3 ? 8 : 16);
                if (dig >= 0) {
                    result = (result << bitsPerDigit) | dig;
                    offset += UTF16.getCharCount(c2);
                    n++;
                }
            }
            if (n < minDig) {
                return -1;
            }
            if (braces) {
                if (c2 != 125) {
                    return -1;
                }
                offset++;
            }
            if (result < 0 || result >= 1114112) {
                return -1;
            }
            if (offset < length && UTF16.isLeadSurrogate((char) result)) {
                int ahead = offset + 1;
                c2 = s.charAt(offset);
                if (c2 == 92 && ahead < length) {
                    int[] o = new int[]{ahead};
                    c2 = unescapeAt(s, o);
                    ahead = o[0];
                }
                if (UTF16.isTrailSurrogate((char) c2)) {
                    offset = ahead;
                    result = Character.toCodePoint((char) result, (char) c2);
                }
            }
            offset16[0] = offset;
            return result;
        }
        int i = 0;
        while (i < UNESCAPE_MAP.length) {
            if (c == UNESCAPE_MAP[i]) {
                offset16[0] = offset;
                return UNESCAPE_MAP[i + 1];
            } else if (c >= UNESCAPE_MAP[i]) {
                i += 2;
            } else if (c == 'c' || offset >= length) {
                offset16[0] = offset;
                return c;
            } else {
                c2 = UTF16.charAt(s, offset);
                offset16[0] = UTF16.getCharCount(c2) + offset;
                return c2 & 31;
            }
        }
        if (c == 'c') {
        }
        offset16[0] = offset;
        return c;
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
                if (e < 0) {
                    throw new IllegalArgumentException("Invalid escape sequence " + s.substring(i2 - 1, Math.min(i2 + 8, s.length())));
                }
                buf.appendCodePoint(e);
                i = pos[0];
            } else {
                buf.append(c);
                i = i2;
            }
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
                    i = i2;
                } else {
                    buf.appendCodePoint(e);
                    i = pos[0];
                }
            } else {
                buf.append(c);
                i = i2;
            }
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
        if (negative) {
            return '-' + result;
        }
        return result;
    }

    public static String hex(CharSequence s) {
        return ((StringBuilder) hex(s, 4, ",", true, new StringBuilder())).toString();
    }

    public static <S extends CharSequence, U extends CharSequence, T extends Appendable> T hex(S s, int width, U separator, boolean useCodePoints, T result) {
        int i;
        if (useCodePoints) {
            i = 0;
            while (i < s.length()) {
                try {
                    int cp = Character.codePointAt(s, i);
                    if (i != 0) {
                        result.append(separator);
                    }
                    result.append(hex((long) cp, width));
                    i += UTF16.getCharCount(cp);
                } catch (Throwable e) {
                    throw new IllegalIcuArgumentException(e);
                }
            }
        }
        for (i = 0; i < s.length(); i++) {
            if (i != 0) {
                result.append(separator);
            }
            result.append(hex((long) s.charAt(i), width));
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
        int current;
        int last = 0;
        int current2 = 0;
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == divider) {
                current = current2 + 1;
                output[current2] = s.substring(last, i);
                last = i + 1;
                current2 = current;
            }
            i++;
        }
        current = current2 + 1;
        output[current2] = s.substring(last, i);
        while (true) {
            current2 = current;
            if (current2 < output.length) {
                current = current2 + 1;
                output[current2] = "";
            } else {
                return;
            }
        }
    }

    public static String[] split(String s, char divider) {
        int last = 0;
        ArrayList<String> output = new ArrayList();
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
        for (int i = 0; i < pattern.length(); i++) {
            char cpat = pattern.charAt(i);
            int pos2;
            switch (cpat) {
                case ' ':
                    if (pos < limit) {
                        pos2 = pos + 1;
                        if (PatternProps.isWhiteSpace(rule.charAt(pos))) {
                            pos = pos2;
                            break;
                        }
                        return -1;
                    }
                    return -1;
                case '#':
                    p[0] = pos;
                    int intCount2 = intCount + 1;
                    parsedInts[intCount] = parseInteger(rule, p, limit);
                    if (p[0] == pos) {
                        return -1;
                    }
                    pos = p[0];
                    intCount = intCount2;
                    continue;
                case '~':
                    break;
                default:
                    if (pos >= limit) {
                        return -1;
                    }
                    pos2 = pos + 1;
                    if (((char) UCharacter.toLowerCase(rule.charAt(pos))) != cpat) {
                        return -1;
                    }
                    pos = pos2;
                    continue;
            }
            pos = PatternProps.skipWhiteSpace(rule, pos);
        }
        return pos;
    }

    public static int parsePattern(String pat, Replaceable text, int index, int limit) {
        int ipat = 0;
        if (pat.length() == 0) {
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
        int p;
        int count = 0;
        int value = 0;
        int p2 = pos[0];
        int radix = 10;
        if (rule.regionMatches(true, p2, "0x", 0, 2)) {
            radix = 16;
            p = p2 + 2;
        } else if (p2 >= limit || rule.charAt(p2) != '0') {
            p = p2;
        } else {
            count = 1;
            radix = 8;
            p = p2 + 1;
        }
        while (p < limit) {
            p2 = p + 1;
            int d = UCharacter.digit(rule.charAt(p), radix);
            if (d < 0) {
                p2--;
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
        p2 = p;
        if (count > 0) {
            pos[0] = p2;
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
        } catch (Throwable e) {
            throw new IllegalIcuArgumentException(e);
        }
    }

    public static <T extends Appendable> T appendNumber(T result, int n, int radix, int minDigits) {
        if (radix < 2 || radix > 36) {
            try {
                throw new IllegalArgumentException("Illegal radix " + radix);
            } catch (Throwable e) {
                throw new IllegalIcuArgumentException(e);
            }
        }
        int abs = n;
        if (n < 0) {
            abs = -n;
            result.append(LanguageTag.SEP);
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
            result.append(DIGITS[c & 15]);
            return true;
        } catch (Throwable e) {
            throw new IllegalIcuArgumentException(e);
        }
    }

    public static int quotedIndexOf(String text, int start, int limit, String setOfChars) {
        int i = start;
        while (i < limit) {
            char c = text.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '\'') {
                while (true) {
                    i++;
                    if (i >= limit || text.charAt(i) == '\'') {
                        break;
                    }
                }
            } else if (setOfChars.indexOf(c) >= 0) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0078  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void appendToRule(StringBuffer rule, int c, boolean isLiteral, boolean escapeUnprintable, StringBuffer quoteBuf) {
        if (isLiteral || (escapeUnprintable && isUnprintable(c))) {
            if (quoteBuf.length() > 0) {
                int trailingCount;
                while (quoteBuf.length() >= 2 && quoteBuf.charAt(0) == '\'' && quoteBuf.charAt(1) == '\'') {
                    rule.append('\\').append('\'');
                    quoteBuf.delete(0, 2);
                }
                int trailingCount2 = 0;
                while (quoteBuf.length() >= 2 && quoteBuf.charAt(quoteBuf.length() - 2) == '\'' && quoteBuf.charAt(quoteBuf.length() - 1) == '\'') {
                    quoteBuf.setLength(quoteBuf.length() - 2);
                    trailingCount2++;
                }
                if (quoteBuf.length() > 0) {
                    rule.append('\'');
                    rule.append(quoteBuf);
                    rule.append('\'');
                    quoteBuf.setLength(0);
                    trailingCount = trailingCount2;
                    trailingCount2 = trailingCount - 1;
                    if (trailingCount > 0) {
                        rule.append('\\').append('\'');
                    }
                }
                trailingCount = trailingCount2;
                trailingCount2 = trailingCount - 1;
                if (trailingCount > 0) {
                }
            }
            if (c == -1) {
                return;
            }
            if (c == 32) {
                int len = rule.length();
                if (len > 0 && rule.charAt(len - 1) != ' ') {
                    rule.append(' ');
                }
            } else if (!escapeUnprintable || (escapeUnprintable(rule, c) ^ 1) != 0) {
                rule.appendCodePoint(c);
            }
        } else if (quoteBuf.length() == 0 && (c == 39 || c == 92)) {
            rule.append('\\').append((char) c);
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
            appendToRule(rule, text.charAt(i), isLiteral, escapeUnprintable, quoteBuf);
        }
    }

    public static void appendToRule(StringBuffer rule, UnicodeMatcher matcher, boolean escapeUnprintable, StringBuffer quoteBuf) {
        if (matcher != null) {
            appendToRule(rule, matcher.toPattern(escapeUnprintable), true, escapeUnprintable, quoteBuf);
        }
    }

    public static final int compareUnsigned(int source, int target) {
        source -= Integer.MIN_VALUE;
        target -= Integer.MIN_VALUE;
        if (source < target) {
            return -1;
        }
        if (source > target) {
            return 1;
        }
        return 0;
    }

    public static final byte highBit(int n) {
        if (n <= 0) {
            return (byte) -1;
        }
        byte bit = (byte) 0;
        if (n >= 65536) {
            n >>= 16;
            bit = (byte) 16;
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
            n >>= 1;
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
        if (separator == null) {
            separator = "\\s+";
        }
        return fromHex(string, minLength, Pattern.compile(separator));
    }

    public static String fromHex(String string, int minLength, Pattern separator) {
        StringBuilder buffer = new StringBuilder();
        for (String part : separator.split(string)) {
            if (part.length() < minLength) {
                throw new IllegalArgumentException("code point too short: " + part);
            }
            buffer.appendCodePoint(Integer.parseInt(part, 16));
        }
        return buffer.toString();
    }
}
