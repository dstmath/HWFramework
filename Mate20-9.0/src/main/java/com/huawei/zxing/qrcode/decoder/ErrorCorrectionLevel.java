package com.huawei.zxing.qrcode.decoder;

public enum ErrorCorrectionLevel {
    L(1),
    M(0),
    Q(3),
    H(2);
    
    private static final ErrorCorrectionLevel[] FOR_BITS = null;
    private final int bits;

    static {
        FOR_BITS = new ErrorCorrectionLevel[]{M, L, H, Q};
    }

    private ErrorCorrectionLevel(int bits2) {
        this.bits = bits2;
    }

    public int getBits() {
        return this.bits;
    }

    public static ErrorCorrectionLevel forBits(int bits2) {
        if (bits2 >= 0 && bits2 < FOR_BITS.length) {
            return FOR_BITS[bits2];
        }
        throw new IllegalArgumentException();
    }
}
