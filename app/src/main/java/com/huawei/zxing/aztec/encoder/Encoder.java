package com.huawei.zxing.aztec.encoder;

import com.huawei.lcagent.client.MetricConstant;
import com.huawei.telephony.HuaweiTelephonyManager;
import com.huawei.zxing.common.BitArray;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.reedsolomon.GenericGF;
import com.huawei.zxing.common.reedsolomon.ReedSolomonEncoder;
import huawei.android.widget.DialogContentHelper.Dex;
import huawei.android.widget.ViewDragHelper;

public final class Encoder {
    public static final int DEFAULT_AZTEC_LAYERS = 0;
    public static final int DEFAULT_EC_PERCENT = 33;
    private static final int MAX_NB_BITS = 32;
    private static final int MAX_NB_BITS_COMPACT = 4;
    private static final int[] WORD_SIZE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.aztec.encoder.Encoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.aztec.encoder.Encoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.aztec.encoder.Encoder.<clinit>():void");
    }

    private Encoder() {
    }

    public static AztecCode encode(byte[] data) {
        return encode(data, DEFAULT_EC_PERCENT, DEFAULT_AZTEC_LAYERS);
    }

    public static AztecCode encode(byte[] data, int minECCPercent, int userSpecifiedLayers) {
        boolean z;
        int layers;
        int totalBitsInLayer;
        int wordSize;
        BitArray stuffBits;
        int matrixSize;
        BitArray bits = new HighLevelEncoder(data).encode();
        int eccBits = ((bits.getSize() * minECCPercent) / 100) + 11;
        int totalSizeBits = bits.getSize() + eccBits;
        if (userSpecifiedLayers != 0) {
            z = userSpecifiedLayers < 0;
            layers = Math.abs(userSpecifiedLayers);
            if (layers > (z ? MAX_NB_BITS_COMPACT : MAX_NB_BITS)) {
                throw new IllegalArgumentException(String.format("Illegal value %s for layers", new Object[]{Integer.valueOf(userSpecifiedLayers)}));
            }
            totalBitsInLayer = totalBitsInLayer(layers, z);
            wordSize = WORD_SIZE[layers];
            int usableBitsInLayers = totalBitsInLayer - (totalBitsInLayer % wordSize);
            stuffBits = stuffBits(bits, wordSize);
            if (stuffBits.getSize() + eccBits > usableBitsInLayers) {
                throw new IllegalArgumentException("Data to large for user specified layer");
            } else if (z && stuffBits.getSize() > wordSize * 64) {
                throw new IllegalArgumentException("Data to large for user specified layer");
            }
        }
        wordSize = DEFAULT_AZTEC_LAYERS;
        stuffBits = null;
        int i = DEFAULT_AZTEC_LAYERS;
        while (i <= MAX_NB_BITS) {
            z = i <= 3;
            if (z) {
                layers = i + 1;
            } else {
                layers = i;
            }
            totalBitsInLayer = totalBitsInLayer(layers, z);
            if (totalSizeBits <= totalBitsInLayer) {
                if (wordSize != WORD_SIZE[layers]) {
                    wordSize = WORD_SIZE[layers];
                    stuffBits = stuffBits(bits, wordSize);
                }
                usableBitsInLayers = totalBitsInLayer - (totalBitsInLayer % wordSize);
                if ((!z || r24.getSize() <= wordSize * 64) && r24.getSize() + eccBits <= usableBitsInLayers) {
                }
            }
            i++;
        }
        throw new IllegalArgumentException("Data too large for an Aztec code");
        BitArray messageBits = generateCheckWords(stuffBits, totalBitsInLayer, wordSize);
        int messageSizeInWords = stuffBits.getSize() / wordSize;
        BitArray modeMessage = generateModeMessage(z, layers, messageSizeInWords);
        int baseMatrixSize = z ? (layers * MAX_NB_BITS_COMPACT) + 11 : (layers * MAX_NB_BITS_COMPACT) + 14;
        int[] alignmentMap = new int[baseMatrixSize];
        if (z) {
            matrixSize = baseMatrixSize;
            i = DEFAULT_AZTEC_LAYERS;
            while (true) {
                int length = alignmentMap.length;
                if (i >= r0) {
                    break;
                }
                alignmentMap[i] = i;
                i++;
            }
        } else {
            matrixSize = (baseMatrixSize + 1) + ((((baseMatrixSize / 2) - 1) / 15) * 2);
            int origCenter = baseMatrixSize / 2;
            int center = matrixSize / 2;
            for (i = DEFAULT_AZTEC_LAYERS; i < origCenter; i++) {
                int newOffset = i + (i / 15);
                alignmentMap[(origCenter - i) - 1] = (center - newOffset) - 1;
                alignmentMap[origCenter + i] = (center + newOffset) + 1;
            }
        }
        BitMatrix matrix = new BitMatrix(matrixSize);
        int rowOffset = DEFAULT_AZTEC_LAYERS;
        for (i = DEFAULT_AZTEC_LAYERS; i < layers; i++) {
            int j;
            int rowSize = z ? ((layers - i) * MAX_NB_BITS_COMPACT) + 9 : ((layers - i) * MAX_NB_BITS_COMPACT) + 12;
            for (j = DEFAULT_AZTEC_LAYERS; j < rowSize; j++) {
                int k;
                int columnOffset = j * 2;
                for (k = DEFAULT_AZTEC_LAYERS; k < 2; k++) {
                    if (messageBits.get((rowOffset + columnOffset) + k)) {
                        matrix.set(alignmentMap[(i * 2) + k], alignmentMap[(i * 2) + j]);
                    }
                    if (messageBits.get((((rowSize * 2) + rowOffset) + columnOffset) + k)) {
                        matrix.set(alignmentMap[(i * 2) + j], alignmentMap[((baseMatrixSize - 1) - (i * 2)) - k]);
                    }
                    if (messageBits.get((((rowSize * MAX_NB_BITS_COMPACT) + rowOffset) + columnOffset) + k)) {
                        matrix.set(alignmentMap[((baseMatrixSize - 1) - (i * 2)) - k], alignmentMap[((baseMatrixSize - 1) - (i * 2)) - j]);
                    }
                    if (messageBits.get((((rowSize * 6) + rowOffset) + columnOffset) + k)) {
                        matrix.set(alignmentMap[((baseMatrixSize - 1) - (i * 2)) - j], alignmentMap[(i * 2) + k]);
                    }
                }
            }
            rowOffset += rowSize * 8;
        }
        drawModeMessage(matrix, z, matrixSize, modeMessage);
        if (z) {
            drawBullsEye(matrix, matrixSize / 2, 5);
        } else {
            drawBullsEye(matrix, matrixSize / 2, 7);
            i = DEFAULT_AZTEC_LAYERS;
            j = DEFAULT_AZTEC_LAYERS;
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
        aztec.setCompact(z);
        aztec.setSize(matrixSize);
        aztec.setLayers(layers);
        aztec.setCodeWords(messageSizeInWords);
        aztec.setMatrix(matrix);
        return aztec;
    }

    private static void drawBullsEye(BitMatrix matrix, int center, int size) {
        for (int i = DEFAULT_AZTEC_LAYERS; i < size; i += 2) {
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
            return generateCheckWords(modeMessage, 28, MAX_NB_BITS_COMPACT);
        }
        modeMessage.appendBits(layers - 1, 5);
        modeMessage.appendBits(messageSizeInWords - 1, 11);
        return generateCheckWords(modeMessage, 40, MAX_NB_BITS_COMPACT);
    }

    private static void drawModeMessage(BitMatrix matrix, boolean compact, int matrixSize, BitArray modeMessage) {
        int center = matrixSize / 2;
        int i;
        int offset;
        if (compact) {
            for (i = DEFAULT_AZTEC_LAYERS; i < 7; i++) {
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
        for (i = DEFAULT_AZTEC_LAYERS; i < 10; i++) {
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
        int i = DEFAULT_AZTEC_LAYERS;
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
        messageBits.appendBits(DEFAULT_AZTEC_LAYERS, startPad);
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
        for (int i = DEFAULT_AZTEC_LAYERS; i < n; i++) {
            int value = DEFAULT_AZTEC_LAYERS;
            for (int j = DEFAULT_AZTEC_LAYERS; j < wordSize; j++) {
                value |= stuffedBits.get((i * wordSize) + j) ? 1 << ((wordSize - j) - 1) : DEFAULT_AZTEC_LAYERS;
            }
            message[i] = value;
        }
        return message;
    }

    private static GenericGF getGF(int wordSize) {
        switch (wordSize) {
            case MAX_NB_BITS_COMPACT /*4*/:
                return GenericGF.AZTEC_PARAM;
            case Dex.DIALOG_BODY_View /*6*/:
                return GenericGF.AZTEC_DATA_6;
            case ViewDragHelper.EDGE_BOTTOM /*8*/:
                return GenericGF.AZTEC_DATA_8;
            case HuaweiTelephonyManager.SINGLE_MODE_SIM_CARD /*10*/:
                return GenericGF.AZTEC_DATA_10;
            case MetricConstant.LOG_TRACK_METRIC_ID /*12*/:
                return GenericGF.AZTEC_DATA_12;
            default:
                return null;
        }
    }

    static BitArray stuffBits(BitArray bits, int wordSize) {
        BitArray out = new BitArray();
        int n = bits.getSize();
        int mask = (1 << wordSize) - 2;
        int i = DEFAULT_AZTEC_LAYERS;
        while (i < n) {
            int word = DEFAULT_AZTEC_LAYERS;
            int j = DEFAULT_AZTEC_LAYERS;
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
