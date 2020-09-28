package com.huawei.zxing.oned;

import com.huawei.motiondetection.MotionTypeApps;
import com.huawei.okhttp3.internal.http.StatusLine;
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
    private static final char[] ALPHABET = ALPHABET_STRING.toCharArray();
    private static final String ALPHABET_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*";
    private static final int ASTERISK_ENCODING = CHARACTER_ENCODINGS[47];
    private static final int[] CHARACTER_ENCODINGS = {276, 328, 324, 322, 296, 292, 290, 336, 274, 266, 424, 420, 418, MotionTypeApps.TYPE_SHAKE_START_PRIVACY, MotionTypeApps.TYPE_SHAKE_CHANGE_WALLPAPER, 394, 360, 356, 354, StatusLine.HTTP_PERM_REDIRECT, 282, 344, 332, 326, 300, 278, 436, 434, 428, 422, 406, 410, 364, 358, 310, 314, MotionTypeApps.TYPE_PROXIMITY_DIAL, 468, 466, 458, 366, 374, 430, 294, 474, 470, 306, 350};
    private final int[] counters = new int[6];
    private final StringBuilder decodeRowResult = new StringBuilder(20);

    @Override // com.huawei.zxing.oned.OneDReader
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException {
        BitArray bitArray = row;
        int[] start = findAsteriskPattern(bitArray);
        int nextStart = bitArray.getNextSet(start[1]);
        int end = row.getSize();
        int[] theCounters = this.counters;
        Arrays.fill(theCounters, 0);
        StringBuilder result = this.decodeRowResult;
        result.setLength(0);
        while (true) {
            recordPattern(bitArray, nextStart, theCounters);
            int pattern = toPattern(theCounters);
            if (pattern >= 0) {
                char decodedChar = patternToChar(pattern);
                result.append(decodedChar);
                int nextStart2 = nextStart;
                for (int counter : theCounters) {
                    nextStart2 += counter;
                }
                nextStart = bitArray.getNextSet(nextStart2);
                if (decodedChar == '*') {
                    result.deleteCharAt(result.length() - 1);
                    int lastPatternSize = 0;
                    for (int counter2 : theCounters) {
                        lastPatternSize += counter2;
                    }
                    if (nextStart == end || !bitArray.get(nextStart)) {
                        throw NotFoundException.getNotFoundInstance();
                    } else if (result.length() >= 2) {
                        checkChecksums(result);
                        result.setLength(result.length() - 2);
                        return new Result(decodeExtended(result), null, new ResultPoint[]{new ResultPoint(((float) (start[1] + start[0])) / 2.0f, (float) rowNumber), new ResultPoint(((float) nextStart) + (((float) lastPatternSize) / 2.0f), (float) rowNumber)}, BarcodeFormat.CODE_93);
                    } else {
                        throw NotFoundException.getNotFoundInstance();
                    }
                } else {
                    bitArray = row;
                }
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
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
            boolean z = true;
            if (row.get(i) ^ isWhite) {
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
                if (isWhite) {
                    z = false;
                }
                isWhite = z;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int toPattern(int[] counters2) {
        int max = counters2.length;
        int sum = 0;
        for (int counter : counters2) {
            sum += counter;
        }
        int pattern = 0;
        for (int i = 0; i < max; i++) {
            int scaledShifted = ((counters2[i] << 8) * 9) / sum;
            int scaledUnshifted = scaledShifted >> 8;
            if ((scaledShifted & 255) > 127) {
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
        int i = 0;
        while (true) {
            int[] iArr = CHARACTER_ENCODINGS;
            if (i >= iArr.length) {
                throw NotFoundException.getNotFoundInstance();
            } else if (iArr[i] == pattern) {
                return ALPHABET[i];
            } else {
                i++;
            }
        }
    }

    private static String decodeExtended(CharSequence encoded) throws FormatException {
        int length = encoded.length();
        StringBuilder decoded = new StringBuilder(length);
        int i = 0;
        while (i < length) {
            char c = encoded.charAt(i);
            if (c < 'a' || c > 'd') {
                decoded.append(c);
            } else if (i < length - 1) {
                char next = encoded.charAt(i + 1);
                char decodedChar = 0;
                switch (c) {
                    case 'a':
                        if (next >= 'A' && next <= 'Z') {
                            decodedChar = (char) (next - '@');
                            break;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                        break;
                    case 'b':
                        if (next < 'A' || next > 'E') {
                            if (next >= 'F' && next <= 'W') {
                                decodedChar = (char) (next - 11);
                                break;
                            } else {
                                throw FormatException.getFormatInstance();
                            }
                        } else {
                            decodedChar = (char) (next - '&');
                            break;
                        }
                        break;
                    case 'c':
                        if (next >= 'A' && next <= 'O') {
                            decodedChar = (char) (next - ' ');
                            break;
                        } else if (next == 'Z') {
                            decodedChar = ':';
                            break;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                        break;
                    case 'd':
                        if (next >= 'A' && next <= 'Z') {
                            decodedChar = (char) (next + ' ');
                            break;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                        break;
                }
                decoded.append(decodedChar);
                i++;
            } else {
                throw FormatException.getFormatInstance();
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
