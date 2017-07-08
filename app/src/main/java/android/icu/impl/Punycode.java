package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.StringPrepParseException;
import android.icu.text.UTF16;
import android.icu.util.AnnualTimeZoneRule;
import dalvik.bytecode.Opcodes;

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
    static final int[] basicToDigit = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.Punycode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.Punycode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Punycode.<clinit>():void");
    }

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
            count += BASE;
        }
        return ((delta * BASE) / (delta + SKEW)) + count;
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
        if (digit >= TMAX) {
            return (char) (digit + 22);
        }
        if (uppercase) {
            return (char) (digit + CAPITAL_A);
        }
        return (char) (digit + SMALL_A);
    }

    public static StringBuilder encode(CharSequence src, boolean[] caseFlags) throws StringPrepParseException {
        int n;
        int srcLength = src.length();
        int[] cpBuffer = new int[srcLength];
        StringBuilder dest = new StringBuilder(srcLength);
        int j = 0;
        int srcCPCount = 0;
        while (j < srcLength) {
            int srcCPCount2;
            char c = src.charAt(j);
            if (isBasic(c)) {
                srcCPCount2 = srcCPCount + TMIN;
                cpBuffer[srcCPCount] = 0;
                if (caseFlags != null) {
                    c = asciiCaseMap(c, caseFlags[j]);
                }
                dest.append(c);
            } else {
                int i = (caseFlags == null || !caseFlags[j]) ? 0 : TMIN;
                n = i << 31;
                if (UTF16.isSurrogate(c)) {
                    if (UTF16.isLeadSurrogate(c) && j + TMIN < srcLength) {
                        char c2 = src.charAt(j + TMIN);
                        if (UTF16.isTrailSurrogate(c2)) {
                            j += TMIN;
                            n |= UCharacter.getCodePoint(c, c2);
                        }
                    }
                    throw new StringPrepParseException("Illegal char found", TMIN);
                }
                n |= c;
                srcCPCount2 = srcCPCount + TMIN;
                cpBuffer[srcCPCount] = n;
            }
            j += TMIN;
            srcCPCount = srcCPCount2;
        }
        int basicLength = dest.length();
        if (basicLength > 0) {
            dest.append(HYPHEN);
        }
        n = INITIAL_N;
        int delta = 0;
        int bias = INITIAL_BIAS;
        int handledCPCount = basicLength;
        while (handledCPCount < srcCPCount) {
            int m = AnnualTimeZoneRule.MAX_YEAR;
            for (j = 0; j < srcCPCount; j += TMIN) {
                int q = cpBuffer[j] & AnnualTimeZoneRule.MAX_YEAR;
                if (n <= q && q < m) {
                    m = q;
                }
            }
            if (m - n > (AnnualTimeZoneRule.MAX_YEAR - delta) / (handledCPCount + TMIN)) {
                throw new IllegalStateException("Internal program error");
            }
            delta += (m - n) * (handledCPCount + TMIN);
            n = m;
            for (j = 0; j < srcCPCount; j += TMIN) {
                q = cpBuffer[j] & AnnualTimeZoneRule.MAX_YEAR;
                if (q < n) {
                    delta += TMIN;
                } else if (q == n) {
                    q = delta;
                    int k = BASE;
                    while (true) {
                        int t = k - bias;
                        if (t < TMIN) {
                            t = TMIN;
                        } else if (k >= bias + TMAX) {
                            t = TMAX;
                        }
                        if (q < t) {
                            break;
                        }
                        dest.append(digitToBasic(((q - t) % (36 - t)) + t, false));
                        q = (q - t) / (36 - t);
                        k += BASE;
                    }
                    dest.append(digitToBasic(q, cpBuffer[j] < 0));
                    bias = adaptBias(delta, handledCPCount + TMIN, handledCPCount == basicLength);
                    delta = 0;
                    handledCPCount += TMIN;
                }
            }
            delta += TMIN;
            n += TMIN;
        }
        return dest;
    }

    private static boolean isBasic(int ch) {
        return ch < INITIAL_N;
    }

    private static boolean isBasicUpperCase(int ch) {
        return CAPITAL_A <= ch && ch >= CAPITAL_Z;
    }

    private static boolean isSurrogate(int ch) {
        return (ch & -2048) == UTF16.SURROGATE_MIN_VALUE;
    }

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
                j += TMIN;
            } else {
                throw new StringPrepParseException("Illegal char found", 0);
            }
        }
        int n = INITIAL_N;
        int i = 0;
        int bias = INITIAL_BIAS;
        int firstSupplementaryIndex = 1000000000;
        int in = basicLength > 0 ? basicLength + TMIN : 0;
        while (in < srcLength) {
            int oldi = i;
            int w = TMIN;
            int k = BASE;
            int in2 = in;
            while (in2 < srcLength) {
                in = in2 + TMIN;
                int digit = basicToDigit[src.charAt(in2) & Opcodes.OP_CONST_CLASS_JUMBO];
                if (digit < 0) {
                    throw new StringPrepParseException("Invalid char found", 0);
                } else if (digit > (AnnualTimeZoneRule.MAX_YEAR - i) / w) {
                    throw new StringPrepParseException("Illegal char found", TMIN);
                } else {
                    i += digit * w;
                    int t = k - bias;
                    if (t < TMIN) {
                        t = TMIN;
                    } else if (k >= bias + TMAX) {
                        t = TMAX;
                    }
                    if (digit < t) {
                        destCPCount += TMIN;
                        bias = adaptBias(i - oldi, destCPCount, oldi == 0);
                        if (i / destCPCount > AnnualTimeZoneRule.MAX_YEAR - n) {
                            throw new StringPrepParseException("Illegal char found", TMIN);
                        }
                        n += i / destCPCount;
                        i %= destCPCount;
                        if (n > 1114111 || isSurrogate(n)) {
                            throw new StringPrepParseException("Illegal char found", TMIN);
                        }
                        int codeUnitIndex;
                        int cpLength = Character.charCount(n);
                        if (i <= firstSupplementaryIndex) {
                            codeUnitIndex = i;
                            if (cpLength > TMIN) {
                                firstSupplementaryIndex = i;
                            } else {
                                firstSupplementaryIndex += TMIN;
                            }
                        } else {
                            codeUnitIndex = dest.offsetByCodePoints(firstSupplementaryIndex, i - firstSupplementaryIndex);
                        }
                        if (caseFlags != null && dest.length() + cpLength <= caseFlags.length) {
                            if (codeUnitIndex < dest.length()) {
                                System.arraycopy(caseFlags, codeUnitIndex, caseFlags, codeUnitIndex + cpLength, dest.length() - codeUnitIndex);
                            }
                            caseFlags[codeUnitIndex] = isBasicUpperCase(src.charAt(in - 1));
                            if (cpLength == 2) {
                                caseFlags[codeUnitIndex + TMIN] = false;
                            }
                        }
                        if (cpLength == TMIN) {
                            dest.insert(codeUnitIndex, (char) n);
                        } else {
                            dest.insert(codeUnitIndex, UTF16.getLeadSurrogate(n));
                            dest.insert(codeUnitIndex + TMIN, UTF16.getTrailSurrogate(n));
                        }
                        i += TMIN;
                    } else if (w > AnnualTimeZoneRule.MAX_YEAR / (36 - t)) {
                        throw new StringPrepParseException("Illegal char found", TMIN);
                    } else {
                        w *= 36 - t;
                        k += BASE;
                        in2 = in;
                    }
                }
            }
            throw new StringPrepParseException("Illegal char found", TMIN);
        }
        return dest;
    }
}
