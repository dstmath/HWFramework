package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.StringPrepParseException;
import android.icu.text.UTF16;

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

    private static int adaptBias(int delta, int length, boolean firstTime) {
        int delta2;
        if (firstTime) {
            delta2 = delta / DAMP;
        } else {
            delta2 = delta / 2;
        }
        int delta3 = delta2 + (delta2 / length);
        int count = 0;
        while (delta3 > 455) {
            delta3 /= 35;
            count += 36;
        }
        return ((36 * delta3) / (delta3 + 38)) + count;
    }

    private static char asciiCaseMap(char b, boolean uppercase) {
        if (uppercase) {
            if ('a' > b || b > 'z') {
                return b;
            }
            return (char) (b - ' ');
        } else if ('A' > b || b > 'Z') {
            return b;
        } else {
            return (char) (b + ' ');
        }
    }

    private static char digitToBasic(int digit, boolean uppercase) {
        if (digit >= 26) {
            return (char) (22 + digit);
        }
        if (uppercase) {
            return (char) (65 + digit);
        }
        return (char) (97 + digit);
    }

    public static StringBuilder encode(CharSequence src, boolean[] caseFlags) throws StringPrepParseException {
        int i;
        int n;
        CharSequence charSequence = src;
        int srcLength = src.length();
        int[] cpBuffer = new int[srcLength];
        StringBuilder dest = new StringBuilder(srcLength);
        int srcCPCount = 0;
        int j = 0;
        while (j < srcLength) {
            char c = charSequence.charAt(j);
            if (isBasic(c)) {
                int srcCPCount2 = srcCPCount + 1;
                cpBuffer[srcCPCount] = 0;
                dest.append(caseFlags != null ? asciiCaseMap(c, caseFlags[j]) : c);
                srcCPCount = srcCPCount2;
            } else {
                int n2 = ((caseFlags == null || !caseFlags[j]) ? 0 : 1) << 31;
                if (!UTF16.isSurrogate(c)) {
                    n = n2 | c;
                } else {
                    if (UTF16.isLeadSurrogate(c) && j + 1 < srcLength) {
                        char charAt = charSequence.charAt(j + 1);
                        char c2 = charAt;
                        if (UTF16.isTrailSurrogate(charAt)) {
                            j++;
                            n = n2 | UCharacter.getCodePoint(c, c2);
                        }
                    }
                    throw new StringPrepParseException("Illegal char found", 1);
                }
                cpBuffer[srcCPCount] = n;
                srcCPCount++;
            }
            j++;
        }
        int basicLength = dest.length();
        if (basicLength > 0) {
            dest.append('-');
        }
        int bias = 72;
        int delta = 0;
        int delta2 = 128;
        int n3 = j;
        int handledCPCount = basicLength;
        while (handledCPCount < srcCPCount) {
            int m = Integer.MAX_VALUE;
            int j2 = 0;
            while (true) {
                i = Integer.MAX_VALUE;
                if (j2 >= srcCPCount) {
                    break;
                }
                int q = cpBuffer[j2] & Integer.MAX_VALUE;
                if (delta2 <= q && q < m) {
                    m = q;
                }
                j2++;
            }
            if (m - delta2 <= (Integer.MAX_VALUE - delta) / (handledCPCount + 1)) {
                int delta3 = delta + ((m - delta2) * (handledCPCount + 1));
                int n4 = m;
                int j3 = 0;
                while (j3 < srcCPCount) {
                    int q2 = cpBuffer[j3] & i;
                    if (q2 < n4) {
                        delta3++;
                    } else if (q2 == n4) {
                        int q3 = delta3;
                        int k = 36;
                        while (true) {
                            int t = k - bias;
                            if (t < 1) {
                                t = 1;
                            } else if (k >= bias + 26) {
                                t = 26;
                            }
                            if (q3 < t) {
                                break;
                            }
                            dest.append(digitToBasic(((q3 - t) % (36 - t)) + t, false));
                            q3 = (q3 - t) / (36 - t);
                            k += 36;
                            CharSequence charSequence2 = src;
                        }
                        dest.append(digitToBasic(q3, cpBuffer[j3] < 0));
                        bias = adaptBias(delta3, handledCPCount + 1, handledCPCount == basicLength);
                        handledCPCount++;
                        delta3 = 0;
                        int i2 = q3;
                    }
                    j3++;
                    CharSequence charSequence3 = src;
                    i = Integer.MAX_VALUE;
                }
                delta = delta3 + 1;
                delta2 = n4 + 1;
                CharSequence charSequence4 = src;
            } else {
                throw new IllegalStateException("Internal program error");
            }
        }
        return dest;
    }

    private static boolean isBasic(int ch) {
        return ch < 128;
    }

    private static boolean isBasicUpperCase(int ch) {
        return 65 <= ch && ch >= 90;
    }

    private static boolean isSurrogate(int ch) {
        return (ch & -2048) == 55296;
    }

    public static StringBuilder decode(CharSequence src, boolean[] caseFlags) throws StringPrepParseException {
        int j;
        boolean z;
        int destCPCount;
        int codeUnitIndex;
        int basicLength;
        CharSequence charSequence = src;
        boolean[] zArr = caseFlags;
        int srcLength = src.length();
        StringBuilder dest = new StringBuilder(src.length());
        int j2 = srcLength;
        while (j2 > 0) {
            j2--;
            if (charSequence.charAt(j2) == '-') {
                break;
            }
        }
        int destCPCount2 = j2;
        int basicLength2 = j2;
        int j3 = 0;
        while (j3 < basicLength2) {
            char b = charSequence.charAt(j3);
            if (isBasic(b)) {
                dest.append(b);
                if (zArr != null && j3 < zArr.length) {
                    zArr[j3] = isBasicUpperCase(b);
                }
                j3++;
            } else {
                throw new StringPrepParseException("Illegal char found", 0);
            }
        }
        int n = 128;
        int k = 0;
        int bias = 72;
        int firstSupplementaryIndex = 1000000000;
        int in = basicLength2 > 0 ? basicLength2 + 1 : 0;
        while (in < srcLength) {
            int oldi = k;
            int w = 1;
            int i = k;
            int k2 = 36;
            while (in < srcLength) {
                int in2 = in + 1;
                int digit = basicToDigit[charSequence.charAt(in) & 255];
                if (digit < 0) {
                    int i2 = j3;
                    int i3 = basicLength2;
                    throw new StringPrepParseException("Invalid char found", 0);
                } else if (digit <= (Integer.MAX_VALUE - i) / w) {
                    i += digit * w;
                    int t = k2 - bias;
                    int srcLength2 = srcLength;
                    if (t < 1) {
                        t = 1;
                    } else if (k2 >= bias + 26) {
                        t = 26;
                    }
                    if (digit < t) {
                        int destCPCount3 = destCPCount2 + 1;
                        int i4 = i - oldi;
                        if (oldi == 0) {
                            j = j3;
                            z = true;
                        } else {
                            j = j3;
                            z = false;
                        }
                        bias = adaptBias(i4, destCPCount3, z);
                        if (i / destCPCount3 <= Integer.MAX_VALUE - n) {
                            n += i / destCPCount3;
                            int i5 = i % destCPCount3;
                            if (n > 1114111 || isSurrogate(n)) {
                                int i6 = basicLength2;
                                throw new StringPrepParseException("Illegal char found", 1);
                            }
                            int cpLength = Character.charCount(n);
                            if (i5 <= firstSupplementaryIndex) {
                                codeUnitIndex = i5;
                                destCPCount = destCPCount3;
                                if (cpLength > 1) {
                                    firstSupplementaryIndex = codeUnitIndex;
                                } else {
                                    firstSupplementaryIndex++;
                                }
                            } else {
                                destCPCount = destCPCount3;
                                codeUnitIndex = dest.offsetByCodePoints(firstSupplementaryIndex, i5 - firstSupplementaryIndex);
                            }
                            if (zArr != null) {
                                basicLength = basicLength2;
                                if (dest.length() + cpLength <= zArr.length) {
                                    if (codeUnitIndex < dest.length()) {
                                        System.arraycopy(zArr, codeUnitIndex, zArr, codeUnitIndex + cpLength, dest.length() - codeUnitIndex);
                                    }
                                    zArr[codeUnitIndex] = isBasicUpperCase(charSequence.charAt(in2 - 1));
                                    if (cpLength == 2) {
                                        zArr[codeUnitIndex + 1] = false;
                                    }
                                }
                            } else {
                                basicLength = basicLength2;
                            }
                            if (cpLength == 1) {
                                dest.insert(codeUnitIndex, (char) n);
                            } else {
                                dest.insert(codeUnitIndex, UTF16.getLeadSurrogate(n));
                                dest.insert(codeUnitIndex + 1, UTF16.getTrailSurrogate(n));
                            }
                            k = i5 + 1;
                            in = in2;
                            srcLength = srcLength2;
                            j3 = j;
                            destCPCount2 = destCPCount;
                            basicLength2 = basicLength;
                        } else {
                            int i7 = basicLength2;
                            throw new StringPrepParseException("Illegal char found", 1);
                        }
                    } else {
                        int j4 = j3;
                        int basicLength3 = basicLength2;
                        if (w <= Integer.MAX_VALUE / (36 - t)) {
                            w *= 36 - t;
                            k2 += 36;
                            in = in2;
                            srcLength = srcLength2;
                            j3 = j4;
                            basicLength2 = basicLength3;
                        } else {
                            throw new StringPrepParseException("Illegal char found", 1);
                        }
                    }
                } else {
                    int i8 = j3;
                    int i9 = basicLength2;
                    throw new StringPrepParseException("Illegal char found", 1);
                }
            }
            int i10 = j3;
            int i11 = basicLength2;
            throw new StringPrepParseException("Illegal char found", 1);
        }
        int i12 = j3;
        int i13 = basicLength2;
        return dest;
    }
}
