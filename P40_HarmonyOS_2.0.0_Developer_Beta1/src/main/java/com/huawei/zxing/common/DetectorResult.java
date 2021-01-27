package com.huawei.zxing.common;

import com.huawei.zxing.ResultPoint;

public class DetectorResult {
    private final BitMatrix bits;
    private final ResultPoint[] points;

    public DetectorResult(BitMatrix bits2, ResultPoint[] points2) {
        this.bits = bits2;
        this.points = points2;
    }

    public final BitMatrix getBits() {
        return this.bits;
    }

    public final ResultPoint[] getPoints() {
        return this.points;
    }
}
