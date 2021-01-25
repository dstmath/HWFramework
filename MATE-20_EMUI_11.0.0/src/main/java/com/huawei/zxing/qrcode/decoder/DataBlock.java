package com.huawei.zxing.qrcode.decoder;

import com.huawei.zxing.qrcode.decoder.Version;

/* access modifiers changed from: package-private */
public final class DataBlock {
    private final byte[] codewords;
    private final int numDataCodewords;

    private DataBlock(int numDataCodewords2, byte[] codewords2) {
        this.numDataCodewords = numDataCodewords2;
        this.codewords = codewords2;
    }

    static DataBlock[] getDataBlocks(byte[] rawCodewords, Version version, ErrorCorrectionLevel ecLevel) {
        if (rawCodewords.length == version.getTotalCodewords()) {
            Version.ECBlocks ecBlocks = version.getECBlocksForLevel(ecLevel);
            Version.ECB[] ecBlockArray = ecBlocks.getECBlocks();
            int totalBlocks = 0;
            for (Version.ECB ecBlock : ecBlockArray) {
                totalBlocks += ecBlock.getCount();
            }
            DataBlock[] result = new DataBlock[totalBlocks];
            int numResultBlocks = 0;
            for (Version.ECB ecBlock2 : ecBlockArray) {
                int i = 0;
                while (i < ecBlock2.getCount()) {
                    int numDataCodewords2 = ecBlock2.getDataCodewords();
                    result[numResultBlocks] = new DataBlock(numDataCodewords2, new byte[(ecBlocks.getECCodewordsPerBlock() + numDataCodewords2)]);
                    i++;
                    numResultBlocks++;
                }
            }
            int shorterBlocksTotalCodewords = result[0].codewords.length;
            int longerBlocksStartAt = result.length - 1;
            while (longerBlocksStartAt >= 0 && result[longerBlocksStartAt].codewords.length != shorterBlocksTotalCodewords) {
                longerBlocksStartAt--;
            }
            int longerBlocksStartAt2 = longerBlocksStartAt + 1;
            int shorterBlocksNumDataCodewords = shorterBlocksTotalCodewords - ecBlocks.getECCodewordsPerBlock();
            int rawCodewordsOffset = 0;
            for (int i2 = 0; i2 < shorterBlocksNumDataCodewords; i2++) {
                int j = 0;
                while (j < numResultBlocks) {
                    result[j].codewords[i2] = rawCodewords[rawCodewordsOffset];
                    j++;
                    rawCodewordsOffset++;
                }
            }
            int j2 = longerBlocksStartAt2;
            while (j2 < numResultBlocks) {
                result[j2].codewords[shorterBlocksNumDataCodewords] = rawCodewords[rawCodewordsOffset];
                j2++;
                rawCodewordsOffset++;
            }
            int max = result[0].codewords.length;
            for (int i3 = shorterBlocksNumDataCodewords; i3 < max; i3++) {
                int j3 = 0;
                while (j3 < numResultBlocks) {
                    result[j3].codewords[j3 < longerBlocksStartAt2 ? i3 : i3 + 1] = rawCodewords[rawCodewordsOffset];
                    j3++;
                    rawCodewordsOffset++;
                }
            }
            return result;
        }
        throw new IllegalArgumentException();
    }

    /* access modifiers changed from: package-private */
    public int getNumDataCodewords() {
        return this.numDataCodewords;
    }

    /* access modifiers changed from: package-private */
    public byte[] getCodewords() {
        return this.codewords;
    }
}
