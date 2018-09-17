package com.huawei.zxing.datamatrix.decoder;

import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitMatrix;

final class BitMatrixParser {
    private final BitMatrix mappingBitMatrix;
    private final BitMatrix readMappingMatrix;
    private final Version version;

    BitMatrixParser(BitMatrix bitMatrix) throws FormatException {
        int dimension = bitMatrix.getHeight();
        if (dimension < 8 || dimension > 144 || (dimension & 1) != 0) {
            throw FormatException.getFormatInstance();
        }
        this.version = readVersion(bitMatrix);
        this.mappingBitMatrix = extractDataRegion(bitMatrix);
        this.readMappingMatrix = new BitMatrix(this.mappingBitMatrix.getWidth(), this.mappingBitMatrix.getHeight());
    }

    Version getVersion() {
        return this.version;
    }

    private static Version readVersion(BitMatrix bitMatrix) throws FormatException {
        return Version.getVersionForDimensions(bitMatrix.getHeight(), bitMatrix.getWidth());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    byte[] readCodewords() throws FormatException {
        byte[] result = new byte[this.version.getTotalCodewords()];
        int row = 4;
        int column = 0;
        int numRows = this.mappingBitMatrix.getHeight();
        int numColumns = this.mappingBitMatrix.getWidth();
        boolean corner1Read = false;
        boolean corner2Read = false;
        boolean corner3Read = false;
        boolean corner4Read = false;
        int resultOffset = 0;
        while (true) {
            int resultOffset2;
            if (row == numRows && column == 0 && !corner1Read) {
                resultOffset2 = resultOffset + 1;
                result[resultOffset] = (byte) readCorner1(numRows, numColumns);
                row -= 2;
                column += 2;
                corner1Read = true;
            } else if (row == numRows - 2 && column == 0 && (numColumns & 3) != 0 && !corner2Read) {
                resultOffset2 = resultOffset + 1;
                result[resultOffset] = (byte) readCorner2(numRows, numColumns);
                row -= 2;
                column += 2;
                corner2Read = true;
            } else if (row == numRows + 4 && column == 2 && (numColumns & 7) == 0 && !corner3Read) {
                resultOffset2 = resultOffset + 1;
                result[resultOffset] = (byte) readCorner3(numRows, numColumns);
                row -= 2;
                column += 2;
                corner3Read = true;
            } else if (row == numRows - 2 && column == 0 && (numColumns & 7) == 4 && !corner4Read) {
                resultOffset2 = resultOffset + 1;
                result[resultOffset] = (byte) readCorner4(numRows, numColumns);
                row -= 2;
                column += 2;
                corner4Read = true;
            } else {
                while (true) {
                    if (row >= numRows || column < 0 || this.readMappingMatrix.get(column, row)) {
                        resultOffset2 = resultOffset;
                    } else {
                        resultOffset2 = resultOffset + 1;
                        result[resultOffset] = (byte) readUtah(row, column, numRows, numColumns);
                    }
                    row -= 2;
                    column += 2;
                    if (row < 0 || column >= numColumns) {
                        row++;
                        column += 3;
                        resultOffset = resultOffset2;
                    } else {
                        resultOffset = resultOffset2;
                    }
                }
                row++;
                column += 3;
                resultOffset = resultOffset2;
                while (true) {
                    if (row < 0 || column >= numColumns || this.readMappingMatrix.get(column, row)) {
                        resultOffset2 = resultOffset;
                    } else {
                        resultOffset2 = resultOffset + 1;
                        result[resultOffset] = (byte) readUtah(row, column, numRows, numColumns);
                    }
                    row += 2;
                    column -= 2;
                    if (row >= numRows || column < 0) {
                        row += 3;
                        column++;
                    } else {
                        resultOffset = resultOffset2;
                    }
                }
                row += 3;
                column++;
            }
            if (row >= numRows && column >= numColumns) {
                break;
            }
            resultOffset = resultOffset2;
        }
        if (resultOffset2 == this.version.getTotalCodewords()) {
            return result;
        }
        throw FormatException.getFormatInstance();
    }

    boolean readModule(int row, int column, int numRows, int numColumns) {
        if (row < 0) {
            row += numRows;
            column += 4 - ((numRows + 4) & 7);
        }
        if (column < 0) {
            column += numColumns;
            row += 4 - ((numColumns + 4) & 7);
        }
        this.readMappingMatrix.set(column, row);
        return this.mappingBitMatrix.get(column, row);
    }

    int readUtah(int row, int column, int numRows, int numColumns) {
        int currentByte = 0;
        if (readModule(row - 2, column - 2, numRows, numColumns)) {
            currentByte = 1;
        }
        currentByte <<= 1;
        if (readModule(row - 2, column - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(row - 1, column - 2, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(row - 1, column - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(row - 1, column, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(row, column - 2, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(row, column - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(row, column, numRows, numColumns)) {
            return currentByte | 1;
        }
        return currentByte;
    }

    int readCorner1(int numRows, int numColumns) {
        int currentByte = 0;
        if (readModule(numRows - 1, 0, numRows, numColumns)) {
            currentByte = 1;
        }
        currentByte <<= 1;
        if (readModule(numRows - 1, 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(numRows - 1, 2, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 2, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(1, numColumns - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(2, numColumns - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(3, numColumns - 1, numRows, numColumns)) {
            return currentByte | 1;
        }
        return currentByte;
    }

    int readCorner2(int numRows, int numColumns) {
        int currentByte = 0;
        if (readModule(numRows - 3, 0, numRows, numColumns)) {
            currentByte = 1;
        }
        currentByte <<= 1;
        if (readModule(numRows - 2, 0, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(numRows - 1, 0, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 4, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 3, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 2, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(1, numColumns - 1, numRows, numColumns)) {
            return currentByte | 1;
        }
        return currentByte;
    }

    int readCorner3(int numRows, int numColumns) {
        int currentByte = 0;
        if (readModule(numRows - 1, 0, numRows, numColumns)) {
            currentByte = 1;
        }
        currentByte <<= 1;
        if (readModule(numRows - 1, numColumns - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 3, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 2, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(1, numColumns - 3, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(1, numColumns - 2, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(1, numColumns - 1, numRows, numColumns)) {
            return currentByte | 1;
        }
        return currentByte;
    }

    int readCorner4(int numRows, int numColumns) {
        int currentByte = 0;
        if (readModule(numRows - 3, 0, numRows, numColumns)) {
            currentByte = 1;
        }
        currentByte <<= 1;
        if (readModule(numRows - 2, 0, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(numRows - 1, 0, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 2, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(0, numColumns - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(1, numColumns - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(2, numColumns - 1, numRows, numColumns)) {
            currentByte |= 1;
        }
        currentByte <<= 1;
        if (readModule(3, numColumns - 1, numRows, numColumns)) {
            return currentByte | 1;
        }
        return currentByte;
    }

    BitMatrix extractDataRegion(BitMatrix bitMatrix) {
        int symbolSizeRows = this.version.getSymbolSizeRows();
        int symbolSizeColumns = this.version.getSymbolSizeColumns();
        if (bitMatrix.getHeight() != symbolSizeRows) {
            throw new IllegalArgumentException("Dimension of bitMarix must match the version size");
        }
        int dataRegionSizeRows = this.version.getDataRegionSizeRows();
        int dataRegionSizeColumns = this.version.getDataRegionSizeColumns();
        int numDataRegionsRow = symbolSizeRows / dataRegionSizeRows;
        int numDataRegionsColumn = symbolSizeColumns / dataRegionSizeColumns;
        BitMatrix bitMatrixWithoutAlignment = new BitMatrix(numDataRegionsColumn * dataRegionSizeColumns, numDataRegionsRow * dataRegionSizeRows);
        for (int dataRegionRow = 0; dataRegionRow < numDataRegionsRow; dataRegionRow++) {
            int dataRegionRowOffset = dataRegionRow * dataRegionSizeRows;
            for (int dataRegionColumn = 0; dataRegionColumn < numDataRegionsColumn; dataRegionColumn++) {
                int dataRegionColumnOffset = dataRegionColumn * dataRegionSizeColumns;
                for (int i = 0; i < dataRegionSizeRows; i++) {
                    int readRowOffset = (((dataRegionSizeRows + 2) * dataRegionRow) + 1) + i;
                    int writeRowOffset = dataRegionRowOffset + i;
                    for (int j = 0; j < dataRegionSizeColumns; j++) {
                        if (bitMatrix.get((((dataRegionSizeColumns + 2) * dataRegionColumn) + 1) + j, readRowOffset)) {
                            bitMatrixWithoutAlignment.set(dataRegionColumnOffset + j, writeRowOffset);
                        }
                    }
                }
            }
        }
        return bitMatrixWithoutAlignment;
    }
}
