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
    static final int[] basicToDigit = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    private static int adaptBias(int delta, int length, boolean firstTime) {
        if (firstTime) {
            delta /= DAMP;
        } else {
            delta /= 2;
        }
        delta += delta / length;
        int count = 0;
        while (delta > 455) {
            delta /= 35;
            count += 36;
        }
        return ((delta * 36) / (delta + 38)) + count;
    }

    private static char asciiCaseMap(char b, boolean uppercase) {
        if (uppercase) {
            if ('a' > b || b > 'z') {
                return b;
            }
            return (char) (b - 32);
        } else if ('A' > b || b > 'Z') {
            return b;
        } else {
            return (char) (b + 32);
        }
    }

    private static char digitToBasic(int digit, boolean uppercase) {
        if (digit >= 26) {
            return (char) (digit + 22);
        }
        if (uppercase) {
            return (char) (digit + 65);
        }
        return (char) (digit + 97);
    }

    public static StringBuilder encode(CharSequence src, boolean[] caseFlags) throws StringPrepParseException {
        int srcLength = src.length();
        int[] cpBuffer = new int[srcLength];
        StringBuilder dest = new StringBuilder(srcLength);
        int srcCPCount = 0;
        int j = 0;
        while (true) {
            int srcCPCount2 = srcCPCount;
            int n;
            if (j < srcLength) {
                char c = src.charAt(j);
                if (isBasic(c)) {
                    srcCPCount = srcCPCount2 + 1;
                    cpBuffer[srcCPCount2] = 0;
                    if (caseFlags != null) {
                        c = asciiCaseMap(c, caseFlags[j]);
                    }
                    dest.append(c);
                } else {
                    int i = (caseFlags == null || !caseFlags[j]) ? 0 : 1;
                    n = i << 31;
                    if (UTF16.isSurrogate(c)) {
                        if (!UTF16.isLeadSurrogate(c) || j + 1 >= srcLength) {
                            break;
                        }
                        char c2 = src.charAt(j + 1);
                        if (!UTF16.isTrailSurrogate(c2)) {
                            break;
                        }
                        j++;
                        n |= UCharacter.getCodePoint(c, c2);
                    } else {
                        n |= c;
                    }
                    srcCPCount = srcCPCount2 + 1;
                    cpBuffer[srcCPCount2] = n;
                }
                j++;
            } else {
                int basicLength = dest.length();
                if (basicLength > 0) {
                    dest.append('-');
                }
                n = 128;
                int delta = 0;
                int bias = 72;
                int handledCPCount = basicLength;
                while (handledCPCount < srcCPCount2) {
                    int q;
                    int m = Integer.MAX_VALUE;
                    for (j = 0; j < srcCPCount2; j++) {
                        q = cpBuffer[j] & Integer.MAX_VALUE;
                        if (n <= q && q < m) {
                            m = q;
                        }
                    }
                    if (m - n > (Integer.MAX_VALUE - delta) / (handledCPCount + 1)) {
                        throw new IllegalStateException("Internal program error");
                    }
                    delta += (m - n) * (handledCPCount + 1);
                    n = m;
                    for (j = 0; j < srcCPCount2; j++) {
                        q = cpBuffer[j] & Integer.MAX_VALUE;
                        if (q < n) {
                            delta++;
                        } else if (q == n) {
                            q = delta;
                            int k = 36;
                            while (true) {
                                int t = k - bias;
                                if (t < 1) {
                                    t = 1;
                                } else if (k >= bias + 26) {
                                    t = 26;
                                }
                                if (q < t) {
                                    break;
                                }
                                dest.append(digitToBasic(((q - t) % (36 - t)) + t, false));
                                q = (q - t) / (36 - t);
                                k += 36;
                            }
                            dest.append(digitToBasic(q, cpBuffer[j] < 0));
                            bias = adaptBias(delta, handledCPCount + 1, handledCPCount == basicLength);
                            delta = 0;
                            handledCPCount++;
                        }
                    }
                    delta++;
                    n++;
                }
                return dest;
            }
        }
        throw new StringPrepParseException("Illegal char found", 1);
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

    /* JADX WARNING: Missing block: B:41:0x00d2, code:
            r10 = r10 + 1;
            r24 = r13 - r19;
     */
    /* JADX WARNING: Missing block: B:42:0x00d6, code:
            if (r19 != 0) goto L_0x0126;
     */
    /* JADX WARNING: Missing block: B:43:0x00d8, code:
            r23 = true;
     */
    /* JADX WARNING: Missing block: B:44:0x00da, code:
            r6 = adaptBias(r24, r10, r23);
     */
    /* JADX WARNING: Missing block: B:45:0x00ed, code:
            if ((r13 / r10) <= (Integer.MAX_VALUE - r18)) goto L_0x0129;
     */
    /* JADX WARNING: Missing block: B:47:0x00f9, code:
            throw new android.icu.text.StringPrepParseException("Illegal char found", 1);
     */
    /* JADX WARNING: Missing block: B:56:0x0126, code:
            r23 = false;
     */
    /* JADX WARNING: Missing block: B:57:0x0129, code:
            r18 = r18 + (r13 / r10);
            r13 = r13 % r10;
     */
    /* JADX WARNING: Missing block: B:58:0x0135, code:
            if (r18 > 1114111) goto L_0x013d;
     */
    /* JADX WARNING: Missing block: B:60:0x013b, code:
            if (isSurrogate(r18) == false) goto L_0x0148;
     */
    /* JADX WARNING: Missing block: B:62:0x0147, code:
            throw new android.icu.text.StringPrepParseException("Illegal char found", 1);
     */
    /* JADX WARNING: Missing block: B:63:0x0148, code:
            r8 = java.lang.Character.charCount(r18);
     */
    /* JADX WARNING: Missing block: B:64:0x014c, code:
            if (r13 > r12) goto L_0x01b7;
     */
    /* JADX WARNING: Missing block: B:65:0x014e, code:
            r7 = r13;
     */
    /* JADX WARNING: Missing block: B:66:0x0153, code:
            if (r8 <= 1) goto L_0x01b4;
     */
    /* JADX WARNING: Missing block: B:67:0x0155, code:
            r12 = r13;
     */
    /* JADX WARNING: Missing block: B:68:0x0156, code:
            if (r27 == null) goto L_0x01a0;
     */
    /* JADX WARNING: Missing block: B:70:0x0167, code:
            if ((r9.length() + r8) > r27.length) goto L_0x01a0;
     */
    /* JADX WARNING: Missing block: B:72:0x016f, code:
            if (r7 >= r9.length()) goto L_0x0184;
     */
    /* JADX WARNING: Missing block: B:73:0x0171, code:
            java.lang.System.arraycopy(r27, r7, r27, r7 + r8, r9.length() - r7);
     */
    /* JADX WARNING: Missing block: B:74:0x0184, code:
            r27[r7] = isBasicUpperCase(r26.charAt(r14 - 1));
     */
    /* JADX WARNING: Missing block: B:75:0x0198, code:
            if (r8 != 2) goto L_0x01a0;
     */
    /* JADX WARNING: Missing block: B:76:0x019a, code:
            r27[r7 + 1] = false;
     */
    /* JADX WARNING: Missing block: B:78:0x01a4, code:
            if (r8 != 1) goto L_0x01c0;
     */
    /* JADX WARNING: Missing block: B:79:0x01a6, code:
            r9.insert(r7, (char) r18);
     */
    /* JADX WARNING: Missing block: B:80:0x01b0, code:
            r13 = r13 + 1;
     */
    /* JADX WARNING: Missing block: B:81:0x01b4, code:
            r12 = r12 + 1;
     */
    /* JADX WARNING: Missing block: B:82:0x01b7, code:
            r7 = r9.offsetByCodePoints(r12, r13 - r12);
     */
    /* JADX WARNING: Missing block: B:83:0x01c0, code:
            r9.insert(r7, android.icu.text.UTF16.getLeadSurrogate(r18));
            r9.insert(r7 + 1, android.icu.text.UTF16.getTrailSurrogate(r18));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static StringBuilder decode(CharSequence src, boolean[] caseFlags) throws StringPrepParseException {
        int srcLength = src.length();
        StringBuilder dest = new StringBuilder(src.length());
        int j = srcLength;
        while (j > 0) {
            j--;
            if (src.charAt(j) == '-') {
                break;
            }
        }
        int destCPCount = j;
        int basicLength = j;
        j = 0;
        while (j < basicLength) {
            char b = src.charAt(j);
            if (isBasic(b)) {
                dest.append(b);
                if (caseFlags != null && j < caseFlags.length) {
                    caseFlags[j] = isBasicUpperCase(b);
                }
                j++;
            } else {
                throw new StringPrepParseException("Illegal char found", 0);
            }
        }
        int n = 128;
        int i = 0;
        int bias = 72;
        int firstSupplementaryIndex = 1000000000;
        int in = basicLength > 0 ? basicLength + 1 : 0;
        while (in < srcLength) {
            int oldi = i;
            int w = 1;
            int k = 36;
            while (true) {
                int i2 = in;
                if (i2 >= srcLength) {
                    throw new StringPrepParseException("Illegal char found", 1);
                }
                in = i2 + 1;
                int digit = basicToDigit[src.charAt(i2) & 255];
                if (digit < 0) {
                    throw new StringPrepParseException("Invalid char found", 0);
                } else if (digit > (Integer.MAX_VALUE - i) / w) {
                    throw new StringPrepParseException("Illegal char found", 1);
                } else {
                    i += digit * w;
                    int t = k - bias;
                    if (t < 1) {
                        t = 1;
                    } else if (k >= bias + 26) {
                        t = 26;
                    }
                    if (digit < t) {
                        break;
                    } else if (w > Integer.MAX_VALUE / (36 - t)) {
                        throw new StringPrepParseException("Illegal char found", 1);
                    } else {
                        w *= 36 - t;
                        k += 36;
                    }
                }
            }
        }
        return dest;
    }
}
