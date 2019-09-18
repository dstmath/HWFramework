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
                Map<ResultMetadataType, Object> resultMetadata = result.getResultMetadata();
                int orientation = 270;
                if (resultMetadata != null && resultMetadata.containsKey(ResultMetadataType.ORIENTATION)) {
                    orientation = (((Integer) resultMetadata.get(ResultMetadataType.ORIENTATION)).intValue() + 270) % 360;
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

    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00fb, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x013b, code lost:
        r15 = r22;
        r20 = r1;
        r17 = r2;
        r19 = r4;
     */
    private Result doDecode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException {
        int maxLines;
        int width;
        int middle;
        int height;
        int width2;
        int middle2;
        Map<DecodeHintType, ?> hints2;
        int height2;
        Map<DecodeHintType, ?> map = hints;
        int width3 = image.getWidth();
        int height3 = image.getHeight();
        BitArray row = new BitArray(width3);
        int middle3 = height3 >> 1;
        boolean z = true;
        boolean tryHarder = map != null && map.containsKey(DecodeHintType.TRY_HARDER);
        int rowStep = Math.max(1, height3 >> (tryHarder ? 8 : 5));
        if (tryHarder) {
            maxLines = height3;
        } else {
            maxLines = 15;
        }
        BitArray row2 = row;
        Map<DecodeHintType, ?> hints3 = map;
        int x = 0;
        loop0:
        while (true) {
            int x2 = x;
            if (x2 >= maxLines) {
                break;
            }
            int rowStepsAboveOrBelow = (x2 + 1) >> 1;
            int rowNumber = middle3 + (((x2 & 1) == 0 ? z : false ? rowStepsAboveOrBelow : -rowStepsAboveOrBelow) * rowStep);
            if (rowNumber >= 0) {
                if (rowNumber >= height3) {
                    BinaryBitmap binaryBitmap = image;
                    int i = width3;
                    int i2 = height3;
                    int i3 = middle3;
                    break;
                }
                try {
                    row2 = image.getBlackRow(rowNumber, row2);
                    int attempt = 0;
                    while (true) {
                        int attempt2 = attempt;
                        if (attempt2 >= 2) {
                            width = width3;
                            height = height3;
                            middle = middle3;
                            break;
                        }
                        if (attempt2 == z) {
                            row2.reverse();
                            if (hints3 != null && hints3.containsKey(DecodeHintType.NEED_RESULT_POINT_CALLBACK)) {
                                Map<DecodeHintType, ?> enumMap = new EnumMap<>(DecodeHintType.class);
                                enumMap.putAll(hints3);
                                enumMap.remove(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
                                hints3 = enumMap;
                            }
                        }
                        try {
                            Result result = decodeRow(rowNumber, row2, hints3);
                            height2 = height3;
                            if (attempt2 != 1) {
                                Map<DecodeHintType, ?> map2 = hints3;
                                int i4 = middle3;
                                break loop0;
                            }
                            try {
                                hints2 = hints3;
                                try {
                                    result.putMetadata(ResultMetadataType.ORIENTATION, 180);
                                    ResultPoint[] points = result.getResultPoints();
                                    if (points == null) {
                                        int i5 = middle3;
                                        break loop0;
                                    }
                                    middle2 = middle3;
                                    try {
                                        points[0] = new ResultPoint((((float) width3) - points[0].getX()) - 1.0f, points[0].getY());
                                        width2 = width3;
                                        z = true;
                                        try {
                                            points[1] = new ResultPoint((((float) width3) - points[1].getX()) - 1.0f, points[1].getY());
                                            break loop0;
                                        } catch (ReaderException e) {
                                        }
                                    } catch (ReaderException e2) {
                                        width2 = width3;
                                        z = true;
                                    }
                                } catch (ReaderException e3) {
                                    width2 = width3;
                                    middle2 = middle3;
                                    z = true;
                                }
                            } catch (ReaderException e4) {
                                width2 = width3;
                                hints2 = hints3;
                                middle2 = middle3;
                                z = true;
                            }
                        } catch (ReaderException e5) {
                            width2 = width3;
                            height2 = height3;
                            hints2 = hints3;
                            middle2 = middle3;
                            z = true;
                        }
                        attempt = attempt2 + 1;
                        height3 = height2;
                        hints3 = hints2;
                        middle3 = middle2;
                        width3 = width2;
                    }
                } catch (NotFoundException e6) {
                    width = width3;
                    height = height3;
                    middle = middle3;
                    NotFoundException notFoundException = e6;
                }
                x = x2 + 1;
                height3 = height;
                middle3 = middle;
                width3 = width;
            } else {
                break;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected static void recordPattern(BitArray row, int start, int[] counters) throws NotFoundException {
        int numCounters = counters.length;
        Arrays.fill(counters, 0, numCounters, 0);
        int end = row.getSize();
        if (start < end) {
            int counterPosition = 0;
            boolean isWhite = !row.get(start);
            int i = start;
            while (i < end) {
                if (row.get(i) ^ isWhite) {
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
            return;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected static void recordPatternInReverse(BitArray row, int start, int[] counters) throws NotFoundException {
        int numTransitionsLeft = counters.length;
        boolean last = row.get(start);
        while (start > 0 && numTransitionsLeft >= 0) {
            start--;
            if (row.get(start) != last) {
                numTransitionsLeft--;
                last = !last;
            }
        }
        if (numTransitionsLeft < 0) {
            recordPattern(row, start + 1, counters);
            return;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected static int patternMatchVariance(int[] counters, int[] pattern, int maxIndividualVariance) {
        int numCounters = counters.length;
        int patternLength = 0;
        int total = 0;
        for (int i = 0; i < numCounters; i++) {
            total += counters[i];
            patternLength += pattern[i];
        }
        if (total < patternLength) {
            return Integer.MAX_VALUE;
        }
        int unitBarWidth = (total << 8) / patternLength;
        int maxIndividualVariance2 = (maxIndividualVariance * unitBarWidth) >> 8;
        int totalVariance = 0;
        for (int x = 0; x < numCounters; x++) {
            int counter = counters[x] << 8;
            int scaledPattern = pattern[x] * unitBarWidth;
            int variance = counter > scaledPattern ? counter - scaledPattern : scaledPattern - counter;
            if (variance > maxIndividualVariance2) {
                return Integer.MAX_VALUE;
            }
            totalVariance += variance;
        }
        return totalVariance / total;
    }
}
