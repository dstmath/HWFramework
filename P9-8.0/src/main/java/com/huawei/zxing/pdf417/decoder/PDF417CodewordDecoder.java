package com.huawei.zxing.pdf417.decoder;

import com.huawei.zxing.pdf417.PDF417Common;
import java.lang.reflect.Array;

final class PDF417CodewordDecoder {
    private static final float[][] RATIOS_TABLE = ((float[][]) Array.newInstance(Float.TYPE, new int[]{PDF417Common.SYMBOL_TABLE.length, 8}));

    static {
        for (int i = 0; i < PDF417Common.SYMBOL_TABLE.length; i++) {
            int currentSymbol = PDF417Common.SYMBOL_TABLE[i];
            int currentBit = currentSymbol & 1;
            for (int j = 0; j < 8; j++) {
                float size = 0.0f;
                while ((currentSymbol & 1) == currentBit) {
                    size += 1.0f;
                    currentSymbol >>= 1;
                }
                currentBit = currentSymbol & 1;
                RATIOS_TABLE[i][(8 - j) - 1] = size / 17.0f;
            }
        }
    }

    private PDF417CodewordDecoder() {
    }

    static int getDecodedValue(int[] moduleBitCount) {
        int decodedValue = getDecodedCodewordValue(sampleBitCounts(moduleBitCount));
        if (decodedValue != -1) {
            return decodedValue;
        }
        return getClosestDecodedValue(moduleBitCount);
    }

    private static int[] sampleBitCounts(int[] moduleBitCount) {
        float bitCountSum = (float) PDF417Common.getBitCountSum(moduleBitCount);
        int[] result = new int[8];
        int bitCountIndex = 0;
        int sumPreviousBits = 0;
        for (int i = 0; i < 17; i++) {
            if (((float) (moduleBitCount[bitCountIndex] + sumPreviousBits)) <= (bitCountSum / 34.0f) + ((((float) i) * bitCountSum) / 17.0f)) {
                sumPreviousBits += moduleBitCount[bitCountIndex];
                bitCountIndex++;
            }
            result[bitCountIndex] = result[bitCountIndex] + 1;
        }
        return result;
    }

    private static int getDecodedCodewordValue(int[] moduleBitCount) {
        int decodedValue = getBitValue(moduleBitCount);
        return PDF417Common.getCodeword((long) decodedValue) == -1 ? -1 : decodedValue;
    }

    private static int getBitValue(int[] moduleBitCount) {
        long result = 0;
        for (int i = 0; i < moduleBitCount.length; i++) {
            for (int bit = 0; bit < moduleBitCount[i]; bit++) {
                int i2;
                long j = result << 1;
                if (i % 2 == 0) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                result = j | ((long) i2);
            }
        }
        return (int) result;
    }

    private static int getClosestDecodedValue(int[] moduleBitCount) {
        int bitCountSum = PDF417Common.getBitCountSum(moduleBitCount);
        float[] bitCountRatios = new float[8];
        for (int i = 0; i < bitCountRatios.length; i++) {
            bitCountRatios[i] = ((float) moduleBitCount[i]) / ((float) bitCountSum);
        }
        float bestMatchError = Float.MAX_VALUE;
        int bestMatch = -1;
        for (int j = 0; j < RATIOS_TABLE.length; j++) {
            float error = 0.0f;
            float[] ratioTableRow = RATIOS_TABLE[j];
            for (int k = 0; k < 8; k++) {
                float diff = ratioTableRow[k] - bitCountRatios[k];
                error += diff * diff;
                if (error >= bestMatchError) {
                    break;
                }
            }
            if (error < bestMatchError) {
                bestMatchError = error;
                bestMatch = PDF417Common.SYMBOL_TABLE[j];
            }
        }
        return bestMatch;
    }
}
