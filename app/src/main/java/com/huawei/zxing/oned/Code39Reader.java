package com.huawei.zxing.oned;

import android.telephony.HwTelephonyManager;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.ChecksumException;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitArray;
import java.util.Arrays;
import java.util.Map;

public final class Code39Reader extends OneDReader {
    private static final char[] ALPHABET = null;
    static final String ALPHABET_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%";
    private static final int ASTERISK_ENCODING = 0;
    static final int[] CHARACTER_ENCODINGS = null;
    private final int[] counters;
    private final StringBuilder decodeRowResult;
    private final boolean extendedMode;
    private final boolean usingCheckDigit;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.oned.Code39Reader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.oned.Code39Reader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.oned.Code39Reader.<clinit>():void");
    }

    public Code39Reader() {
        this(false);
    }

    public Code39Reader(boolean usingCheckDigit) {
        this(usingCheckDigit, false);
    }

    public Code39Reader(boolean usingCheckDigit, boolean extendedMode) {
        this.usingCheckDigit = usingCheckDigit;
        this.extendedMode = extendedMode;
        this.decodeRowResult = new StringBuilder(20);
        this.counters = new int[9];
    }

    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException {
        int lastStart;
        int[] theCounters = this.counters;
        Arrays.fill(theCounters, 0);
        StringBuilder result = this.decodeRowResult;
        result.setLength(0);
        int[] start = findAsteriskPattern(row, theCounters);
        int nextStart = row.getNextSet(start[1]);
        int end = row.getSize();
        char decodedChar;
        do {
            OneDReader.recordPattern(row, nextStart, theCounters);
            int pattern = toNarrowWidePattern(theCounters);
            if (pattern < 0) {
                throw NotFoundException.getNotFoundInstance();
            }
            decodedChar = patternToChar(pattern);
            result.append(decodedChar);
            lastStart = nextStart;
            for (int counter : theCounters) {
                nextStart += counter;
            }
            nextStart = row.getNextSet(nextStart);
        } while (decodedChar != '*');
        result.setLength(result.length() - 1);
        int lastPatternSize = 0;
        for (int counter2 : theCounters) {
            lastPatternSize += counter2;
        }
        int whiteSpaceAfterEnd = (nextStart - lastStart) - lastPatternSize;
        if (nextStart == end || (whiteSpaceAfterEnd >> 1) >= lastPatternSize) {
            if (this.usingCheckDigit) {
                int max = result.length() - 1;
                int total = 0;
                for (int i = 0; i < max; i++) {
                    total += ALPHABET_STRING.indexOf(this.decodeRowResult.charAt(i));
                }
                if (result.charAt(max) != ALPHABET[total % 43]) {
                    throw ChecksumException.getChecksumInstance();
                }
                result.setLength(max);
            }
            if (result.length() == 0) {
                throw NotFoundException.getNotFoundInstance();
            }
            String resultString;
            if (this.extendedMode) {
                resultString = decodeExtended(result);
            } else {
                resultString = result.toString();
            }
            float right = ((float) lastStart) + (((float) lastPatternSize) / 2.0f);
            r22 = new ResultPoint[2];
            r22[0] = new ResultPoint(((float) (start[1] + start[0])) / 2.0f, (float) rowNumber);
            r22[1] = new ResultPoint(right, (float) rowNumber);
            return new Result(resultString, null, r22, BarcodeFormat.CODE_39);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int[] findAsteriskPattern(BitArray row, int[] counters) throws NotFoundException {
        int width = row.getSize();
        int rowOffset = row.getNextSet(0);
        int counterPosition = 0;
        int patternStart = rowOffset;
        boolean isWhite = false;
        int patternLength = counters.length;
        int i = rowOffset;
        while (i < width) {
            if ((row.get(i) ^ isWhite) != 0) {
                counters[counterPosition] = counters[counterPosition] + 1;
            } else {
                if (counterPosition != patternLength - 1) {
                    counterPosition++;
                } else if (toNarrowWidePattern(counters) == ASTERISK_ENCODING && row.isRange(Math.max(0, patternStart - ((i - patternStart) >> 1)), patternStart, false)) {
                    return new int[]{patternStart, i};
                } else {
                    patternStart += counters[0] + counters[1];
                    System.arraycopy(counters, 2, counters, 0, patternLength - 2);
                    counters[patternLength - 2] = 0;
                    counters[patternLength - 1] = 0;
                    counterPosition--;
                }
                counters[counterPosition] = 1;
                isWhite = !isWhite;
            }
            i++;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int toNarrowWidePattern(int[] counters) {
        int numCounters = counters.length;
        int maxNarrowCounter = 0;
        int wideCounters;
        do {
            int i;
            int minCounter = Integer.MAX_VALUE;
            for (int counter : counters) {
                int counter2;
                if (counter2 < minCounter && counter2 > maxNarrowCounter) {
                    minCounter = counter2;
                }
            }
            maxNarrowCounter = minCounter;
            wideCounters = 0;
            int totalWideCountersWidth = 0;
            int pattern = 0;
            for (i = 0; i < numCounters; i++) {
                counter2 = counters[i];
                if (counter2 > maxNarrowCounter) {
                    pattern |= 1 << ((numCounters - 1) - i);
                    wideCounters++;
                    totalWideCountersWidth += counter2;
                }
            }
            if (wideCounters == 3) {
                for (i = 0; i < numCounters && wideCounters > 0; i++) {
                    counter2 = counters[i];
                    if (counter2 > maxNarrowCounter) {
                        wideCounters--;
                        if ((counter2 << 1) >= totalWideCountersWidth) {
                            return -1;
                        }
                    }
                }
                return pattern;
            }
        } while (wideCounters > 3);
        return -1;
    }

    private static char patternToChar(int pattern) throws NotFoundException {
        for (int i = 0; i < CHARACTER_ENCODINGS.length; i++) {
            if (CHARACTER_ENCODINGS[i] == pattern) {
                return ALPHABET[i];
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static String decodeExtended(CharSequence encoded) throws FormatException {
        int length = encoded.length();
        StringBuilder decoded = new StringBuilder(length);
        int i = 0;
        while (i < length) {
            char c = encoded.charAt(i);
            if (c == '+' || c == '$' || c == '%' || c == '/') {
                char next = encoded.charAt(i + 1);
                char decodedChar = '\u0000';
                switch (c) {
                    case IccConstantsEx.RUIM_SMS_SUBADDRESS_LEN /*36*/:
                        if (next >= 'A' && next <= 'Z') {
                            decodedChar = (char) (next - 64);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                        break;
                    case '%':
                        if (next < 'A' || next > 'E') {
                            if (next >= 'F' && next <= 'W') {
                                decodedChar = (char) (next - 11);
                                break;
                            }
                            throw FormatException.getFormatInstance();
                        }
                        decodedChar = (char) (next - 38);
                        break;
                        break;
                    case HwTelephonyManager.DUAL_MODE_TELECOM_LTE_CARD /*43*/:
                        if (next >= 'A' && next <= 'Z') {
                            decodedChar = (char) (next + 32);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                        break;
                    case '/':
                        if (next >= 'A' && next <= 'O') {
                            decodedChar = (char) (next - 32);
                            break;
                        } else if (next == 'Z') {
                            decodedChar = ':';
                            break;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                }
                decoded.append(decodedChar);
                i++;
            } else {
                decoded.append(c);
            }
            i++;
        }
        return decoded.toString();
    }
}
