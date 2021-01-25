package com.huawei.zxing.pdf417.decoder;

final class BarcodeMetadata {
    private final int columnCount;
    private final int errorCorrectionLevel;
    private final int rowCount;
    private final int rowCountLowerPart;
    private final int rowCountUpperPart;

    BarcodeMetadata(int columnCount2, int rowCountUpperPart2, int rowCountLowerPart2, int errorCorrectionLevel2) {
        this.columnCount = columnCount2;
        this.errorCorrectionLevel = errorCorrectionLevel2;
        this.rowCountUpperPart = rowCountUpperPart2;
        this.rowCountLowerPart = rowCountLowerPart2;
        this.rowCount = rowCountUpperPart2 + rowCountLowerPart2;
    }

    /* access modifiers changed from: package-private */
    public int getColumnCount() {
        return this.columnCount;
    }

    /* access modifiers changed from: package-private */
    public int getErrorCorrectionLevel() {
        return this.errorCorrectionLevel;
    }

    /* access modifiers changed from: package-private */
    public int getRowCount() {
        return this.rowCount;
    }

    /* access modifiers changed from: package-private */
    public int getRowCountUpperPart() {
        return this.rowCountUpperPart;
    }

    /* access modifiers changed from: package-private */
    public int getRowCountLowerPart() {
        return this.rowCountLowerPart;
    }
}
