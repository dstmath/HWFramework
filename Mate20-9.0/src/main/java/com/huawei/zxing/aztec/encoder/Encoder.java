package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitArray;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.reedsolomon.GenericGF;
import com.huawei.zxing.common.reedsolomon.ReedSolomonEncoder;

public final class Encoder {
    public static final int DEFAULT_AZTEC_LAYERS = 0;
    public static final int DEFAULT_EC_PERCENT = 33;
    private static final int MAX_NB_BITS = 32;
    private static final int MAX_NB_BITS_COMPACT = 4;
    private static final int[] WORD_SIZE = {4, 6, 6, 8, 8, 8, 8, 8, 8, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12};

    private Encoder() {
    }

    public static AztecCode encode(byte[] data) {
        return encode(data, 33, 0);
    }

    public static AztecCode encode(byte[] data, int minECCPercent, int userSpecifiedLayers) {
        BitArray stuffedBits;
        int totalBitsInLayer;
        int wordSize;
        boolean compact;
        int wordSize2;
        int matrixSize;
        int totalSizeBits;
        int rowSize;
        int wordSize3;
        int totalBitsInLayer2;
        BitArray stuffedBits2;
        int totalSizeBits2;
        int eccBits;
        BitArray bits;
        boolean z;
        BitArray bits2 = new HighLevelEncoder(data).encode();
        int eccBits2 = ((bits2.getSize() * minECCPercent) / 100) + 11;
        int totalSizeBits3 = bits2.getSize() + eccBits2;
        int i = 32;
        boolean z2 = false;
        boolean z3 = true;
        if (userSpecifiedLayers != 0) {
            boolean compact2 = userSpecifiedLayers < 0;
            int layers = Math.abs(userSpecifiedLayers);
            if (compact2) {
                i = 4;
            }
            if (layers <= i) {
                int totalBitsInLayer3 = totalBitsInLayer(layers, compact2);
                int wordSize4 = WORD_SIZE[layers];
                int usableBitsInLayers = totalBitsInLayer3 - (totalBitsInLayer3 % wordSize4);
                stuffedBits = stuffBits(bits2, wordSize4);
                if (stuffedBits.getSize() + eccBits2 > usableBitsInLayers) {
                    throw new IllegalArgumentException("Data to large for user specified layer");
                } else if (!compact2 || stuffedBits.getSize() <= wordSize4 * 64) {
                    totalBitsInLayer = totalBitsInLayer3;
                    int i2 = layers;
                    compact = compact2;
                    wordSize2 = wordSize4;
                    wordSize = i2;
                } else {
                    throw new IllegalArgumentException("Data to large for user specified layer");
                }
            } else {
                throw new IllegalArgumentException(String.format("Illegal value %s for layers", new Object[]{Integer.valueOf(userSpecifiedLayers)}));
            }
        } else {
            BitArray stuffedBits3 = null;
            wordSize2 = 0;
            int i3 = 0;
            while (i3 <= i) {
                compact = i3 <= 3 ? z3 : z2;
                wordSize = compact ? i3 + 1 : i3;
                totalBitsInLayer = totalBitsInLayer(wordSize, compact);
                if (totalSizeBits3 > totalBitsInLayer) {
                    bits = bits2;
                    eccBits = eccBits2;
                    totalSizeBits2 = totalSizeBits3;
                    z = z3;
                } else {
                    if (wordSize2 != WORD_SIZE[wordSize]) {
                        wordSize2 = WORD_SIZE[wordSize];
                        stuffedBits3 = stuffBits(bits2, wordSize2);
                    }
                    stuffedBits = stuffedBits3;
                    int usableBitsInLayers2 = totalBitsInLayer - (totalBitsInLayer % wordSize2);
                    if (compact && stuffedBits.getSize() > wordSize2 * 64) {
                        bits = bits2;
                        eccBits = eccBits2;
                        totalSizeBits2 = totalSizeBits3;
                        z = z3;
                    } else if (stuffedBits.getSize() + eccBits2 > usableBitsInLayers2) {
                        bits = bits2;
                        eccBits = eccBits2;
                        totalSizeBits2 = totalSizeBits3;
                        z = z3;
                    }
                    stuffedBits3 = stuffedBits;
                }
                i3++;
                z3 = z;
                bits2 = bits;
                eccBits2 = eccBits;
                totalSizeBits3 = totalSizeBits2;
                byte[] bArr = data;
                i = 32;
                z2 = false;
            }
            int i4 = eccBits2;
            int i5 = totalSizeBits3;
            throw new IllegalArgumentException("Data too large for an Aztec code");
        }
        BitArray messageBits = generateCheckWords(stuffedBits, totalBitsInLayer, wordSize2);
        int messageSizeInWords = stuffedBits.getSize() / wordSize2;
        BitArray modeMessage = generateModeMessage(compact, wordSize, messageSizeInWords);
        int baseMatrixSize = compact ? 11 + (wordSize * 4) : 14 + (wordSize * 4);
        int[] alignmentMap = new int[baseMatrixSize];
        if (!compact) {
            matrixSize = baseMatrixSize + 1 + ((((baseMatrixSize / 2) - 1) / 15) * 2);
            int origCenter = baseMatrixSize / 2;
            int center = matrixSize / 2;
            int i6 = 0;
            while (true) {
                int i7 = i6;
                if (i7 >= origCenter) {
                    break;
                }
                alignmentMap[(origCenter - i7) - 1] = (center - i7 + (i7 / 15)) - 1;
                alignmentMap[origCenter + i7] = center + i7 + (i7 / 15) + 1;
                i6 = i7 + 1;
                byte[] bArr2 = data;
            }
        } else {
            matrixSize = baseMatrixSize;
            int i8 = 0;
            while (true) {
                BitArray bits3 = bits2;
                int i9 = i8;
                if (i9 >= alignmentMap.length) {
                    break;
                }
                alignmentMap[i9] = i9;
                i8 = i9 + 1;
                bits2 = bits3;
            }
        }
        int matrixSize2 = matrixSize;
        BitMatrix matrix = new BitMatrix(matrixSize2);
        int i10 = eccBits2;
        int i11 = 0;
        int rowOffset = 0;
        while (i11 < wordSize) {
            int rowSize2 = compact ? ((wordSize - i11) * 4) + 9 : ((wordSize - i11) * 4) + 12;
            int j = 0;
            while (true) {
                totalSizeBits = totalSizeBits3;
                rowSize = rowSize2;
                int totalSizeBits4 = j;
                if (totalSizeBits4 >= rowSize) {
                    break;
                }
                int columnOffset = totalSizeBits4 * 2;
                int k = 0;
                while (true) {
                    wordSize3 = wordSize2;
                    totalBitsInLayer2 = totalBitsInLayer;
                    int k2 = k;
                    if (k2 >= 2) {
                        break;
                    }
                    if (messageBits.get(rowOffset + columnOffset + k2)) {
                        stuffedBits2 = stuffedBits;
                        matrix.set(alignmentMap[(i11 * 2) + k2], alignmentMap[(i11 * 2) + totalSizeBits4]);
                    } else {
                        stuffedBits2 = stuffedBits;
                    }
                    if (messageBits.get(rowOffset + (rowSize * 2) + columnOffset + k2)) {
                        matrix.set(alignmentMap[(i11 * 2) + totalSizeBits4], alignmentMap[((baseMatrixSize - 1) - (i11 * 2)) - k2]);
                    }
                    if (messageBits.get(rowOffset + (rowSize * 4) + columnOffset + k2)) {
                        matrix.set(alignmentMap[((baseMatrixSize - 1) - (i11 * 2)) - k2], alignmentMap[((baseMatrixSize - 1) - (i11 * 2)) - totalSizeBits4]);
                    }
                    if (messageBits.get(rowOffset + (rowSize * 6) + columnOffset + k2)) {
                        matrix.set(alignmentMap[((baseMatrixSize - 1) - (i11 * 2)) - totalSizeBits4], alignmentMap[(i11 * 2) + k2]);
                    }
                    k = k2 + 1;
                    wordSize2 = wordSize3;
                    totalBitsInLayer = totalBitsInLayer2;
                    stuffedBits = stuffedBits2;
                }
                j = totalSizeBits4 + 1;
                rowSize2 = rowSize;
                totalSizeBits3 = totalSizeBits;
                wordSize2 = wordSize3;
                totalBitsInLayer = totalBitsInLayer2;
            }
            int i12 = totalBitsInLayer;
            BitArray bitArray = stuffedBits;
            rowOffset += rowSize * 8;
            i11++;
            totalSizeBits3 = totalSizeBits;
        }
        int i13 = wordSize2;
        int i14 = totalBitsInLayer;
        BitArray bitArray2 = stuffedBits;
        drawModeMessage(matrix, compact, matrixSize2, modeMessage);
        if (!compact) {
            drawBullsEye(matrix, matrixSize2 / 2, 7);
            int i15 = 0;
            int j2 = 0;
            while (true) {
                int j3 = j2;
                if (i15 >= (baseMatrixSize / 2) - 1) {
                    break;
                }
                for (int k3 = (matrixSize2 / 2) & 1; k3 < matrixSize2; k3 += 2) {
                    matrix.set((matrixSize2 / 2) - j3, k3);
                    matrix.set((matrixSize2 / 2) + j3, k3);
                    matrix.set(k3, (matrixSize2 / 2) - j3);
                    matrix.set(k3, (matrixSize2 / 2) + j3);
                }
                i15 += 15;
                j2 = j3 + 16;
            }
        } else {
            drawBullsEye(matrix, matrixSize2 / 2, 5);
        }
        AztecCode aztec = new AztecCode();
        aztec.setCompact(compact);
        aztec.setSize(matrixSize2);
        aztec.setLayers(wordSize);
        aztec.setCodeWords(messageSizeInWords);
        aztec.setMatrix(matrix);
        return aztec;
    }

    private static void drawBullsEye(BitMatrix matrix, int center, int size) {
        for (int i = 0; i < size; i += 2) {
            for (int j = center - i; j <= center + i; j++) {
                matrix.set(j, center - i);
                matrix.set(j, center + i);
                matrix.set(center - i, j);
                matrix.set(center + i, j);
            }
        }
        matrix.set(center - size, center - size);
        matrix.set((center - size) + 1, center - size);
        matrix.set(center - size, (center - size) + 1);
        matrix.set(center + size, center - size);
        matrix.set(center + size, (center - size) + 1);
        matrix.set(center + size, (center + size) - 1);
    }

    static BitArray generateModeMessage(boolean compact, int layers, int messageSizeInWords) {
        BitArray modeMessage = new BitArray();
        if (compact) {
            modeMessage.appendBits(layers - 1, 2);
            modeMessage.appendBits(messageSizeInWords - 1, 6);
            return generateCheckWords(modeMessage, 28, 4);
        }
        modeMessage.appendBits(layers - 1, 5);
        modeMessage.appendBits(messageSizeInWords - 1, 11);
        return generateCheckWords(modeMessage, 40, 4);
    }

    private static void drawModeMessage(BitMatrix matrix, boolean compact, int matrixSize, BitArray modeMessage) {
        int center = matrixSize / 2;
        int i = 0;
        if (compact) {
            while (i < 7) {
                int offset = (center - 3) + i;
                if (modeMessage.get(i)) {
                    matrix.set(offset, center - 5);
                }
                if (modeMessage.get(i + 7)) {
                    matrix.set(center + 5, offset);
                }
                if (modeMessage.get(20 - i)) {
                    matrix.set(offset, center + 5);
                }
                if (modeMessage.get(27 - i)) {
                    matrix.set(center - 5, offset);
                }
                i++;
            }
            return;
        }
        while (i < 10) {
            int offset2 = (center - 5) + i + (i / 5);
            if (modeMessage.get(i)) {
                matrix.set(offset2, center - 7);
            }
            if (modeMessage.get(i + 10)) {
                matrix.set(center + 7, offset2);
            }
            if (modeMessage.get(29 - i)) {
                matrix.set(offset2, center + 7);
            }
            if (modeMessage.get(39 - i)) {
                matrix.set(center - 7, offset2);
            }
            i++;
        }
    }

    private static BitArray generateCheckWords(BitArray bitArray, int totalBits, int wordSize) {
        int messageSizeInWords = bitArray.getSize() / wordSize;
        GenericGF gfTmp = getGF(wordSize);
        if (gfTmp != null) {
            ReedSolomonEncoder rs = new ReedSolomonEncoder(gfTmp);
            int totalWords = totalBits / wordSize;
            int[] messageWords = bitsToWords(bitArray, wordSize, totalWords);
            rs.encode(messageWords, totalWords - messageSizeInWords);
            BitArray messageBits = new BitArray();
            messageBits.appendBits(0, totalBits % wordSize);
            for (int messageWord : messageWords) {
                messageBits.appendBits(messageWord, wordSize);
            }
            return messageBits;
        }
        throw new IllegalArgumentException("the wrong wordSize.");
    }

    private static int[] bitsToWords(BitArray stuffedBits, int wordSize, int totalWords) {
        int[] message = new int[totalWords];
        int n = stuffedBits.getSize() / wordSize;
        for (int i = 0; i < n; i++) {
            int value = 0;
            for (int j = 0; j < wordSize; j++) {
                value |= stuffedBits.get((i * wordSize) + j) ? 1 << ((wordSize - j) - 1) : 0;
            }
            message[i] = value;
        }
        return message;
    }

    private static GenericGF getGF(int wordSize) {
        if (wordSize == 4) {
            return GenericGF.AZTEC_PARAM;
        }
        if (wordSize == 6) {
            return GenericGF.AZTEC_DATA_6;
        }
        if (wordSize == 8) {
            return GenericGF.AZTEC_DATA_8;
        }
        if (wordSize == 10) {
            return GenericGF.AZTEC_DATA_10;
        }
        if (wordSize != 12) {
            return null;
        }
        return GenericGF.AZTEC_DATA_12;
    }

    static BitArray stuffBits(BitArray bits, int wordSize) {
        BitArray out = new BitArray();
        int n = bits.getSize();
        int mask = (1 << wordSize) - 2;
        int i = 0;
        while (i < n) {
            int word = 0;
            for (int j = 0; j < wordSize; j++) {
                if (i + j >= n || bits.get(i + j)) {
                    word |= 1 << ((wordSize - 1) - j);
                }
            }
            if ((word & mask) == mask) {
                out.appendBits(word & mask, wordSize);
                i--;
            } else if ((word & mask) == 0) {
                out.appendBits(word | 1, wordSize);
                i--;
            } else {
                out.appendBits(word, wordSize);
            }
            i += wordSize;
        }
        return out;
    }

    private static int totalBitsInLayer(int layers, boolean compact) {
        return ((compact ? 88 : 112) + (16 * layers)) * layers;
    }
}
