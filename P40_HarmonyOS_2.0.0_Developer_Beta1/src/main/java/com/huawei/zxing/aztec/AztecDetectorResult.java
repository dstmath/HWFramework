package com.huawei.zxing.aztec;

import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.DetectorResult;

public final class AztecDetectorResult extends DetectorResult {
    private final boolean compact;
    private final int nbDatablocks;
    private final int nbLayers;

    public AztecDetectorResult(BitMatrix bits, ResultPoint[] points, boolean compact2, int nbDatablocks2, int nbLayers2) {
        super(bits, points);
        this.compact = compact2;
        this.nbDatablocks = nbDatablocks2;
        this.nbLayers = nbLayers2;
    }

    public int getNbLayers() {
        return this.nbLayers;
    }

    public int getNbDatablocks() {
        return this.nbDatablocks;
    }

    public boolean isCompact() {
        return this.compact;
    }
}
