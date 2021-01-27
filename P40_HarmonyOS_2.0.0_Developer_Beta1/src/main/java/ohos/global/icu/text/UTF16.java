package ohos.global.icu.text;

import java.util.Comparator;
import ohos.global.icu.impl.Utility;

public final class UTF16 {
    public static final int CODEPOINT_MAX_VALUE = 1114111;
    public static final int CODEPOINT_MIN_VALUE = 0;
    private static final int LEAD_SURROGATE_BITMASK = -1024;
    private static final int LEAD_SURROGATE_BITS = 55296;
    public static final int LEAD_SURROGATE_BOUNDARY = 2;
    public static final int LEAD_SURROGATE_MAX_VALUE = 56319;
    public static final int LEAD_SURROGATE_MIN_VALUE = 55296;
    private static final int LEAD_SURROGATE_OFFSET_ = 55232;
    private static final int LEAD_SURROGATE_SHIFT_ = 10;
    public static final int SINGLE_CHAR_BOUNDARY = 1;
    public static final int SUPPLEMENTARY_MIN_VALUE = 65536;
    private static final int SURROGATE_BITMASK = -2048;
    private static final int SURROGATE_BITS = 55296;
    public static final int SURROGATE_MAX_VALUE = 57343;
    public static final int SURROGATE_MIN_VALUE = 55296;
    private static final int TRAIL_SURROGATE_BITMASK = -1024;
    private static final int TRAIL_SURROGATE_BITS = 56320;
    public static final int TRAIL_SURROGATE_BOUNDARY = 5;
    private static final int TRAIL_SURROGATE_MASK_ = 1023;
    public static final int TRAIL_SURROGATE_MAX_VALUE = 57343;
    public static final int TRAIL_SURROGATE_MIN_VALUE = 56320;

    public static int getCharCount(int i) {
        return i < 65536 ? 1 : 2;
    }

    public static char getLeadSurrogate(int i) {
        if (i >= 65536) {
            return (char) ((i >> 10) + LEAD_SURROGATE_OFFSET_);
        }
        return 0;
    }

    public static char getTrailSurrogate(int i) {
        return i >= 65536 ? (char) ((i & TRAIL_SURROGATE_MASK_) + 56320) : (char) i;
    }

    public static boolean isLeadSurrogate(char c) {
        return (c & 64512) == 55296;
    }

    public static boolean isSurrogate(char c) {
        return (c & 63488) == 55296;
    }

    public static boolean isTrailSurrogate(char c) {
        return (c & 64512) == 56320;
    }

    private UTF16() {
    }

    public static int charAt(String str, int i) {
        char charAt = str.charAt(i);
        if (charAt < 55296) {
            return charAt;
        }
        return _charAt(str, i, charAt);
    }

    private static int _charAt(String str, int i, char c) {
        char charAt;
        char charAt2;
        if (c > 57343) {
            return c;
        }
        if (c <= 56319) {
            int i2 = i + 1;
            if (str.length() != i2 && (charAt2 = str.charAt(i2)) >= 56320 && charAt2 <= 57343) {
                return Character.toCodePoint(c, charAt2);
            }
        } else {
            int i3 = i - 1;
            if (i3 >= 0 && (charAt = str.charAt(i3)) >= 55296 && charAt <= 56319) {
                return Character.toCodePoint(charAt, c);
            }
        }
        return c;
    }

    public static int charAt(CharSequence charSequence, int i) {
        char charAt = charSequence.charAt(i);
        if (charAt < 55296) {
            return charAt;
        }
        return _charAt(charSequence, i, charAt);
    }

    private static int _charAt(CharSequence charSequence, int i, char c) {
        char charAt;
        char charAt2;
        if (c > 57343) {
            return c;
        }
        if (c <= 56319) {
            int i2 = i + 1;
            if (charSequence.length() != i2 && (charAt2 = charSequence.charAt(i2)) >= 56320 && charAt2 <= 57343) {
                return Character.toCodePoint(c, charAt2);
            }
        } else {
            int i3 = i - 1;
            if (i3 >= 0 && (charAt = charSequence.charAt(i3)) >= 55296 && charAt <= 56319) {
                return Character.toCodePoint(charAt, c);
            }
        }
        return c;
    }

    public static int charAt(StringBuffer stringBuffer, int i) {
        if (i < 0 || i >= stringBuffer.length()) {
            throw new StringIndexOutOfBoundsException(i);
        }
        char charAt = stringBuffer.charAt(i);
        if (!isSurrogate(charAt)) {
            return charAt;
        }
        if (charAt <= 56319) {
            int i2 = i + 1;
            if (stringBuffer.length() != i2) {
                char charAt2 = stringBuffer.charAt(i2);
                if (isTrailSurrogate(charAt2)) {
                    return Character.toCodePoint(charAt, charAt2);
                }
            }
        } else {
            int i3 = i - 1;
            if (i3 >= 0) {
                char charAt3 = stringBuffer.charAt(i3);
                if (isLeadSurrogate(charAt3)) {
                    return Character.toCodePoint(charAt3, charAt);
                }
            }
        }
        return charAt;
    }

    public static int charAt(char[] cArr, int i, int i2, int i3) {
        int i4 = i3 + i;
        if (i4 < i || i4 >= i2) {
            throw new ArrayIndexOutOfBoundsException(i4);
        }
        char c = cArr[i4];
        if (!isSurrogate(c)) {
            return c;
        }
        if (c <= 56319) {
            int i5 = i4 + 1;
            if (i5 >= i2) {
                return c;
            }
            char c2 = cArr[i5];
            if (isTrailSurrogate(c2)) {
                return Character.toCodePoint(c, c2);
            }
        } else if (i4 == i) {
            return c;
        } else {
            char c3 = cArr[i4 - 1];
            if (isLeadSurrogate(c3)) {
                return Character.toCodePoint(c3, c);
            }
        }
        return c;
    }

    public static int charAt(Replaceable replaceable, int i) {
        if (i < 0 || i >= replaceable.length()) {
            throw new StringIndexOutOfBoundsException(i);
        }
        char charAt = replaceable.charAt(i);
        if (!isSurrogate(charAt)) {
            return charAt;
        }
        if (charAt <= 56319) {
            int i2 = i + 1;
            if (replaceable.length() != i2) {
                char charAt2 = replaceable.charAt(i2);
                if (isTrailSurrogate(charAt2)) {
                    return Character.toCodePoint(charAt, charAt2);
                }
            }
        } else {
            int i3 = i - 1;
            if (i3 >= 0) {
                char charAt3 = replaceable.charAt(i3);
                if (isLeadSurrogate(charAt3)) {
                    return Character.toCodePoint(charAt3, charAt);
                }
            }
        }
        return charAt;
    }

    public static int bounds(String str, int i) {
        char charAt = str.charAt(i);
        if (isSurrogate(charAt)) {
            if (isLeadSurrogate(charAt)) {
                int i2 = i + 1;
                if (i2 < str.length() && isTrailSurrogate(str.charAt(i2))) {
                    return 2;
                }
            } else {
                int i3 = i - 1;
                if (i3 >= 0 && isLeadSurrogate(str.charAt(i3))) {
                    return 5;
                }
            }
        }
        return 1;
    }

    public static int bounds(StringBuffer stringBuffer, int i) {
        char charAt = stringBuffer.charAt(i);
        if (isSurrogate(charAt)) {
            if (isLeadSurrogate(charAt)) {
                int i2 = i + 1;
                if (i2 < stringBuffer.length() && isTrailSurrogate(stringBuffer.charAt(i2))) {
                    return 2;
                }
            } else {
                int i3 = i - 1;
                if (i3 >= 0 && isLeadSurrogate(stringBuffer.charAt(i3))) {
                    return 5;
                }
            }
        }
        return 1;
    }

    public static int bounds(char[] cArr, int i, int i2, int i3) {
        int i4 = i3 + i;
        if (i4 < i || i4 >= i2) {
            throw new ArrayIndexOutOfBoundsException(i4);
        }
        char c = cArr[i4];
        if (isSurrogate(c)) {
            if (isLeadSurrogate(c)) {
                int i5 = i4 + 1;
                if (i5 < i2 && isTrailSurrogate(cArr[i5])) {
                    return 2;
                }
            } else {
                int i6 = i4 - 1;
                if (i6 >= i && isLeadSurrogate(cArr[i6])) {
                    return 5;
                }
            }
        }
        return 1;
    }

    public static String valueOf(int i) {
        if (i >= 0 && i <= 1114111) {
            return toString(i);
        }
        throw new IllegalArgumentException("Illegal codepoint");
    }

    public static String valueOf(String str, int i) {
        int bounds = bounds(str, i);
        if (bounds == 2) {
            return str.substring(i, i + 2);
        }
        if (bounds != 5) {
            return str.substring(i, i + 1);
        }
        return str.substring(i - 1, i + 1);
    }

    public static String valueOf(StringBuffer stringBuffer, int i) {
        int bounds = bounds(stringBuffer, i);
        if (bounds == 2) {
            return stringBuffer.substring(i, i + 2);
        }
        if (bounds != 5) {
            return stringBuffer.substring(i, i + 1);
        }
        return stringBuffer.substring(i - 1, i + 1);
    }

    public static String valueOf(char[] cArr, int i, int i2, int i3) {
        int bounds = bounds(cArr, i, i2, i3);
        if (bounds == 2) {
            return new String(cArr, i + i3, 2);
        }
        if (bounds != 5) {
            return new String(cArr, i + i3, 1);
        }
        return new String(cArr, (i + i3) - 1, 2);
    }

    public static int findOffsetFromCodePoint(String str, int i) {
        int i2;
        int length = str.length();
        if (i < 0 || i > length) {
            throw new StringIndexOutOfBoundsException(i);
        }
        int i3 = 0;
        int i4 = i;
        while (i3 < length && i4 > 0) {
            if (isLeadSurrogate(str.charAt(i3)) && (i2 = i3 + 1) < length && isTrailSurrogate(str.charAt(i2))) {
                i3 = i2;
            }
            i4--;
            i3++;
        }
        if (i4 == 0) {
            return i3;
        }
        throw new StringIndexOutOfBoundsException(i);
    }

    public static int findOffsetFromCodePoint(StringBuffer stringBuffer, int i) {
        int i2;
        int length = stringBuffer.length();
        if (i < 0 || i > length) {
            throw new StringIndexOutOfBoundsException(i);
        }
        int i3 = 0;
        int i4 = i;
        while (i3 < length && i4 > 0) {
            if (isLeadSurrogate(stringBuffer.charAt(i3)) && (i2 = i3 + 1) < length && isTrailSurrogate(stringBuffer.charAt(i2))) {
                i3 = i2;
            }
            i4--;
            i3++;
        }
        if (i4 == 0) {
            return i3;
        }
        throw new StringIndexOutOfBoundsException(i);
    }

    public static int findOffsetFromCodePoint(char[] cArr, int i, int i2, int i3) {
        int i4;
        if (i3 <= i2 - i) {
            int i5 = i;
            int i6 = i3;
            while (i5 < i2 && i6 > 0) {
                if (isLeadSurrogate(cArr[i5]) && (i4 = i5 + 1) < i2 && isTrailSurrogate(cArr[i4])) {
                    i5 = i4;
                }
                i6--;
                i5++;
            }
            if (i6 == 0) {
                return i5 - i;
            }
            throw new ArrayIndexOutOfBoundsException(i3);
        }
        throw new ArrayIndexOutOfBoundsException(i3);
    }

    public static int findCodePointOffset(String str, int i) {
        if (i < 0 || i > str.length()) {
            throw new StringIndexOutOfBoundsException(i);
        }
        boolean z = false;
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            char charAt = str.charAt(i3);
            if (!z || !isTrailSurrogate(charAt)) {
                z = isLeadSurrogate(charAt);
                i2++;
            } else {
                z = false;
            }
        }
        return (i != str.length() && z && isTrailSurrogate(str.charAt(i))) ? i2 - 1 : i2;
    }

    public static int findCodePointOffset(StringBuffer stringBuffer, int i) {
        if (i < 0 || i > stringBuffer.length()) {
            throw new StringIndexOutOfBoundsException(i);
        }
        boolean z = false;
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            char charAt = stringBuffer.charAt(i3);
            if (!z || !isTrailSurrogate(charAt)) {
                z = isLeadSurrogate(charAt);
                i2++;
            } else {
                z = false;
            }
        }
        return (i != stringBuffer.length() && z && isTrailSurrogate(stringBuffer.charAt(i))) ? i2 - 1 : i2;
    }

    public static int findCodePointOffset(char[] cArr, int i, int i2, int i3) {
        int i4 = i3 + i;
        if (i4 <= i2) {
            boolean z = false;
            int i5 = 0;
            while (i < i4) {
                char c = cArr[i];
                if (!z || !isTrailSurrogate(c)) {
                    z = isLeadSurrogate(c);
                    i5++;
                } else {
                    z = false;
                }
                i++;
            }
            return (i4 != i2 && z && isTrailSurrogate(cArr[i4])) ? i5 - 1 : i5;
        }
        throw new StringIndexOutOfBoundsException(i4);
    }

    public static StringBuffer append(StringBuffer stringBuffer, int i) {
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Illegal codepoint: " + Integer.toHexString(i));
        }
        if (i >= 65536) {
            stringBuffer.append(getLeadSurrogate(i));
            stringBuffer.append(getTrailSurrogate(i));
        } else {
            stringBuffer.append((char) i);
        }
        return stringBuffer;
    }

    public static StringBuffer appendCodePoint(StringBuffer stringBuffer, int i) {
        return append(stringBuffer, i);
    }

    public static int append(char[] cArr, int i, int i2) {
        if (i2 < 0 || i2 > 1114111) {
            throw new IllegalArgumentException("Illegal codepoint");
        } else if (i2 >= 65536) {
            int i3 = i + 1;
            cArr[i] = getLeadSurrogate(i2);
            int i4 = i3 + 1;
            cArr[i3] = getTrailSurrogate(i2);
            return i4;
        } else {
            int i5 = i + 1;
            cArr[i] = (char) i2;
            return i5;
        }
    }

    public static int countCodePoint(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        return findCodePointOffset(str, str.length());
    }

    public static int countCodePoint(StringBuffer stringBuffer) {
        if (stringBuffer == null || stringBuffer.length() == 0) {
            return 0;
        }
        return findCodePointOffset(stringBuffer, stringBuffer.length());
    }

    public static int countCodePoint(char[] cArr, int i, int i2) {
        if (cArr == null || cArr.length == 0) {
            return 0;
        }
        return findCodePointOffset(cArr, i, i2, i2 - i);
    }

    public static void setCharAt(StringBuffer stringBuffer, int i, int i2) {
        int i3;
        char charAt = stringBuffer.charAt(i);
        int i4 = 2;
        if (isSurrogate(charAt)) {
            if (!isLeadSurrogate(charAt) || stringBuffer.length() <= (i3 = i + 1) || !isTrailSurrogate(stringBuffer.charAt(i3))) {
                if (isTrailSurrogate(charAt) && i > 0 && isLeadSurrogate(stringBuffer.charAt(i - 1))) {
                    i--;
                }
            }
            stringBuffer.replace(i, i4 + i, valueOf(i2));
        }
        i4 = 1;
        stringBuffer.replace(i, i4 + i, valueOf(i2));
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0052  */
    public static int setCharAt(char[] cArr, int i, int i2, int i3) {
        int i4;
        int length;
        int i5;
        if (i2 < i) {
            char c = cArr[i2];
            if (isSurrogate(c)) {
                if (!isLeadSurrogate(c) || cArr.length <= (i5 = i2 + 1) || !isTrailSurrogate(cArr[i5])) {
                    if (isTrailSurrogate(c) && i2 > 0 && isLeadSurrogate(cArr[i2 - 1])) {
                        i2--;
                    }
                }
                i4 = 2;
                String valueOf = valueOf(i3);
                length = valueOf.length();
                cArr[i2] = valueOf.charAt(0);
                if (i4 == length) {
                    int i6 = i2 + i4;
                    System.arraycopy(cArr, i6, cArr, i2 + length, i - i6);
                    if (i4 < length) {
                        cArr[i2 + 1] = valueOf.charAt(1);
                        int i7 = i + 1;
                        if (i7 >= cArr.length) {
                            return i7;
                        }
                        cArr[i7] = 0;
                        return i7;
                    }
                    int i8 = i - 1;
                    cArr[i8] = 0;
                    return i8;
                } else if (i4 != 2) {
                    return i;
                } else {
                    cArr[i2 + 1] = valueOf.charAt(1);
                    return i;
                }
            }
            i4 = 1;
            String valueOf2 = valueOf(i3);
            length = valueOf2.length();
            cArr[i2] = valueOf2.charAt(0);
            if (i4 == length) {
            }
        } else {
            throw new ArrayIndexOutOfBoundsException(i2);
        }
    }

    public static int moveCodePointOffset(String str, int i, int i2) {
        int i3;
        int i4;
        int length = str.length();
        if (i < 0 || i > length) {
            throw new StringIndexOutOfBoundsException(i);
        }
        if (i2 > 0) {
            if (i2 + i <= length) {
                i3 = i2;
                while (i < length && i3 > 0) {
                    if (isLeadSurrogate(str.charAt(i)) && (i4 = i + 1) < length && isTrailSurrogate(str.charAt(i4))) {
                        i = i4;
                    }
                    i3--;
                    i++;
                }
            } else {
                throw new StringIndexOutOfBoundsException(i);
            }
        } else if (i + i2 >= 0) {
            i3 = -i2;
            while (i3 > 0) {
                i--;
                if (i < 0) {
                    break;
                }
                if (isTrailSurrogate(str.charAt(i)) && i > 0 && isLeadSurrogate(str.charAt(i - 1))) {
                    i--;
                }
                i3--;
            }
        } else {
            throw new StringIndexOutOfBoundsException(i);
        }
        if (i3 == 0) {
            return i;
        }
        throw new StringIndexOutOfBoundsException(i2);
    }

    public static int moveCodePointOffset(StringBuffer stringBuffer, int i, int i2) {
        int i3;
        int i4;
        int length = stringBuffer.length();
        if (i < 0 || i > length) {
            throw new StringIndexOutOfBoundsException(i);
        }
        if (i2 > 0) {
            if (i2 + i <= length) {
                i3 = i2;
                while (i < length && i3 > 0) {
                    if (isLeadSurrogate(stringBuffer.charAt(i)) && (i4 = i + 1) < length && isTrailSurrogate(stringBuffer.charAt(i4))) {
                        i = i4;
                    }
                    i3--;
                    i++;
                }
            } else {
                throw new StringIndexOutOfBoundsException(i);
            }
        } else if (i + i2 >= 0) {
            i3 = -i2;
            while (i3 > 0) {
                i--;
                if (i < 0) {
                    break;
                }
                if (isTrailSurrogate(stringBuffer.charAt(i)) && i > 0 && isLeadSurrogate(stringBuffer.charAt(i - 1))) {
                    i--;
                }
                i3--;
            }
        } else {
            throw new StringIndexOutOfBoundsException(i);
        }
        if (i3 == 0) {
            return i;
        }
        throw new StringIndexOutOfBoundsException(i2);
    }

    public static int moveCodePointOffset(char[] cArr, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int length = cArr.length;
        int i7 = i3 + i;
        if (i < 0 || i2 < i) {
            throw new StringIndexOutOfBoundsException(i);
        } else if (i2 > length) {
            throw new StringIndexOutOfBoundsException(i2);
        } else if (i3 < 0 || i7 > i2) {
            throw new StringIndexOutOfBoundsException(i3);
        } else {
            if (i4 > 0) {
                if (i4 + i7 <= length) {
                    i5 = i4;
                    while (i7 < i2 && i5 > 0) {
                        if (!isLeadSurrogate(cArr[i7]) || (i6 = i7 + 1) >= i2 || !isTrailSurrogate(cArr[i6])) {
                            i6 = i7;
                        }
                        i5--;
                        i7 = i6 + 1;
                    }
                } else {
                    throw new StringIndexOutOfBoundsException(i7);
                }
            } else if (i7 + i4 >= i) {
                i5 = -i4;
                while (i5 > 0) {
                    i7--;
                    if (i7 < i) {
                        break;
                    }
                    if (isTrailSurrogate(cArr[i7]) && i7 > i && isLeadSurrogate(cArr[i7 - 1])) {
                        i7--;
                    }
                    i5--;
                }
            } else {
                throw new StringIndexOutOfBoundsException(i7);
            }
            if (i5 == 0) {
                return i7 - i;
            }
            throw new StringIndexOutOfBoundsException(i4);
        }
    }

    public static StringBuffer insert(StringBuffer stringBuffer, int i, int i2) {
        String valueOf = valueOf(i2);
        if (i != stringBuffer.length() && bounds(stringBuffer, i) == 5) {
            i++;
        }
        stringBuffer.insert(i, valueOf);
        return stringBuffer;
    }

    public static int insert(char[] cArr, int i, int i2, int i3) {
        String valueOf = valueOf(i3);
        if (i2 != i && bounds(cArr, 0, i, i2) == 5) {
            i2++;
        }
        int length = valueOf.length();
        int i4 = i + length;
        if (i4 <= cArr.length) {
            System.arraycopy(cArr, i2, cArr, i2 + length, i - i2);
            cArr[i2] = valueOf.charAt(0);
            if (length == 2) {
                cArr[i2 + 1] = valueOf.charAt(1);
            }
            return i4;
        }
        throw new ArrayIndexOutOfBoundsException(i2 + length);
    }

    public static StringBuffer delete(StringBuffer stringBuffer, int i) {
        int bounds = bounds(stringBuffer, i);
        int i2 = 2;
        if (bounds != 2) {
            if (bounds != 5) {
                i2 = 1;
            } else {
                i--;
            }
        }
        stringBuffer.delete(i, i2 + i);
        return stringBuffer;
    }

    public static int delete(char[] cArr, int i, int i2) {
        int bounds = bounds(cArr, 0, i, i2);
        int i3 = 2;
        if (bounds != 2) {
            if (bounds != 5) {
                i3 = 1;
            } else {
                i2--;
            }
        }
        int i4 = i2 + i3;
        System.arraycopy(cArr, i4, cArr, i2, i - i4);
        int i5 = i - i3;
        cArr[i5] = 0;
        return i5;
    }

    public static int indexOf(String str, int i) {
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (i < 55296 || (i > 57343 && i < 65536)) {
            return str.indexOf((char) i);
        } else {
            if (i >= 65536) {
                return str.indexOf(toString(i));
            }
            char c = (char) i;
            int indexOf = str.indexOf(c);
            if (indexOf >= 0) {
                if (isLeadSurrogate(c) && indexOf < str.length() - 1) {
                    int i2 = indexOf + 1;
                    if (isTrailSurrogate(str.charAt(i2))) {
                        return indexOf(str, i, i2);
                    }
                }
                if (indexOf > 0 && isLeadSurrogate(str.charAt(indexOf - 1))) {
                    return indexOf(str, i, indexOf + 1);
                }
            }
            return indexOf;
        }
    }

    public static int indexOf(String str, String str2) {
        int length = str2.length();
        if (!isTrailSurrogate(str2.charAt(0)) && !isLeadSurrogate(str2.charAt(length - 1))) {
            return str.indexOf(str2);
        }
        int indexOf = str.indexOf(str2);
        int i = indexOf + length;
        if (indexOf >= 0) {
            if (isLeadSurrogate(str2.charAt(length - 1)) && indexOf < str.length() - 1) {
                int i2 = i + 1;
                if (isTrailSurrogate(str.charAt(i2))) {
                    return indexOf(str, str2, i2);
                }
            }
            if (isTrailSurrogate(str2.charAt(0)) && indexOf > 0 && isLeadSurrogate(str.charAt(indexOf - 1))) {
                return indexOf(str, str2, i + 1);
            }
        }
        return indexOf;
    }

    public static int indexOf(String str, int i, int i2) {
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (i < 55296 || (i > 57343 && i < 65536)) {
            return str.indexOf((char) i, i2);
        } else {
            if (i >= 65536) {
                return str.indexOf(toString(i), i2);
            }
            char c = (char) i;
            int indexOf = str.indexOf(c, i2);
            if (indexOf >= 0) {
                if (isLeadSurrogate(c) && indexOf < str.length() - 1) {
                    int i3 = indexOf + 1;
                    if (isTrailSurrogate(str.charAt(i3))) {
                        return indexOf(str, i, i3);
                    }
                }
                if (indexOf > 0 && isLeadSurrogate(str.charAt(indexOf - 1))) {
                    return indexOf(str, i, indexOf + 1);
                }
            }
            return indexOf;
        }
    }

    public static int indexOf(String str, String str2, int i) {
        int length = str2.length();
        if (!isTrailSurrogate(str2.charAt(0)) && !isLeadSurrogate(str2.charAt(length - 1))) {
            return str.indexOf(str2, i);
        }
        int indexOf = str.indexOf(str2, i);
        int i2 = indexOf + length;
        if (indexOf >= 0) {
            if (isLeadSurrogate(str2.charAt(length - 1)) && indexOf < str.length() - 1 && isTrailSurrogate(str.charAt(i2))) {
                return indexOf(str, str2, i2 + 1);
            }
            if (isTrailSurrogate(str2.charAt(0)) && indexOf > 0 && isLeadSurrogate(str.charAt(indexOf - 1))) {
                return indexOf(str, str2, i2 + 1);
            }
        }
        return indexOf;
    }

    public static int lastIndexOf(String str, int i) {
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (i < 55296 || (i > 57343 && i < 65536)) {
            return str.lastIndexOf((char) i);
        } else {
            if (i >= 65536) {
                return str.lastIndexOf(toString(i));
            }
            char c = (char) i;
            int lastIndexOf = str.lastIndexOf(c);
            if (lastIndexOf >= 0) {
                if (isLeadSurrogate(c) && lastIndexOf < str.length() - 1 && isTrailSurrogate(str.charAt(lastIndexOf + 1))) {
                    return lastIndexOf(str, i, lastIndexOf - 1);
                }
                if (lastIndexOf > 0) {
                    int i2 = lastIndexOf - 1;
                    if (isLeadSurrogate(str.charAt(i2))) {
                        return lastIndexOf(str, i, i2);
                    }
                }
            }
            return lastIndexOf;
        }
    }

    public static int lastIndexOf(String str, String str2) {
        int length = str2.length();
        if (!isTrailSurrogate(str2.charAt(0)) && !isLeadSurrogate(str2.charAt(length - 1))) {
            return str.lastIndexOf(str2);
        }
        int lastIndexOf = str.lastIndexOf(str2);
        if (lastIndexOf >= 0) {
            if (isLeadSurrogate(str2.charAt(length - 1)) && lastIndexOf < str.length() - 1 && isTrailSurrogate(str.charAt(length + lastIndexOf + 1))) {
                return lastIndexOf(str, str2, lastIndexOf - 1);
            }
            if (isTrailSurrogate(str2.charAt(0)) && lastIndexOf > 0) {
                int i = lastIndexOf - 1;
                if (isLeadSurrogate(str.charAt(i))) {
                    return lastIndexOf(str, str2, i);
                }
            }
        }
        return lastIndexOf;
    }

    public static int lastIndexOf(String str, int i, int i2) {
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Argument char32 is not a valid codepoint");
        } else if (i < 55296 || (i > 57343 && i < 65536)) {
            return str.lastIndexOf((char) i, i2);
        } else {
            if (i >= 65536) {
                return str.lastIndexOf(toString(i), i2);
            }
            char c = (char) i;
            int lastIndexOf = str.lastIndexOf(c, i2);
            if (lastIndexOf >= 0) {
                if (isLeadSurrogate(c) && lastIndexOf < str.length() - 1 && isTrailSurrogate(str.charAt(lastIndexOf + 1))) {
                    return lastIndexOf(str, i, lastIndexOf - 1);
                }
                if (lastIndexOf > 0) {
                    int i3 = lastIndexOf - 1;
                    if (isLeadSurrogate(str.charAt(i3))) {
                        return lastIndexOf(str, i, i3);
                    }
                }
            }
            return lastIndexOf;
        }
    }

    public static int lastIndexOf(String str, String str2, int i) {
        int length = str2.length();
        if (!isTrailSurrogate(str2.charAt(0)) && !isLeadSurrogate(str2.charAt(length - 1))) {
            return str.lastIndexOf(str2, i);
        }
        int lastIndexOf = str.lastIndexOf(str2, i);
        if (lastIndexOf >= 0) {
            if (isLeadSurrogate(str2.charAt(length - 1)) && lastIndexOf < str.length() - 1 && isTrailSurrogate(str.charAt(length + lastIndexOf))) {
                return lastIndexOf(str, str2, lastIndexOf - 1);
            }
            if (isTrailSurrogate(str2.charAt(0)) && lastIndexOf > 0) {
                int i2 = lastIndexOf - 1;
                if (isLeadSurrogate(str.charAt(i2))) {
                    return lastIndexOf(str, str2, i2);
                }
            }
        }
        return lastIndexOf;
    }

    public static String replace(String str, int i, int i2) {
        if (i <= 0 || i > 1114111) {
            throw new IllegalArgumentException("Argument oldChar32 is not a valid codepoint");
        } else if (i2 <= 0 || i2 > 1114111) {
            throw new IllegalArgumentException("Argument newChar32 is not a valid codepoint");
        } else {
            int indexOf = indexOf(str, i);
            if (indexOf == -1) {
                return str;
            }
            String utf16 = toString(i2);
            int i3 = 1;
            int length = utf16.length();
            StringBuffer stringBuffer = new StringBuffer(str);
            if (i >= 65536) {
                i3 = 2;
            }
            int i4 = indexOf;
            while (indexOf != -1) {
                stringBuffer.replace(i4, i4 + i3, utf16);
                int i5 = indexOf + i3;
                int indexOf2 = indexOf(str, i, i5);
                i4 += (length + indexOf2) - i5;
                indexOf = indexOf2;
            }
            return stringBuffer.toString();
        }
    }

    public static String replace(String str, String str2, String str3) {
        int indexOf = indexOf(str, str2);
        if (indexOf == -1) {
            return str;
        }
        int length = str2.length();
        int length2 = str3.length();
        StringBuffer stringBuffer = new StringBuffer(str);
        int i = indexOf;
        while (indexOf != -1) {
            stringBuffer.replace(i, i + length, str3);
            int i2 = indexOf + length;
            int indexOf2 = indexOf(str, str2, i2);
            i += (length2 + indexOf2) - i2;
            indexOf = indexOf2;
        }
        return stringBuffer.toString();
    }

    public static StringBuffer reverse(StringBuffer stringBuffer) {
        int length = stringBuffer.length();
        StringBuffer stringBuffer2 = new StringBuffer(length);
        while (true) {
            int i = length - 1;
            if (length <= 0) {
                return stringBuffer2;
            }
            char charAt = stringBuffer.charAt(i);
            if (isTrailSurrogate(charAt) && i > 0) {
                char charAt2 = stringBuffer.charAt(i - 1);
                if (isLeadSurrogate(charAt2)) {
                    stringBuffer2.append(charAt2);
                    stringBuffer2.append(charAt);
                    length = i - 1;
                }
            }
            stringBuffer2.append(charAt);
            length = i;
        }
    }

    public static boolean hasMoreCodePointsThan(String str, int i) {
        if (i < 0) {
            return true;
        }
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (((length + 1) >> 1) > i) {
            return true;
        }
        int i2 = length - i;
        if (i2 <= 0) {
            return false;
        }
        int i3 = i2;
        int i4 = 0;
        while (length != 0) {
            if (i == 0) {
                return true;
            }
            int i5 = i4 + 1;
            if (isLeadSurrogate(str.charAt(i4)) && i5 != length && isTrailSurrogate(str.charAt(i5))) {
                i5++;
                i3--;
                if (i3 <= 0) {
                    return false;
                }
            }
            i4 = i5;
            i--;
        }
        return false;
    }

    public static boolean hasMoreCodePointsThan(char[] cArr, int i, int i2, int i3) {
        int i4 = i2 - i;
        if (i4 < 0 || i < 0 || i2 < 0) {
            throw new IndexOutOfBoundsException("Start and limit indexes should be non-negative and start <= limit");
        } else if (i3 < 0) {
            return true;
        } else {
            if (cArr == null) {
                return false;
            }
            if (((i4 + 1) >> 1) > i3) {
                return true;
            }
            int i5 = i4 - i3;
            if (i5 <= 0) {
                return false;
            }
            while (i4 != 0) {
                if (i3 == 0) {
                    return true;
                }
                int i6 = i + 1;
                if (isLeadSurrogate(cArr[i]) && i6 != i2 && isTrailSurrogate(cArr[i6])) {
                    i6++;
                    i5--;
                    if (i5 <= 0) {
                        return false;
                    }
                }
                i = i6;
                i3--;
            }
            return false;
        }
    }

    public static boolean hasMoreCodePointsThan(StringBuffer stringBuffer, int i) {
        if (i < 0) {
            return true;
        }
        if (stringBuffer == null) {
            return false;
        }
        int length = stringBuffer.length();
        if (((length + 1) >> 1) > i) {
            return true;
        }
        int i2 = length - i;
        if (i2 <= 0) {
            return false;
        }
        int i3 = i2;
        int i4 = 0;
        while (length != 0) {
            if (i == 0) {
                return true;
            }
            int i5 = i4 + 1;
            if (isLeadSurrogate(stringBuffer.charAt(i4)) && i5 != length && isTrailSurrogate(stringBuffer.charAt(i5))) {
                i5++;
                i3--;
                if (i3 <= 0) {
                    return false;
                }
            }
            i4 = i5;
            i--;
        }
        return false;
    }

    public static String newString(int[] iArr, int i, int i2) {
        if (i2 >= 0) {
            char[] cArr = new char[i2];
            int i3 = i2 + i;
            char[] cArr2 = cArr;
            int i4 = 0;
            for (int i5 = i; i5 < i3; i5++) {
                int i6 = iArr[i5];
                if (i6 < 0 || i6 > 1114111) {
                    throw new IllegalArgumentException();
                }
                while (true) {
                    if (i6 >= 65536) {
                        cArr2[i4] = (char) ((i6 >> 10) + LEAD_SURROGATE_OFFSET_);
                        cArr2[i4 + 1] = (char) ((i6 & TRAIL_SURROGATE_MASK_) + 56320);
                        i4 += 2;
                        break;
                    }
                    try {
                        cArr2[i4] = (char) i6;
                        i4++;
                        break;
                    } catch (IndexOutOfBoundsException unused) {
                        char[] cArr3 = new char[((int) Math.ceil((((double) iArr.length) * ((double) (i4 + 2))) / ((double) ((i5 - i) + 1))))];
                        System.arraycopy(cArr2, 0, cArr3, 0, i4);
                        cArr2 = cArr3;
                    }
                }
            }
            return new String(cArr2, 0, i4);
        }
        throw new IllegalArgumentException();
    }

    public static final class StringComparator implements Comparator<String> {
        private static final int CODE_POINT_COMPARE_SURROGATE_OFFSET_ = 10240;
        public static final int FOLD_CASE_DEFAULT = 0;
        public static final int FOLD_CASE_EXCLUDE_SPECIAL_I = 1;
        private int m_codePointCompare_;
        private int m_foldCase_;
        private boolean m_ignoreCase_;

        public StringComparator() {
            this(false, false, 0);
        }

        public StringComparator(boolean z, boolean z2, int i) {
            setCodePointCompare(z);
            this.m_ignoreCase_ = z2;
            if (i < 0 || i > 1) {
                throw new IllegalArgumentException("Invalid fold case option");
            }
            this.m_foldCase_ = i;
        }

        public void setCodePointCompare(boolean z) {
            if (z) {
                this.m_codePointCompare_ = 32768;
            } else {
                this.m_codePointCompare_ = 0;
            }
        }

        public void setIgnoreCase(boolean z, int i) {
            this.m_ignoreCase_ = z;
            if (i < 0 || i > 1) {
                throw new IllegalArgumentException("Invalid fold case option");
            }
            this.m_foldCase_ = i;
        }

        public boolean getCodePointCompare() {
            return this.m_codePointCompare_ == 32768;
        }

        public boolean getIgnoreCase() {
            return this.m_ignoreCase_;
        }

        public int getIgnoreCaseOption() {
            return this.m_foldCase_;
        }

        public int compare(String str, String str2) {
            if (Utility.sameObjects(str, str2)) {
                return 0;
            }
            if (str == null) {
                return -1;
            }
            if (str2 == null) {
                return 1;
            }
            if (this.m_ignoreCase_) {
                return compareCaseInsensitive(str, str2);
            }
            return compareCaseSensitive(str, str2);
        }

        private int compareCaseInsensitive(String str, String str2) {
            return Normalizer.cmpEquivFold(str, str2, this.m_codePointCompare_ | this.m_foldCase_ | 65536);
        }

        private int compareCaseSensitive(String str, String str2) {
            int i;
            int i2;
            int i3;
            int i4;
            int length = str.length();
            int length2 = str2.length();
            boolean z = false;
            if (length < length2) {
                i = -1;
                i2 = length;
            } else if (length > length2) {
                i2 = length2;
                i = 1;
            } else {
                i2 = length;
                i = 0;
            }
            int i5 = 0;
            char c = 0;
            char c2 = 0;
            while (i5 < i2) {
                c = str.charAt(i5);
                c2 = str2.charAt(i5);
                if (c != c2) {
                    break;
                }
                i5++;
            }
            if (i5 == i2) {
                return i;
            }
            if (this.m_codePointCompare_ == 32768) {
                z = true;
            }
            if (c >= 55296 && c2 >= 55296 && z) {
                if ((c > 56319 || (i4 = i5 + 1) == length || !UTF16.isTrailSurrogate(str.charAt(i4))) && (!UTF16.isTrailSurrogate(c) || i5 == 0 || !UTF16.isLeadSurrogate(str.charAt(i5 - 1)))) {
                    c = (char) (c - 10240);
                }
                if ((c2 > 56319 || (i3 = i5 + 1) == length2 || !UTF16.isTrailSurrogate(str2.charAt(i3))) && (!UTF16.isTrailSurrogate(c2) || i5 == 0 || !UTF16.isLeadSurrogate(str2.charAt(i5 - 1)))) {
                    c2 = (char) (c2 - 10240);
                }
            }
            return c - c2;
        }
    }

    public static int getSingleCodePoint(CharSequence charSequence) {
        int codePointAt;
        if (!(charSequence == null || charSequence.length() == 0)) {
            if (charSequence.length() == 1) {
                return charSequence.charAt(0);
            }
            if (charSequence.length() <= 2 && (codePointAt = Character.codePointAt(charSequence, 0)) > 65535) {
                return codePointAt;
            }
        }
        return -1;
    }

    public static int compareCodePoint(int i, CharSequence charSequence) {
        int length;
        if (charSequence == null || (length = charSequence.length()) == 0) {
            return 1;
        }
        int codePointAt = i - Character.codePointAt(charSequence, 0);
        if (codePointAt != 0) {
            return codePointAt;
        }
        if (length == Character.charCount(i)) {
            return 0;
        }
        return -1;
    }

    /* JADX DEBUG: TODO: convert one arg to string using `String.valueOf()`, args: [(wrap: char : 0x000f: INVOKE  (r1v0 char) = (r2v0 int) type: STATIC call: ohos.global.icu.text.UTF16.getLeadSurrogate(int):char), (wrap: char : 0x0016: INVOKE  (r2v1 char) = (r2v0 int) type: STATIC call: ohos.global.icu.text.UTF16.getTrailSurrogate(int):char)] */
    private static String toString(int i) {
        if (i < 65536) {
            return String.valueOf((char) i);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getLeadSurrogate(i));
        sb.append(getTrailSurrogate(i));
        return sb.toString();
    }
}
