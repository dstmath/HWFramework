package com.huawei.zxing.pdf417.decoder;

final class BarcodeMetadata {
    private final int columnCount;
    private final int errorCorrectionLevel;
    private final int rowCount;
    private final int rowCountLowerPart;
    private final int rowCountUpperPart;

    BarcodeMetadata(int columnCount, int rowCountUpperPart, int rowCountLowerPart, int errorCorrectionLevel) {
        this.columnCount = columnCount;
        this.errorCorrectionLevel = errorCorrectionLevel;
        this.rowCountUpperPart = rowCountUpperPart;
        this.rowCountLowerPart = rowCountLowerPart;
        this.rowCount = rowCountUpperPart + rowCountLowerPart;
    }

    int getColumnCount() {
        return this.columnCount;
    }

    int getErrorCorrectionLevel() {
        return this.errorCorrectionLevel;
    }

    int getRowCount() {
        return this.rowCount;
    }

    int getRowCountUpperPart() {
        return this.rowCountUpperPart;
    }

    int getRowCountLowerPart() {
        return this.rowCountLowerPart;
    }
}
