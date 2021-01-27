package ohos.global.icu.impl;

import ohos.global.icu.text.StringPrepParseException;
import ohos.global.icu.text.UTF16;

public final class Punycode {
    private static final int BASE = 36;
    private static final int CAPITAL_A = 65;
    private static final int CAPITAL_Z = 90;
    private static final int DAMP = 700;
    private static final char DELIMITER = '-';
    private static final char HYPHEN = '-';
    private static final int INITIAL_BIAS = 72;
    private static final int INITIAL_N = 128;
    private static final int SKEW = 38;
    private static final int SMALL_A = 97;
    private static final int SMALL_Z = 122;
    private static final int TMAX = 26;
    private static final int TMIN = 1;
    private static final int ZERO = 48;
    static final int[] basicToDigit = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    private static char asciiCaseMap(char c, boolean z) {
        int i;
        if (z) {
            if ('a' > c || c > 'z') {
                return c;
            }
            i = c - ' ';
        } else if ('A' > c || c > 'Z') {
            return c;
        } else {
            i = c + ' ';
        }
        return (char) i;
    }

    private static char digitToBasic(int i, boolean z) {
        return (char) (i < 26 ? z ? i + 65 : i + 97 : i + 22);
    }

    private static boolean isBasic(int i) {
        return i < 128;
    }

    private static boolean isBasicUpperCase(int i) {
        return 65 <= i && i >= 90;
    }

    private static boolean isSurrogate(int i) {
        return (i & -2048) == 55296;
    }

    private static int adaptBias(int i, int i2, boolean z) {
        int i3;
        if (z) {
            i3 = i / 700;
        } else {
            i3 = i / 2;
        }
        int i4 = i3 + (i3 / i2);
        int i5 = 0;
        while (i4 > 455) {
            i4 /= 35;
            i5 += 36;
        }
        return i5 + ((i4 * 36) / (i4 + 38));
    }

    /*  JADX ERROR: JadxOverflowException in pass: LoopRegionVisitor
        jadx.core.utils.exceptions.JadxOverflowException: LoopRegionVisitor.assignOnlyInLoop endless recursion
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    public static java.lang.StringBuilder encode(java.lang.CharSequence r18, boolean[] r19) throws ohos.global.icu.text.StringPrepParseException {
        /*
        // Method dump skipped, instructions count: 259
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.global.icu.impl.Punycode.encode(java.lang.CharSequence, boolean[]):java.lang.StringBuilder");
    }

    public static StringBuilder decode(CharSequence charSequence, boolean[] zArr) throws StringPrepParseException {
        int i;
        int length = charSequence.length();
        StringBuilder sb = new StringBuilder(charSequence.length());
        int i2 = length;
        while (i2 > 0) {
            i2--;
            if (charSequence.charAt(i2) == '-') {
                break;
            }
        }
        for (int i3 = 0; i3 < i2; i3++) {
            char charAt = charSequence.charAt(i3);
            if (isBasic(charAt)) {
                sb.append(charAt);
                if (zArr != null && i3 < zArr.length) {
                    zArr[i3] = isBasicUpperCase(charAt);
                }
            } else {
                throw new StringPrepParseException("Illegal char found", 0);
            }
        }
        int i4 = 72;
        int i5 = i2 > 0 ? i2 + 1 : 0;
        int i6 = 1000000000;
        int i7 = 128;
        int i8 = i2;
        int i9 = 0;
        while (i5 < length) {
            int i10 = 1;
            int i11 = i9;
            int i12 = 1;
            int i13 = 36;
            while (i5 < length) {
                int i14 = i5 + 1;
                int i15 = basicToDigit[charSequence.charAt(i5) & 255];
                if (i15 < 0) {
                    throw new StringPrepParseException("Invalid char found", 0);
                } else if (i15 <= (Integer.MAX_VALUE - i11) / i12) {
                    i11 += i15 * i12;
                    int i16 = i13 - i4;
                    if (i16 >= i10) {
                        i10 = i13 >= i4 + 26 ? 26 : i16;
                    }
                    if (i15 < i10) {
                        i8++;
                        i4 = adaptBias(i11 - i9, i8, i9 == 0);
                        int i17 = i11 / i8;
                        if (i17 <= Integer.MAX_VALUE - i7) {
                            i7 += i17;
                            int i18 = i11 % i8;
                            if (i7 > 1114111 || isSurrogate(i7)) {
                                throw new StringPrepParseException("Illegal char found", 1);
                            }
                            int charCount = Character.charCount(i7);
                            if (i18 > i6) {
                                i = sb.offsetByCodePoints(i6, i18 - i6);
                            } else if (charCount > 1) {
                                i = i18;
                                i6 = i;
                            } else {
                                i6++;
                                i = i18;
                            }
                            if (zArr != null && sb.length() + charCount <= zArr.length) {
                                if (i < sb.length()) {
                                    System.arraycopy(zArr, i, zArr, i + charCount, sb.length() - i);
                                }
                                zArr[i] = isBasicUpperCase(charSequence.charAt(i14 - 1));
                                if (charCount == 2) {
                                    zArr[i + 1] = false;
                                }
                            }
                            if (charCount == 1) {
                                sb.insert(i, (char) i7);
                            } else {
                                sb.insert(i, UTF16.getLeadSurrogate(i7));
                                sb.insert(i + 1, UTF16.getTrailSurrogate(i7));
                            }
                            i9 = i18 + 1;
                            i5 = i14;
                        } else {
                            throw new StringPrepParseException("Illegal char found", 1);
                        }
                    } else {
                        int i19 = 36 - i10;
                        if (i12 <= Integer.MAX_VALUE / i19) {
                            i12 *= i19;
                            i13 += 36;
                            i10 = 1;
                            i5 = i14;
                        } else {
                            throw new StringPrepParseException("Illegal char found", 1);
                        }
                    }
                } else {
                    throw new StringPrepParseException("Illegal char found", i10);
                }
            }
            throw new StringPrepParseException("Illegal char found", i10);
        }
        return sb;
    }
}
