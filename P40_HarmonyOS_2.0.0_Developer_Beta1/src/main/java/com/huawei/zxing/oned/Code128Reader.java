package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.ChecksumException;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Code128Reader extends OneDReader {
    private static final int CODE_CODE_A = 101;
    private static final int CODE_CODE_B = 100;
    private static final int CODE_CODE_C = 99;
    private static final int CODE_FNC_1 = 102;
    private static final int CODE_FNC_2 = 97;
    private static final int CODE_FNC_3 = 96;
    private static final int CODE_FNC_4_A = 101;
    private static final int CODE_FNC_4_B = 100;
    static final int[][] CODE_PATTERNS = {new int[]{2, 1, 2, 2, 2, 2}, new int[]{2, 2, 2, 1, 2, 2}, new int[]{2, 2, 2, 2, 2, 1}, new int[]{1, 2, 1, 2, 2, 3}, new int[]{1, 2, 1, 3, 2, 2}, new int[]{1, 3, 1, 2, 2, 2}, new int[]{1, 2, 2, 2, 1, 3}, new int[]{1, 2, 2, 3, 1, 2}, new int[]{1, 3, 2, 2, 1, 2}, new int[]{2, 2, 1, 2, 1, 3}, new int[]{2, 2, 1, 3, 1, 2}, new int[]{2, 3, 1, 2, 1, 2}, new int[]{1, 1, 2, 2, 3, 2}, new int[]{1, 2, 2, 1, 3, 2}, new int[]{1, 2, 2, 2, 3, 1}, new int[]{1, 1, 3, 2, 2, 2}, new int[]{1, 2, 3, 1, 2, 2}, new int[]{1, 2, 3, 2, 2, 1}, new int[]{2, 2, 3, 2, 1, 1}, new int[]{2, 2, 1, 1, 3, 2}, new int[]{2, 2, 1, 2, 3, 1}, new int[]{2, 1, 3, 2, 1, 2}, new int[]{2, 2, 3, 1, 1, 2}, new int[]{3, 1, 2, 1, 3, 1}, new int[]{3, 1, 1, 2, 2, 2}, new int[]{3, 2, 1, 1, 2, 2}, new int[]{3, 2, 1, 2, 2, 1}, new int[]{3, 1, 2, 2, 1, 2}, new int[]{3, 2, 2, 1, 1, 2}, new int[]{3, 2, 2, 2, 1, 1}, new int[]{2, 1, 2, 1, 2, 3}, new int[]{2, 1, 2, 3, 2, 1}, new int[]{2, 3, 2, 1, 2, 1}, new int[]{1, 1, 1, 3, 2, 3}, new int[]{1, 3, 1, 1, 2, 3}, new int[]{1, 3, 1, 3, 2, 1}, new int[]{1, 1, 2, 3, 1, 3}, new int[]{1, 3, 2, 1, 1, 3}, new int[]{1, 3, 2, 3, 1, 1}, new int[]{2, 1, 1, 3, 1, 3}, new int[]{2, 3, 1, 1, 1, 3}, new int[]{2, 3, 1, 3, 1, 1}, new int[]{1, 1, 2, 1, 3, 3}, new int[]{1, 1, 2, 3, 3, 1}, new int[]{1, 3, 2, 1, 3, 1}, new int[]{1, 1, 3, 1, 2, 3}, new int[]{1, 1, 3, 3, 2, 1}, new int[]{1, 3, 3, 1, 2, 1}, new int[]{3, 1, 3, 1, 2, 1}, new int[]{2, 1, 1, 3, 3, 1}, new int[]{2, 3, 1, 1, 3, 1}, new int[]{2, 1, 3, 1, 1, 3}, new int[]{2, 1, 3, 3, 1, 1}, new int[]{2, 1, 3, 1, 3, 1}, new int[]{3, 1, 1, 1, 2, 3}, new int[]{3, 1, 1, 3, 2, 1}, new int[]{3, 3, 1, 1, 2, 1}, new int[]{3, 1, 2, 1, 1, 3}, new int[]{3, 1, 2, 3, 1, 1}, new int[]{3, 3, 2, 1, 1, 1}, new int[]{3, 1, 4, 1, 1, 1}, new int[]{2, 2, 1, 4, 1, 1}, new int[]{4, 3, 1, 1, 1, 1}, new int[]{1, 1, 1, 2, 2, 4}, new int[]{1, 1, 1, 4, 2, 2}, new int[]{1, 2, 1, 1, 2, 4}, new int[]{1, 2, 1, 4, 2, 1}, new int[]{1, 4, 1, 1, 2, 2}, new int[]{1, 4, 1, 2, 2, 1}, new int[]{1, 1, 2, 2, 1, 4}, new int[]{1, 1, 2, 4, 1, 2}, new int[]{1, 2, 2, 1, 1, 4}, new int[]{1, 2, 2, 4, 1, 1}, new int[]{1, 4, 2, 1, 1, 2}, new int[]{1, 4, 2, 2, 1, 1}, new int[]{2, 4, 1, 2, 1, 1}, new int[]{2, 2, 1, 1, 1, 4}, new int[]{4, 1, 3, 1, 1, 1}, new int[]{2, 4, 1, 1, 1, 2}, new int[]{1, 3, 4, 1, 1, 1}, new int[]{1, 1, 1, 2, 4, 2}, new int[]{1, 2, 1, 1, 4, 2}, new int[]{1, 2, 1, 2, 4, 1}, new int[]{1, 1, 4, 2, 1, 2}, new int[]{1, 2, 4, 1, 1, 2}, new int[]{1, 2, 4, 2, 1, 1}, new int[]{4, 1, 1, 2, 1, 2}, new int[]{4, 2, 1, 1, 1, 2}, new int[]{4, 2, 1, 2, 1, 1}, new int[]{2, 1, 2, 1, 4, 1}, new int[]{2, 1, 4, 1, 2, 1}, new int[]{4, 1, 2, 1, 2, 1}, new int[]{1, 1, 1, 1, 4, 3}, new int[]{1, 1, 1, 3, 4, 1}, new int[]{1, 3, 1, 1, 4, 1}, new int[]{1, 1, 4, 1, 1, 3}, new int[]{1, 1, 4, 3, 1, 1}, new int[]{4, 1, 1, 1, 1, 3}, new int[]{4, 1, 1, 3, 1, 1}, new int[]{1, 1, 3, 1, 4, 1}, new int[]{1, 1, 4, 1, 3, 1}, new int[]{3, 1, 1, 1, 4, 1}, new int[]{4, 1, 1, 1, 3, 1}, new int[]{2, 1, 1, 4, 1, 2}, new int[]{2, 1, 1, 2, 1, 4}, new int[]{2, 1, 1, 2, 3, 2}, new int[]{2, 3, 3, 1, 1, 1, 2}};
    private static final int CODE_SHIFT = 98;
    private static final int CODE_START_A = 103;
    private static final int CODE_START_B = 104;
    private static final int CODE_START_C = 105;
    private static final int CODE_STOP = 106;
    private static final int MAX_AVG_VARIANCE = 64;
    private static final int MAX_INDIVIDUAL_VARIANCE = 179;

    private static int[] findStartPattern(BitArray row) throws NotFoundException {
        int width = row.getSize();
        int rowOffset = row.getNextSet(0);
        int counterPosition = 0;
        int[] counters = new int[6];
        int patternStart = rowOffset;
        boolean isWhite = false;
        int patternLength = counters.length;
        for (int i = rowOffset; i < width; i++) {
            boolean z = true;
            if (row.get(i) ^ isWhite) {
                counters[counterPosition] = counters[counterPosition] + 1;
            } else {
                if (counterPosition == patternLength - 1) {
                    int bestVariance = 64;
                    int bestMatch = -1;
                    for (int startCode = 103; startCode <= 105; startCode++) {
                        int variance = patternMatchVariance(counters, CODE_PATTERNS[startCode], MAX_INDIVIDUAL_VARIANCE);
                        if (variance < bestVariance) {
                            bestVariance = variance;
                            bestMatch = startCode;
                        }
                    }
                    if (bestMatch >= 0 && row.isRange(Math.max(0, patternStart - ((i - patternStart) / 2)), patternStart, false)) {
                        return new int[]{patternStart, i, bestMatch};
                    }
                    patternStart += counters[0] + counters[1];
                    System.arraycopy(counters, 2, counters, 0, patternLength - 2);
                    counters[patternLength - 2] = 0;
                    counters[patternLength - 1] = 0;
                    counterPosition--;
                } else {
                    counterPosition++;
                }
                counters[counterPosition] = 1;
                if (isWhite) {
                    z = false;
                }
                isWhite = z;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int decodeCode(BitArray row, int[] counters, int rowOffset) throws NotFoundException {
        recordPattern(row, rowOffset, counters);
        int bestVariance = 64;
        int bestMatch = -1;
        int d = 0;
        while (true) {
            int[][] iArr = CODE_PATTERNS;
            if (d >= iArr.length) {
                break;
            }
            int variance = patternMatchVariance(counters, iArr[d], MAX_INDIVIDUAL_VARIANCE);
            if (variance < bestVariance) {
                bestVariance = variance;
                bestMatch = d;
            }
            d++;
        }
        if (bestMatch >= 0) {
            return bestMatch;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    /* JADX INFO: Multiple debug info for r2v2 byte[]: [D('rawBytes' byte[]), D('lastPatternSize' int)] */
    @Override // com.huawei.zxing.oned.OneDReader
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException, FormatException, ChecksumException {
        int codeSet;
        boolean convertFNC1 = hints != null && hints.containsKey(DecodeHintType.ASSUME_GS1);
        int[] startPatternInfo = findStartPattern(row);
        int startCode = startPatternInfo[2];
        List<Byte> rawCodes = new ArrayList<>(20);
        rawCodes.add(Byte.valueOf((byte) startCode));
        switch (startCode) {
            case 103:
                codeSet = 101;
                break;
            case 104:
                codeSet = 100;
                break;
            case 105:
                codeSet = 99;
                break;
            default:
                throw FormatException.getFormatInstance();
        }
        boolean done = false;
        boolean isNextShifted = false;
        StringBuilder result = new StringBuilder(20);
        int lastStart = startPatternInfo[0];
        int nextStart = startPatternInfo[1];
        int[] counters = new int[6];
        int code = 0;
        int checksumTotal = startCode;
        int multiplier = 0;
        boolean lastCharacterWasPrintable = true;
        int codeSet2 = codeSet;
        int lastCode = 0;
        while (!done) {
            isNextShifted = false;
            lastCode = code;
            int code2 = decodeCode(row, counters, nextStart);
            rawCodes.add(Byte.valueOf((byte) code2));
            if (code2 != 106) {
                lastCharacterWasPrintable = true;
            }
            if (code2 != 106) {
                multiplier++;
                checksumTotal += multiplier * code2;
            }
            lastStart = nextStart;
            int nextStart2 = nextStart;
            for (int counter : counters) {
                nextStart2 += counter;
            }
            switch (code2) {
                case 103:
                case 104:
                case 105:
                    throw FormatException.getFormatInstance();
                default:
                    switch (codeSet2) {
                        case 99:
                            if (code2 < 100) {
                                if (code2 < 10) {
                                    result.append('0');
                                }
                                result.append(code2);
                                break;
                            } else {
                                if (code2 != 106) {
                                    lastCharacterWasPrintable = false;
                                }
                                if (code2 != 106) {
                                    switch (code2) {
                                        case 100:
                                            codeSet2 = 100;
                                            break;
                                        case 101:
                                            codeSet2 = 101;
                                            break;
                                        case 102:
                                            if (convertFNC1) {
                                                if (result.length() == 0) {
                                                    result.append("]C1");
                                                    break;
                                                } else {
                                                    result.append((char) 29);
                                                    break;
                                                }
                                            }
                                            break;
                                    }
                                } else {
                                    done = true;
                                    break;
                                }
                            }
                        case 100:
                            if (code2 < CODE_FNC_3) {
                                result.append((char) (code2 + 32));
                                break;
                            } else {
                                if (code2 != 106) {
                                    lastCharacterWasPrintable = false;
                                }
                                if (code2 != 106) {
                                    switch (code2) {
                                        case CODE_SHIFT /* 98 */:
                                            isNextShifted = true;
                                            codeSet2 = 101;
                                            break;
                                        case 99:
                                            codeSet2 = 99;
                                            break;
                                        case 101:
                                            codeSet2 = 101;
                                            break;
                                        case 102:
                                            if (convertFNC1) {
                                                if (result.length() == 0) {
                                                    result.append("]C1");
                                                    break;
                                                } else {
                                                    result.append((char) 29);
                                                    break;
                                                }
                                            }
                                            break;
                                    }
                                } else {
                                    done = true;
                                    break;
                                }
                            }
                            break;
                        case 101:
                            if (code2 >= 64) {
                                if (code2 < CODE_FNC_3) {
                                    result.append((char) (code2 - 64));
                                    break;
                                } else {
                                    if (code2 != 106) {
                                        lastCharacterWasPrintable = false;
                                    }
                                    if (code2 != 106) {
                                        switch (code2) {
                                            case CODE_SHIFT /* 98 */:
                                                isNextShifted = true;
                                                codeSet2 = 100;
                                                break;
                                            case 99:
                                                codeSet2 = 99;
                                                break;
                                            case 100:
                                                codeSet2 = 100;
                                                break;
                                            case 102:
                                                if (convertFNC1) {
                                                    if (result.length() == 0) {
                                                        result.append("]C1");
                                                        break;
                                                    } else {
                                                        result.append((char) 29);
                                                        break;
                                                    }
                                                }
                                                break;
                                        }
                                    } else {
                                        done = true;
                                        break;
                                    }
                                }
                            } else {
                                result.append((char) (code2 + 32));
                                break;
                            }
                    }
                    if (isNextShifted) {
                        int i = 101;
                        if (codeSet2 == 101) {
                            i = 100;
                        }
                        codeSet2 = i;
                    }
                    code = code2;
                    nextStart = nextStart2;
            }
        }
        int lastPatternSize = nextStart - lastStart;
        int nextStart3 = row.getNextUnset(nextStart);
        if (!row.isRange(nextStart3, Math.min(row.getSize(), nextStart3 + ((nextStart3 - lastStart) / 2)), false)) {
            throw NotFoundException.getNotFoundInstance();
        } else if ((checksumTotal - (multiplier * lastCode)) % 103 == lastCode) {
            int resultLength = result.length();
            if (resultLength != 0) {
                if (resultLength > 0 && lastCharacterWasPrintable) {
                    if (codeSet2 == 99) {
                        result.delete(resultLength - 2, resultLength);
                    } else {
                        result.delete(resultLength - 1, resultLength);
                    }
                }
                float left = ((float) (startPatternInfo[1] + startPatternInfo[0])) / 2.0f;
                float right = ((float) lastStart) + (((float) lastPatternSize) / 2.0f);
                int rawCodesSize = rawCodes.size();
                byte[] rawBytes = new byte[rawCodesSize];
                for (int i2 = 0; i2 < rawCodesSize; i2++) {
                    rawBytes[i2] = rawCodes.get(i2).byteValue();
                }
                return new Result(result.toString(), rawBytes, new ResultPoint[]{new ResultPoint(left, (float) rowNumber), new ResultPoint(right, (float) rowNumber)}, BarcodeFormat.CODE_128);
            }
            throw NotFoundException.getNotFoundInstance();
        } else {
            throw ChecksumException.getChecksumInstance();
        }
    }
}
