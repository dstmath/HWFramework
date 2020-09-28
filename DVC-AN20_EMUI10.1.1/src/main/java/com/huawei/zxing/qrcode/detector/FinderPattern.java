package com.huawei.zxing.qrcode.detector;

import com.huawei.zxing.ResultPoint;

public final class FinderPattern extends ResultPoint {
    private final int count;
    private final float estimatedModuleSize;

    FinderPattern(float posX, float posY, float estimatedModuleSize2) {
        this(posX, posY, estimatedModuleSize2, 1);
    }

    private FinderPattern(float posX, float posY, float estimatedModuleSize2, int count2) {
        super(posX, posY);
        this.estimatedModuleSize = estimatedModuleSize2;
        this.count = count2;
    }

    public float getEstimatedModuleSize() {
        return this.estimatedModuleSize;
    }

    /* access modifiers changed from: package-private */
    public int getCount() {
        return this.count;
    }

    /* access modifiers changed from: package-private */
    public boolean aboutEquals(float moduleSize, float i, float j) {
        if (Math.abs(i - getY()) > moduleSize || Math.abs(j - getX()) > moduleSize) {
            return false;
        }
        float moduleSizeDiff = Math.abs(moduleSize - this.estimatedModuleSize);
        if (moduleSizeDiff <= 1.0f || moduleSizeDiff <= this.estimatedModuleSize) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public FinderPattern combineEstimate(float i, float j, float newModuleSize) {
        int i2 = this.count;
        int combinedCount = i2 + 1;
        return new FinderPattern(((((float) i2) * getX()) + j) / ((float) combinedCount), ((((float) this.count) * getY()) + i) / ((float) combinedCount), ((((float) this.count) * this.estimatedModuleSize) + newModuleSize) / ((float) combinedCount), combinedCount);
    }
}
