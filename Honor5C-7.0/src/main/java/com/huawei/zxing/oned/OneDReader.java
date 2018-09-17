package com.huawei.zxing.oned;

import com.huawei.zxing.BinaryBitmap;
import com.huawei.zxing.ChecksumException;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Reader;
import com.huawei.zxing.ReaderException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultMetadataType;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitArray;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public abstract class OneDReader implements Reader {
    protected static final int INTEGER_MATH_SHIFT = 8;
    protected static final int PATTERN_MATCH_RESULT_SCALE_FACTOR = 256;

    public abstract Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException;

    public Result decode(BinaryBitmap image) throws NotFoundException, FormatException {
        return decode(image, null);
    }

    public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException, FormatException {
        try {
            return doDecode(image, hints);
        } catch (NotFoundException nfe) {
            if (image.isRotateSupported()) {
                BinaryBitmap rotatedImage = image.rotateCounterClockwise();
                Result result = doDecode(rotatedImage, hints);
                Map<ResultMetadataType, ?> metadata = result.getResultMetadata();
                int orientation = 270;
                if (metadata != null && metadata.containsKey(ResultMetadataType.ORIENTATION)) {
                    orientation = (((Integer) metadata.get(ResultMetadataType.ORIENTATION)).intValue() + 270) % 360;
                }
                result.putMetadata(ResultMetadataType.ORIENTATION, Integer.valueOf(orientation));
                ResultPoint[] points = result.getResultPoints();
                if (points != null) {
                    int height = rotatedImage.getHeight();
                    for (int i = 0; i < points.length; i++) {
                        points[i] = new ResultPoint((((float) height) - points[i].getY()) - 1.0f, points[i].getX());
                    }
                }
                return result;
            }
            throw nfe;
        }
    }

    public void reset() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Result doDecode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException {
        int maxLines;
        int width = image.getWidth();
        int height = image.getHeight();
        BitArray row = new BitArray(width);
        int middle = height >> 1;
        boolean tryHarder = hints != null ? hints.containsKey(DecodeHintType.TRY_HARDER) : false;
        int rowStep = Math.max(1, height >> (tryHarder ? INTEGER_MATH_SHIFT : 5));
        if (tryHarder) {
            maxLines = height;
        } else {
            maxLines = 15;
        }
        int x = 0;
        while (x < maxLines) {
            int rowStepsAboveOrBelow = (x + 1) >> 1;
            if (!((x & 1) == 0)) {
                rowStepsAboveOrBelow = -rowStepsAboveOrBelow;
            }
            int rowNumber = middle + (rowStep * rowStepsAboveOrBelow);
            if (rowNumber >= 0 && rowNumber < height) {
                try {
                    row = image.getBlackRow(rowNumber, row);
                    int attempt = 0;
                    while (attempt < 2) {
                        if (attempt == 1) {
                            row.reverse();
                            if (hints != null) {
                                if (hints.containsKey(DecodeHintType.NEED_RESULT_POINT_CALLBACK)) {
                                    Map<DecodeHintType, Object> newHints = new EnumMap(DecodeHintType.class);
                                    newHints.putAll(hints);
                                    newHints.remove(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
                                    hints = newHints;
                                }
                            }
                        }
                        try {
                            Result result = decodeRow(rowNumber, row, hints);
                            if (attempt == 1) {
                                result.putMetadata(ResultMetadataType.ORIENTATION, Integer.valueOf(180));
                                ResultPoint[] points = result.getResultPoints();
                                if (points != null) {
                                    points[0] = new ResultPoint((((float) width) - points[0].getX()) - 1.0f, points[0].getY());
                                    points[1] = new ResultPoint((((float) width) - points[1].getX()) - 1.0f, points[1].getY());
                                }
                            }
                            return result;
                        } catch (ReaderException e) {
                            attempt++;
                        }
                    }
                    continue;
                } catch (NotFoundException e2) {
                }
                x++;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected static void recordPattern(BitArray row, int start, int[] counters) throws NotFoundException {
        int numCounters = counters.length;
        Arrays.fill(counters, 0, numCounters, 0);
        int end = row.getSize();
        if (start >= end) {
            throw NotFoundException.getNotFoundInstance();
        }
        boolean isWhite = !row.get(start);
        int counterPosition = 0;
        int i = start;
        while (i < end) {
            if ((row.get(i) ^ isWhite) != 0) {
                counters[counterPosition] = counters[counterPosition] + 1;
            } else {
                counterPosition++;
                if (counterPosition == numCounters) {
                    break;
                }
                counters[counterPosition] = 1;
                isWhite = !isWhite;
            }
            i++;
        }
        if (counterPosition == numCounters) {
            return;
        }
        if (counterPosition != numCounters - 1 || i != end) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    protected static void recordPatternInReverse(BitArray row, int start, int[] counters) throws NotFoundException {
        int numTransitionsLeft = counters.length;
        boolean z = row.get(start);
        while (start > 0 && numTransitionsLeft >= 0) {
            start--;
            if (row.get(start) != z) {
                numTransitionsLeft--;
                z = !z;
            }
        }
        if (numTransitionsLeft >= 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        recordPattern(row, start + 1, counters);
    }

    protected static int patternMatchVariance(int[] counters, int[] pattern, int maxIndividualVariance) {
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
