package com.huawei.zxing.datamatrix.encoder;

import com.huawei.zxing.Dimension;
import java.util.Arrays;

public final class HighLevelEncoder {
    static final int ASCII_ENCODATION = 0;
    static final int BASE256_ENCODATION = 5;
    static final int C40_ENCODATION = 1;
    static final char C40_UNLATCH = '\u00fe';
    static final int EDIFACT_ENCODATION = 4;
    static final char LATCH_TO_ANSIX12 = '\u00ee';
    static final char LATCH_TO_BASE256 = '\u00e7';
    static final char LATCH_TO_C40 = '\u00e6';
    static final char LATCH_TO_EDIFACT = '\u00f0';
    static final char LATCH_TO_TEXT = '\u00ef';
    private static final char MACRO_05 = '\u00ec';
    private static final String MACRO_05_HEADER = "[)>\u001e05\u001d";
    private static final char MACRO_06 = '\u00ed';
    private static final String MACRO_06_HEADER = "[)>\u001e06\u001d";
    private static final String MACRO_TRAILER = "\u001e\u0004";
    private static final char PAD = '\u0081';
    static final int TEXT_ENCODATION = 2;
    static final char UPPER_SHIFT = '\u00eb';
    static final int X12_ENCODATION = 3;
    static final char X12_UNLATCH = '\u00fe';

    private HighLevelEncoder() {
    }

    private static char randomize253State(char ch, int codewordPosition) {
        int tempVariable = ch + (((codewordPosition * 149) % 253) + C40_ENCODATION);
        return tempVariable <= 254 ? (char) tempVariable : (char) (tempVariable - 254);
    }

    public static String encodeHighLevel(String msg) {
        return encodeHighLevel(msg, SymbolShapeHint.FORCE_NONE, null, null);
    }

    public static String encodeHighLevel(String msg, SymbolShapeHint shape, Dimension minSize, Dimension maxSize) {
        Encoder[] encoders = new Encoder[]{new ASCIIEncoder(), new C40Encoder(), new TextEncoder(), new X12Encoder(), new EdifactEncoder(), new Base256Encoder()};
        EncoderContext context = new EncoderContext(msg);
        context.setSymbolShape(shape);
        context.setSizeConstraints(minSize, maxSize);
        if (msg.startsWith(MACRO_05_HEADER) && msg.endsWith(MACRO_TRAILER)) {
            context.writeCodeword(MACRO_05);
            context.setSkipAtEnd(TEXT_ENCODATION);
            context.pos += MACRO_05_HEADER.length();
        } else if (msg.startsWith(MACRO_06_HEADER) && msg.endsWith(MACRO_TRAILER)) {
            context.writeCodeword(MACRO_06);
            context.setSkipAtEnd(TEXT_ENCODATION);
            context.pos += MACRO_06_HEADER.length();
        }
        int encodingMode = ASCII_ENCODATION;
        while (context.hasMoreCharacters()) {
            encoders[encodingMode].encode(context);
            if (context.getNewEncoding() >= 0) {
                encodingMode = context.getNewEncoding();
                context.resetEncoderSignal();
            }
        }
        int len = context.getCodewordCount();
        context.updateSymbolInfo();
        int capacity = context.getSymbolInfo().getDataCapacity();
        if (!(len >= capacity || encodingMode == 0 || encodingMode == BASE256_ENCODATION)) {
            context.writeCodeword(X12_UNLATCH);
        }
        StringBuilder codewords = context.getCodewords();
        if (codewords.length() < capacity) {
            codewords.append(PAD);
        }
        while (codewords.length() < capacity) {
            codewords.append(randomize253State(PAD, codewords.length() + C40_ENCODATION));
        }
        return context.getCodewords().toString();
    }

    static int lookAheadTest(CharSequence msg, int startpos, int currentMode) {
        if (startpos >= msg.length()) {
            return currentMode;
        }
        float[] charCounts;
        int[] intCharCounts;
        byte[] mins;
        int minCount;
        if (currentMode == 0) {
            charCounts = new float[]{0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.25f};
        } else {
            charCounts = new float[]{1.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.25f};
            charCounts[currentMode] = 0.0f;
        }
        int charsProcessed = ASCII_ENCODATION;
        while (startpos + charsProcessed != msg.length()) {
            char c = msg.charAt(startpos + charsProcessed);
            charsProcessed += C40_ENCODATION;
            if (isDigit(c)) {
                charCounts[ASCII_ENCODATION] = (float) (((double) charCounts[ASCII_ENCODATION]) + 0.5d);
            } else if (isExtendedASCII(c)) {
                charCounts[ASCII_ENCODATION] = (float) ((int) Math.ceil((double) charCounts[ASCII_ENCODATION]));
                charCounts[ASCII_ENCODATION] = charCounts[ASCII_ENCODATION] + 2.0f;
            } else {
                charCounts[ASCII_ENCODATION] = (float) ((int) Math.ceil((double) charCounts[ASCII_ENCODATION]));
                charCounts[ASCII_ENCODATION] = charCounts[ASCII_ENCODATION] + 1.0f;
            }
            if (isNativeC40(c)) {
                charCounts[C40_ENCODATION] = charCounts[C40_ENCODATION] + 0.6666667f;
            } else if (isExtendedASCII(c)) {
                charCounts[C40_ENCODATION] = charCounts[C40_ENCODATION] + 2.6666667f;
            } else {
                charCounts[C40_ENCODATION] = charCounts[C40_ENCODATION] + 1.3333334f;
            }
            if (isNativeText(c)) {
                charCounts[TEXT_ENCODATION] = charCounts[TEXT_ENCODATION] + 0.6666667f;
            } else if (isExtendedASCII(c)) {
                charCounts[TEXT_ENCODATION] = charCounts[TEXT_ENCODATION] + 2.6666667f;
            } else {
                charCounts[TEXT_ENCODATION] = charCounts[TEXT_ENCODATION] + 1.3333334f;
            }
            if (isNativeX12(c)) {
                charCounts[X12_ENCODATION] = charCounts[X12_ENCODATION] + 0.6666667f;
            } else if (isExtendedASCII(c)) {
                charCounts[X12_ENCODATION] = charCounts[X12_ENCODATION] + 4.3333335f;
            } else {
                charCounts[X12_ENCODATION] = charCounts[X12_ENCODATION] + 3.3333333f;
            }
            if (isNativeEDIFACT(c)) {
                charCounts[EDIFACT_ENCODATION] = charCounts[EDIFACT_ENCODATION] + 0.75f;
            } else if (isExtendedASCII(c)) {
                charCounts[EDIFACT_ENCODATION] = charCounts[EDIFACT_ENCODATION] + 4.25f;
            } else {
                charCounts[EDIFACT_ENCODATION] = charCounts[EDIFACT_ENCODATION] + 3.25f;
            }
            if (isSpecialB256(c)) {
                charCounts[BASE256_ENCODATION] = charCounts[BASE256_ENCODATION] + 4.0f;
            } else {
                charCounts[BASE256_ENCODATION] = charCounts[BASE256_ENCODATION] + 1.0f;
            }
            if (charsProcessed >= EDIFACT_ENCODATION) {
                intCharCounts = new int[6];
                mins = new byte[6];
                findMinimums(charCounts, intCharCounts, Integer.MAX_VALUE, mins);
                minCount = getMinimumCount(mins);
                if (intCharCounts[ASCII_ENCODATION] < intCharCounts[BASE256_ENCODATION] && intCharCounts[ASCII_ENCODATION] < intCharCounts[C40_ENCODATION] && intCharCounts[ASCII_ENCODATION] < intCharCounts[TEXT_ENCODATION] && intCharCounts[ASCII_ENCODATION] < intCharCounts[X12_ENCODATION] && intCharCounts[ASCII_ENCODATION] < intCharCounts[EDIFACT_ENCODATION]) {
                    return ASCII_ENCODATION;
                }
                if (intCharCounts[BASE256_ENCODATION] < intCharCounts[ASCII_ENCODATION] || ((mins[C40_ENCODATION] + mins[TEXT_ENCODATION]) + mins[X12_ENCODATION]) + mins[EDIFACT_ENCODATION] == 0) {
                    return BASE256_ENCODATION;
                }
                if (minCount == C40_ENCODATION && mins[EDIFACT_ENCODATION] > null) {
                    return EDIFACT_ENCODATION;
                }
                if (minCount == C40_ENCODATION && mins[TEXT_ENCODATION] > null) {
                    return TEXT_ENCODATION;
                }
                if (minCount == C40_ENCODATION && mins[X12_ENCODATION] > null) {
                    return X12_ENCODATION;
                }
                if (intCharCounts[C40_ENCODATION] + C40_ENCODATION < intCharCounts[ASCII_ENCODATION] && intCharCounts[C40_ENCODATION] + C40_ENCODATION < intCharCounts[BASE256_ENCODATION] && intCharCounts[C40_ENCODATION] + C40_ENCODATION < intCharCounts[EDIFACT_ENCODATION] && intCharCounts[C40_ENCODATION] + C40_ENCODATION < intCharCounts[TEXT_ENCODATION]) {
                    if (intCharCounts[C40_ENCODATION] < intCharCounts[X12_ENCODATION]) {
                        return C40_ENCODATION;
                    }
                    if (intCharCounts[C40_ENCODATION] == intCharCounts[X12_ENCODATION]) {
                        for (int p = (startpos + charsProcessed) + C40_ENCODATION; p < msg.length(); p += C40_ENCODATION) {
                            char tc = msg.charAt(p);
                            if (isX12TermSep(tc)) {
                                return X12_ENCODATION;
                            }
                            if (!isNativeX12(tc)) {
                                break;
                            }
                        }
                        return C40_ENCODATION;
                    }
                }
            }
        }
        mins = new byte[6];
        intCharCounts = new int[6];
        int min = findMinimums(charCounts, intCharCounts, Integer.MAX_VALUE, mins);
        minCount = getMinimumCount(mins);
        if (intCharCounts[ASCII_ENCODATION] == min) {
            return ASCII_ENCODATION;
        }
        if (minCount == C40_ENCODATION && mins[BASE256_ENCODATION] > null) {
            return BASE256_ENCODATION;
        }
        if (minCount == C40_ENCODATION && mins[EDIFACT_ENCODATION] > null) {
            return EDIFACT_ENCODATION;
        }
        if (minCount == C40_ENCODATION && mins[TEXT_ENCODATION] > null) {
            return TEXT_ENCODATION;
        }
        if (minCount != C40_ENCODATION || mins[X12_ENCODATION] <= null) {
            return C40_ENCODATION;
        }
        return X12_ENCODATION;
    }

    private static int findMinimums(float[] charCounts, int[] intCharCounts, int min, byte[] mins) {
        Arrays.fill(mins, (byte) 0);
        for (int i = ASCII_ENCODATION; i < 6; i += C40_ENCODATION) {
            intCharCounts[i] = (int) Math.ceil((double) charCounts[i]);
            int current = intCharCounts[i];
            if (min > current) {
                min = current;
                Arrays.fill(mins, (byte) 0);
            }
            if (min == current) {
                mins[i] = (byte) (mins[i] + C40_ENCODATION);
            }
        }
        return min;
    }

    private static int getMinimumCount(byte[] mins) {
        int minCount = ASCII_ENCODATION;
        for (int i = ASCII_ENCODATION; i < 6; i += C40_ENCODATION) {
            minCount += mins[i];
        }
        return minCount;
    }

    static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    static boolean isExtendedASCII(char ch) {
        return ch >= '\u0080' && ch <= '\u00ff';
    }

    private static boolean isNativeC40(char ch) {
        if (ch == ' ') {
            return true;
        }
        if (ch < '0' || ch > '9') {
            return ch >= 'A' && ch <= 'Z';
        } else {
            return true;
        }
    }

    private static boolean isNativeText(char ch) {
        if (ch == ' ') {
            return true;
        }
        if (ch < '0' || ch > '9') {
            return ch >= 'a' && ch <= 'z';
        } else {
            return true;
        }
    }

    private static boolean isNativeX12(char ch) {
        if (isX12TermSep(ch) || ch == ' ') {
            return true;
        }
        if (ch < '0' || ch > '9') {
            return ch >= 'A' && ch <= 'Z';
        } else {
            return true;
        }
    }

    private static boolean isX12TermSep(char ch) {
        if (ch == '\r' || ch == '*' || ch == '>') {
            return true;
        }
        return false;
    }

    private static boolean isNativeEDIFACT(char ch) {
        return ch >= ' ' && ch <= '^';
    }

    private static boolean isSpecialB256(char ch) {
        return false;
    }

    public static int determineConsecutiveDigitCount(CharSequence msg, int startpos) {
        int count = ASCII_ENCODATION;
        int len = msg.length();
        int idx = startpos;
        if (startpos < len) {
            char ch = msg.charAt(startpos);
            while (isDigit(ch) && idx < len) {
                count += C40_ENCODATION;
                idx += C40_ENCODATION;
                if (idx < len) {
                    ch = msg.charAt(idx);
                }
            }
        }
        return count;
    }

    static void illegalCharacter(char c) {
        String hex = Integer.toHexString(c);
        throw new IllegalArgumentException("Illegal character: " + c + " (0x" + ("0000".substring(ASCII_ENCODATION, 4 - hex.length()) + hex) + ')');
    }
}
