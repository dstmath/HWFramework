package com.huawei.zxing.oned;

import com.huawei.android.media.ToneGeneratorEx;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.motiondetection.MotionTypeApps;
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

public final class Code93Reader extends OneDReader {
    private static final char[] ALPHABET = null;
    private static final String ALPHABET_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*";
    private static final int ASTERISK_ENCODING = 0;
    private static final int[] CHARACTER_ENCODINGS = null;
    private final int[] counters;
    private final StringBuilder decodeRowResult;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.oned.Code93Reader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.oned.Code93Reader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.oned.Code93Reader.<clinit>():void");
    }

    public Code93Reader() {
        this.decodeRowResult = new StringBuilder(20);
        this.counters = new int[6];
    }

    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException {
        int[] start = findAsteriskPattern(row);
        int nextStart = row.getNextSet(start[1]);
        int end = row.getSize();
        int[] theCounters = this.counters;
        Arrays.fill(theCounters, 0);
        StringBuilder result = this.decodeRowResult;
        result.setLength(0);
        char decodedChar;
        do {
            OneDReader.recordPattern(row, nextStart, theCounters);
            int pattern = toPattern(theCounters);
            if (pattern < 0) {
                throw NotFoundException.getNotFoundInstance();
            }
            decodedChar = patternToChar(pattern);
            result.append(decodedChar);
            int lastStart = nextStart;
            for (int counter : theCounters) {
                nextStart += counter;
            }
            nextStart = row.getNextSet(nextStart);
        } while (decodedChar != '*');
        result.deleteCharAt(result.length() - 1);
        int lastPatternSize = 0;
        for (int counter2 : theCounters) {
            lastPatternSize += counter2;
        }
        if (nextStart == end || !row.get(nextStart)) {
            throw NotFoundException.getNotFoundInstance();
        } else if (result.length() < 2) {
            throw NotFoundException.getNotFoundInstance();
        } else {
            checkChecksums(result);
            result.setLength(result.length() - 2);
            String resultString = decodeExtended(result);
            float right = ((float) lastStart) + (((float) lastPatternSize) / 2.0f);
            r18 = new ResultPoint[2];
            r18[0] = new ResultPoint(((float) (start[1] + start[0])) / 2.0f, (float) rowNumber);
            r18[1] = new ResultPoint(right, (float) rowNumber);
            return new Result(resultString, null, r18, BarcodeFormat.CODE_93);
        }
    }

    private int[] findAsteriskPattern(BitArray row) throws NotFoundException {
        int width = row.getSize();
        int rowOffset = row.getNextSet(0);
        Arrays.fill(this.counters, 0);
        int[] theCounters = this.counters;
        int patternStart = rowOffset;
        boolean isWhite = false;
        int patternLength = theCounters.length;
        int counterPosition = 0;
        for (int i = rowOffset; i < width; i++) {
            if ((row.get(i) ^ isWhite) != 0) {
                theCounters[counterPosition] = theCounters[counterPosition] + 1;
            } else {
                if (counterPosition != patternLength - 1) {
                    counterPosition++;
                } else if (toPattern(theCounters) == ASTERISK_ENCODING) {
                    return new int[]{patternStart, i};
                } else {
                    patternStart += theCounters[0] + theCounters[1];
                    System.arraycopy(theCounters, 2, theCounters, 0, patternLength - 2);
                    theCounters[patternLength - 2] = 0;
                    theCounters[patternLength - 1] = 0;
                    counterPosition--;
                }
                theCounters[counterPosition] = 1;
                isWhite = !isWhite;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int toPattern(int[] counters) {
        int max = counters.length;
        int sum = 0;
        for (int counter : counters) {
            sum += counter;
        }
        int pattern = 0;
        for (int i = 0; i < max; i++) {
            int scaledShifted = ((counters[i] << 8) * 9) / sum;
            int scaledUnshifted = scaledShifted >> 8;
            if ((scaledShifted & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN) > 127) {
                scaledUnshifted++;
            }
            if (scaledUnshifted < 1 || scaledUnshifted > 4) {
                return -1;
            }
            if ((i & 1) == 0) {
                for (int j = 0; j < scaledUnshifted; j++) {
                    pattern = (pattern << 1) | 1;
                }
            } else {
                pattern <<= scaledUnshifted;
            }
        }
        return pattern;
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
            if (c < 'a' || c > 'd') {
                decoded.append(c);
            } else if (i >= length - 1) {
                throw FormatException.getFormatInstance();
            } else {
                char next = encoded.charAt(i + 1);
                char decodedChar = '\u0000';
                switch (c) {
                    case 'a':
                        if (next >= 'A' && next <= 'Z') {
                            decodedChar = (char) (next - 64);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                        break;
                    case 'b':
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
                    case ToneGeneratorEx.TONE_LOCAL_CW /*99*/:
                        if (next >= 'A' && next <= 'O') {
                            decodedChar = (char) (next - 32);
                            break;
                        } else if (next == 'Z') {
                            decodedChar = ':';
                            break;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                    case MotionTypeApps.TYPE_PICKUP /*100*/:
                        if (next >= 'A' && next <= 'Z') {
                            decodedChar = (char) (next + 32);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                        break;
                }
                decoded.append(decodedChar);
                i++;
            }
            i++;
        }
        return decoded.toString();
    }

    private static void checkChecksums(CharSequence result) throws ChecksumException {
        int length = result.length();
        checkOneChecksum(result, length - 2, 20);
        checkOneChecksum(result, length - 1, 15);
    }

    private static void checkOneChecksum(CharSequence result, int checkPosition, int weightMax) throws ChecksumException {
        int weight = 1;
        int total = 0;
        for (int i = checkPosition - 1; i >= 0; i--) {
            total += ALPHABET_STRING.indexOf(result.charAt(i)) * weight;
            weight++;
            if (weight > weightMax) {
                weight = 1;
            }
        }
        if (result.charAt(checkPosition) != ALPHABET[total % 47]) {
            throw ChecksumException.getChecksumInstance();
        }
    }
}
