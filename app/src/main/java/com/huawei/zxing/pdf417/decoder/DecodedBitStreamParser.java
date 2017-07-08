package com.huawei.zxing.pdf417.decoder;

import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.DecoderResult;
import com.huawei.zxing.pdf417.PDF417ResultMetadata;
import huawei.android.widget.DialogContentHelper.Dex;
import huawei.android.widget.ViewDragHelper;
import java.math.BigInteger;
import java.util.Arrays;

final class DecodedBitStreamParser {
    private static final /* synthetic */ int[] -com-huawei-zxing-pdf417-decoder-DecodedBitStreamParser$ModeSwitchesValues = null;
    private static final int AL = 28;
    private static final int AS = 27;
    private static final int BEGIN_MACRO_PDF417_CONTROL_BLOCK = 928;
    private static final int BEGIN_MACRO_PDF417_OPTIONAL_FIELD = 923;
    private static final int BYTE_COMPACTION_MODE_LATCH = 901;
    private static final int BYTE_COMPACTION_MODE_LATCH_6 = 924;
    private static final BigInteger[] EXP900 = null;
    private static final int LL = 27;
    private static final int MACRO_PDF417_TERMINATOR = 922;
    private static final int MAX_NUMERIC_CODEWORDS = 15;
    private static final char[] MIXED_CHARS = null;
    private static final int ML = 28;
    private static final int MODE_SHIFT_TO_BYTE_COMPACTION_MODE = 913;
    private static final int NUMBER_OF_SEQUENCE_CODEWORDS = 2;
    private static final int NUMERIC_COMPACTION_MODE_LATCH = 902;
    private static final int PAL = 29;
    private static final int PL = 25;
    private static final int PS = 29;
    private static final char[] PUNCT_CHARS = null;
    private static final int TEXT_COMPACTION_MODE_LATCH = 900;

    private enum Mode {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-zxing-pdf417-decoder-DecodedBitStreamParser$ModeSwitchesValues() {
        if (-com-huawei-zxing-pdf417-decoder-DecodedBitStreamParser$ModeSwitchesValues != null) {
            return -com-huawei-zxing-pdf417-decoder-DecodedBitStreamParser$ModeSwitchesValues;
        }
        int[] iArr = new int[Mode.values().length];
        try {
            iArr[Mode.ALPHA.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Mode.ALPHA_SHIFT.ordinal()] = NUMBER_OF_SEQUENCE_CODEWORDS;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Mode.LOWER.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Mode.MIXED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Mode.PUNCT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Mode.PUNCT_SHIFT.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-huawei-zxing-pdf417-decoder-DecodedBitStreamParser$ModeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.pdf417.decoder.DecodedBitStreamParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.pdf417.decoder.DecodedBitStreamParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.pdf417.decoder.DecodedBitStreamParser.<clinit>():void");
    }

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(int[] codewords, String ecLevel) throws FormatException {
        StringBuilder result = new StringBuilder(codewords.length * NUMBER_OF_SEQUENCE_CODEWORDS);
        int codeIndex = NUMBER_OF_SEQUENCE_CODEWORDS;
        int code = codewords[1];
        PDF417ResultMetadata resultMetadata = new PDF417ResultMetadata();
        while (codeIndex < codewords[0]) {
            switch (code) {
                case TEXT_COMPACTION_MODE_LATCH /*900*/:
                    codeIndex = textCompaction(codewords, codeIndex, result);
                    break;
                case BYTE_COMPACTION_MODE_LATCH /*901*/:
                case MODE_SHIFT_TO_BYTE_COMPACTION_MODE /*913*/:
                case BYTE_COMPACTION_MODE_LATCH_6 /*924*/:
                    codeIndex = byteCompaction(code, codewords, codeIndex, result);
                    break;
                case NUMERIC_COMPACTION_MODE_LATCH /*902*/:
                    codeIndex = numericCompaction(codewords, codeIndex, result);
                    break;
                case MACRO_PDF417_TERMINATOR /*922*/:
                case BEGIN_MACRO_PDF417_OPTIONAL_FIELD /*923*/:
                    throw FormatException.getFormatInstance();
                case BEGIN_MACRO_PDF417_CONTROL_BLOCK /*928*/:
                    codeIndex = decodeMacroBlock(codewords, codeIndex, resultMetadata);
                    break;
                default:
                    codeIndex = textCompaction(codewords, codeIndex - 1, result);
                    break;
            }
            if (codeIndex < codewords.length) {
                int codeIndex2 = codeIndex + 1;
                code = codewords[codeIndex];
                codeIndex = codeIndex2;
            } else {
                throw FormatException.getFormatInstance();
            }
        }
        if (result.length() == 0) {
            throw FormatException.getFormatInstance();
        }
        DecoderResult decoderResult = new DecoderResult(null, result.toString(), null, ecLevel);
        decoderResult.setOther(resultMetadata);
        return decoderResult;
    }

    private static int decodeMacroBlock(int[] codewords, int codeIndex, PDF417ResultMetadata resultMetadata) throws FormatException {
        if (codeIndex + NUMBER_OF_SEQUENCE_CODEWORDS > codewords[0]) {
            throw FormatException.getFormatInstance();
        }
        int[] segmentIndexArray = new int[NUMBER_OF_SEQUENCE_CODEWORDS];
        int i = 0;
        while (i < NUMBER_OF_SEQUENCE_CODEWORDS) {
            segmentIndexArray[i] = codewords[codeIndex];
            i++;
            codeIndex++;
        }
        resultMetadata.setSegmentIndex(Integer.parseInt(decodeBase900toBase10(segmentIndexArray, NUMBER_OF_SEQUENCE_CODEWORDS)));
        StringBuilder fileId = new StringBuilder();
        codeIndex = textCompaction(codewords, codeIndex, fileId);
        resultMetadata.setFileId(fileId.toString());
        if (codewords[codeIndex] == BEGIN_MACRO_PDF417_OPTIONAL_FIELD) {
            codeIndex++;
            int[] additionalOptionCodeWords = new int[(codewords[0] - codeIndex)];
            int additionalOptionCodeWordsIndex = 0;
            boolean end = false;
            while (codeIndex < codewords[0] && !end) {
                int codeIndex2 = codeIndex + 1;
                int code = codewords[codeIndex];
                if (code < TEXT_COMPACTION_MODE_LATCH) {
                    int additionalOptionCodeWordsIndex2 = additionalOptionCodeWordsIndex + 1;
                    additionalOptionCodeWords[additionalOptionCodeWordsIndex] = code;
                    additionalOptionCodeWordsIndex = additionalOptionCodeWordsIndex2;
                    codeIndex = codeIndex2;
                } else {
                    switch (code) {
                        case MACRO_PDF417_TERMINATOR /*922*/:
                            resultMetadata.setLastSegment(true);
                            codeIndex = codeIndex2 + 1;
                            end = true;
                            break;
                        default:
                            throw FormatException.getFormatInstance();
                    }
                }
            }
            resultMetadata.setOptionalData(Arrays.copyOf(additionalOptionCodeWords, additionalOptionCodeWordsIndex));
            return codeIndex;
        } else if (codewords[codeIndex] != MACRO_PDF417_TERMINATOR) {
            return codeIndex;
        } else {
            resultMetadata.setLastSegment(true);
            return codeIndex + 1;
        }
    }

    private static int textCompaction(int[] codewords, int codeIndex, StringBuilder result) {
        int[] textCompactionData = new int[((codewords[0] - codeIndex) << 1)];
        int[] byteCompactionData = new int[((codewords[0] - codeIndex) << 1)];
        int index = 0;
        boolean end = false;
        while (codeIndex < codewords[0] && !end) {
            int codeIndex2 = codeIndex + 1;
            int code = codewords[codeIndex];
            if (code >= TEXT_COMPACTION_MODE_LATCH) {
                switch (code) {
                    case TEXT_COMPACTION_MODE_LATCH /*900*/:
                        int index2 = index + 1;
                        textCompactionData[index] = TEXT_COMPACTION_MODE_LATCH;
                        index = index2;
                        codeIndex = codeIndex2;
                        break;
                    case BYTE_COMPACTION_MODE_LATCH /*901*/:
                    case NUMERIC_COMPACTION_MODE_LATCH /*902*/:
                    case MACRO_PDF417_TERMINATOR /*922*/:
                    case BEGIN_MACRO_PDF417_OPTIONAL_FIELD /*923*/:
                    case BYTE_COMPACTION_MODE_LATCH_6 /*924*/:
                    case BEGIN_MACRO_PDF417_CONTROL_BLOCK /*928*/:
                        codeIndex = codeIndex2 - 1;
                        end = true;
                        break;
                    case MODE_SHIFT_TO_BYTE_COMPACTION_MODE /*913*/:
                        textCompactionData[index] = MODE_SHIFT_TO_BYTE_COMPACTION_MODE;
                        codeIndex = codeIndex2 + 1;
                        byteCompactionData[index] = codewords[codeIndex2];
                        index++;
                        break;
                    default:
                        codeIndex = codeIndex2;
                        break;
                }
            }
            textCompactionData[index] = code / 30;
            textCompactionData[index + 1] = code % 30;
            index += NUMBER_OF_SEQUENCE_CODEWORDS;
            codeIndex = codeIndex2;
        }
        decodeTextCompaction(textCompactionData, byteCompactionData, index, result);
        return codeIndex;
    }

    private static void decodeTextCompaction(int[] textCompactionData, int[] byteCompactionData, int length, StringBuilder result) {
        Mode subMode = Mode.ALPHA;
        Mode priorToShiftMode = Mode.ALPHA;
        for (int i = 0; i < length; i++) {
            int subModeCh = textCompactionData[i];
            char ch = '\u0000';
            switch (-getcom-huawei-zxing-pdf417-decoder-DecodedBitStreamParser$ModeSwitchesValues()[subMode.ordinal()]) {
                case ViewDragHelper.STATE_DRAGGING /*1*/:
                    if (subModeCh >= 26) {
                        if (subModeCh != 26) {
                            if (subModeCh != LL) {
                                if (subModeCh != ML) {
                                    if (subModeCh != PS) {
                                        if (subModeCh != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                                            if (subModeCh == TEXT_COMPACTION_MODE_LATCH) {
                                                subMode = Mode.ALPHA;
                                                break;
                                            }
                                        }
                                        result.append((char) byteCompactionData[i]);
                                        break;
                                    }
                                    priorToShiftMode = subMode;
                                    subMode = Mode.PUNCT_SHIFT;
                                    break;
                                }
                                subMode = Mode.MIXED;
                                break;
                            }
                            subMode = Mode.LOWER;
                            break;
                        }
                        ch = ' ';
                        break;
                    }
                    ch = (char) (subModeCh + 65);
                    break;
                    break;
                case NUMBER_OF_SEQUENCE_CODEWORDS /*2*/:
                    subMode = priorToShiftMode;
                    if (subModeCh >= 26) {
                        if (subModeCh != 26) {
                            if (subModeCh == TEXT_COMPACTION_MODE_LATCH) {
                                subMode = Mode.ALPHA;
                                break;
                            }
                        }
                        ch = ' ';
                        break;
                    }
                    ch = (char) (subModeCh + 65);
                    break;
                    break;
                case ViewDragHelper.DIRECTION_ALL /*3*/:
                    if (subModeCh >= 26) {
                        if (subModeCh != 26) {
                            if (subModeCh != LL) {
                                if (subModeCh != ML) {
                                    if (subModeCh != PS) {
                                        if (subModeCh != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                                            if (subModeCh == TEXT_COMPACTION_MODE_LATCH) {
                                                subMode = Mode.ALPHA;
                                                break;
                                            }
                                        }
                                        result.append((char) byteCompactionData[i]);
                                        break;
                                    }
                                    priorToShiftMode = subMode;
                                    subMode = Mode.PUNCT_SHIFT;
                                    break;
                                }
                                subMode = Mode.MIXED;
                                break;
                            }
                            priorToShiftMode = subMode;
                            subMode = Mode.ALPHA_SHIFT;
                            break;
                        }
                        ch = ' ';
                        break;
                    }
                    ch = (char) (subModeCh + 97);
                    break;
                    break;
                case ViewDragHelper.EDGE_TOP /*4*/:
                    if (subModeCh >= PL) {
                        if (subModeCh != PL) {
                            if (subModeCh != 26) {
                                if (subModeCh != LL) {
                                    if (subModeCh != ML) {
                                        if (subModeCh != PS) {
                                            if (subModeCh != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                                                if (subModeCh == TEXT_COMPACTION_MODE_LATCH) {
                                                    subMode = Mode.ALPHA;
                                                    break;
                                                }
                                            }
                                            result.append((char) byteCompactionData[i]);
                                            break;
                                        }
                                        priorToShiftMode = subMode;
                                        subMode = Mode.PUNCT_SHIFT;
                                        break;
                                    }
                                    subMode = Mode.ALPHA;
                                    break;
                                }
                                subMode = Mode.LOWER;
                                break;
                            }
                            ch = ' ';
                            break;
                        }
                        subMode = Mode.PUNCT;
                        break;
                    }
                    ch = MIXED_CHARS[subModeCh];
                    break;
                    break;
                case Dex.DIALOG_BODY_TWO_IMAGES /*5*/:
                    if (subModeCh >= PS) {
                        if (subModeCh != PS) {
                            if (subModeCh != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                                if (subModeCh == TEXT_COMPACTION_MODE_LATCH) {
                                    subMode = Mode.ALPHA;
                                    break;
                                }
                            }
                            result.append((char) byteCompactionData[i]);
                            break;
                        }
                        subMode = Mode.ALPHA;
                        break;
                    }
                    ch = PUNCT_CHARS[subModeCh];
                    break;
                    break;
                case Dex.DIALOG_BODY_View /*6*/:
                    subMode = priorToShiftMode;
                    if (subModeCh >= PS) {
                        if (subModeCh != PS) {
                            if (subModeCh != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                                if (subModeCh == TEXT_COMPACTION_MODE_LATCH) {
                                    subMode = Mode.ALPHA;
                                    break;
                                }
                            }
                            result.append((char) byteCompactionData[i]);
                            break;
                        }
                        subMode = Mode.ALPHA;
                        break;
                    }
                    ch = PUNCT_CHARS[subModeCh];
                    break;
                    break;
            }
            if (ch != '\u0000') {
                result.append(ch);
            }
        }
    }

    private static int byteCompaction(int mode, int[] codewords, int codeIndex, StringBuilder result) {
        int count;
        long value;
        char[] decodedData;
        boolean end;
        int codeIndex2;
        int j;
        if (mode == BYTE_COMPACTION_MODE_LATCH) {
            int count2;
            count = 0;
            value = 0;
            decodedData = new char[6];
            int[] byteCompactedCodewords = new int[6];
            end = false;
            codeIndex2 = codeIndex + 1;
            int nextCode = codewords[codeIndex];
            codeIndex = codeIndex2;
            while (codeIndex < codewords[0] && !end) {
                count2 = count + 1;
                byteCompactedCodewords[count] = nextCode;
                value = (900 * value) + ((long) nextCode);
                codeIndex2 = codeIndex + 1;
                nextCode = codewords[codeIndex];
                if (nextCode == TEXT_COMPACTION_MODE_LATCH || nextCode == BYTE_COMPACTION_MODE_LATCH || nextCode == NUMERIC_COMPACTION_MODE_LATCH || nextCode == BYTE_COMPACTION_MODE_LATCH_6 || nextCode == BEGIN_MACRO_PDF417_CONTROL_BLOCK || nextCode == BEGIN_MACRO_PDF417_OPTIONAL_FIELD || nextCode == MACRO_PDF417_TERMINATOR) {
                    codeIndex = codeIndex2 - 1;
                    end = true;
                    count = count2;
                } else if (count2 % 5 != 0 || count2 <= 0) {
                    count = count2;
                    codeIndex = codeIndex2;
                } else {
                    for (j = 0; j < 6; j++) {
                        decodedData[5 - j] = (char) ((int) (value % 256));
                        value >>= 8;
                    }
                    result.append(decodedData);
                    count = 0;
                    codeIndex = codeIndex2;
                }
            }
            if (codeIndex == codewords[0] && nextCode < TEXT_COMPACTION_MODE_LATCH) {
                count2 = count + 1;
                byteCompactedCodewords[count] = nextCode;
                count = count2;
            }
            for (int i = 0; i < count; i++) {
                result.append((char) byteCompactedCodewords[i]);
            }
        } else if (mode == BYTE_COMPACTION_MODE_LATCH_6) {
            count = 0;
            value = 0;
            end = false;
            while (codeIndex < codewords[0] && !r8) {
                codeIndex2 = codeIndex + 1;
                int code = codewords[codeIndex];
                if (code < TEXT_COMPACTION_MODE_LATCH) {
                    count++;
                    value = (900 * value) + ((long) code);
                    codeIndex = codeIndex2;
                } else if (code == TEXT_COMPACTION_MODE_LATCH || code == BYTE_COMPACTION_MODE_LATCH || code == NUMERIC_COMPACTION_MODE_LATCH || code == BYTE_COMPACTION_MODE_LATCH_6 || code == BEGIN_MACRO_PDF417_CONTROL_BLOCK || code == BEGIN_MACRO_PDF417_OPTIONAL_FIELD || code == MACRO_PDF417_TERMINATOR) {
                    codeIndex = codeIndex2 - 1;
                    end = true;
                } else {
                    codeIndex = codeIndex2;
                }
                if (count % 5 == 0 && count > 0) {
                    decodedData = new char[6];
                    for (j = 0; j < 6; j++) {
                        decodedData[5 - j] = (char) ((int) (255 & value));
                        value >>= 8;
                    }
                    result.append(decodedData);
                    count = 0;
                }
            }
        }
        return codeIndex;
    }

    private static int numericCompaction(int[] codewords, int codeIndex, StringBuilder result) throws FormatException {
        int count = 0;
        boolean end = false;
        int[] numericCodewords = new int[MAX_NUMERIC_CODEWORDS];
        while (codeIndex < codewords[0] && !end) {
            int codeIndex2 = codeIndex + 1;
            int code = codewords[codeIndex];
            if (codeIndex2 == codewords[0]) {
                end = true;
            }
            if (code < TEXT_COMPACTION_MODE_LATCH) {
                numericCodewords[count] = code;
                count++;
                codeIndex = codeIndex2;
            } else if (code == TEXT_COMPACTION_MODE_LATCH || code == BYTE_COMPACTION_MODE_LATCH || code == BYTE_COMPACTION_MODE_LATCH_6 || code == BEGIN_MACRO_PDF417_CONTROL_BLOCK || code == BEGIN_MACRO_PDF417_OPTIONAL_FIELD || code == MACRO_PDF417_TERMINATOR) {
                codeIndex = codeIndex2 - 1;
                end = true;
            } else {
                codeIndex = codeIndex2;
            }
            if (!(count % MAX_NUMERIC_CODEWORDS == 0 || code == NUMERIC_COMPACTION_MODE_LATCH)) {
                if (end) {
                }
            }
            result.append(decodeBase900toBase10(numericCodewords, count));
            count = 0;
        }
        return codeIndex;
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
