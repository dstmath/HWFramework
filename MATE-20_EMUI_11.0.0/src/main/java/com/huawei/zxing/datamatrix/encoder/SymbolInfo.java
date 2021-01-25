package com.huawei.zxing.datamatrix.encoder;

import com.huawei.android.hardware.input.HwSideTouchManagerEx;
import com.huawei.android.os.ProcessCommonEx;
import com.huawei.zxing.Dimension;

public class SymbolInfo {
    public static final SymbolInfo[] PROD_SYMBOLS = {new SymbolInfo(false, 3, 5, 8, 8, 1), new SymbolInfo(false, 5, 7, 10, 10, 1), new SymbolInfo(true, 5, 7, 16, 6, 1), new SymbolInfo(false, 8, 10, 12, 12, 1), new SymbolInfo(true, 10, 11, 14, 6, 2), new SymbolInfo(false, 12, 12, 14, 14, 1), new SymbolInfo(true, 16, 14, 24, 10, 1), new SymbolInfo(false, 18, 14, 16, 16, 1), new SymbolInfo(false, 22, 18, 18, 18, 1), new SymbolInfo(true, 22, 18, 16, 10, 2), new SymbolInfo(false, 30, 20, 20, 20, 1), new SymbolInfo(true, 32, 24, 16, 14, 2), new SymbolInfo(false, 36, 24, 22, 22, 1), new SymbolInfo(false, 44, 28, 24, 24, 1), new SymbolInfo(true, 49, 28, 22, 14, 2), new SymbolInfo(false, 62, 36, 14, 14, 4), new SymbolInfo(false, 86, 42, 16, 16, 4), new SymbolInfo(false, 114, 48, 18, 18, 4), new SymbolInfo(false, 144, 56, 20, 20, 4), new SymbolInfo(false, 174, 68, 22, 22, 4), new SymbolInfo(false, HwSideTouchManagerEx.VOLUME_MODE_DOUBLE_SLIDE_MAPPING, 84, 24, 24, 4, 102, 42), new SymbolInfo(false, 280, 112, 14, 14, 16, 140, 56), new SymbolInfo(false, 368, 144, 16, 16, 16, 92, 36), new SymbolInfo(false, 456, 192, 18, 18, 16, 114, 48), new SymbolInfo(false, 576, 224, 20, 20, 16, 144, 56), new SymbolInfo(false, 696, 272, 22, 22, 16, 174, 68), new SymbolInfo(false, 816, 336, 24, 24, 16, 136, 56), new SymbolInfo(false, ProcessCommonEx.SMARTCARD_UID, 408, 18, 18, 36, 175, 68), new SymbolInfo(false, 1304, 496, 20, 20, 36, 163, 62), new DataMatrixSymbolInfo144()};
    private static SymbolInfo[] symbols = PROD_SYMBOLS;
    private final int dataCapacity;
    private final int dataRegions;
    private final int errorCodewords;
    public final int matrixHeight;
    public final int matrixWidth;
    private final boolean rectangular;
    private final int rsBlockData;
    private final int rsBlockError;

    public static void overrideSymbolSet(SymbolInfo[] override) {
        symbols = override;
    }

    public SymbolInfo(boolean rectangular2, int dataCapacity2, int errorCodewords2, int matrixWidth2, int matrixHeight2, int dataRegions2) {
        this(rectangular2, dataCapacity2, errorCodewords2, matrixWidth2, matrixHeight2, dataRegions2, dataCapacity2, errorCodewords2);
    }

    SymbolInfo(boolean rectangular2, int dataCapacity2, int errorCodewords2, int matrixWidth2, int matrixHeight2, int dataRegions2, int rsBlockData2, int rsBlockError2) {
        this.rectangular = rectangular2;
        this.dataCapacity = dataCapacity2;
        this.errorCodewords = errorCodewords2;
        this.matrixWidth = matrixWidth2;
        this.matrixHeight = matrixHeight2;
        this.dataRegions = dataRegions2;
        this.rsBlockData = rsBlockData2;
        this.rsBlockError = rsBlockError2;
    }

    public static SymbolInfo lookup(int dataCodewords) {
        return lookup(dataCodewords, SymbolShapeHint.FORCE_NONE, true);
    }

    public static SymbolInfo lookup(int dataCodewords, SymbolShapeHint shape) {
        return lookup(dataCodewords, shape, true);
    }

    public static SymbolInfo lookup(int dataCodewords, boolean allowRectangular, boolean fail) {
        return lookup(dataCodewords, allowRectangular ? SymbolShapeHint.FORCE_NONE : SymbolShapeHint.FORCE_SQUARE, fail);
    }

    private static SymbolInfo lookup(int dataCodewords, SymbolShapeHint shape, boolean fail) {
        return lookup(dataCodewords, shape, null, null, fail);
    }

    public static SymbolInfo lookup(int dataCodewords, SymbolShapeHint shape, Dimension minSize, Dimension maxSize, boolean fail) {
        SymbolInfo[] symbolInfoArr = symbols;
        for (SymbolInfo symbol : symbolInfoArr) {
            if ((shape != SymbolShapeHint.FORCE_SQUARE || !symbol.rectangular) && ((shape != SymbolShapeHint.FORCE_RECTANGLE || symbol.rectangular) && ((minSize == null || (symbol.getSymbolWidth() >= minSize.getWidth() && symbol.getSymbolHeight() >= minSize.getHeight())) && ((maxSize == null || (symbol.getSymbolWidth() <= maxSize.getWidth() && symbol.getSymbolHeight() <= maxSize.getHeight())) && dataCodewords <= symbol.dataCapacity)))) {
                return symbol;
            }
        }
        if (!fail) {
            return null;
        }
        throw new IllegalArgumentException("Can't find a symbol arrangement that matches the message. Data codewords: " + dataCodewords);
    }

    /* access modifiers changed from: package-private */
    public final int getHorizontalDataRegions() {
        int i = this.dataRegions;
        if (i == 1) {
            return 1;
        }
        if (i == 2 || i == 4) {
            return 2;
        }
        if (i == 16) {
            return 4;
        }
        if (i == 36) {
            return 6;
        }
        throw new IllegalStateException("Cannot handle this number of data regions");
    }

    /* access modifiers changed from: package-private */
    public final int getVerticalDataRegions() {
        int i = this.dataRegions;
        if (i == 1 || i == 2) {
            return 1;
        }
        if (i == 4) {
            return 2;
        }
        if (i == 16) {
            return 4;
        }
        if (i == 36) {
            return 6;
        }
        throw new IllegalStateException("Cannot handle this number of data regions");
    }

    public final int getSymbolDataWidth() {
        return getHorizontalDataRegions() * this.matrixWidth;
    }

    public final int getSymbolDataHeight() {
        return getVerticalDataRegions() * this.matrixHeight;
    }

    public final int getSymbolWidth() {
        return getSymbolDataWidth() + (getHorizontalDataRegions() * 2);
    }

    public final int getSymbolHeight() {
        return getSymbolDataHeight() + (getVerticalDataRegions() * 2);
    }

    public int getCodewordCount() {
        return this.dataCapacity + this.errorCodewords;
    }

    public int getInterleavedBlockCount() {
        return this.dataCapacity / this.rsBlockData;
    }

    public final int getDataCapacity() {
        return this.dataCapacity;
    }

    public final int getErrorCodewords() {
        return this.errorCodewords;
    }

    public int getDataLengthForInterleavedBlock(int index) {
        return this.rsBlockData;
    }

    public final int getErrorLengthForInterleavedBlock(int index) {
        return this.rsBlockError;
    }

    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.rectangular ? "Rectangular Symbol:" : "Square Symbol:");
        sb.append(" data region ");
        sb.append(this.matrixWidth);
        sb.append('x');
        sb.append(this.matrixHeight);
        sb.append(", symbol size ");
        sb.append(getSymbolWidth());
        sb.append('x');
        sb.append(getSymbolHeight());
        sb.append(", symbol data size ");
        sb.append(getSymbolDataWidth());
        sb.append('x');
        sb.append(getSymbolDataHeight());
        sb.append(", codewords ");
        sb.append(this.dataCapacity);
        sb.append('+');
        sb.append(this.errorCodewords);
        return sb.toString();
    }
}
