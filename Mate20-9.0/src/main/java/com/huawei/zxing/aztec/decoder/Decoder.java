package com.huawei.zxing.aztec.decoder;

import com.huawei.android.smcs.SmartTrimProcessEvent;
import com.huawei.internal.telephony.PhoneConstantsEx;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.aztec.AztecDetectorResult;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.DecoderResult;
import com.huawei.zxing.common.reedsolomon.GenericGF;
import com.huawei.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.huawei.zxing.common.reedsolomon.ReedSolomonException;
import java.util.Arrays;

public final class Decoder {
    private static final String[] DIGIT_TABLE = {"CTRL_PS", " ", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", SmartTrimProcessEvent.ST_EVENT_STRING_TOKEN, ".", "CTRL_UL", "CTRL_US"};
    private static final String[] LOWER_TABLE = {"CTRL_PS", " ", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "CTRL_US", "CTRL_ML", "CTRL_DL", "CTRL_BS"};
    private static final String[] MIXED_TABLE = {"CTRL_PS", " ", "\u0001", "\u0002", "\u0003", "\u0004", "\u0005", "\u0006", "\u0007", "\b", "\t", "\n", "\u000b", "\f", "\r", "\u001b", "\u001c", "\u001d", "\u001e", "\u001f", "@", "\\", "^", "_", "`", "|", "~", "", "CTRL_LL", "CTRL_UL", "CTRL_PL", "CTRL_BS"};
    private static final String[] PUNCT_TABLE = {"", "\r", "\r\n", ". ", ", ", ": ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", PhoneConstantsEx.APN_TYPE_ALL, "+", SmartTrimProcessEvent.ST_EVENT_STRING_TOKEN, "-", ".", "/", ":", SmartTrimProcessEvent.ST_EVENT_INTER_STRING_TOKEN, "<", "=", ">", "?", "[", "]", "{", "}", "CTRL_UL"};
    private static final String[] UPPER_TABLE = {"CTRL_PS", " ", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CTRL_LL", "CTRL_ML", "CTRL_DL", "CTRL_BS"};
    private AztecDetectorResult ddata;

    private enum Table {
        UPPER,
        LOWER,
        MIXED,
        DIGIT,
        PUNCT,
        BINARY
    }

    public DecoderResult decode(AztecDetectorResult detectorResult) throws FormatException {
        this.ddata = detectorResult;
        return new DecoderResult(null, getEncodedData(correctBits(extractBits(detectorResult.getBits()))), null, null);
    }

    public static String highLevelDecode(boolean[] correctedBits) {
        return getEncodedData(correctedBits);
    }

    private static String getEncodedData(boolean[] correctedBits) {
        int endIndex = correctedBits.length;
        Table latchTable = Table.UPPER;
        Table shiftTable = Table.UPPER;
        StringBuilder result = new StringBuilder(20);
        Table latchTable2 = latchTable;
        int index = 0;
        while (index < endIndex) {
            if (shiftTable != Table.BINARY) {
                int size = shiftTable == Table.DIGIT ? 4 : 5;
                if (endIndex - index < size) {
                    break;
                }
                int code = readCode(correctedBits, index, size);
                index += size;
                String str = getCharacter(shiftTable, code);
                if (str.startsWith("CTRL_")) {
                    shiftTable = getTable(str.charAt(5));
                    if (str.charAt(6) == 'L') {
                        latchTable2 = shiftTable;
                    }
                } else {
                    result.append(str);
                    shiftTable = latchTable2;
                }
            } else if (endIndex - index < 5) {
                break;
            } else {
                int length = readCode(correctedBits, index, 5);
                int index2 = index + 5;
                if (length == 0) {
                    if (endIndex - index2 < 11) {
                        break;
                    }
                    length = readCode(correctedBits, index2, 11) + 31;
                    index2 += 11;
                }
                int index3 = index2;
                int charCount = 0;
                while (true) {
                    if (charCount >= length) {
                        break;
                    } else if (endIndex - index3 < 8) {
                        index3 = endIndex;
                        break;
                    } else {
                        result.append((char) readCode(correctedBits, index3, 8));
                        index3 += 8;
                        charCount++;
                    }
                }
                index = index3;
                shiftTable = latchTable2;
            }
        }
        return result.toString();
    }

    private static Table getTable(char t) {
        if (t == 'B') {
            return Table.BINARY;
        }
        if (t == 'D') {
            return Table.DIGIT;
        }
        if (t == 'P') {
            return Table.PUNCT;
        }
        switch (t) {
            case 'L':
                return Table.LOWER;
            case 'M':
                return Table.MIXED;
            default:
                return Table.UPPER;
        }
    }

    private static String getCharacter(Table table, int code) {
        switch (table) {
            case UPPER:
                return UPPER_TABLE[code];
            case LOWER:
                return LOWER_TABLE[code];
            case MIXED:
                return MIXED_TABLE[code];
            case PUNCT:
                return PUNCT_TABLE[code];
            case DIGIT:
                return DIGIT_TABLE[code];
            default:
                throw new IllegalStateException("Bad table");
        }
    }

    private boolean[] correctBits(boolean[] rawbits) throws FormatException {
        int codewordSize;
        GenericGF gf;
        boolean[] zArr = rawbits;
        if (this.ddata.getNbLayers() <= 2) {
            codewordSize = 6;
            gf = GenericGF.AZTEC_DATA_6;
        } else if (this.ddata.getNbLayers() <= 8) {
            codewordSize = 8;
            gf = GenericGF.AZTEC_DATA_8;
        } else if (this.ddata.getNbLayers() <= 22) {
            codewordSize = 10;
            gf = GenericGF.AZTEC_DATA_10;
        } else {
            codewordSize = 12;
            gf = GenericGF.AZTEC_DATA_12;
        }
        int codewordSize2 = codewordSize;
        int numDataCodewords = this.ddata.getNbDatablocks();
        int numCodewords = zArr.length / codewordSize2;
        int numECCodewords = numCodewords - numDataCodewords;
        int[] dataWords = new int[numCodewords];
        int offset = zArr.length % codewordSize2;
        int i = 0;
        while (i < numCodewords) {
            dataWords[i] = readCode(zArr, offset, codewordSize2);
            i++;
            offset += codewordSize2;
        }
        try {
            new ReedSolomonDecoder(gf).decode(dataWords, numECCodewords);
            int i2 = 1;
            int mask = (1 << codewordSize2) - 1;
            int stuffedBits = 0;
            for (int i3 = 0; i3 < numDataCodewords; i3++) {
                int dataWord = dataWords[i3];
                if (dataWord == 0 || dataWord == mask) {
                    throw FormatException.getFormatInstance();
                }
                if (dataWord == 1 || dataWord == mask - 1) {
                    stuffedBits++;
                }
            }
            boolean[] correctedBits = new boolean[((numDataCodewords * codewordSize2) - stuffedBits)];
            int index = 0;
            int i4 = 0;
            while (i4 < numDataCodewords) {
                int dataWord2 = dataWords[i4];
                if (dataWord2 == i2 || dataWord2 == mask - 1) {
                    boolean z = true;
                    int i5 = (index + codewordSize2) - 1;
                    if (dataWord2 <= 1) {
                        z = false;
                    }
                    Arrays.fill(correctedBits, index, i5, z);
                    index += codewordSize2 - 1;
                } else {
                    int bit = codewordSize2 - 1;
                    while (bit >= 0) {
                        int index2 = index + 1;
                        correctedBits[index] = (dataWord2 & (1 << bit)) != 0;
                        bit--;
                        index = index2;
                    }
                }
                i4++;
                i2 = 1;
            }
            return correctedBits;
        } catch (ReedSolomonException e) {
            throw FormatException.getFormatInstance();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean[] extractBits(BitMatrix matrix) {
        BitMatrix bitMatrix = matrix;
        boolean compact = this.ddata.isCompact();
        int layers = this.ddata.getNbLayers();
        int baseMatrixSize = (compact ? 11 : 14) + (layers * 4);
        int[] alignmentMap = new int[baseMatrixSize];
        boolean[] rawbits = new boolean[totalBitsInLayer(layers, compact)];
        int i = 2;
        if (compact) {
            for (int i2 = 0; i2 < alignmentMap.length; i2++) {
                alignmentMap[i2] = i2;
            }
        } else {
            int origCenter = baseMatrixSize / 2;
            int center = ((baseMatrixSize + 1) + ((((baseMatrixSize / 2) - 1) / 15) * 2)) / 2;
            for (int i3 = 0; i3 < origCenter; i3++) {
                alignmentMap[(origCenter - i3) - 1] = (center - (i3 / 15) + i3) - 1;
                alignmentMap[origCenter + i3] = center + (i3 / 15) + i3 + 1;
            }
        }
        int i4 = 0;
        int rowOffset = 0;
        while (i4 < layers) {
            int rowSize = compact ? ((layers - i4) * 4) + 9 : ((layers - i4) * 4) + 12;
            int low = i4 * 2;
            int high = (baseMatrixSize - 1) - low;
            int j = 0;
            while (j < rowSize) {
                int columnOffset = j * 2;
                int k = 0;
                while (true) {
                    int k2 = k;
                    if (k2 >= i) {
                        break;
                    }
                    rawbits[rowOffset + columnOffset + k2] = bitMatrix.get(alignmentMap[low + k2], alignmentMap[low + j]);
                    rawbits[(2 * rowSize) + rowOffset + columnOffset + k2] = bitMatrix.get(alignmentMap[low + j], alignmentMap[high - k2]);
                    rawbits[(4 * rowSize) + rowOffset + columnOffset + k2] = bitMatrix.get(alignmentMap[high - k2], alignmentMap[high - j]);
                    rawbits[(6 * rowSize) + rowOffset + columnOffset + k2] = bitMatrix.get(alignmentMap[high - j], alignmentMap[low + k2]);
                    k = k2 + 1;
                    i = 2;
                }
                j++;
                i = 2;
            }
            rowOffset += rowSize * 8;
            i4++;
            i = 2;
        }
        return rawbits;
    }

    private static int readCode(boolean[] rawbits, int startIndex, int length) {
        int res = 0;
        for (int i = startIndex; i < startIndex + length; i++) {
            res <<= 1;
            if (rawbits[i]) {
                res++;
            }
        }
        return res;
    }

    private static int totalBitsInLayer(int layers, boolean compact) {
        return ((compact ? 88 : 112) + (16 * layers)) * layers;
    }
}
