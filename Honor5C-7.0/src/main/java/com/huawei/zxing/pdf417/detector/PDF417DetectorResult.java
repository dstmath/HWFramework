package com.huawei.zxing.pdf417.detector;

import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitMatrix;
import java.util.List;

public final class PDF417DetectorResult {
    private final BitMatrix bits;
    private final List<ResultPoint[]> points;

    public PDF417DetectorResult(BitMatrix bits, List<ResultPoint[]> points) {
        this.bits = bits;
        this.points = points;
    }

    public BitMatrix getBits() {
        return this.bits;
    }

    public List<ResultPoint[]> getPoints() {
        return this.points;
    }
}
