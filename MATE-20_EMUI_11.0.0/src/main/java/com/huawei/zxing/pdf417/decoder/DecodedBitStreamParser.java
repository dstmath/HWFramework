package com.huawei.zxing.pdf417.decoder;

import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.DecoderResult;
import com.huawei.zxing.pdf417.PDF417ResultMetadata;
import java.math.BigInteger;
import java.util.Arrays;

/* access modifiers changed from: package-private */
public final class DecodedBitStreamParser {
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
    private static final char[] MIXED_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '&', '\r', '\t', ',', ':', '#', '-', '.', '$', '/', '+', '%', '*', '=', '^'};
    private static final int ML = 28;
    private static final int MODE_SHIFT_TO_BYTE_COMPACTION_MODE = 913;
    private static final int NUMBER_OF_SEQUENCE_CODEWORDS = 2;
    private static final int NUMERIC_COMPACTION_MODE_LATCH = 902;
    private static final int PAL = 29;
    private static final int PL = 25;
    private static final int PS = 29;
    private static final char[] PUNCT_CHARS = {';', '<', '>', '@', '[', '\\', '}', '_', '`', '~', '!', '\r', '\t', ',', ':', '\n', '-', '.', '$', '/', '\"', '|', '*', '(', ')', '?', '{', '}', '\''};
    private static final int TEXT_COMPACTION_MODE_LATCH = 900;

    /* access modifiers changed from: private */
    public enum Mode {
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
        int i = 2;
        while (true) {
            BigInteger[] bigIntegerArr = EXP900;
            if (i < bigIntegerArr.length) {
                bigIntegerArr[i] = bigIntegerArr[i - 1].multiply(nineHundred);
                i++;
            } else {
                return;
            }
        }
    }

    private DecodedBitStreamParser() {
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0048 A[LOOP:0: B:1:0x0012->B:18:0x0048, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x004e A[SYNTHETIC] */
    static DecoderResult decode(int[] codewords, String ecLevel) throws FormatException {
        int codeIndex;
        StringBuilder result = new StringBuilder(codewords.length * 2);
        int code = codewords[1];
        PDF417ResultMetadata resultMetadata = new PDF417ResultMetadata();
        for (int codeIndex2 = 1 + 1; codeIndex2 < codewords[0]; codeIndex2 = codeIndex + 1) {
            if (code != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                if (code != 928) {
                    switch (code) {
                        case 900:
                            codeIndex = textCompaction(codewords, codeIndex2, result);
                            break;
                        case 901:
                            break;
                        case NUMERIC_COMPACTION_MODE_LATCH /* 902 */:
                            codeIndex = numericCompaction(codewords, codeIndex2, result);
                            break;
                        default:
                            switch (code) {
                                case MACRO_PDF417_TERMINATOR /* 922 */:
                                case BEGIN_MACRO_PDF417_OPTIONAL_FIELD /* 923 */:
                                    throw FormatException.getFormatInstance();
                                case BYTE_COMPACTION_MODE_LATCH_6 /* 924 */:
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

    /* JADX INFO: Multiple debug info for r11v6 int: [D('codeIndex' int), D('code' int)] */
    private static int decodeMacroBlock(int[] codewords, int codeIndex, PDF417ResultMetadata resultMetadata) throws FormatException {
        if (codeIndex + 2 <= codewords[0]) {
            int[] segmentIndexArray = new int[2];
            int i = 0;
            while (i < 2) {
                segmentIndexArray[i] = codewords[codeIndex];
                i++;
                codeIndex++;
            }
            resultMetadata.setSegmentIndex(Integer.parseInt(decodeBase900toBase10(segmentIndexArray, 2)));
            StringBuilder fileId = new StringBuilder();
            int codeIndex2 = textCompaction(codewords, codeIndex, fileId);
            resultMetadata.setFileId(fileId.toString());
            if (codewords[codeIndex2] == BEGIN_MACRO_PDF417_OPTIONAL_FIELD) {
                int code = codeIndex2 + 1;
                int[] additionalOptionCodeWords = new int[(codewords[0] - code)];
                int additionalOptionCodeWordsIndex = 0;
                boolean end = false;
                while (code < codewords[0] && !end) {
                    int codeIndex3 = code + 1;
                    int code2 = codewords[code];
                    if (code2 < 900) {
                        additionalOptionCodeWords[additionalOptionCodeWordsIndex] = code2;
                        code = codeIndex3;
                        additionalOptionCodeWordsIndex++;
                    } else if (code2 == MACRO_PDF417_TERMINATOR) {
                        resultMetadata.setLastSegment(true);
                        end = true;
                        code = codeIndex3 + 1;
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                }
                resultMetadata.setOptionalData(Arrays.copyOf(additionalOptionCodeWords, additionalOptionCodeWordsIndex));
                return code;
            } else if (codewords[codeIndex2] != MACRO_PDF417_TERMINATOR) {
                return codeIndex2;
            } else {
                resultMetadata.setLastSegment(true);
                return codeIndex2 + 1;
            }
        } else {
            throw FormatException.getFormatInstance();
        }
    }

    /* JADX INFO: Multiple debug info for r9v2 int: [D('codeIndex' int), D('code' int)] */
    private static int textCompaction(int[] codewords, int code, StringBuilder result) {
        int[] textCompactionData = new int[((codewords[0] - code) << 1)];
        int[] byteCompactionData = new int[((codewords[0] - code) << 1)];
        int index = 0;
        boolean end = false;
        while (code < codewords[0] && !end) {
            int codeIndex = code + 1;
            int code2 = codewords[code];
            if (code2 < 900) {
                textCompactionData[index] = code2 / 30;
                textCompactionData[index + 1] = code2 % 30;
                index += 2;
                code = codeIndex;
            } else if (code2 != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                if (code2 != 928) {
                    switch (code2) {
                        case 900:
                            textCompactionData[index] = 900;
                            code = codeIndex;
                            index++;
                            break;
                        default:
                            switch (code2) {
                                case MACRO_PDF417_TERMINATOR /* 922 */:
                                case BEGIN_MACRO_PDF417_OPTIONAL_FIELD /* 923 */:
                                case BYTE_COMPACTION_MODE_LATCH_6 /* 924 */:
                                    break;
                                default:
                                    code = codeIndex;
                                    break;
                            }
                        case 901:
                        case NUMERIC_COMPACTION_MODE_LATCH /* 902 */:
                            end = true;
                            code = codeIndex - 1;
                            break;
                    }
                }
                end = true;
                code = codeIndex - 1;
            } else {
                textCompactionData[index] = MODE_SHIFT_TO_BYTE_COMPACTION_MODE;
                byteCompactionData[index] = codewords[codeIndex];
                index++;
                code = codeIndex + 1;
            }
        }
        decodeTextCompaction(textCompactionData, byteCompactionData, index, result);
        return code;
    }

    private static void decodeTextCompaction(int[] textCompactionData, int[] byteCompactionData, int length, StringBuilder result) {
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
                                            result.append((char) byteCompactionData[i]);
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
                        ch = (char) (subModeCh + 65);
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
                                            result.append((char) byteCompactionData[i]);
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
                        ch = (char) (subModeCh + 97);
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
                                                result.append((char) byteCompactionData[i]);
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
                                result.append((char) byteCompactionData[i]);
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
                        ch = (char) (subModeCh + 65);
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
                                result.append((char) byteCompactionData[i]);
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
                result.append(ch);
            }
        }
    }

    /* JADX INFO: Multiple debug info for r2v3 int: [D('codeIndex' int), D('code' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00eb, code lost:
        if (r2 == com.huawei.zxing.pdf417.decoder.DecodedBitStreamParser.MACRO_PDF417_TERMINATOR) goto L_0x00f4;
     */
    private static int byteCompaction(int mode, int[] codewords, int codeIndex, StringBuilder result) {
        boolean end;
        int count;
        int i = BEGIN_MACRO_PDF417_OPTIONAL_FIELD;
        int i2 = 928;
        int i3 = NUMERIC_COMPACTION_MODE_LATCH;
        long j = 900;
        if (mode == 901) {
            char[] decodedData = new char[6];
            int[] byteCompactedCodewords = new int[6];
            boolean end2 = false;
            int count2 = 0;
            int count3 = codeIndex + 1;
            int nextCode = codewords[codeIndex];
            long value = 0;
            while (count3 < codewords[0] && !end2) {
                int count4 = count2 + 1;
                byteCompactedCodewords[count2] = nextCode;
                value = (value * j) + ((long) nextCode);
                int codeIndex2 = count3 + 1;
                nextCode = codewords[count3];
                if (nextCode == 900 || nextCode == 901 || nextCode == NUMERIC_COMPACTION_MODE_LATCH || nextCode == BYTE_COMPACTION_MODE_LATCH_6 || nextCode == i2 || nextCode == i || nextCode == MACRO_PDF417_TERMINATOR) {
                    count3 = codeIndex2 - 1;
                    end2 = true;
                    count2 = count4;
                    i = BEGIN_MACRO_PDF417_OPTIONAL_FIELD;
                    i2 = 928;
                    j = 900;
                } else if (count4 % 5 != 0 || count4 <= 0) {
                    count3 = codeIndex2;
                    count2 = count4;
                    i = BEGIN_MACRO_PDF417_OPTIONAL_FIELD;
                    i2 = 928;
                    j = 900;
                } else {
                    for (int j2 = 0; j2 < 6; j2++) {
                        decodedData[5 - j2] = (char) ((int) (value % 256));
                        value >>= 8;
                    }
                    result.append(decodedData);
                    count2 = 0;
                    count3 = codeIndex2;
                    i = BEGIN_MACRO_PDF417_OPTIONAL_FIELD;
                    i2 = 928;
                    j = 900;
                }
            }
            if (count3 != codewords[0] || nextCode >= 900) {
                count = count2;
            } else {
                count = count2 + 1;
                byteCompactedCodewords[count2] = nextCode;
            }
            for (int i4 = 0; i4 < count; i4++) {
                result.append((char) byteCompactedCodewords[i4]);
            }
            return count3;
        } else if (mode != BYTE_COMPACTION_MODE_LATCH_6) {
            return codeIndex;
        } else {
            boolean end3 = false;
            long value2 = 0;
            int count5 = 0;
            int count6 = codeIndex;
            for (char c = 0; count6 < codewords[c] && !end3; c = 0) {
                int codeIndex3 = count6 + 1;
                int code = codewords[count6];
                if (code < 900) {
                    count5++;
                    value2 = (value2 * 900) + ((long) code);
                } else {
                    if (code != 900 && code != 901 && code != i3 && code != BYTE_COMPACTION_MODE_LATCH_6) {
                        if (code != 928) {
                            if (code != BEGIN_MACRO_PDF417_OPTIONAL_FIELD) {
                            }
                            codeIndex3--;
                            end3 = true;
                        }
                    }
                    codeIndex3--;
                    end3 = true;
                }
                if (count5 % 5 != 0 || count5 <= 0) {
                    end = end3;
                } else {
                    char[] decodedData2 = new char[6];
                    int j3 = 0;
                    long value3 = value2;
                    for (int i5 = 6; j3 < i5; i5 = 6) {
                        decodedData2[5 - j3] = (char) ((int) (value3 & 255));
                        value3 >>= 8;
                        j3++;
                        end3 = end3;
                    }
                    end = end3;
                    result.append(decodedData2);
                    count5 = 0;
                    value2 = value3;
                }
                end3 = end;
                count6 = codeIndex3;
                i3 = NUMERIC_COMPACTION_MODE_LATCH;
            }
            return count6;
        }
    }

    /* JADX INFO: Multiple debug info for r6v2 int: [D('codeIndex' int), D('code' int)] */
    private static int numericCompaction(int[] codewords, int code, StringBuilder result) throws FormatException {
        int count = 0;
        boolean end = false;
        int[] numericCodewords = new int[15];
        while (code < codewords[0] && !end) {
            int codeIndex = code + 1;
            int code2 = codewords[code];
            if (codeIndex == codewords[0]) {
                end = true;
            }
            if (code2 < 900) {
                numericCodewords[count] = code2;
                count++;
            } else if (code2 == 900 || code2 == 901 || code2 == BYTE_COMPACTION_MODE_LATCH_6 || code2 == 928 || code2 == BEGIN_MACRO_PDF417_OPTIONAL_FIELD || code2 == MACRO_PDF417_TERMINATOR) {
                codeIndex--;
                end = true;
            }
            if (count % 15 == 0 || code2 == NUMERIC_COMPACTION_MODE_LATCH || end) {
                result.append(decodeBase900toBase10(numericCodewords, count));
                count = 0;
            }
            code = codeIndex;
        }
        return code;
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
