package com.huawei.zxing.common;

import com.huawei.zxing.ResultPoint;

public class DetectorResult {
    private final BitMatrix bits;
    private final ResultPoint[] points;

    public DetectorResult(BitMatrix bits, ResultPoint[] points) {
        this.bits = bits;
        this.points = points;
    }

    public final BitMatrix getBits() {
        return this.bits;
    }

    public final ResultPoint[] getPoints() {
        return this.points;
    }
}
