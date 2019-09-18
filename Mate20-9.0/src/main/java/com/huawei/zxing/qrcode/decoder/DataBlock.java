package com.huawei.zxing.qrcode.decoder;

import com.huawei.zxing.qrcode.decoder.Version;

final class DataBlock {
    private final byte[] codewords;
    private final int numDataCodewords;

    private DataBlock(int numDataCodewords2, byte[] codewords2) {
        this.numDataCodewords = numDataCodewords2;
        this.codewords = codewords2;
    }

    static DataBlock[] getDataBlocks(byte[] rawCodewords, Version version, ErrorCorrectionLevel ecLevel) {
        byte[] bArr = rawCodewords;
        if (bArr.length == version.getTotalCodewords()) {
            Version.ECBlocks ecBlocks = version.getECBlocksForLevel(ecLevel);
            Version.ECB[] ecBlockArray = ecBlocks.getECBlocks();
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
                int i = 0;
                while (i < ecBlock2.getCount()) {
                    int numDataCodewords2 = ecBlock2.getDataCodewords();
                    result[numResultBlocks3] = new DataBlock(numDataCodewords2, new byte[(ecBlocks.getECCodewordsPerBlock() + numDataCodewords2)]);
                    i++;
                    numResultBlocks3++;
                }
                numResultBlocks2++;
                numResultBlocks = numResultBlocks3;
            }
            int shorterBlocksTotalCodewords = result[0].codewords.length;
            int longerBlocksStartAt = result.length - 1;
            while (longerBlocksStartAt >= 0 && result[longerBlocksStartAt].codewords.length != shorterBlocksTotalCodewords) {
                longerBlocksStartAt--;
            }
            int longerBlocksStartAt2 = longerBlocksStartAt + 1;
            int shorterBlocksNumDataCodewords = shorterBlocksTotalCodewords - ecBlocks.getECCodewordsPerBlock();
            int rawCodewordsOffset = 0;
            int i2 = 0;
            while (i2 < shorterBlocksNumDataCodewords) {
                int rawCodewordsOffset2 = rawCodewordsOffset;
                int j = 0;
                while (j < numResultBlocks) {
                    result[j].codewords[i2] = bArr[rawCodewordsOffset2];
                    j++;
                    rawCodewordsOffset2++;
                }
                i2++;
                rawCodewordsOffset = rawCodewordsOffset2;
            }
            int j2 = longerBlocksStartAt2;
            while (j2 < numResultBlocks) {
                result[j2].codewords[shorterBlocksNumDataCodewords] = bArr[rawCodewordsOffset];
                j2++;
                rawCodewordsOffset++;
            }
            int max = result[0].codewords.length;
            int rawCodewordsOffset3 = rawCodewordsOffset;
            int i3 = shorterBlocksNumDataCodewords;
            while (i3 < max) {
                int rawCodewordsOffset4 = rawCodewordsOffset3;
                int j3 = 0;
                while (j3 < numResultBlocks) {
                    result[j3].codewords[j3 < longerBlocksStartAt2 ? i3 : i3 + 1] = bArr[rawCodewordsOffset4];
                    j3++;
                    rawCodewordsOffset4++;
                }
                i3++;
                rawCodewordsOffset3 = rawCodewordsOffset4;
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
