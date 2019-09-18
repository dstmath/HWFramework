package com.huawei.zxing.pdf417.decoder;

import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.DecoderResult;
import com.huawei.zxing.pdf417.PDF417ResultMetadata;
import java.math.BigInteger;
import java.util.Arrays;

final class DecodedBitStreamParser {
    private static final int AL = 28;
    private static final int AS = 27;
    private static final int BEGIN_MACRO_PDF417_CONTROL_BLOCK = 928;
    private static final int BEGIN_MACRO_PDF417_OPTIONAL_FIELD = 923;
    private static final int BYTE_COMPACTION_MODE_LATCH = 901;
    private static final int BYTE_COMPACTION_MODE_LATCH_6 = 924;
    private static final BigInteger[] EXP900 = new BigInteger[16];
    private static final int LL = 27;
    private static final int MACRO_PDF417_TERMINATOR = 922;
    private static final int MAX_NUMERIC_CODEWORDS = 15;
    private static final char[] MIXED_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '&', 13, 9, ',', ':', '#', '-', '.', '$', '/', '+', '%', '*', '=', '^'};
    private static final int ML = 28;
    private static final int MODE_SHIFT_TO_BYTE_COMPACTION_MODE = 913;
    private static final int NUMBER_OF_SEQUENCE_CODEWORDS = 2;
    private static final int NUMERIC_COMPACTION_MODE_LATCH = 902;
    private static final int PAL = 29;
    private static final int PL = 25;
    private static final int PS = 29;
    private static final char[] PUNCT_CHARS = {';', '<', '>', '@', '[', '\\', '}', '_', '`', '~', '!', 13, 9, ',', ':', 10, '-', '.', '$', '/', '\"', '|', '*', '(', ')', '?', '{', '}', '\''};
    private static final int TEXT_COMPACTION_MODE_LATCH = 900;

    private enum Mode {
        ALPHA,
        LOWER,
        MIXED,
        PUNCT,
        ALPHA_SHIFT,
        PUNCT_SHIFT
    }

    static {
        EXP900[0] = BigInteger.ONE;
        BigInteger nineHundred = BigInteger.valueOf(900);
        EXP900[1] = nineHundred;
        for (int i = 2; i < EXP900.length; i++) {
            EXP900[i] = EXP900[i - 1].multiply(nineHundred);
        }
    }

    private DecodedBitStreamParser() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0048 A[LOOP:0: B:1:0x0012->B:18:0x0048, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x004e A[SYNTHETIC] */
    static DecoderResult decode(int[] codewords, String ecLevel) throws FormatException {
        int codeIndex;
        StringBuilder result = new StringBuilder(codewords.length * 2);
        int codeIndex2 = 1 + 1;
        int code = codewords[1];
        PDF417ResultMetadata resultMetadata = new PDF417ResultMetadata();
        while (codeIndex2 < codewords[0]) {
            if (code != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                if (code != 928) {
                    switch (code) {
                        case 900:
                            codeIndex = textCompaction(codewords, codeIndex2, result);
                            break;
                        case 901:
                            break;
                        case NUMERIC_COMPACTION_MODE_LATCH /*902*/:
                            codeIndex = numericCompaction(codewords, codeIndex2, result);
                            break;
                        default:
                            switch (code) {
                                case MACRO_PDF417_TERMINATOR /*922*/:
                                case BEGIN_MACRO_PDF417_OPTIONAL_FIELD /*923*/:
                                    throw FormatException.getFormatInstance();
                                case BYTE_COMPACTION_MODE_LATCH_6 /*924*/:
                                    break;
                                default:
                                    codeIndex = textCompaction(codewords, codeIndex2 - 1, result);
                                    break;
                            }
                    }
                } else {
                    codeIndex = decodeMacroBlock(codewords, codeIndex2, resultMetadata);
                }
                if (codeIndex >= codewords.length) {
                    code = codewords[codeIndex];
                    codeIndex2 = codeIndex + 1;
                } else {
                    throw FormatException.getFormatInstance();
                }
            }
            codeIndex = byteCompaction(code, codewords, codeIndex2, result);
            if (codeIndex >= codewords.length) {
            }
        }
        if (result.length() != 0) {
            DecoderResult decoderResult = new DecoderResult(null, result.toString(), null, ecLevel);
            decoderResult.setOther(resultMetadata);
            return decoderResult;
        }
        throw FormatException.getFormatInstance();
    }

    private static int decodeMacroBlock(int[] codewords, int codeIndex, PDF417ResultMetadata resultMetadata) throws FormatException {
        if (codeIndex + 2 <= codewords[0]) {
            int[] segmentIndexArray = new int[2];
            int codeIndex2 = codeIndex;
            int i = 0;
            while (i < 2) {
                segmentIndexArray[i] = codewords[codeIndex2];
                i++;
                codeIndex2++;
            }
            resultMetadata.setSegmentIndex(Integer.parseInt(decodeBase900toBase10(segmentIndexArray, 2)));
            StringBuilder fileId = new StringBuilder();
            int codeIndex3 = textCompaction(codewords, codeIndex2, fileId);
            resultMetadata.setFileId(fileId.toString());
            if (codewords[codeIndex3] == BEGIN_MACRO_PDF417_OPTIONAL_FIELD) {
                int codeIndex4 = codeIndex3 + 1;
                int[] additionalOptionCodeWords = new int[(codewords[0] - codeIndex4)];
                int additionalOptionCodeWordsIndex = 0;
                int code = codeIndex4;
                boolean end = false;
                while (code < codewords[0] && !end) {
                    int codeIndex5 = code + 1;
                    int code2 = codewords[code];
                    if (code2 < 900) {
                        additionalOptionCodeWords[additionalOptionCodeWordsIndex] = code2;
                        code = codeIndex5;
                        additionalOptionCodeWordsIndex++;
                    } else if (code2 == MACRO_PDF417_TERMINATOR) {
                        resultMetadata.setLastSegment(true);
                        end = true;
                        code = codeIndex5 + 1;
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                }
                resultMetadata.setOptionalData(Arrays.copyOf(additionalOptionCodeWords, additionalOptionCodeWordsIndex));
                return code;
            } else if (codewords[codeIndex3] != MACRO_PDF417_TERMINATOR) {
                return codeIndex3;
            } else {
                resultMetadata.setLastSegment(true);
                return codeIndex3 + 1;
            }
        } else {
            throw FormatException.getFormatInstance();
        }
    }

    private static int textCompaction(int[] codewords, int codeIndex, StringBuilder result) {
        int[] textCompactionData = new int[((codewords[0] - codeIndex) << 1)];
        int[] byteCompactionData = new int[((codewords[0] - codeIndex) << 1)];
        int index = 0;
        int codeIndex2 = codeIndex;
        int codeIndex3 = 0;
        while (codeIndex2 < codewords[0] && codeIndex3 == 0) {
            int codeIndex4 = codeIndex2 + 1;
            int code = codewords[codeIndex2];
            if (code < 900) {
                textCompactionData[index] = code / 30;
                textCompactionData[index + 1] = code % 30;
                index += 2;
            } else if (code != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                if (code != 928) {
                    switch (code) {
                        case 900:
                            textCompactionData[index] = 900;
                            codeIndex2 = codeIndex4;
                            index++;
                            continue;
                        case 901:
                        case NUMERIC_COMPACTION_MODE_LATCH /*902*/:
                            break;
                        default:
                            switch (code) {
                                case MACRO_PDF417_TERMINATOR /*922*/:
                                case BEGIN_MACRO_PDF417_OPTIONAL_FIELD /*923*/:
                                case BYTE_COMPACTION_MODE_LATCH_6 /*924*/:
                                    break;
                            }
                    }
                }
                codeIndex4--;
                codeIndex3 = 1;
            } else {
                textCompactionData[index] = MODE_SHIFT_TO_BYTE_COMPACTION_MODE;
                byteCompactionData[index] = codewords[codeIndex4];
                index++;
                codeIndex2 = codeIndex4 + 1;
            }
            codeIndex2 = codeIndex4;
            continue;
        }
        decodeTextCompaction(textCompactionData, byteCompactionData, index, result);
        return codeIndex2;
    }

    private static void decodeTextCompaction(int[] textCompactionData, int[] byteCompactionData, int length, StringBuilder result) {
        StringBuilder sb = result;
        Mode subMode = Mode.ALPHA;
        Mode priorToShiftMode = Mode.ALPHA;
        for (int i = 0; i < length; i++) {
            int subModeCh = textCompactionData[i];
            char ch = 0;
            switch (subMode) {
                case ALPHA:
                    if (subModeCh >= 26) {
                        if (subModeCh != 26) {
                            if (subModeCh != 27) {
                                if (subModeCh != 28) {
                                    if (subModeCh != 29) {
                                        if (subModeCh != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                                            if (subModeCh == 900) {
                                                subMode = Mode.ALPHA;
                                                break;
                                            }
                                        } else {
                                            sb.append((char) byteCompactionData[i]);
                                            break;
                                        }
                                    } else {
                                        priorToShiftMode = subMode;
                                        subMode = Mode.PUNCT_SHIFT;
                                        break;
                                    }
                                } else {
                                    subMode = Mode.MIXED;
                                    break;
                                }
                            } else {
                                subMode = Mode.LOWER;
                                break;
                            }
                        } else {
                            ch = ' ';
                            break;
                        }
                    } else {
                        ch = (char) (65 + subModeCh);
                        break;
                    }
                    break;
                case LOWER:
                    if (subModeCh >= 26) {
                        if (subModeCh != 26) {
                            if (subModeCh != 27) {
                                if (subModeCh != 28) {
                                    if (subModeCh != 29) {
                                        if (subModeCh != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                                            if (subModeCh == 900) {
                                                subMode = Mode.ALPHA;
                                                break;
                                            }
                                        } else {
                                            sb.append((char) byteCompactionData[i]);
                                            break;
                                        }
                                    } else {
                                        priorToShiftMode = subMode;
                                        subMode = Mode.PUNCT_SHIFT;
                                        break;
                                    }
                                } else {
                                    subMode = Mode.MIXED;
                                    break;
                                }
                            } else {
                                priorToShiftMode = subMode;
                                subMode = Mode.ALPHA_SHIFT;
                                break;
                            }
                        } else {
                            ch = ' ';
                            break;
                        }
                    } else {
                        ch = (char) (97 + subModeCh);
                        break;
                    }
                    break;
                case MIXED:
                    if (subModeCh >= 25) {
                        if (subModeCh != 25) {
                            if (subModeCh != 26) {
                                if (subModeCh != 27) {
                                    if (subModeCh != 28) {
                                        if (subModeCh != 29) {
                                            if (subModeCh != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                                                if (subModeCh == 900) {
                                                    subMode = Mode.ALPHA;
                                                    break;
                                                }
                                            } else {
                                                sb.append((char) byteCompactionData[i]);
                                                break;
                                            }
                                        } else {
                                            priorToShiftMode = subMode;
                                            subMode = Mode.PUNCT_SHIFT;
                                            break;
                                        }
                                    } else {
                                        subMode = Mode.ALPHA;
                                        break;
                                    }
                                } else {
                                    subMode = Mode.LOWER;
                                    break;
                                }
                            } else {
                                ch = ' ';
                                break;
                            }
                        } else {
                            subMode = Mode.PUNCT;
                            break;
                        }
                    } else {
                        ch = MIXED_CHARS[subModeCh];
                        break;
                    }
                    break;
                case PUNCT:
                    if (subModeCh >= 29) {
                        if (subModeCh != 29) {
                            if (subModeCh != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                                if (subModeCh == 900) {
                                    subMode = Mode.ALPHA;
                                    break;
                                }
                            } else {
                                sb.append((char) byteCompactionData[i]);
                                break;
                            }
                        } else {
                            subMode = Mode.ALPHA;
                            break;
                        }
                    } else {
                        ch = PUNCT_CHARS[subModeCh];
                        break;
                    }
                    break;
                case ALPHA_SHIFT:
                    subMode = priorToShiftMode;
                    if (subModeCh >= 26) {
                        if (subModeCh != 26) {
                            if (subModeCh == 900) {
                                subMode = Mode.ALPHA;
                                break;
                            }
                        } else {
                            ch = ' ';
                            break;
                        }
                    } else {
                        ch = (char) (65 + subModeCh);
                        break;
                    }
                    break;
                case PUNCT_SHIFT:
                    subMode = priorToShiftMode;
                    if (subModeCh >= 29) {
                        if (subModeCh != 29) {
                            if (subModeCh != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                                if (subModeCh == 900) {
                                    subMode = Mode.ALPHA;
                                    break;
                                }
                            } else {
                                sb.append((char) byteCompactionData[i]);
                                break;
                            }
                        } else {
                            subMode = Mode.ALPHA;
                            break;
                        }
                    } else {
                        ch = PUNCT_CHARS[subModeCh];
                        break;
                    }
                    break;
            }
            if (ch != 0) {
                sb.append(ch);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00d0, code lost:
        if (r3 == MACRO_PDF417_TERMINATOR) goto L_0x00df;
     */
    private static int byteCompaction(int mode, int[] codewords, int codeIndex, StringBuilder result) {
        int count;
        int i = mode;
        StringBuilder sb = result;
        int i2 = 928;
        int i3 = NUMERIC_COMPACTION_MODE_LATCH;
        long j = 900;
        int i4 = BYTE_COMPACTION_MODE_LATCH_6;
        int i5 = 901;
        int i6 = 0;
        if (i == 901) {
            int count2 = 0;
            long value = 0;
            char[] decodedData = new char[6];
            int[] byteCompactedCodewords = new int[6];
            boolean end = false;
            int nextCode = codewords[codeIndex];
            int codeIndex2 = codeIndex + 1;
            while (codeIndex2 < codewords[0] && !end) {
                int count3 = count2 + 1;
                byteCompactedCodewords[count2] = nextCode;
                value = (j * value) + ((long) nextCode);
                int codeIndex3 = codeIndex2 + 1;
                nextCode = codewords[codeIndex2];
                if (nextCode == 900 || nextCode == 901 || nextCode == i3 || nextCode == BYTE_COMPACTION_MODE_LATCH_6 || nextCode == i2 || nextCode == BEGIN_MACRO_PDF417_OPTIONAL_FIELD || nextCode == MACRO_PDF417_TERMINATOR) {
                    codeIndex2 = codeIndex3 - 1;
                    end = true;
                    count2 = count3;
                } else {
                    if (count3 % 5 != 0 || count3 <= 0) {
                        count2 = count3;
                    } else {
                        for (int j2 = 0; j2 < 6; j2++) {
                            decodedData[5 - j2] = (char) ((int) (value % 256));
                            value >>= 8;
                        }
                        sb.append(decodedData);
                        count2 = 0;
                    }
                    codeIndex2 = codeIndex3;
                }
                i2 = 928;
                i3 = NUMERIC_COMPACTION_MODE_LATCH;
                j = 900;
            }
            if (codeIndex2 != codewords[0] || nextCode >= 900) {
                count = count2;
            } else {
                count = count2 + 1;
                byteCompactedCodewords[count2] = nextCode;
            }
            while (true) {
                int i7 = i6;
                if (i7 >= count) {
                    return codeIndex2;
                }
                sb.append((char) byteCompactedCodewords[i7]);
                i6 = i7 + 1;
            }
        } else if (i != BYTE_COMPACTION_MODE_LATCH_6) {
            return codeIndex;
        } else {
            long value2 = 0;
            boolean end2 = false;
            int count4 = 0;
            int count5 = codeIndex;
            while (count5 < codewords[0] && !end2) {
                int codeIndex4 = count5 + 1;
                int codeIndex5 = codewords[count5];
                if (codeIndex5 < 900) {
                    count4++;
                    value2 = (900 * value2) + ((long) codeIndex5);
                } else {
                    if (codeIndex5 != 900 && codeIndex5 != i5 && codeIndex5 != NUMERIC_COMPACTION_MODE_LATCH && codeIndex5 != i4) {
                        if (codeIndex5 != 928) {
                            if (codeIndex5 != BEGIN_MACRO_PDF417_OPTIONAL_FIELD) {
                            }
                            codeIndex4--;
                            end2 = true;
                        }
                    }
                    codeIndex4--;
                    end2 = true;
                }
                if (count4 % 5 == 0 && count4 > 0) {
                    char[] decodedData2 = new char[6];
                    long value3 = value2;
                    for (int j3 = 0; j3 < 6; j3++) {
                        decodedData2[5 - j3] = (char) ((int) (value3 & 255));
                        value3 >>= 8;
                    }
                    sb.append(decodedData2);
                    count4 = 0;
                    value2 = value3;
                }
                count5 = codeIndex4;
                i4 = BYTE_COMPACTION_MODE_LATCH_6;
                i5 = 901;
            }
            return count5;
        }
    }

    private static int numericCompaction(int[] codewords, int count, StringBuilder result) throws FormatException {
        int count2 = 0;
        boolean end = false;
        int[] numericCodewords = new int[15];
        while (count < codewords[0] && !end) {
            int codeIndex = count + 1;
            int codeIndex2 = codewords[count];
            if (codeIndex == codewords[0]) {
                end = true;
            }
            if (codeIndex2 < 900) {
                numericCodewords[count2] = codeIndex2;
                count2++;
            } else if (codeIndex2 == 900 || codeIndex2 == 901 || codeIndex2 == BYTE_COMPACTION_MODE_LATCH_6 || codeIndex2 == 928 || codeIndex2 == BEGIN_MACRO_PDF417_OPTIONAL_FIELD || codeIndex2 == MACRO_PDF417_TERMINATOR) {
                codeIndex--;
                end = true;
            }
            if (count2 % 15 == 0 || codeIndex2 == NUMERIC_COMPACTION_MODE_LATCH || end) {
                result.append(decodeBase900toBase10(numericCodewords, count2));
                count2 = 0;
            }
            count = codeIndex;
        }
        return count;
    }

    private static String decodeBase900toBase10(int[] codewords, int count) throws FormatException {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < count; i++) {
            result = result.add(EXP900[(count - i) - 1].multiply(BigInteger.valueOf((long) codewords[i])));
        }
        String resultString = result.toString();
        if (resultString.charAt(0) == '1') {
            return resultString.substring(1);
        }
        throw FormatException.getFormatInstance();
    }
}
