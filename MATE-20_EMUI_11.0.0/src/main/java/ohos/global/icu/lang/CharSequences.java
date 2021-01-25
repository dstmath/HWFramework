package ohos.global.icu.lang;

import ohos.global.icu.impl.UCharacterProperty;
import ohos.global.icu.text.UTF16;

@Deprecated
public class CharSequences {
    @Deprecated
    public static int matchAfter(CharSequence charSequence, CharSequence charSequence2, int i, int i2) {
        int length = charSequence.length();
        int length2 = charSequence2.length();
        int i3 = i2;
        int i4 = i;
        while (i4 < length && i3 < length2 && charSequence.charAt(i4) == charSequence2.charAt(i3)) {
            i4++;
            i3++;
        }
        int i5 = i4 - i;
        return (i5 == 0 || onCharacterBoundary(charSequence, i4) || onCharacterBoundary(charSequence2, i3)) ? i5 : i5 - 1;
    }

    @Deprecated
    public int codePointLength(CharSequence charSequence) {
        return Character.codePointCount(charSequence, 0, charSequence.length());
    }

    @Deprecated
    public static final boolean equals(int i, CharSequence charSequence) {
        if (charSequence == null) {
            return false;
        }
        int length = charSequence.length();
        return length != 1 ? length == 2 && i > 65535 && i == Character.codePointAt(charSequence, 0) : i == charSequence.charAt(0);
    }

    @Deprecated
    public static final boolean equals(CharSequence charSequence, int i) {
        return equals(i, charSequence);
    }

    @Deprecated
    public static int compare(CharSequence charSequence, int i) {
        int charAt;
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException();
        }
        int length = charSequence.length();
        if (length == 0) {
            return -1;
        }
        char charAt2 = charSequence.charAt(0);
        int i2 = i - 65536;
        if (i2 < 0) {
            int i3 = charAt2 - i;
            return i3 != 0 ? i3 : length - 1;
        }
        int i4 = charAt2 - ((char) ((i2 >>> 10) + 55296));
        if (i4 != 0) {
            return i4;
        }
        return (length <= 1 || (charAt = charSequence.charAt(1) - ((char) ((i2 & UCharacterProperty.MAX_SCRIPT) + UTF16.TRAIL_SURROGATE_MIN_VALUE))) == 0) ? length - 2 : charAt;
    }

    @Deprecated
    public static int compare(int i, CharSequence charSequence) {
        int compare = compare(charSequence, i);
        if (compare > 0) {
            return -1;
        }
        return compare < 0 ? 1 : 0;
    }

    @Deprecated
    public static int getSingleCodePoint(CharSequence charSequence) {
        int length = charSequence.length();
        boolean z = true;
        if (length < 1 || length > 2) {
            return Integer.MAX_VALUE;
        }
        int codePointAt = Character.codePointAt(charSequence, 0);
        boolean z2 = codePointAt < 65536;
        if (length != 1) {
            z = false;
        }
        if (z2 == z) {
            return codePointAt;
        }
        return Integer.MAX_VALUE;
    }

    @Deprecated
    public static final <T> boolean equals(T t, T t2) {
        if (t == null) {
            return t2 == null;
        }
        if (t2 == null) {
            return false;
        }
        return t.equals(t2);
    }

    @Deprecated
    public static int compare(CharSequence charSequence, CharSequence charSequence2) {
        int length = charSequence.length();
        int length2 = charSequence2.length();
        int i = length <= length2 ? length : length2;
        for (int i2 = 0; i2 < i; i2++) {
            int charAt = charSequence.charAt(i2) - charSequence2.charAt(i2);
            if (charAt != 0) {
                return charAt;
            }
        }
        return length - length2;
    }

    @Deprecated
    public static boolean equalsChars(CharSequence charSequence, CharSequence charSequence2) {
        return charSequence.length() == charSequence2.length() && compare(charSequence, charSequence2) == 0;
    }

    @Deprecated
    public static boolean onCharacterBoundary(CharSequence charSequence, int i) {
        return i <= 0 || i >= charSequence.length() || !Character.isHighSurrogate(charSequence.charAt(i + -1)) || !Character.isLowSurrogate(charSequence.charAt(i));
    }

    @Deprecated
    public static int indexOf(CharSequence charSequence, int i) {
        int i2 = 0;
        while (i2 < charSequence.length()) {
            int codePointAt = Character.codePointAt(charSequence, i2);
            if (codePointAt == i) {
                return i2;
            }
            i2 += Character.charCount(codePointAt);
        }
        return -1;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: int[] */
    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v1, types: [char] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Deprecated
    public static int[] codePoints(CharSequence charSequence) {
        int[] iArr = new int[charSequence.length()];
        int i = 0;
        for (int i2 = 0; i2 < charSequence.length(); i2++) {
            ?? charAt = charSequence.charAt(i2);
            if (charAt >= 56320 && charAt <= 57343 && i2 != 0) {
                int i3 = i - 1;
                char c = (char) iArr[i3];
                if (c >= 55296 && c <= 56319) {
                    iArr[i3] = Character.toCodePoint(c, charAt);
                }
            }
            iArr[i] = charAt;
            i++;
        }
        if (i == iArr.length) {
            return iArr;
        }
        int[] iArr2 = new int[i];
        System.arraycopy(iArr, 0, iArr2, 0, i);
        return iArr2;
    }

    private CharSequences() {
    }
}
