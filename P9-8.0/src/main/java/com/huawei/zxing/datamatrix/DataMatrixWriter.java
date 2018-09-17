package com.huawei.zxing.datamatrix;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.Dimension;
import com.huawei.zxing.EncodeHintType;
import com.huawei.zxing.Writer;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.datamatrix.encoder.DefaultPlacement;
import com.huawei.zxing.datamatrix.encoder.ErrorCorrection;
import com.huawei.zxing.datamatrix.encoder.HighLevelEncoder;
import com.huawei.zxing.datamatrix.encoder.SymbolInfo;
import com.huawei.zxing.datamatrix.encoder.SymbolShapeHint;
import com.huawei.zxing.qrcode.encoder.ByteMatrix;
import java.util.Map;

public final class DataMatrixWriter implements Writer {
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) {
        return encode(contents, format, width, height, null);
    }

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) {
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Found empty contents");
        } else if (format != BarcodeFormat.DATA_MATRIX) {
            throw new IllegalArgumentException("Can only encode DATA_MATRIX, but got " + format);
        } else if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Requested dimensions are too small: " + width + 'x' + height);
        } else {
            SymbolShapeHint shape = SymbolShapeHint.FORCE_NONE;
            Dimension minSize = null;
            Dimension maxSize = null;
            if (hints != null) {
                SymbolShapeHint requestedShape = (SymbolShapeHint) hints.get(EncodeHintType.DATA_MATRIX_SHAPE);
                if (requestedShape != null) {
                    shape = requestedShape;
                }
                Dimension requestedMinSize = (Dimension) hints.get(EncodeHintType.MIN_SIZE);
                if (requestedMinSize != null) {
                    minSize = requestedMinSize;
                }
                Dimension requestedMaxSize = (Dimension) hints.get(EncodeHintType.MAX_SIZE);
                if (requestedMaxSize != null) {
                    maxSize = requestedMaxSize;
                }
            }
            String encoded = HighLevelEncoder.encodeHighLevel(contents, shape, minSize, maxSize);
            SymbolInfo symbolInfo = SymbolInfo.lookup(encoded.length(), shape, minSize, maxSize, true);
            DefaultPlacement placement = new DefaultPlacement(ErrorCorrection.encodeECC200(encoded, symbolInfo), symbolInfo.getSymbolDataWidth(), symbolInfo.getSymbolDataHeight());
            placement.place();
            return encodeLowLevel(placement, symbolInfo);
        }
    }

    private static BitMatrix encodeLowLevel(DefaultPlacement placement, SymbolInfo symbolInfo) {
        int symbolWidth = symbolInfo.getSymbolDataWidth();
        int symbolHeight = symbolInfo.getSymbolDataHeight();
        ByteMatrix matrix = new ByteMatrix(symbolInfo.getSymbolWidth(), symbolInfo.getSymbolHeight());
        int matrixY = 0;
        for (int y = 0; y < symbolHeight; y++) {
            int matrixX;
            int x;
            boolean z;
            if (y % symbolInfo.matrixHeight == 0) {
                matrixX = 0;
                for (x = 0; x < symbolInfo.getSymbolWidth(); x++) {
                    if (x % 2 == 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    matrix.set(matrixX, matrixY, z);
                    matrixX++;
                }
                matrixY++;
            }
            matrixX = 0;
            for (x = 0; x < symbolWidth; x++) {
                if (x % symbolInfo.matrixWidth == 0) {
                    matrix.set(matrixX, matrixY, true);
                    matrixX++;
                }
                matrix.set(matrixX, matrixY, placement.getBit(x, y));
                matrixX++;
                if (x % symbolInfo.matrixWidth == symbolInfo.matrixWidth - 1) {
                    if (y % 2 == 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    matrix.set(matrixX, matrixY, z);
                    matrixX++;
                }
            }
            matrixY++;
            if (y % symbolInfo.matrixHeight == symbolInfo.matrixHeight - 1) {
                matrixX = 0;
                for (x = 0; x < symbolInfo.getSymbolWidth(); x++) {
                    matrix.set(matrixX, matrixY, true);
                    matrixX++;
                }
                matrixY++;
            }
        }
        return convertByteMatrixToBitMatrix(matrix);
    }

    private static BitMatrix convertByteMatrixToBitMatrix(ByteMatrix matrix) {
        int matrixWidgth = matrix.getWidth();
        int matrixHeight = matrix.getHeight();
        BitMatrix output = new BitMatrix(matrixWidgth, matrixHeight);
        output.clear();
        for (int i = 0; i < matrixWidgth; i++) {
            for (int j = 0; j < matrixHeight; j++) {
                if (matrix.get(i, j) == (byte) 1) {
                    output.set(i, j);
                }
            }
        }
        return output;
    }
}
