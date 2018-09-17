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
    static final int[][] L_AND_G_PATTERNS = null;
    static final int[][] L_PATTERNS = null;
    private static final int MAX_AVG_VARIANCE = 122;
    private static final int MAX_INDIVIDUAL_VARIANCE = 179;
    static final int[] MIDDLE_PATTERN = null;
    static final int[] START_END_PATTERN = null;
    private final StringBuilder decodeRowStringBuffer;
    private final EANManufacturerOrgSupport eanManSupport;
    private final UPCEANExtensionSupport extensionReader;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.oned.UPCEANReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.oned.UPCEANReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.oned.UPCEANReader.<clinit>():void");
    }

    protected abstract int decodeMiddle(BitArray bitArray, int[] iArr, StringBuilder stringBuilder) throws NotFoundException;

    abstract BarcodeFormat getBarcodeFormat();

    protected UPCEANReader() {
        this.decodeRowStringBuffer = new StringBuilder(20);
        this.extensionReader = new UPCEANExtensionSupport();
        this.eanManSupport = new EANManufacturerOrgSupport();
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
        ResultPointCallback resultPointCallback;
        if (hints == null) {
            resultPointCallback = null;
        } else {
            resultPointCallback = (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
        }
        if (resultPointCallback != null) {
            float f = (float) rowNumber;
            resultPointCallback.foundPossibleResultPoint(new ResultPoint(((float) (startGuardRange[0] + startGuardRange[1])) / 2.0f, r0));
        }
        StringBuilder result = this.decodeRowStringBuffer;
        result.setLength(0);
        int endStart = decodeMiddle(row, startGuardRange, result);
        if (resultPointCallback != null) {
            resultPointCallback.foundPossibleResultPoint(new ResultPoint((float) endStart, (float) rowNumber));
        }
        int[] endRange = decodeEnd(row, endStart);
        if (resultPointCallback != null) {
            f = (float) rowNumber;
            resultPointCallback.foundPossibleResultPoint(new ResultPoint(((float) (endRange[0] + endRange[1])) / 2.0f, r0));
        }
        int end = endRange[1];
        int quietEnd = end + (end - endRange[0]);
        if (quietEnd >= row.getSize() || !row.isRange(end, quietEnd, false)) {
            throw NotFoundException.getNotFoundInstance();
        }
        String resultString = result.toString();
        if (resultString.length() < 8) {
            throw FormatException.getFormatInstance();
        } else if (checkChecksum(resultString)) {
            float left = ((float) (startGuardRange[1] + startGuardRange[0])) / 2.0f;
            float right = ((float) (endRange[1] + endRange[0])) / 2.0f;
            BarcodeFormat format = getBarcodeFormat();
            r18 = new ResultPoint[2];
            r18[0] = new ResultPoint(left, (float) rowNumber);
            r18[1] = new ResultPoint(right, (float) rowNumber);
            Result decodeResult = new Result(resultString, null, r18, format);
            try {
                Result extensionResult = this.extensionReader.decodeRow(rowNumber, row, endRange[1]);
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

    boolean checkChecksum(String s) throws ChecksumException, FormatException {
        return checkStandardUPCEANChecksum(s);
    }

    static boolean checkStandardUPCEANChecksum(CharSequence s) throws FormatException {
        boolean z = false;
        int length = s.length();
        if (length == 0) {
            return false;
        }
        int i;
        int sum = 0;
        for (i = length - 2; i >= 0; i -= 2) {
            int digit = s.charAt(i) - 48;
            if (digit < 0 || digit > 9) {
                throw FormatException.getFormatInstance();
            }
            sum += digit;
        }
        sum *= 3;
        for (i = length - 1; i >= 0; i -= 2) {
            digit = s.charAt(i) - 48;
            if (digit < 0 || digit > 9) {
                throw FormatException.getFormatInstance();
            }
            sum += digit;
        }
        if (sum % 10 == 0) {
            z = true;
        }
        return z;
    }

    int[] decodeEnd(BitArray row, int endStart) throws NotFoundException {
        return findGuardPattern(row, endStart, false, START_END_PATTERN);
    }

    static int[] findGuardPattern(BitArray row, int rowOffset, boolean whiteFirst, int[] pattern) throws NotFoundException {
        return findGuardPattern(row, rowOffset, whiteFirst, pattern, new int[pattern.length]);
    }

    private static int[] findGuardPattern(BitArray row, int rowOffset, boolean whiteFirst, int[] pattern, int[] counters) throws NotFoundException {
        int patternLength = pattern.length;
        int width = row.getSize();
        int isWhite = whiteFirst;
        rowOffset = whiteFirst ? row.getNextUnset(rowOffset) : row.getNextSet(rowOffset);
        int counterPosition = 0;
        int patternStart = rowOffset;
        for (int x = rowOffset; x < width; x++) {
            if ((row.get(x) ^ isWhite) != 0) {
                counters[counterPosition] = counters[counterPosition] + 1;
            } else {
                if (counterPosition != patternLength - 1) {
                    counterPosition++;
                } else if (OneDReader.patternMatchVariance(counters, pattern, MAX_INDIVIDUAL_VARIANCE) < MAX_AVG_VARIANCE) {
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
        throw NotFoundException.getNotFoundInstance();
    }

    static int decodeDigit(BitArray row, int[] counters, int rowOffset, int[][] patterns) throws NotFoundException {
        OneDReader.recordPattern(row, rowOffset, counters);
        int bestVariance = MAX_AVG_VARIANCE;
        int bestMatch = -1;
        int max = patterns.length;
        for (int i = 0; i < max; i++) {
            int variance = OneDReader.patternMatchVariance(counters, patterns[i], MAX_INDIVIDUAL_VARIANCE);
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
