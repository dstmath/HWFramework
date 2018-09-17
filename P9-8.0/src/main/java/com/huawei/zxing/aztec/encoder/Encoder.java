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
    private static final int[] WORD_SIZE = new int[]{4, 6, 6, 8, 8, 8, 8, 8, 8, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12};

    private Encoder() {
    }

    public static AztecCode encode(byte[] data) {
        return encode(data, 33, 0);
    }

    public static AztecCode encode(byte[] data, int minECCPercent, int userSpecifiedLayers) {
        boolean compact;
        int layers;
        int totalBitsInLayer;
        int wordSize;
        BitArray stuffedBits;
        int i;
        int matrixSize;
        int j;
        int k;
        BitArray bits = new HighLevelEncoder(data).encode();
        int eccBits = ((bits.getSize() * minECCPercent) / 100) + 11;
        int totalSizeBits = bits.getSize() + eccBits;
        int usableBitsInLayers;
        if (userSpecifiedLayers != 0) {
            compact = userSpecifiedLayers < 0;
            layers = Math.abs(userSpecifiedLayers);
            if (layers > (compact ? 4 : 32)) {
                throw new IllegalArgumentException(String.format("Illegal value %s for layers", new Object[]{Integer.valueOf(userSpecifiedLayers)}));
            }
            totalBitsInLayer = totalBitsInLayer(layers, compact);
            wordSize = WORD_SIZE[layers];
            usableBitsInLayers = totalBitsInLayer - (totalBitsInLayer % wordSize);
            stuffedBits = stuffBits(bits, wordSize);
            if (stuffedBits.getSize() + eccBits > usableBitsInLayers) {
                throw new IllegalArgumentException("Data to large for user specified layer");
            } else if (compact && stuffedBits.getSize() > wordSize * 64) {
                throw new IllegalArgumentException("Data to large for user specified layer");
            }
        }
        wordSize = 0;
        stuffedBits = null;
        i = 0;
        while (i <= 32) {
            compact = i <= 3;
            layers = compact ? i + 1 : i;
            totalBitsInLayer = totalBitsInLayer(layers, compact);
            if (totalSizeBits <= totalBitsInLayer) {
                if (wordSize != WORD_SIZE[layers]) {
                    wordSize = WORD_SIZE[layers];
                    stuffedBits = stuffBits(bits, wordSize);
                }
                usableBitsInLayers = totalBitsInLayer - (totalBitsInLayer % wordSize);
                if ((!compact || stuffedBits.getSize() <= wordSize * 64) && stuffedBits.getSize() + eccBits <= usableBitsInLayers) {
                }
            }
            i++;
        }
        throw new IllegalArgumentException("Data too large for an Aztec code");
        BitArray messageBits = generateCheckWords(stuffedBits, totalBitsInLayer, wordSize);
        int messageSizeInWords = stuffedBits.getSize() / wordSize;
        BitArray modeMessage = generateModeMessage(compact, layers, messageSizeInWords);
        int baseMatrixSize = compact ? (layers * 4) + 11 : (layers * 4) + 14;
        int[] alignmentMap = new int[baseMatrixSize];
        if (compact) {
            matrixSize = baseMatrixSize;
            for (i = 0; i < alignmentMap.length; i++) {
                alignmentMap[i] = i;
            }
        } else {
            matrixSize = (baseMatrixSize + 1) + ((((baseMatrixSize / 2) - 1) / 15) * 2);
            int origCenter = baseMatrixSize / 2;
            int center = matrixSize / 2;
            for (i = 0; i < origCenter; i++) {
                int newOffset = i + (i / 15);
                alignmentMap[(origCenter - i) - 1] = (center - newOffset) - 1;
                alignmentMap[origCenter + i] = (center + newOffset) + 1;
            }
        }
        BitMatrix matrix = new BitMatrix(matrixSize);
        int rowOffset = 0;
        for (i = 0; i < layers; i++) {
            int rowSize = compact ? ((layers - i) * 4) + 9 : ((layers - i) * 4) + 12;
            for (j = 0; j < rowSize; j++) {
                int columnOffset = j * 2;
                for (k = 0; k < 2; k++) {
                    if (messageBits.get((rowOffset + columnOffset) + k)) {
                        matrix.set(alignmentMap[(i * 2) + k], alignmentMap[(i * 2) + j]);
                    }
                    if (messageBits.get((((rowSize * 2) + rowOffset) + columnOffset) + k)) {
                        matrix.set(alignmentMap[(i * 2) + j], alignmentMap[((baseMatrixSize - 1) - (i * 2)) - k]);
                    }
                    if (messageBits.get((((rowSize * 4) + rowOffset) + columnOffset) + k)) {
                        matrix.set(alignmentMap[((baseMatrixSize - 1) - (i * 2)) - k], alignmentMap[((baseMatrixSize - 1) - (i * 2)) - j]);
                    }
                    if (messageBits.get((((rowSize * 6) + rowOffset) + columnOffset) + k)) {
                        matrix.set(alignmentMap[((baseMatrixSize - 1) - (i * 2)) - j], alignmentMap[(i * 2) + k]);
                    }
                }
            }
            rowOffset += rowSize * 8;
        }
        drawModeMessage(matrix, compact, matrixSize, modeMessage);
        if (compact) {
            drawBullsEye(matrix, matrixSize / 2, 5);
        } else {
            drawBullsEye(matrix, matrixSize / 2, 7);
            i = 0;
            j = 0;
            while (i < (baseMatrixSize / 2) - 1) {
                for (k = (matrixSize / 2) & 1; k < matrixSize; k += 2) {
                    matrix.set((matrixSize / 2) - j, k);
                    matrix.set((matrixSize / 2) + j, k);
                    matrix.set(k, (matrixSize / 2) - j);
                    matrix.set(k, (matrixSize / 2) + j);
                }
                i += 15;
                j += 16;
            }
        }
        AztecCode aztec = new AztecCode();
        aztec.setCompact(compact);
        aztec.setSize(matrixSize);
        aztec.setLayers(layers);
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
        int i;
        int offset;
        if (compact) {
            for (i = 0; i < 7; i++) {
                offset = (center - 3) + i;
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
            }
            return;
        }
        for (i = 0; i < 10; i++) {
            offset = ((center - 5) + i) + (i / 5);
            if (modeMessage.get(i)) {
                matrix.set(offset, center - 7);
            }
            if (modeMessage.get(i + 10)) {
                matrix.set(center + 7, offset);
            }
            if (modeMessage.get(29 - i)) {
                matrix.set(offset, center + 7);
            }
            if (modeMessage.get(39 - i)) {
                matrix.set(center - 7, offset);
            }
        }
    }

    private static BitArray generateCheckWords(BitArray bitArray, int totalBits, int wordSize) {
        int i = 0;
        int messageSizeInWords = bitArray.getSize() / wordSize;
        GenericGF gfTmp = getGF(wordSize);
        if (gfTmp == null) {
            throw new IllegalArgumentException("the wrong wordSize.");
        }
        ReedSolomonEncoder rs = new ReedSolomonEncoder(gfTmp);
        int totalWords = totalBits / wordSize;
        int[] messageWords = bitsToWords(bitArray, wordSize, totalWords);
        rs.encode(messageWords, totalWords - messageSizeInWords);
        int startPad = totalBits % wordSize;
        BitArray messageBits = new BitArray();
        messageBits.appendBits(0, startPad);
        int length = messageWords.length;
        while (i < length) {
            messageBits.appendBits(messageWords[i], wordSize);
            i++;
        }
        return messageBits;
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
        switch (wordSize) {
            case 4:
                return GenericGF.AZTEC_PARAM;
            case 6:
                return GenericGF.AZTEC_DATA_6;
            case 8:
                return GenericGF.AZTEC_DATA_8;
            case 10:
                return GenericGF.AZTEC_DATA_10;
            case 12:
                return GenericGF.AZTEC_DATA_12;
            default:
                return null;
        }
    }

    static BitArray stuffBits(BitArray bits, int wordSize) {
        BitArray out = new BitArray();
        int n = bits.getSize();
        int mask = (1 << wordSize) - 2;
        int i = 0;
        while (i < n) {
            int word = 0;
            int j = 0;
            while (j < wordSize) {
                if (i + j >= n || bits.get(i + j)) {
                    word |= 1 << ((wordSize - 1) - j);
                }
                j++;
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
        return ((compact ? 88 : 112) + (layers * 16)) * layers;
    }
}
