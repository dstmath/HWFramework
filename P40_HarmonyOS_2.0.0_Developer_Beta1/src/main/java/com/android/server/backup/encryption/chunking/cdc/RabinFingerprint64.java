package com.android.server.backup.encryption.chunking.cdc;

public class RabinFingerprint64 {
    private static final long DEFAULT_IRREDUCIBLE_POLYNOMIAL_64 = 27;
    private static final int POLYNOMIAL_DEGREE = 64;
    private static final int SLIDING_WINDOW_SIZE_BYTES = 31;
    private final long mPoly64;
    private final long[] mTableFP64;
    private final long[] mTableOutByte;

    public RabinFingerprint64(long poly64) {
        this.mTableFP64 = new long[256];
        this.mTableOutByte = new long[256];
        this.mPoly64 = poly64;
    }

    public RabinFingerprint64() {
        this(DEFAULT_IRREDUCIBLE_POLYNOMIAL_64);
        computeFingerprintTables64();
        computeFingerprintTables64Windowed();
    }

    public long computeFingerprint64(byte inChar, byte outChar, long fingerPrint) {
        return (((fingerPrint << 8) ^ ((long) (inChar & 255))) ^ this.mTableFP64[(int) (fingerPrint >>> 56)]) ^ this.mTableOutByte[outChar & 255];
    }

    private void computeFingerprintTables64() {
        long[] degreesRes64 = new long[64];
        degreesRes64[0] = this.mPoly64;
        for (int i = 1; i < 64; i++) {
            if ((degreesRes64[i - 1] & Long.MIN_VALUE) == 0) {
                degreesRes64[i] = degreesRes64[i - 1] << 1;
            } else {
                degreesRes64[i] = (degreesRes64[i - 1] << 1) ^ this.mPoly64;
            }
        }
        for (int i2 = 0; i2 < 256; i2++) {
            int currIndex = i2;
            int j = 0;
            while (currIndex > 0 && j < 8) {
                if ((currIndex & 1) == 1) {
                    long[] jArr = this.mTableFP64;
                    jArr[i2] = jArr[i2] ^ degreesRes64[j];
                }
                currIndex >>>= 1;
                j++;
            }
        }
    }

    private void computeFingerprintTables64Windowed() {
        long[] degsRes64 = new long[8];
        degsRes64[0] = this.mPoly64;
        for (int i = 65; i < 256; i++) {
            if ((degsRes64[(i - 1) % 8] & Long.MIN_VALUE) == 0) {
                degsRes64[i % 8] = degsRes64[(i - 1) % 8] << 1;
            } else {
                degsRes64[i % 8] = (degsRes64[(i - 1) % 8] << 1) ^ this.mPoly64;
            }
        }
        for (int i2 = 0; i2 < 256; i2++) {
            int currIndex = i2;
            int j = 0;
            while (currIndex > 0 && j < 8) {
                if ((currIndex & 1) == 1) {
                    long[] jArr = this.mTableOutByte;
                    jArr[i2] = jArr[i2] ^ degsRes64[j];
                }
                currIndex >>>= 1;
                j++;
            }
        }
    }
}
