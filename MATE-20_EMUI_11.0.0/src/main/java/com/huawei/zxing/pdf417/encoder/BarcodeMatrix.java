package com.huawei.zxing.pdf417.encoder;

import java.lang.reflect.Array;

public final class BarcodeMatrix {
    private int currentRow;
    private final int height;
    private final BarcodeRow[] matrix;
    private final int width;

    BarcodeMatrix(int height2, int width2) {
        this.matrix = new BarcodeRow[height2];
        int matrixLength = this.matrix.length;
        for (int i = 0; i < matrixLength; i++) {
            this.matrix[i] = new BarcodeRow(((width2 + 4) * 17) + 1);
        }
        this.width = width2 * 17;
        this.height = height2;
        this.currentRow = -1;
    }

    /* access modifiers changed from: package-private */
    public void set(int x, int y, byte value) {
        this.matrix[y].set(x, value);
    }

    /* access modifiers changed from: package-private */
    public void startRow() {
        this.currentRow++;
    }

    /* access modifiers changed from: package-private */
    public BarcodeRow getCurrentRow() {
        return this.matrix[this.currentRow];
    }

    public byte[][] getMatrix() {
        return getScaledMatrix(1, 1);
    }

    public byte[][] getScaledMatrix(int xScale, int yScale) {
        byte[][] matrixOut = (byte[][]) Array.newInstance(byte.class, this.height * yScale, this.width * xScale);
        int yMax = this.height * yScale;
        for (int i = 0; i < yMax; i++) {
            matrixOut[(yMax - i) - 1] = this.matrix[i / yScale].getScaledRow(xScale);
        }
        return matrixOut;
    }
}
