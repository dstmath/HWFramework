package com.huawei.zxing.oned;

import com.huawei.android.telephony.SignalStrengthEx;
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

    @Override // com.huawei.zxing.Reader
    public Result decode(BinaryBitmap image) throws NotFoundException, FormatException {
        return decode(image, null);
    }

    @Override // com.huawei.zxing.Reader
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

    @Override // com.huawei.zxing.Reader
    public void reset() {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0104, code lost:
        return r0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x008a A[SYNTHETIC, Splitter:B:44:0x008a] */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x00fe A[SYNTHETIC] */
    private Result doDecode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException {
        int maxLines;
        int middle;
        int height;
        int middle2;
        Map<DecodeHintType, Object> newHints;
        BitArray row;
        Map<DecodeHintType, Object> hints2;
        int middle3;
        int height2;
        int middle4;
        int width = image.getWidth();
        int height3 = image.getHeight();
        BitArray row2 = new BitArray(width);
        int middle5 = height3 >> 1;
        int i = 1;
        boolean tryHarder = hints != null && hints.containsKey(DecodeHintType.TRY_HARDER);
        int rowStep = Math.max(1, height3 >> (tryHarder ? 8 : 5));
        if (tryHarder) {
            maxLines = height3;
        } else {
            maxLines = 15;
        }
        int x = 0;
        Map<DecodeHintType, Object> hints3 = hints;
        loop0:
        while (true) {
            if (x >= maxLines) {
                break;
            }
            int rowStepsAboveOrBelow = (x + 1) >> 1;
            int rowNumber = middle5 + ((((x & 1) == 0 ? i : 0) != 0 ? rowStepsAboveOrBelow : -rowStepsAboveOrBelow) * rowStep);
            if (rowNumber >= 0) {
                if (rowNumber >= height3) {
                    break;
                }
                try {
                    row2 = image.getBlackRow(rowNumber, row2);
                    int attempt = 0;
                    Map<DecodeHintType, Object> hints4 = hints3;
                    while (attempt < 2) {
                        if (attempt == i) {
                            row2.reverse();
                            if (hints4 != null && hints4.containsKey(DecodeHintType.NEED_RESULT_POINT_CALLBACK)) {
                                newHints = new EnumMap<>(DecodeHintType.class);
                                newHints.putAll(hints4);
                                newHints.remove(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
                                Result result = decodeRow(rowNumber, row2, newHints);
                                height2 = height3;
                                if (attempt == 1) {
                                    break loop0;
                                }
                                try {
                                    row = row2;
                                } catch (ReaderException e) {
                                    row = row2;
                                    middle3 = middle5;
                                    hints2 = newHints;
                                    middle4 = 1;
                                    attempt++;
                                    row2 = row;
                                    i = middle4;
                                    height3 = height2;
                                    middle5 = middle3;
                                    hints4 = hints2;
                                }
                                try {
                                    result.putMetadata(ResultMetadataType.ORIENTATION, 180);
                                    ResultPoint[] points = result.getResultPoints();
                                    if (points == null) {
                                        break loop0;
                                    }
                                    middle3 = middle5;
                                    try {
                                        hints2 = newHints;
                                        try {
                                            points[0] = new ResultPoint((((float) width) - points[0].getX()) - 1.0f, points[0].getY());
                                        } catch (ReaderException e2) {
                                            middle4 = 1;
                                            attempt++;
                                            row2 = row;
                                            i = middle4;
                                            height3 = height2;
                                            middle5 = middle3;
                                            hints4 = hints2;
                                        }
                                    } catch (ReaderException e3) {
                                        hints2 = newHints;
                                        middle4 = 1;
                                        attempt++;
                                        row2 = row;
                                        i = middle4;
                                        height3 = height2;
                                        middle5 = middle3;
                                        hints4 = hints2;
                                    }
                                    try {
                                        middle4 = 1;
                                        try {
                                            points[1] = new ResultPoint((((float) width) - points[1].getX()) - 1.0f, points[1].getY());
                                            break loop0;
                                        } catch (ReaderException e4) {
                                        }
                                    } catch (ReaderException e5) {
                                        middle4 = 1;
                                        attempt++;
                                        row2 = row;
                                        i = middle4;
                                        height3 = height2;
                                        middle5 = middle3;
                                        hints4 = hints2;
                                    }
                                } catch (ReaderException e6) {
                                    middle3 = middle5;
                                    hints2 = newHints;
                                    middle4 = 1;
                                    attempt++;
                                    row2 = row;
                                    i = middle4;
                                    height3 = height2;
                                    middle5 = middle3;
                                    hints4 = hints2;
                                }
                            }
                        }
                        newHints = hints4;
                        try {
                            Result result2 = decodeRow(rowNumber, row2, newHints);
                            height2 = height3;
                            if (attempt == 1) {
                            }
                        } catch (ReaderException e7) {
                            height2 = height3;
                            row = row2;
                            middle3 = middle5;
                            hints2 = newHints;
                            middle4 = 1;
                            attempt++;
                            row2 = row;
                            i = middle4;
                            height3 = height2;
                            middle5 = middle3;
                            hints4 = hints2;
                        }
                    }
                    height = height3;
                    middle = middle5;
                    middle2 = i;
                    hints3 = hints4;
                } catch (NotFoundException e8) {
                    height = height3;
                    middle = middle5;
                    middle2 = i;
                }
                x++;
                i = middle2;
                height3 = height;
                middle5 = middle;
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
            boolean isWhite = !row.get(start);
            int counterPosition = 0;
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
        int total = 0;
        int patternLength = 0;
        for (int i = 0; i < numCounters; i++) {
            total += counters[i];
            patternLength += pattern[i];
        }
        if (total < patternLength) {
            return SignalStrengthEx.INVALID;
        }
        int unitBarWidth = (total << 8) / patternLength;
        int maxIndividualVariance2 = (maxIndividualVariance * unitBarWidth) >> 8;
        int totalVariance = 0;
        for (int x = 0; x < numCounters; x++) {
            int counter = counters[x] << 8;
            int scaledPattern = pattern[x] * unitBarWidth;
            int variance = counter > scaledPattern ? counter - scaledPattern : scaledPattern - counter;
            if (variance > maxIndividualVariance2) {
                return SignalStrengthEx.INVALID;
            }
            totalVariance += variance;
        }
        return totalVariance / total;
    }
}
