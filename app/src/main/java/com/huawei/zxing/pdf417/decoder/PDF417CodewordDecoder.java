package com.huawei.zxing.pdf417.decoder;

import com.huawei.zxing.pdf417.PDF417Common;

final class PDF417CodewordDecoder {
    private static final float[][] RATIOS_TABLE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.pdf417.decoder.PDF417CodewordDecoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.pdf417.decoder.PDF417CodewordDecoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.pdf417.decoder.PDF417CodewordDecoder.<clinit>():void");
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
