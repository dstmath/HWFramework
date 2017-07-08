package com.huawei.zxing.pdf417.detector;

import com.huawei.zxing.BinaryBitmap;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitArray;
import com.huawei.zxing.common.BitMatrix;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class Detector {
    private static final int BARCODE_MIN_HEIGHT = 10;
    private static final int[] INDEXES_START_PATTERN = null;
    private static final int[] INDEXES_STOP_PATTERN = null;
    private static final int INTEGER_MATH_SHIFT = 8;
    private static final int MAX_AVG_VARIANCE = 107;
    private static final int MAX_INDIVIDUAL_VARIANCE = 204;
    private static final int MAX_PATTERN_DRIFT = 5;
    private static final int MAX_PIXEL_DRIFT = 3;
    private static final int PATTERN_MATCH_RESULT_SCALE_FACTOR = 256;
    private static final int ROW_STEP = 5;
    private static final int SKIPPED_ROW_COUNT_MAX = 25;
    private static final int[] START_PATTERN = null;
    private static final int[] STOP_PATTERN = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.pdf417.detector.Detector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.pdf417.detector.Detector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.pdf417.detector.Detector.<clinit>():void");
    }

    private Detector() {
    }

    public static PDF417DetectorResult detect(BinaryBitmap image, Map<DecodeHintType, ?> map, boolean multiple) throws NotFoundException {
        BitMatrix bitMatrix = image.getBlackMatrix();
        List<ResultPoint[]> barcodeCoordinates = detect(multiple, bitMatrix);
        if (barcodeCoordinates.isEmpty()) {
            rotate180(bitMatrix);
            barcodeCoordinates = detect(multiple, bitMatrix);
        }
        return new PDF417DetectorResult(bitMatrix, barcodeCoordinates);
    }

    private static List<ResultPoint[]> detect(boolean multiple, BitMatrix bitMatrix) {
        List<ResultPoint[]> barcodeCoordinates = new ArrayList();
        int row = 0;
        int column = 0;
        boolean foundBarcodeInRow = false;
        while (row < bitMatrix.getHeight()) {
            ResultPoint[] vertices = findVertices(bitMatrix, row, column);
            if (vertices[0] != null || vertices[MAX_PIXEL_DRIFT] != null) {
                foundBarcodeInRow = true;
                barcodeCoordinates.add(vertices);
                if (!multiple) {
                    break;
                } else if (vertices[2] != null) {
                    column = (int) vertices[2].getX();
                    row = (int) vertices[2].getY();
                } else {
                    column = (int) vertices[4].getX();
                    row = (int) vertices[4].getY();
                }
            } else if (!foundBarcodeInRow) {
                break;
            } else {
                foundBarcodeInRow = false;
                column = 0;
                for (ResultPoint[] barcodeCoordinate : barcodeCoordinates) {
                    if (barcodeCoordinate[1] != null) {
                        row = (int) Math.max((float) row, barcodeCoordinate[1].getY());
                    }
                    if (barcodeCoordinate[MAX_PIXEL_DRIFT] != null) {
                        row = Math.max(row, (int) barcodeCoordinate[MAX_PIXEL_DRIFT].getY());
                    }
                }
                row += ROW_STEP;
            }
        }
        return barcodeCoordinates;
    }

    static void rotate180(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BitArray firstRowBitArray = new BitArray(width);
        BitArray secondRowBitArray = new BitArray(width);
        BitArray tmpBitArray = new BitArray(width);
        for (int y = 0; y < ((height + 1) >> 1); y++) {
            firstRowBitArray = bitMatrix.getRow(y, firstRowBitArray);
            bitMatrix.setRow(y, mirror(bitMatrix.getRow((height - 1) - y, secondRowBitArray), tmpBitArray));
            bitMatrix.setRow((height - 1) - y, mirror(firstRowBitArray, tmpBitArray));
        }
    }

    static BitArray mirror(BitArray input, BitArray result) {
        result.clear();
        int size = input.getSize();
        for (int i = 0; i < size; i++) {
            if (input.get(i)) {
                result.set((size - 1) - i);
            }
        }
        return result;
    }

    private static ResultPoint[] findVertices(BitMatrix matrix, int startRow, int startColumn) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        ResultPoint[] result = new ResultPoint[INTEGER_MATH_SHIFT];
        copyToResult(result, findRowsWithPattern(matrix, height, width, startRow, startColumn, START_PATTERN), INDEXES_START_PATTERN);
        if (result[4] != null) {
            startColumn = (int) result[4].getX();
            startRow = (int) result[4].getY();
        }
        copyToResult(result, findRowsWithPattern(matrix, height, width, startRow, startColumn, STOP_PATTERN), INDEXES_STOP_PATTERN);
        return result;
    }

    private static void copyToResult(ResultPoint[] result, ResultPoint[] tmpResult, int[] destinationIndexes) {
        for (int i = 0; i < destinationIndexes.length; i++) {
            result[destinationIndexes[i]] = tmpResult[i];
        }
    }

    private static ResultPoint[] findRowsWithPattern(BitMatrix matrix, int height, int width, int startRow, int startColumn, int[] pattern) {
        int[] previousRowLoc;
        int stopRow;
        ResultPoint[] result = new ResultPoint[4];
        boolean found = false;
        int[] counters = new int[pattern.length];
        while (startRow < height) {
            int[] loc = findGuardPattern(matrix, startColumn, startRow, width, false, pattern, counters);
            int skippedRowCount;
            int i;
            if (loc != null) {
                while (startRow > 0) {
                    startRow--;
                    previousRowLoc = findGuardPattern(matrix, startColumn, startRow, width, false, pattern, counters);
                    if (previousRowLoc == null) {
                        startRow++;
                        break;
                    }
                    loc = previousRowLoc;
                }
                result[0] = new ResultPoint((float) loc[0], (float) startRow);
                result[1] = new ResultPoint((float) loc[1], (float) startRow);
                found = true;
                stopRow = startRow + 1;
                if (found) {
                    skippedRowCount = 0;
                    previousRowLoc = new int[]{(int) result[0].getX(), (int) result[1].getX()};
                    while (stopRow < height) {
                        loc = findGuardPattern(matrix, previousRowLoc[0], stopRow, width, false, pattern, counters);
                        if (loc == null && Math.abs(previousRowLoc[0] - loc[0]) < ROW_STEP && Math.abs(previousRowLoc[1] - loc[1]) < ROW_STEP) {
                            previousRowLoc = loc;
                            skippedRowCount = 0;
                        } else if (skippedRowCount <= SKIPPED_ROW_COUNT_MAX) {
                            break;
                        } else {
                            skippedRowCount++;
                        }
                        stopRow++;
                    }
                    stopRow -= skippedRowCount + 1;
                    result[2] = new ResultPoint((float) previousRowLoc[0], (float) stopRow);
                    result[MAX_PIXEL_DRIFT] = new ResultPoint((float) previousRowLoc[1], (float) stopRow);
                }
                if (stopRow - startRow < BARCODE_MIN_HEIGHT) {
                    for (i = 0; i < result.length; i++) {
                        result[i] = null;
                    }
                }
                return result;
            }
            startRow += ROW_STEP;
        }
        stopRow = startRow + 1;
        if (found) {
            skippedRowCount = 0;
            previousRowLoc = new int[]{(int) result[0].getX(), (int) result[1].getX()};
            while (stopRow < height) {
                loc = findGuardPattern(matrix, previousRowLoc[0], stopRow, width, false, pattern, counters);
                if (loc == null) {
                }
                if (skippedRowCount <= SKIPPED_ROW_COUNT_MAX) {
                    skippedRowCount++;
                    stopRow++;
                } else {
                    break;
                    stopRow -= skippedRowCount + 1;
                    result[2] = new ResultPoint((float) previousRowLoc[0], (float) stopRow);
                    result[MAX_PIXEL_DRIFT] = new ResultPoint((float) previousRowLoc[1], (float) stopRow);
                }
            }
            stopRow -= skippedRowCount + 1;
            result[2] = new ResultPoint((float) previousRowLoc[0], (float) stopRow);
            result[MAX_PIXEL_DRIFT] = new ResultPoint((float) previousRowLoc[1], (float) stopRow);
        }
        if (stopRow - startRow < BARCODE_MIN_HEIGHT) {
            for (i = 0; i < result.length; i++) {
                result[i] = null;
            }
        }
        return result;
    }

    private static int[] findGuardPattern(BitMatrix matrix, int column, int row, int width, boolean whiteFirst, int[] pattern, int[] counters) {
        Arrays.fill(counters, 0, counters.length, 0);
        int patternLength = pattern.length;
        int isWhite = whiteFirst;
        int patternStart = column;
        int pixelDrift = 0;
        while (matrix.get(patternStart, row) && patternStart > 0) {
            int pixelDrift2 = pixelDrift + 1;
            if (pixelDrift >= MAX_PIXEL_DRIFT) {
                break;
            }
            patternStart--;
            pixelDrift = pixelDrift2;
        }
        int counterPosition = 0;
        for (int x = patternStart; x < width; x++) {
            if ((matrix.get(x, row) ^ isWhite) != 0) {
                counters[counterPosition] = counters[counterPosition] + 1;
            } else {
                if (counterPosition != patternLength - 1) {
                    counterPosition++;
                } else if (patternMatchVariance(counters, pattern, MAX_INDIVIDUAL_VARIANCE) < MAX_AVG_VARIANCE) {
                    return new int[]{patternStart, x};
                } else {
                    patternStart += counters[0] + counters[1];
                    System.arraycopy(counters, 2, counters, 0, patternLength - 2);
                    counters[patternLength - 2] = 0;
                    counters[patternLength - 1] = 0;
                    counterPosition--;
                }
                counters[counterPosition] = 1;
                isWhite = isWhite != 0 ? 0 : 1;
            }
        }
        if (counterPosition != patternLength - 1 || patternMatchVariance(counters, pattern, MAX_INDIVIDUAL_VARIANCE) >= MAX_AVG_VARIANCE) {
            return null;
        }
        return new int[]{patternStart, x - 1};
    }

    private static int patternMatchVariance(int[] counters, int[] pattern, int maxIndividualVariance) {
        int numCounters = counters.length;
        int total = 0;
        int patternLength = 0;
        for (int i = 0; i < numCounters; i++) {
            total += counters[i];
            patternLength += pattern[i];
        }
        if (total < patternLength) {
            return Integer.MAX_VALUE;
        }
        int unitBarWidth = (total << INTEGER_MATH_SHIFT) / patternLength;
        maxIndividualVariance = (maxIndividualVariance * unitBarWidth) >> INTEGER_MATH_SHIFT;
        int totalVariance = 0;
        for (int x = 0; x < numCounters; x++) {
            int counter = counters[x] << INTEGER_MATH_SHIFT;
            int scaledPattern = pattern[x] * unitBarWidth;
            int variance = counter > scaledPattern ? counter - scaledPattern : scaledPattern - counter;
            if (variance > maxIndividualVariance) {
                return Integer.MAX_VALUE;
            }
            totalVariance += variance;
        }
        return totalVariance / total;
    }
}
