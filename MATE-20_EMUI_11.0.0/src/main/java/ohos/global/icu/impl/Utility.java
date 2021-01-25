package ohos.global.icu.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.global.icu.impl.locale.UnicodeLocaleExtension;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.Replaceable;
import ohos.global.icu.text.UTF16;
import ohos.global.icu.text.UnicodeMatcher;
import ohos.global.icu.util.ICUUncheckedIOException;

public final class Utility {
    private static final char APOSTROPHE = '\'';
    private static final char BACKSLASH = '\\';
    static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char ESCAPE = 42405;
    static final byte ESCAPE_BYTE = -91;
    static final char[] HEX_DIGIT = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int MAGIC_UNSIGNED = Integer.MIN_VALUE;
    private static final char[] UNESCAPE_MAP = {'a', 7, 'b', '\b', 'e', 27, 'f', '\f', 'n', '\n', 'r', CharInfo.S_CARRIAGERETURN, 't', '\t', 'v', 11};

    public static final int compareUnsigned(int i, int i2) {
        int i3 = i - Integer.MIN_VALUE;
        int i4 = i2 - Integer.MIN_VALUE;
        if (i3 < i4) {
            return -1;
        }
        return i3 > i4 ? 1 : 0;
    }

    public static final byte highBit(int i) {
        if (i <= 0) {
            return -1;
        }
        byte b = 0;
        if (i >= 65536) {
            i >>= 16;
            b = (byte) 16;
        }
        if (i >= 256) {
            i >>= 8;
            b = (byte) (b + 8);
        }
        if (i >= 16) {
            i >>= 4;
            b = (byte) (b + 4);
        }
        if (i >= 4) {
            i >>= 2;
            b = (byte) (b + 2);
        }
        return i >= 2 ? (byte) (b + 1) : b;
    }

    public static boolean isUnprintable(int i) {
        return i < 32 || i > 126;
    }

    public static final boolean sameObjects(Object obj, Object obj2) {
        return obj == obj2;
    }

    public static final boolean arrayEquals(Object[] objArr, Object obj) {
        if (objArr == null) {
            return obj == null;
        }
        if (!(obj instanceof Object[])) {
            return false;
        }
        Object[] objArr2 = (Object[]) obj;
        return objArr.length == objArr2.length && arrayRegionMatches(objArr, 0, objArr2, 0, objArr.length);
    }

    public static final boolean arrayEquals(int[] iArr, Object obj) {
        if (iArr == null) {
            return obj == null;
        }
        if (!(obj instanceof int[])) {
            return false;
        }
        int[] iArr2 = (int[]) obj;
        return iArr.length == iArr2.length && arrayRegionMatches(iArr, 0, iArr2, 0, iArr.length);
    }

    public static final boolean arrayEquals(double[] dArr, Object obj) {
        if (dArr == null) {
            return obj == null;
        }
        if (!(obj instanceof double[])) {
            return false;
        }
        double[] dArr2 = (double[]) obj;
        return dArr.length == dArr2.length && arrayRegionMatches(dArr, 0, dArr2, 0, dArr.length);
    }

    public static final boolean arrayEquals(byte[] bArr, Object obj) {
        if (bArr == null) {
            return obj == null;
        }
        if (!(obj instanceof byte[])) {
            return false;
        }
        byte[] bArr2 = (byte[]) obj;
        return bArr.length == bArr2.length && arrayRegionMatches(bArr, 0, bArr2, 0, bArr.length);
    }

    public static final boolean arrayEquals(Object obj, Object obj2) {
        if (obj == null) {
            return obj2 == null;
        }
        if (obj instanceof Object[]) {
            return arrayEquals((Object[]) obj, obj2);
        }
        if (obj instanceof int[]) {
            return arrayEquals((int[]) obj, obj2);
        }
        if (obj instanceof double[]) {
            return arrayEquals((double[]) obj, obj2);
        }
        if (obj instanceof byte[]) {
            return arrayEquals((byte[]) obj, obj2);
        }
        return obj.equals(obj2);
    }

    public static final boolean arrayRegionMatches(Object[] objArr, int i, Object[] objArr2, int i2, int i3) {
        int i4 = i3 + i;
        int i5 = i2 - i;
        while (i < i4) {
            if (!arrayEquals(objArr[i], objArr2[i + i5])) {
                return false;
            }
            i++;
        }
        return true;
    }

    public static final boolean arrayRegionMatches(char[] cArr, int i, char[] cArr2, int i2, int i3) {
        int i4 = i3 + i;
        int i5 = i2 - i;
        while (i < i4) {
            if (cArr[i] != cArr2[i + i5]) {
                return false;
            }
            i++;
        }
        return true;
    }

    public static final boolean arrayRegionMatches(int[] iArr, int i, int[] iArr2, int i2, int i3) {
        int i4 = i3 + i;
        int i5 = i2 - i;
        while (i < i4) {
            if (iArr[i] != iArr2[i + i5]) {
                return false;
            }
            i++;
        }
        return true;
    }

    public static final boolean arrayRegionMatches(double[] dArr, int i, double[] dArr2, int i2, int i3) {
        int i4 = i3 + i;
        int i5 = i2 - i;
        while (i < i4) {
            if (dArr[i] != dArr2[i + i5]) {
                return false;
            }
            i++;
        }
        return true;
    }

    public static final boolean arrayRegionMatches(byte[] bArr, int i, byte[] bArr2, int i2, int i3) {
        int i4 = i3 + i;
        int i5 = i2 - i;
        while (i < i4) {
            if (bArr[i] != bArr2[i + i5]) {
                return false;
            }
            i++;
        }
        return true;
    }

    public static <T extends Comparable<T>> int checkCompare(T t, T t2) {
        if (t == null) {
            return t2 == null ? 0 : -1;
        }
        if (t2 == null) {
            return 1;
        }
        return t.compareTo(t2);
    }

    public static int checkHash(Object obj) {
        if (obj == null) {
            return 0;
        }
        return obj.hashCode();
    }

    public static final String arrayToRLEString(int[] iArr) {
        StringBuilder sb = new StringBuilder();
        appendInt(sb, iArr.length);
        int i = iArr[0];
        int i2 = 1;
        for (int i3 = 1; i3 < iArr.length; i3++) {
            int i4 = iArr[i3];
            if (i4 != i || i2 >= 65535) {
                encodeRun(sb, i, i2);
                i2 = 1;
                i = i4;
            } else {
                i2++;
            }
        }
        encodeRun(sb, i, i2);
        return sb.toString();
    }

    public static final String arrayToRLEString(short[] sArr) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) (sArr.length >> 16));
        sb.append((char) sArr.length);
        short s = sArr[0];
        int i = 1;
        for (int i2 = 1; i2 < sArr.length; i2++) {
            short s2 = sArr[i2];
            if (s2 != s || i >= 65535) {
                encodeRun(sb, s, i);
                i = 1;
                s = s2;
            } else {
                i++;
            }
        }
        encodeRun(sb, s, i);
        return sb.toString();
    }

    public static final String arrayToRLEString(char[] cArr) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) (cArr.length >> 16));
        sb.append((char) cArr.length);
        char c = cArr[0];
        int i = 1;
        for (int i2 = 1; i2 < cArr.length; i2++) {
            char c2 = cArr[i2];
            if (c2 != c || i >= 65535) {
                encodeRun(sb, (short) c, i);
                i = 1;
                c = c2;
            } else {
                i++;
            }
        }
        encodeRun(sb, (short) c, i);
        return sb.toString();
    }

    public static final String arrayToRLEString(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) (bArr.length >> 16));
        sb.append((char) bArr.length);
        byte[] bArr2 = new byte[2];
        byte b = bArr[0];
        int i = 1;
        for (int i2 = 1; i2 < bArr.length; i2++) {
            byte b2 = bArr[i2];
            if (b2 != b || i >= 255) {
                encodeRun(sb, b, i, bArr2);
                i = 1;
                b = b2;
            } else {
                i++;
            }
        }
        encodeRun(sb, b, i, bArr2);
        if (bArr2[0] != 0) {
            appendEncodedByte(sb, (byte) 0, bArr2);
        }
        return sb.toString();
    }

    private static final <T extends Appendable> void encodeRun(T t, int i, int i2) {
        if (i2 < 4) {
            for (int i3 = 0; i3 < i2; i3++) {
                if (i == 42405) {
                    appendInt(t, i);
                }
                appendInt(t, i);
            }
            return;
        }
        if (i2 == 42405) {
            if (i == 42405) {
                appendInt(t, 42405);
            }
            appendInt(t, i);
            i2--;
        }
        appendInt(t, 42405);
        appendInt(t, i2);
        appendInt(t, i);
    }

    private static final <T extends Appendable> void appendInt(T t, int i) {
        try {
            t.append((char) (i >>> 16));
            t.append((char) (i & 65535));
        } catch (IOException e) {
            throw new IllegalIcuArgumentException(e);
        }
    }

    private static final <T extends Appendable> void encodeRun(T t, short s, int i) {
        char c = (char) s;
        if (i < 4) {
            for (int i2 = 0; i2 < i; i2++) {
                if (c == 42405) {
                    try {
                        t.append(ESCAPE);
                    } catch (IOException e) {
                        throw new IllegalIcuArgumentException(e);
                    }
                }
                t.append(c);
            }
            return;
        }
        if (i == 42405) {
            if (c == 42405) {
                t.append(ESCAPE);
            }
            t.append(c);
            i--;
        }
        t.append(ESCAPE);
        t.append((char) i);
        t.append(c);
    }

    private static final <T extends Appendable> void encodeRun(T t, byte b, int i, byte[] bArr) {
        if (i < 4) {
            for (int i2 = 0; i2 < i; i2++) {
                if (b == -91) {
                    appendEncodedByte(t, ESCAPE_BYTE, bArr);
                }
                appendEncodedByte(t, b, bArr);
            }
            return;
        }
        if (((byte) i) == -91) {
            if (b == -91) {
                appendEncodedByte(t, ESCAPE_BYTE, bArr);
            }
            appendEncodedByte(t, b, bArr);
            i--;
        }
        appendEncodedByte(t, ESCAPE_BYTE, bArr);
        appendEncodedByte(t, (byte) i, bArr);
        appendEncodedByte(t, b, bArr);
    }

    private static final <T extends Appendable> void appendEncodedByte(T t, byte b, byte[] bArr) {
        try {
            if (bArr[0] != 0) {
                t.append((char) ((b & 255) | (bArr[1] << 8)));
                bArr[0] = 0;
                return;
            }
            bArr[0] = 1;
            bArr[1] = b;
        } catch (IOException e) {
            throw new IllegalIcuArgumentException(e);
        }
    }

    public static final int[] RLEStringToIntArray(String str) {
        int i;
        int i2 = getInt(str, 0);
        int[] iArr = new int[i2];
        int length = str.length() / 2;
        int i3 = 1;
        int i4 = 0;
        while (i4 < i2 && i3 < length) {
            int i5 = i3 + 1;
            int i6 = getInt(str, i3);
            if (i6 == 42405) {
                i3 = i5 + 1;
                int i7 = getInt(str, i5);
                if (i7 == 42405) {
                    i = i4 + 1;
                    iArr[i4] = i7;
                } else {
                    int i8 = i3 + 1;
                    int i9 = getInt(str, i3);
                    int i10 = i4;
                    int i11 = 0;
                    while (i11 < i7) {
                        iArr[i10] = i9;
                        i11++;
                        i10++;
                    }
                    i3 = i8;
                    i4 = i10;
                }
            } else {
                i = i4 + 1;
                iArr[i4] = i6;
                i3 = i5;
            }
            i4 = i;
        }
        if (i4 == i2 && i3 == length) {
            return iArr;
        }
        throw new IllegalStateException("Bad run-length encoded int array");
    }

    static final int getInt(String str, int i) {
        int i2 = i * 2;
        return str.charAt(i2 + 1) | (str.charAt(i2) << 16);
    }

    public static final short[] RLEStringToShortArray(String str) {
        int i;
        int charAt = (str.charAt(0) << 16) | str.charAt(1);
        short[] sArr = new short[charAt];
        int i2 = 2;
        int i3 = 0;
        while (i2 < str.length()) {
            char charAt2 = str.charAt(i2);
            if (charAt2 == 42405) {
                i2++;
                char charAt3 = str.charAt(i2);
                if (charAt3 == 42405) {
                    i = i3 + 1;
                    sArr[i3] = (short) charAt3;
                } else {
                    i2++;
                    short charAt4 = (short) str.charAt(i2);
                    int i4 = i3;
                    int i5 = 0;
                    while (i5 < charAt3) {
                        sArr[i4] = charAt4;
                        i5++;
                        i4++;
                    }
                    i3 = i4;
                    i2++;
                }
            } else {
                i = i3 + 1;
                sArr[i3] = (short) charAt2;
            }
            i3 = i;
            i2++;
        }
        if (i3 == charAt) {
            return sArr;
        }
        throw new IllegalStateException("Bad run-length encoded short array");
    }

    public static final char[] RLEStringToCharArray(String str) {
        int i;
        int charAt = (str.charAt(0) << 16) | str.charAt(1);
        char[] cArr = new char[charAt];
        int i2 = 2;
        int i3 = 0;
        while (i2 < str.length()) {
            char charAt2 = str.charAt(i2);
            if (charAt2 == 42405) {
                i2++;
                char charAt3 = str.charAt(i2);
                if (charAt3 == 42405) {
                    i = i3 + 1;
                    cArr[i3] = charAt3;
                } else {
                    i2++;
                    char charAt4 = str.charAt(i2);
                    int i4 = i3;
                    int i5 = 0;
                    while (i5 < charAt3) {
                        cArr[i4] = charAt4;
                        i5++;
                        i4++;
                    }
                    i3 = i4;
                    i2++;
                }
            } else {
                i = i3 + 1;
                cArr[i3] = charAt2;
            }
            i3 = i;
            i2++;
        }
        if (i3 == charAt) {
            return cArr;
        }
        throw new IllegalStateException("Bad run-length encoded short array");
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:31:0x005b */
    public static final byte[] RLEStringToByteArray(String str) {
        char c;
        boolean z;
        byte b;
        int i;
        int charAt = (str.charAt(0) << 16) | str.charAt(1);
        byte[] bArr = new byte[charAt];
        int i2 = 0;
        boolean z2 = false;
        char c2 = 0;
        int i3 = 0;
        boolean z3 = true;
        int i4 = 2;
        while (i2 < charAt) {
            if (z3) {
                int i5 = i4 + 1;
                char charAt2 = str.charAt(i4);
                c = charAt2;
                i4 = i5;
                b = (byte) (charAt2 >> '\b');
                z = false;
            } else {
                c = c2;
                z = true;
                b = (byte) (c2 & 255);
            }
            int i6 = b;
            if (z2) {
                if (!z2) {
                    if (z2) {
                        i = i2;
                        int i7 = 0;
                        while (i7 < i3) {
                            bArr[i] = b;
                            i7++;
                            i++;
                        }
                    }
                } else if (b == -91) {
                    i = i2 + 1;
                    bArr[i2] = ESCAPE_BYTE;
                } else {
                    if (b < 0) {
                        i6 = b + 256;
                    }
                    z2 = true;
                    i3 = i6 == 1 ? 1 : 0;
                }
                i2 = i;
                z2 = false;
            } else if (b == -91) {
                z2 = true;
            } else {
                bArr[i2] = b;
                i2++;
            }
            z3 = z;
            c2 = c;
            z2 = z2;
        }
        if (z2) {
            throw new IllegalStateException("Bad run-length encoded byte array");
        } else if (i4 == str.length()) {
            return bArr;
        } else {
            throw new IllegalStateException("Excess data in RLE byte array string");
        }
    }

    public static final String formatForSource(String str) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            if (i > 0) {
                sb.append('+');
                sb.append(LINE_SEPARATOR);
            }
            sb.append("        \"");
            int i2 = 11;
            while (i < str.length() && i2 < 80) {
                int i3 = i + 1;
                char charAt = str.charAt(i);
                if (charAt < ' ' || charAt == '\"' || charAt == '\\') {
                    if (charAt == '\n') {
                        sb.append("\\n");
                    } else if (charAt == '\t') {
                        sb.append("\\t");
                    } else if (charAt == '\r') {
                        sb.append("\\r");
                    } else {
                        sb.append('\\');
                        sb.append(HEX_DIGIT[(charAt & 448) >> 6]);
                        sb.append(HEX_DIGIT[(charAt & '8') >> 3]);
                        sb.append(HEX_DIGIT[charAt & 7]);
                        i2 += 4;
                    }
                    i2 += 2;
                } else if (charAt <= '~') {
                    sb.append(charAt);
                    i2++;
                } else {
                    sb.append("\\u");
                    sb.append(HEX_DIGIT[(61440 & charAt) >> 12]);
                    sb.append(HEX_DIGIT[(charAt & 3840) >> 8]);
                    sb.append(HEX_DIGIT[(charAt & 240) >> 4]);
                    sb.append(HEX_DIGIT[charAt & 15]);
                    i2 += 6;
                }
                i = i3;
            }
            sb.append('\"');
        }
        return sb.toString();
    }

    public static final String format1ForSource(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        int i = 0;
        while (i < str.length()) {
            int i2 = i + 1;
            char charAt = str.charAt(i);
            if (charAt < ' ' || charAt == '\"' || charAt == '\\') {
                if (charAt == '\n') {
                    sb.append("\\n");
                } else if (charAt == '\t') {
                    sb.append("\\t");
                } else if (charAt == '\r') {
                    sb.append("\\r");
                } else {
                    sb.append('\\');
                    sb.append(HEX_DIGIT[(charAt & 448) >> 6]);
                    sb.append(HEX_DIGIT[(charAt & '8') >> 3]);
                    sb.append(HEX_DIGIT[charAt & 7]);
                }
            } else if (charAt <= '~') {
                sb.append(charAt);
            } else {
                sb.append("\\u");
                sb.append(HEX_DIGIT[(61440 & charAt) >> 12]);
                sb.append(HEX_DIGIT[(charAt & 3840) >> 8]);
                sb.append(HEX_DIGIT[(charAt & 240) >> 4]);
                sb.append(HEX_DIGIT[charAt & 15]);
            }
            i = i2;
        }
        sb.append('\"');
        return sb.toString();
    }

    public static final String escape(String str) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            int codePointAt = Character.codePointAt(str, i);
            i += UTF16.getCharCount(codePointAt);
            if (codePointAt < 32 || codePointAt > 127) {
                boolean z = codePointAt <= 65535;
                sb.append(z ? "\\u" : "\\U");
                sb.append(hex((long) codePointAt, z ? 4 : 8));
            } else if (codePointAt == 92) {
                sb.append("\\\\");
            } else {
                sb.append((char) codePointAt);
            }
        }
        return sb.toString();
    }

    public static int unescapeAt(String str, int[] iArr) {
        boolean z;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6 = iArr[0];
        int length = str.length();
        if (i6 < 0 || i6 >= length) {
            return -1;
        }
        int codePointAt = Character.codePointAt(str, i6);
        int charCount = i6 + UTF16.getCharCount(codePointAt);
        int i7 = 4;
        if (codePointAt == 85) {
            i4 = 0;
            i3 = 0;
            z = false;
            i = 4;
            i7 = 8;
            i2 = 8;
        } else if (codePointAt == 117) {
            i4 = 0;
            i3 = 0;
            z = false;
            i2 = 4;
            i = 4;
        } else if (codePointAt != 120) {
            i4 = UCharacter.digit(codePointAt, 8);
            if (i4 >= 0) {
                z = false;
                i2 = 3;
                i = 3;
                i7 = 1;
                i3 = 1;
            } else {
                i4 = 0;
                i3 = 0;
                i2 = 0;
                z = false;
                i = 4;
                i7 = 0;
            }
        } else if (charCount >= length || UTF16.charAt(str, charCount) != 123) {
            i3 = 0;
            z = false;
            i2 = 2;
            i = 4;
            i7 = 1;
            i4 = 0;
        } else {
            charCount++;
            i4 = 0;
            i3 = 0;
            i = 4;
            i2 = 8;
            i7 = 1;
            z = true;
        }
        if (i7 != 0) {
            while (charCount < length && i3 < i2) {
                codePointAt = UTF16.charAt(str, charCount);
                int digit = UCharacter.digit(codePointAt, i == 3 ? 8 : 16);
                if (digit < 0) {
                    break;
                }
                i4 = (i4 << i) | digit;
                charCount += UTF16.getCharCount(codePointAt);
                i3++;
            }
            if (i3 < i7) {
                return -1;
            }
            if (z) {
                if (codePointAt != 125) {
                    return -1;
                }
                charCount++;
            }
            if (i4 < 0 || i4 >= 1114112) {
                return -1;
            }
            if (charCount < length) {
                char c = (char) i4;
                if (UTF16.isLeadSurrogate(c)) {
                    int i8 = charCount + 1;
                    int charAt = str.charAt(charCount);
                    if (charAt != 92 || i8 >= length) {
                        i5 = i8;
                    } else {
                        int[] iArr2 = {i8};
                        charAt = unescapeAt(str, iArr2);
                        i5 = iArr2[0];
                    }
                    char c2 = (char) charAt;
                    if (UTF16.isTrailSurrogate(c2)) {
                        i4 = Character.toCodePoint(c, c2);
                        iArr[0] = i5;
                        return i4;
                    }
                }
            }
            i5 = charCount;
            iArr[0] = i5;
            return i4;
        }
        int i9 = 0;
        while (true) {
            char[] cArr = UNESCAPE_MAP;
            if (i9 >= cArr.length) {
                break;
            } else if (codePointAt == cArr[i9]) {
                iArr[0] = charCount;
                return cArr[i9 + 1];
            } else if (codePointAt < cArr[i9]) {
                break;
            } else {
                i9 += 2;
            }
        }
        if (codePointAt != 99 || charCount >= length) {
            iArr[0] = charCount;
            return codePointAt;
        }
        int charAt2 = UTF16.charAt(str, charCount);
        iArr[0] = charCount + UTF16.getCharCount(charAt2);
        return charAt2 & 31;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001a: APUT  (r1v1 int[]), (0 ??[int, short, byte, char]), (r4v1 int) */
    public static String unescape(String str) {
        StringBuilder sb = new StringBuilder();
        int[] iArr = new int[1];
        int i = 0;
        while (i < str.length()) {
            int i2 = i + 1;
            char charAt = str.charAt(i);
            if (charAt == '\\') {
                iArr[0] = i2;
                int unescapeAt = unescapeAt(str, iArr);
                if (unescapeAt >= 0) {
                    sb.appendCodePoint(unescapeAt);
                    i = iArr[0];
                } else {
                    throw new IllegalArgumentException("Invalid escape sequence " + str.substring(i2 - 1, Math.min(i2 + 8, str.length())));
                }
            } else {
                sb.append(charAt);
                i = i2;
            }
        }
        return sb.toString();
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001a: APUT  (r1v1 int[]), (0 ??[int, short, byte, char]), (r4v1 int) */
    public static String unescapeLeniently(String str) {
        StringBuilder sb = new StringBuilder();
        int[] iArr = new int[1];
        int i = 0;
        while (i < str.length()) {
            int i2 = i + 1;
            char charAt = str.charAt(i);
            if (charAt == '\\') {
                iArr[0] = i2;
                int unescapeAt = unescapeAt(str, iArr);
                if (unescapeAt < 0) {
                    sb.append(charAt);
                } else {
                    sb.appendCodePoint(unescapeAt);
                    i2 = iArr[0];
                }
            } else {
                sb.append(charAt);
            }
            i = i2;
        }
        return sb.toString();
    }

    public static String hex(long j) {
        return hex(j, 4);
    }

    public static String hex(long j, int i) {
        if (j == Long.MIN_VALUE) {
            return "-8000000000000000";
        }
        boolean z = j < 0;
        if (z) {
            j = -j;
        }
        String upperCase = Long.toString(j, 16).toUpperCase(Locale.ENGLISH);
        if (upperCase.length() < i) {
            upperCase = "0000000000000000".substring(upperCase.length(), i) + upperCase;
        }
        if (!z) {
            return upperCase;
        }
        return LocaleUtility.IETF_SEPARATOR + upperCase;
    }

    public static String hex(CharSequence charSequence) {
        return ((StringBuilder) hex(charSequence, 4, ",", true, new StringBuilder())).toString();
    }

    public static <S extends CharSequence, U extends CharSequence, T extends Appendable> T hex(S s, int i, U u, boolean z, T t) {
        int i2 = 0;
        if (z) {
            while (i2 < s.length()) {
                try {
                    int codePointAt = Character.codePointAt(s, i2);
                    if (i2 != 0) {
                        t.append(u);
                    }
                    t.append(hex((long) codePointAt, i));
                    i2 += UTF16.getCharCount(codePointAt);
                } catch (IOException e) {
                    throw new IllegalIcuArgumentException(e);
                }
            }
        } else {
            while (i2 < s.length()) {
                if (i2 != 0) {
                    t.append(u);
                }
                t.append(hex((long) s.charAt(i2), i));
                i2++;
            }
        }
        return t;
    }

    public static String hex(byte[] bArr, int i, int i2, String str) {
        StringBuilder sb = new StringBuilder();
        while (i < i2) {
            if (i != 0) {
                sb.append(str);
            }
            sb.append(hex((long) bArr[i]));
            i++;
        }
        return sb.toString();
    }

    public static <S extends CharSequence> String hex(S s, int i, S s2) {
        return ((StringBuilder) hex(s, i, s2, true, new StringBuilder())).toString();
    }

    public static void split(String str, char c, String[] strArr) {
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        while (i < str.length()) {
            if (str.charAt(i) == c) {
                strArr[i2] = str.substring(i3, i);
                i3 = i + 1;
                i2++;
            }
            i++;
        }
        strArr[i2] = str.substring(i3, i);
        for (int i4 = i2 + 1; i4 < strArr.length; i4++) {
            strArr[i4] = "";
        }
    }

    public static String[] split(String str, char c) {
        ArrayList arrayList = new ArrayList();
        int i = 0;
        int i2 = 0;
        while (i < str.length()) {
            if (str.charAt(i) == c) {
                arrayList.add(str.substring(i2, i));
                i2 = i + 1;
            }
            i++;
        }
        arrayList.add(str.substring(i2, i));
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }

    public static int lookup(String str, String[] strArr) {
        for (int i = 0; i < strArr.length; i++) {
            if (str.equals(strArr[i])) {
                return i;
            }
        }
        return -1;
    }

    public static boolean parseChar(String str, int[] iArr, char c) {
        int i = iArr[0];
        iArr[0] = PatternProps.skipWhiteSpace(str, iArr[0]);
        if (iArr[0] == str.length() || str.charAt(iArr[0]) != c) {
            iArr[0] = i;
            return false;
        }
        iArr[0] = iArr[0] + 1;
        return true;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0031: APUT  (r0v1 int[]), (0 ??[int, short, byte, char]), (r2v1 int) */
    public static int parsePattern(String str, int i, int i2, String str2, int[] iArr) {
        int[] iArr2 = new int[1];
        int i3 = i;
        int i4 = 0;
        for (int i5 = 0; i5 < str2.length(); i5++) {
            char charAt = str2.charAt(i5);
            if (charAt != ' ') {
                if (charAt == '#') {
                    iArr2[0] = i3;
                    int i6 = i4 + 1;
                    iArr[i4] = parseInteger(str, iArr2, i2);
                    if (iArr2[0] == i3) {
                        return -1;
                    }
                    i3 = iArr2[0];
                    i4 = i6;
                } else if (charAt != '~') {
                    if (i3 >= i2) {
                        return -1;
                    }
                    int i7 = i3 + 1;
                    if (((char) UCharacter.toLowerCase(str.charAt(i3))) != charAt) {
                        return -1;
                    }
                    i3 = i7;
                }
            } else if (i3 >= i2) {
                return -1;
            } else {
                int i8 = i3 + 1;
                if (!PatternProps.isWhiteSpace(str.charAt(i3))) {
                    return -1;
                }
                i3 = i8;
            }
            i3 = PatternProps.skipWhiteSpace(str, i3);
        }
        return i3;
    }

    public static int parsePattern(String str, Replaceable replaceable, int i, int i2) {
        if (str.length() == 0) {
            return i;
        }
        int i3 = 0;
        int codePointAt = Character.codePointAt(str, 0);
        while (i < i2) {
            int char32At = replaceable.char32At(i);
            if (codePointAt != 126) {
                if (char32At != codePointAt) {
                    break;
                }
                int charCount = UTF16.getCharCount(char32At);
                i += charCount;
                i3 += charCount;
                if (i3 == str.length()) {
                    return i;
                }
            } else if (PatternProps.isWhiteSpace(char32At)) {
                i += UTF16.getCharCount(char32At);
            } else {
                i3++;
                if (i3 == str.length()) {
                    return i;
                }
            }
            codePointAt = UTF16.charAt(str, i3);
        }
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0046 A[EDGE_INSN: B:22:0x0046->B:17:0x0046 ?: BREAK  , SYNTHETIC] */
    public static int parseInteger(String str, int[] iArr, int i) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7 = iArr[0];
        if (str.regionMatches(true, i7, "0x", 0, 2)) {
            i7 += 2;
            i4 = 16;
        } else if (i7 >= i || str.charAt(i7) != '0') {
            i4 = 10;
        } else {
            i7++;
            i4 = 8;
            i3 = 1;
            i2 = 0;
            while (true) {
                if (i7 < i) {
                    break;
                }
                i5 = i7 + 1;
                int digit = UCharacter.digit(str.charAt(i7), i4);
                if (digit < 0) {
                    i7 = i5 - 1;
                    break;
                }
                i3++;
                i6 = digit + (i2 * i4);
                if (i6 <= i2) {
                    return 0;
                }
                i7 = i5;
                i2 = i6;
            }
            if (i3 > 0) {
                iArr[0] = i7;
            }
            return i2;
        }
        i3 = 0;
        i2 = 0;
        while (true) {
            if (i7 < i) {
            }
            i7 = i5;
            i2 = i6;
        }
        if (i3 > 0) {
        }
        return i2;
    }

    public static String parseUnicodeIdentifier(String str, int[] iArr) {
        StringBuilder sb = new StringBuilder();
        int i = iArr[0];
        while (i < str.length()) {
            int codePointAt = Character.codePointAt(str, i);
            if (sb.length() != 0) {
                if (!UCharacter.isUnicodeIdentifierPart(codePointAt)) {
                    break;
                }
                sb.appendCodePoint(codePointAt);
            } else if (!UCharacter.isUnicodeIdentifierStart(codePointAt)) {
                return null;
            } else {
                sb.appendCodePoint(codePointAt);
            }
            i += UTF16.getCharCount(codePointAt);
        }
        iArr[0] = i;
        return sb.toString();
    }

    private static <T extends Appendable> void recursiveAppendNumber(T t, int i, int i2, int i3) {
        try {
            int i4 = i % i2;
            if (i >= i2 || i3 > 1) {
                recursiveAppendNumber(t, i / i2, i2, i3 - 1);
            }
            t.append(DIGITS[i4]);
        } catch (IOException e) {
            throw new IllegalIcuArgumentException(e);
        }
    }

    public static <T extends Appendable> T appendNumber(T t, int i, int i2, int i3) {
        if (i2 < 2 || i2 > 36) {
            throw new IllegalArgumentException("Illegal radix " + i2);
        }
        if (i < 0) {
            i = -i;
            try {
                t.append(LanguageTag.SEP);
            } catch (IOException e) {
                throw new IllegalIcuArgumentException(e);
            }
        }
        recursiveAppendNumber(t, i, i2, i3);
        return t;
    }

    public static int parseNumber(String str, int[] iArr, int i) {
        int digit;
        int i2 = iArr[0];
        int i3 = 0;
        while (i2 < str.length() && (digit = UCharacter.digit(Character.codePointAt(str, i2), i)) >= 0) {
            i3 = (i3 * i) + digit;
            if (i3 < 0) {
                return -1;
            }
            i2++;
        }
        if (i2 == iArr[0]) {
            return -1;
        }
        iArr[0] = i2;
        return i3;
    }

    public static <T extends Appendable> boolean escapeUnprintable(T t, int i) {
        try {
            if (!isUnprintable(i)) {
                return false;
            }
            t.append('\\');
            if ((-65536 & i) != 0) {
                t.append('U');
                t.append(DIGITS[(i >> 28) & 15]);
                t.append(DIGITS[(i >> 24) & 15]);
                t.append(DIGITS[(i >> 20) & 15]);
                t.append(DIGITS[(i >> 16) & 15]);
            } else {
                t.append(UnicodeLocaleExtension.SINGLETON);
            }
            t.append(DIGITS[(i >> 12) & 15]);
            t.append(DIGITS[(i >> 8) & 15]);
            t.append(DIGITS[(i >> 4) & 15]);
            t.append(DIGITS[i & 15]);
            return true;
        } catch (IOException e) {
            throw new IllegalIcuArgumentException(e);
        }
    }

    public static int quotedIndexOf(String str, int i, int i2, String str2) {
        while (i < i2) {
            char charAt = str.charAt(i);
            if (charAt == '\\') {
                i++;
            } else if (charAt == '\'') {
                do {
                    i++;
                    if (i >= i2) {
                        break;
                    }
                } while (str.charAt(i) != '\'');
            } else if (str2.indexOf(charAt) >= 0) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static void appendToRule(StringBuffer stringBuffer, int i, boolean z, boolean z2, StringBuffer stringBuffer2) {
        if (z || (z2 && isUnprintable(i))) {
            if (stringBuffer2.length() > 0) {
                while (stringBuffer2.length() >= 2 && stringBuffer2.charAt(0) == '\'' && stringBuffer2.charAt(1) == '\'') {
                    stringBuffer.append('\\');
                    stringBuffer.append('\'');
                    stringBuffer2.delete(0, 2);
                }
                int i2 = 0;
                while (stringBuffer2.length() >= 2 && stringBuffer2.charAt(stringBuffer2.length() - 2) == '\'' && stringBuffer2.charAt(stringBuffer2.length() - 1) == '\'') {
                    stringBuffer2.setLength(stringBuffer2.length() - 2);
                    i2++;
                }
                if (stringBuffer2.length() > 0) {
                    stringBuffer.append('\'');
                    stringBuffer.append(stringBuffer2);
                    stringBuffer.append('\'');
                    stringBuffer2.setLength(0);
                }
                while (true) {
                    int i3 = i2 - 1;
                    if (i2 <= 0) {
                        break;
                    }
                    stringBuffer.append('\\');
                    stringBuffer.append('\'');
                    i2 = i3;
                }
            }
            if (i == -1) {
                return;
            }
            if (i == 32) {
                int length = stringBuffer.length();
                if (length > 0 && stringBuffer.charAt(length - 1) != ' ') {
                    stringBuffer.append(' ');
                }
            } else if (!z2 || !escapeUnprintable(stringBuffer, i)) {
                stringBuffer.appendCodePoint(i);
            }
        } else if (stringBuffer2.length() == 0 && (i == 39 || i == 92)) {
            stringBuffer.append('\\');
            stringBuffer.append((char) i);
        } else if (stringBuffer2.length() > 0 || ((i >= 33 && i <= 126 && ((i < 48 || i > 57) && ((i < 65 || i > 90) && (i < 97 || i > 122)))) || PatternProps.isWhiteSpace(i))) {
            stringBuffer2.appendCodePoint(i);
            if (i == 39) {
                stringBuffer2.append((char) i);
            }
        } else {
            stringBuffer.appendCodePoint(i);
        }
    }

    public static void appendToRule(StringBuffer stringBuffer, String str, boolean z, boolean z2, StringBuffer stringBuffer2) {
        for (int i = 0; i < str.length(); i++) {
            appendToRule(stringBuffer, str.charAt(i), z, z2, stringBuffer2);
        }
    }

    public static void appendToRule(StringBuffer stringBuffer, UnicodeMatcher unicodeMatcher, boolean z, StringBuffer stringBuffer2) {
        if (unicodeMatcher != null) {
            appendToRule(stringBuffer, unicodeMatcher.toPattern(z), true, z, stringBuffer2);
        }
    }

    public static String valueOf(int[] iArr) {
        StringBuilder sb = new StringBuilder(iArr.length);
        for (int i : iArr) {
            sb.appendCodePoint(i);
        }
        return sb.toString();
    }

    public static String repeat(String str, int i) {
        if (i <= 0) {
            return "";
        }
        if (i == 1) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i2 = 0; i2 < i; i2++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static String[] splitString(String str, String str2) {
        return str.split("\\Q" + str2 + "\\E");
    }

    public static String[] splitWhitespace(String str) {
        return str.split("\\s+");
    }

    public static String fromHex(String str, int i, String str2) {
        if (str2 == null) {
            str2 = "\\s+";
        }
        return fromHex(str, i, Pattern.compile(str2));
    }

    public static String fromHex(String str, int i, Pattern pattern) {
        StringBuilder sb = new StringBuilder();
        String[] split = pattern.split(str);
        for (String str2 : split) {
            if (str2.length() >= i) {
                sb.appendCodePoint(Integer.parseInt(str2, 16));
            } else {
                throw new IllegalArgumentException("code point too short: " + str2);
            }
        }
        return sb.toString();
    }

    public static int addExact(int i, int i2) {
        int i3 = i + i2;
        if (((i ^ i3) & (i2 ^ i3)) >= 0) {
            return i3;
        }
        throw new ArithmeticException("integer overflow");
    }

    public static boolean charSequenceEquals(CharSequence charSequence, CharSequence charSequence2) {
        if (charSequence == charSequence2) {
            return true;
        }
        if (charSequence == null || charSequence2 == null || charSequence.length() != charSequence2.length()) {
            return false;
        }
        for (int i = 0; i < charSequence.length(); i++) {
            if (charSequence.charAt(i) != charSequence2.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static int charSequenceHashCode(CharSequence charSequence) {
        int i = 0;
        for (int i2 = 0; i2 < charSequence.length(); i2++) {
            i = (i * 31) + charSequence.charAt(i2);
        }
        return i;
    }

    public static <A extends Appendable> A appendTo(CharSequence charSequence, A a) {
        try {
            a.append(charSequence);
            return a;
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }
}
