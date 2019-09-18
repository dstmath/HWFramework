package com.huawei.zxing.datamatrix.decoder;

import com.huawei.zxing.datamatrix.decoder.Version;

final class DataBlock {
    private final byte[] codewords;
    private final int numDataCodewords;

    private DataBlock(int numDataCodewords2, byte[] codewords2) {
        this.numDataCodewords = numDataCodewords2;
        this.codewords = codewords2;
    }

    static DataBlock[] getDataBlocks(byte[] rawCodewords, Version version) {
        byte[] bArr = rawCodewords;
        Version.ECBlocks ecBlocks = version.getECBlocks();
        Version.ECB[] ecBlockArray = ecBlocks.getECBlocks();
        int i = 0;
        int totalBlocks = 0;
        for (Version.ECB ecBlock : ecBlockArray) {
            totalBlocks += ecBlock.getCount();
        }
        DataBlock[] result = new DataBlock[totalBlocks];
        int length = ecBlockArray.length;
        int numResultBlocks = 0;
        int numResultBlocks2 = 0;
        while (numResultBlocks2 < length) {
            Version.ECB ecBlock2 = ecBlockArray[numResultBlocks2];
            int numResultBlocks3 = numResultBlocks;
            int i2 = 0;
            while (i2 < ecBlock2.getCount()) {
                int numDataCodewords2 = ecBlock2.getDataCodewords();
                result[numResultBlocks3] = new DataBlock(numDataCodewords2, new byte[(ecBlocks.getECCodewords() + numDataCodewords2)]);
                i2++;
                numResultBlocks3++;
            }
            numResultBlocks2++;
            numResultBlocks = numResultBlocks3;
        }
        int longerBlocksNumDataCodewords = result[0].codewords.length - ecBlocks.getECCodewords();
        int shorterBlocksNumDataCodewords = longerBlocksNumDataCodewords - 1;
        int rawCodewordsOffset = 0;
        int i3 = 0;
        while (i3 < shorterBlocksNumDataCodewords) {
            int rawCodewordsOffset2 = rawCodewordsOffset;
            int j = 0;
            while (j < numResultBlocks) {
                result[j].codewords[i3] = bArr[rawCodewordsOffset2];
                j++;
                rawCodewordsOffset2++;
            }
            i3++;
            rawCodewordsOffset = rawCodewordsOffset2;
        }
        boolean specialVersion = version.getVersionNumber() == 24;
        int numLongerBlocks = specialVersion ? 8 : numResultBlocks;
        int rawCodewordsOffset3 = rawCodewordsOffset;
        int j2 = 0;
        while (j2 < numLongerBlocks) {
            result[j2].codewords[longerBlocksNumDataCodewords - 1] = bArr[rawCodewordsOffset3];
            j2++;
            rawCodewordsOffset3++;
        }
        int max = result[0].codewords.length;
        int rawCodewordsOffset4 = rawCodewordsOffset3;
        int i4 = longerBlocksNumDataCodewords;
        while (i4 < max) {
            int rawCodewordsOffset5 = rawCodewordsOffset4;
            int j3 = i;
            while (j3 < numResultBlocks) {
                result[j3].codewords[(!specialVersion || j3 <= 7) ? i4 : i4 - 1] = bArr[rawCodewordsOffset5];
                j3++;
                rawCodewordsOffset5++;
                ecBlocks = ecBlocks;
            }
            i4++;
            rawCodewordsOffset4 = rawCodewordsOffset5;
            i = 0;
        }
        if (rawCodewordsOffset4 == bArr.length) {
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
