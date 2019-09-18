package com.huawei.zxing.pdf417.encoder;

public final class Dimensions {
    private final int maxCols;
    private final int maxRows;
    private final int minCols;
    private final int minRows;

    public Dimensions(int minCols2, int maxCols2, int minRows2, int maxRows2) {
        this.minCols = minCols2;
        this.maxCols = maxCols2;
        this.minRows = minRows2;
        this.maxRows = maxRows2;
    }

    public int getMinCols() {
        return this.minCols;
    }

    public int getMaxCols() {
        return this.maxCols;
    }

    public int getMinRows() {
        return this.minRows;
    }

    public int getMaxRows() {
        return this.maxRows;
    }
}
