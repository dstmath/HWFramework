package com.huawei.zxing.pdf417.decoder;

import com.huawei.zxing.ResultPoint;

/* access modifiers changed from: package-private */
public final class DetectionResultRowIndicatorColumn extends DetectionResultColumn {
    private final boolean isLeft;

    DetectionResultRowIndicatorColumn(BoundingBox boundingBox, boolean isLeft2) {
        super(boundingBox);
        this.isLeft = isLeft2;
    }

    /* access modifiers changed from: package-private */
    public void setRowNumbers() {
        Codeword[] codewords = getCodewords();
        for (Codeword codeword : codewords) {
            if (codeword != null) {
                codeword.setRowNumberAsRowIndicatorColumn();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int adjustCompleteIndicatorColumnRowNumbers(BarcodeMetadata barcodeMetadata) {
        int checkedRows;
        Codeword[] codewords = getCodewords();
        setRowNumbers();
        removeIncorrectCodewords(codewords, barcodeMetadata);
        BoundingBox boundingBox = getBoundingBox();
        ResultPoint top = this.isLeft ? boundingBox.getTopLeft() : boundingBox.getTopRight();
        ResultPoint bottom = this.isLeft ? boundingBox.getBottomLeft() : boundingBox.getBottomRight();
        int firstRow = imageRowToCodewordIndex((int) top.getY());
        int lastRow = imageRowToCodewordIndex((int) bottom.getY());
        float averageRowHeight = ((float) (lastRow - firstRow)) / ((float) barcodeMetadata.getRowCount());
        int barcodeRow = -1;
        int maxRowHeight = 1;
        int currentRowHeight = 0;
        int codewordsRow = firstRow;
        while (codewordsRow < lastRow) {
            if (codewords[codewordsRow] != null) {
                Codeword codeword = codewords[codewordsRow];
                int rowDifference = codeword.getRowNumber() - barcodeRow;
                if (rowDifference == 0) {
                    currentRowHeight++;
                } else if (rowDifference == 1) {
                    maxRowHeight = Math.max(maxRowHeight, currentRowHeight);
                    currentRowHeight = 1;
                    barcodeRow = codeword.getRowNumber();
                } else if (rowDifference < 0 || codeword.getRowNumber() >= barcodeMetadata.getRowCount() || rowDifference > codewordsRow) {
                    codewords[codewordsRow] = null;
                } else {
                    if (maxRowHeight > 2) {
                        checkedRows = (maxRowHeight - 2) * rowDifference;
                    } else {
                        checkedRows = rowDifference;
                    }
                    boolean closePreviousCodewordFound = checkedRows >= codewordsRow;
                    for (int i = 1; i <= checkedRows && !closePreviousCodewordFound; i++) {
                        closePreviousCodewordFound = codewords[codewordsRow - i] != null;
                    }
                    if (closePreviousCodewordFound) {
                        codewords[codewordsRow] = null;
                    } else {
                        barcodeRow = codeword.getRowNumber();
                        currentRowHeight = 1;
                    }
                }
            }
            codewordsRow++;
        }
        return (int) (((double) averageRowHeight) + 0.5d);
    }

    /* access modifiers changed from: package-private */
    public int[] getRowHeights() {
        BarcodeMetadata barcodeMetadata = getBarcodeMetadata();
        if (barcodeMetadata == null) {
            return null;
        }
        adjustIncompleteIndicatorColumnRowNumbers(barcodeMetadata);
        int[] result = new int[barcodeMetadata.getRowCount()];
        Codeword[] codewords = getCodewords();
        for (Codeword codeword : codewords) {
            if (codeword != null) {
                int rowNumber = codeword.getRowNumber();
                result[rowNumber] = result[rowNumber] + 1;
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public int adjustIncompleteIndicatorColumnRowNumbers(BarcodeMetadata barcodeMetadata) {
        BoundingBox boundingBox = getBoundingBox();
        ResultPoint top = this.isLeft ? boundingBox.getTopLeft() : boundingBox.getTopRight();
        ResultPoint bottom = this.isLeft ? boundingBox.getBottomLeft() : boundingBox.getBottomRight();
        int firstRow = imageRowToCodewordIndex((int) top.getY());
        int lastRow = imageRowToCodewordIndex((int) bottom.getY());
        float averageRowHeight = ((float) (lastRow - firstRow)) / ((float) barcodeMetadata.getRowCount());
        Codeword[] codewords = getCodewords();
        int barcodeRow = -1;
        int maxRowHeight = 1;
        int currentRowHeight = 0;
        for (int codewordsRow = firstRow; codewordsRow < lastRow; codewordsRow++) {
            if (codewords[codewordsRow] != null) {
                Codeword codeword = codewords[codewordsRow];
                codeword.setRowNumberAsRowIndicatorColumn();
                int rowDifference = codeword.getRowNumber() - barcodeRow;
                if (rowDifference == 0) {
                    currentRowHeight++;
                } else if (rowDifference == 1) {
                    maxRowHeight = Math.max(maxRowHeight, currentRowHeight);
                    currentRowHeight = 1;
                    barcodeRow = codeword.getRowNumber();
                } else if (codeword.getRowNumber() >= barcodeMetadata.getRowCount()) {
                    codewords[codewordsRow] = null;
                } else {
                    barcodeRow = codeword.getRowNumber();
                    currentRowHeight = 1;
                }
            }
        }
        return (int) (((double) averageRowHeight) + 0.5d);
    }

    /* access modifiers changed from: package-private */
    public BarcodeMetadata getBarcodeMetadata() {
        Codeword[] codewords = getCodewords();
        BarcodeValue barcodeColumnCount = new BarcodeValue();
        BarcodeValue barcodeRowCountUpperPart = new BarcodeValue();
        BarcodeValue barcodeRowCountLowerPart = new BarcodeValue();
        BarcodeValue barcodeECLevel = new BarcodeValue();
        for (Codeword codeword : codewords) {
            if (codeword != null) {
                codeword.setRowNumberAsRowIndicatorColumn();
                int rowIndicatorValue = codeword.getValue() % 30;
                int codewordRowNumber = codeword.getRowNumber();
                if (!this.isLeft) {
                    codewordRowNumber += 2;
                }
                int i = codewordRowNumber % 3;
                if (i == 0) {
                    barcodeRowCountUpperPart.setValue((rowIndicatorValue * 3) + 1);
                } else if (i == 1) {
                    barcodeECLevel.setValue(rowIndicatorValue / 3);
                    barcodeRowCountLowerPart.setValue(rowIndicatorValue % 3);
                } else if (i == 2) {
                    barcodeColumnCount.setValue(rowIndicatorValue + 1);
                }
            }
        }
        if (barcodeColumnCount.getValue().length == 0 || barcodeRowCountUpperPart.getValue().length == 0 || barcodeRowCountLowerPart.getValue().length == 0 || barcodeECLevel.getValue().length == 0 || barcodeColumnCount.getValue()[0] < 1 || barcodeRowCountUpperPart.getValue()[0] + barcodeRowCountLowerPart.getValue()[0] < 3 || barcodeRowCountUpperPart.getValue()[0] + barcodeRowCountLowerPart.getValue()[0] > 90) {
            return null;
        }
        BarcodeMetadata barcodeMetadata = new BarcodeMetadata(barcodeColumnCount.getValue()[0], barcodeRowCountUpperPart.getValue()[0], barcodeRowCountLowerPart.getValue()[0], barcodeECLevel.getValue()[0]);
        removeIncorrectCodewords(codewords, barcodeMetadata);
        return barcodeMetadata;
    }

    private void removeIncorrectCodewords(Codeword[] codewords, BarcodeMetadata barcodeMetadata) {
        for (int codewordRow = 0; codewordRow < codewords.length; codewordRow++) {
            Codeword codeword = codewords[codewordRow];
            if (codewords[codewordRow] != null) {
                int rowIndicatorValue = codeword.getValue() % 30;
                int codewordRowNumber = codeword.getRowNumber();
                if (codewordRowNumber > barcodeMetadata.getRowCount()) {
                    codewords[codewordRow] = null;
                } else {
                    if (!this.isLeft) {
                        codewordRowNumber += 2;
                    }
                    int i = codewordRowNumber % 3;
                    if (i != 0) {
                        if (i != 1) {
                            if (i == 2 && rowIndicatorValue + 1 != barcodeMetadata.getColumnCount()) {
                                codewords[codewordRow] = null;
                            }
                        } else if (rowIndicatorValue / 3 != barcodeMetadata.getErrorCorrectionLevel() || rowIndicatorValue % 3 != barcodeMetadata.getRowCountLowerPart()) {
                            codewords[codewordRow] = null;
                        }
                    } else if ((rowIndicatorValue * 3) + 1 != barcodeMetadata.getRowCountUpperPart()) {
                        codewords[codewordRow] = null;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLeft() {
        return this.isLeft;
    }

    @Override // com.huawei.zxing.pdf417.decoder.DetectionResultColumn
    public String toString() {
        return "IsLeft: " + this.isLeft + '\n' + super.toString();
    }
}
