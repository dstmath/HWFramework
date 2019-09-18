package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.ChecksumException;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ReaderException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultMetadataType;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.ResultPointCallback;
import com.huawei.zxing.common.BitArray;
import java.util.Arrays;
import java.util.Map;

public abstract class UPCEANReader extends OneDReader {
    static final int[][] L_AND_G_PATTERNS = new int[20][];
    static final int[][] L_PATTERNS = {new int[]{3, 2, 1, 1}, new int[]{2, 2, 2, 1}, new int[]{2, 1, 2, 2}, new int[]{1, 4, 1, 1}, new int[]{1, 1, 3, 2}, new int[]{1, 2, 3, 1}, new int[]{1, 1, 1, 4}, new int[]{1, 3, 1, 2}, new int[]{1, 2, 1, 3}, new int[]{3, 1, 1, 2}};
    private static final int MAX_AVG_VARIANCE = 122;
    private static final int MAX_INDIVIDUAL_VARIANCE = 179;
    static final int[] MIDDLE_PATTERN = {1, 1, 1, 1, 1};
    static final int[] START_END_PATTERN = {1, 1, 1};
    private final StringBuilder decodeRowStringBuffer = new StringBuilder(20);
    private final EANManufacturerOrgSupport eanManSupport = new EANManufacturerOrgSupport();
    private final UPCEANExtensionSupport extensionReader = new UPCEANExtensionSupport();

    /* access modifiers changed from: protected */
    public abstract int decodeMiddle(BitArray bitArray, int[] iArr, StringBuilder sb) throws NotFoundException;

    /* access modifiers changed from: package-private */
    public abstract BarcodeFormat getBarcodeFormat();

    static {
        int i = 10;
        System.arraycopy(L_PATTERNS, 0, L_AND_G_PATTERNS, 0, 10);
        while (true) {
            int i2 = i;
            if (i2 < 20) {
                int[] widths = L_PATTERNS[i2 - 10];
                int[] reversedWidths = new int[widths.length];
                for (int j = 0; j < widths.length; j++) {
                    reversedWidths[j] = widths[(widths.length - j) - 1];
                }
                L_AND_G_PATTERNS[i2] = reversedWidths;
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    protected UPCEANReader() {
    }

    static int[] findStartGuardPattern(BitArray row) throws NotFoundException {
        boolean foundStart = false;
        int[] startRange = null;
        int nextStart = 0;
        int[] counters = new int[START_END_PATTERN.length];
        while (!foundStart) {
            Arrays.fill(counters, 0, START_END_PATTERN.length, 0);
            startRange = findGuardPattern(row, nextStart, false, START_END_PATTERN, counters);
            int start = startRange[0];
            nextStart = startRange[1];
            int quietStart = start - (nextStart - start);
            if (quietStart >= 0) {
                foundStart = row.isRange(quietStart, start, false);
            }
        }
        return startRange;
    }

    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException, ChecksumException, FormatException {
        return decodeRow(rowNumber, row, findStartGuardPattern(row), hints);
    }

    public Result decodeRow(int rowNumber, BitArray row, int[] startGuardRange, Map<DecodeHintType, ?> hints) throws NotFoundException, ChecksumException, FormatException {
        int i = rowNumber;
        BitArray bitArray = row;
        int[] iArr = startGuardRange;
        Map<DecodeHintType, ?> map = hints;
        ResultPointCallback resultPointCallback = map == null ? null : (ResultPointCallback) map.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
        if (resultPointCallback != null) {
            resultPointCallback.foundPossibleResultPoint(new ResultPoint(((float) (iArr[0] + iArr[1])) / 2.0f, (float) i));
        }
        StringBuilder result = this.decodeRowStringBuffer;
        result.setLength(0);
        int endStart = decodeMiddle(bitArray, iArr, result);
        if (resultPointCallback != null) {
            resultPointCallback.foundPossibleResultPoint(new ResultPoint((float) endStart, (float) i));
        }
        int[] endRange = decodeEnd(bitArray, endStart);
        if (resultPointCallback != null) {
            resultPointCallback.foundPossibleResultPoint(new ResultPoint(((float) (endRange[0] + endRange[1])) / 2.0f, (float) i));
        }
        int end = endRange[1];
        int quietEnd = (end - endRange[0]) + end;
        if (quietEnd >= row.getSize() || !bitArray.isRange(end, quietEnd, false)) {
            throw NotFoundException.getNotFoundInstance();
        }
        String resultString = result.toString();
        if (resultString.length() < 8) {
            throw FormatException.getFormatInstance();
        } else if (checkChecksum(resultString)) {
            float left = ((float) (iArr[1] + iArr[0])) / 2.0f;
            BarcodeFormat format = getBarcodeFormat();
            ResultPointCallback resultPointCallback2 = resultPointCallback;
            Result decodeResult = new Result(resultString, null, new ResultPoint[]{new ResultPoint(left, (float) i), new ResultPoint(((float) (endRange[1] + endRange[0])) / 2.0f, (float) i)}, format);
            try {
                Result extensionResult = this.extensionReader.decodeRow(i, bitArray, endRange[1]);
                decodeResult.putMetadata(ResultMetadataType.UPC_EAN_EXTENSION, extensionResult.getText());
                decodeResult.putAllMetadata(extensionResult.getResultMetadata());
                decodeResult.addResultPoints(extensionResult.getResultPoints());
            } catch (ReaderException e) {
            }
            if (format == BarcodeFormat.EAN_13 || format == BarcodeFormat.UPC_A) {
                String countryID = this.eanManSupport.lookupCountryIdentifier(resultString);
                if (countryID != null) {
                    decodeResult.putMetadata(ResultMetadataType.POSSIBLE_COUNTRY, countryID);
                }
            }
            return decodeResult;
        } else {
            throw ChecksumException.getChecksumInstance();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkChecksum(String s) throws ChecksumException, FormatException {
        return checkStandardUPCEANChecksum(s);
    }

    static boolean checkStandardUPCEANChecksum(CharSequence s) throws FormatException {
        int length = s.length();
        boolean z = false;
        if (length == 0) {
            return false;
        }
        int sum = 0;
        for (int i = length - 2; i >= 0; i -= 2) {
            int digit = s.charAt(i) - '0';
            if (digit < 0 || digit > 9) {
                throw FormatException.getFormatInstance();
            }
            sum += digit;
        }
        int sum2 = sum * 3;
        for (int i2 = length - 1; i2 >= 0; i2 -= 2) {
            int digit2 = s.charAt(i2) - '0';
            if (digit2 < 0 || digit2 > 9) {
                throw FormatException.getFormatInstance();
            }
            sum2 += digit2;
        }
        if (sum2 % 10 == 0) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public int[] decodeEnd(BitArray row, int endStart) throws NotFoundException {
        return findGuardPattern(row, endStart, false, START_END_PATTERN);
    }

    static int[] findGuardPattern(BitArray row, int rowOffset, boolean whiteFirst, int[] pattern) throws NotFoundException {
        return findGuardPattern(row, rowOffset, whiteFirst, pattern, new int[pattern.length]);
    }

    private static int[] findGuardPattern(BitArray row, int rowOffset, boolean whiteFirst, int[] pattern, int[] counters) throws NotFoundException {
        int patternLength = pattern.length;
        int width = row.getSize();
        boolean isWhite = whiteFirst;
        int rowOffset2 = whiteFirst ? row.getNextUnset(rowOffset) : row.getNextSet(rowOffset);
        int patternStart = rowOffset2;
        int counterPosition = 0;
        boolean isWhite2 = isWhite;
        for (int x = rowOffset2; x < width; x++) {
            boolean z = true;
            if (row.get(x) ^ isWhite2) {
                counters[counterPosition] = counters[counterPosition] + 1;
            } else {
                if (counterPosition != patternLength - 1) {
                    counterPosition++;
                } else if (patternMatchVariance(counters, pattern, MAX_INDIVIDUAL_VARIANCE) < 122) {
                    return new int[]{patternStart, x};
                } else {
                    patternStart += counters[0] + counters[1];
                    System.arraycopy(counters, 2, counters, 0, patternLength - 2);
                    counters[patternLength - 2] = 0;
                    counters[patternLength - 1] = 0;
                    counterPosition--;
                }
                counters[counterPosition] = 1;
                if (isWhite2) {
                    z = false;
                }
                isWhite2 = z;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    static int decodeDigit(BitArray row, int[] counters, int rowOffset, int[][] patterns) throws NotFoundException {
        recordPattern(row, rowOffset, counters);
        int bestVariance = 122;
        int bestMatch = -1;
        int max = patterns.length;
        for (int i = 0; i < max; i++) {
            int variance = patternMatchVariance(counters, patterns[i], MAX_INDIVIDUAL_VARIANCE);
            if (variance < bestVariance) {
                bestVariance = variance;
                bestMatch = i;
            }
        }
        if (bestMatch >= 0) {
            return bestMatch;
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
