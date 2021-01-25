package com.huawei.zxing.datamatrix.decoder;

import com.huawei.zxing.datamatrix.decoder.Version;

/* access modifiers changed from: package-private */
public final class DataBlock {
    private final byte[] codewords;
    private final int numDataCodewords;

    private DataBlock(int numDataCodewords2, byte[] codewords2) {
        this.numDataCodewords = numDataCodewords2;
        this.codewords = codewords2;
    }

    static DataBlock[] getDataBlocks(byte[] rawCodewords, Version version) {
        Version.ECBlocks ecBlocks = version.getECBlocks();
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
                result[numResultBlocks] = new DataBlock(numDataCodewords2, new byte[(ecBlocks.getECCodewords() + numDataCodewords2)]);
                i++;
                numResultBlocks++;
            }
        }
        int longerBlocksNumDataCodewords = result[0].codewords.length - ecBlocks.getECCodewords();
        int shorterBlocksNumDataCodewords = longerBlocksNumDataCodewords - 1;
        int rawCodewordsOffset = 0;
        for (int i2 = 0; i2 < shorterBlocksNumDataCodewords; i2++) {
            int j = 0;
            while (j < numResultBlocks) {
                result[j].codewords[i2] = rawCodewords[rawCodewordsOffset];
                j++;
                rawCodewordsOffset++;
            }
        }
        boolean specialVersion = version.getVersionNumber() == 24;
        int numLongerBlocks = specialVersion ? 8 : numResultBlocks;
        int j2 = 0;
        while (j2 < numLongerBlocks) {
            result[j2].codewords[longerBlocksNumDataCodewords - 1] = rawCodewords[rawCodewordsOffset];
            j2++;
            rawCodewordsOffset++;
        }
        int max = result[0].codewords.length;
        for (int i3 = longerBlocksNumDataCodewords; i3 < max; i3++) {
            int j3 = 0;
            while (j3 < numResultBlocks) {
                result[j3].codewords[(!specialVersion || j3 <= 7) ? i3 : i3 - 1] = rawCodewords[rawCodewordsOffset];
                j3++;
                ecBlocks = ecBlocks;
                rawCodewordsOffset++;
            }
        }
        if (rawCodewordsOffset == rawCodewords.length) {
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
