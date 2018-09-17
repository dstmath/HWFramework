package com.huawei.zxing.pdf417.decoder;

final class Codeword {
    private static final int BARCODE_ROW_UNKNOWN = -1;
    private final int bucket;
    private final int endX;
    private int rowNumber = -1;
    private final int startX;
    private final int value;

    Codeword(int startX, int endX, int bucket, int value) {
        this.startX = startX;
        this.endX = endX;
        this.bucket = bucket;
        this.value = value;
    }

    boolean hasValidRowNumber() {
        return isValidRowNumber(this.rowNumber);
    }

    boolean isValidRowNumber(int rowNumber) {
        return rowNumber != -1 && this.bucket == (rowNumber % 3) * 3;
    }

    void setRowNumberAsRowIndicatorColumn() {
        this.rowNumber = ((this.value / 30) * 3) + (this.bucket / 3);
    }

    int getWidth() {
        return this.endX - this.startX;
    }

    int getStartX() {
        return this.startX;
    }

    int getEndX() {
        return this.endX;
    }

    int getBucket() {
        return this.bucket;
    }

    int getValue() {
        return this.value;
    }

    int getRowNumber() {
        return this.rowNumber;
    }

    void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String toString() {
        return this.rowNumber + "|" + this.value;
    }
}
