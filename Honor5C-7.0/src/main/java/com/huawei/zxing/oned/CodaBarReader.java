package com.huawei.zxing.oned;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitArray;
import java.util.Arrays;
import java.util.Map;

public final class CodaBarReader extends OneDReader {
    static final char[] ALPHABET = null;
    private static final String ALPHABET_STRING = "0123456789-$:/.+ABCD";
    static final int[] CHARACTER_ENCODINGS = null;
    private static final int MAX_ACCEPTABLE = 512;
    private static final int MIN_CHARACTER_LENGTH = 3;
    private static final int PADDING = 384;
    private static final char[] STARTEND_ENCODING = null;
    private int counterLength;
    private int[] counters;
    private final StringBuilder decodeRowResult;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.oned.CodaBarReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.oned.CodaBarReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.oned.CodaBarReader.<clinit>():void");
    }

    public CodaBarReader() {
        this.decodeRowResult = new StringBuilder(20);
        this.counters = new int[80];
        this.counterLength = 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException {
        int i;
        Arrays.fill(this.counters, 0);
        setCounters(row);
        int startOffset = findStartPattern();
        int nextStart = startOffset;
        this.decodeRowResult.setLength(0);
        while (true) {
            int charOffset = toNarrowWidePattern(nextStart);
            if (charOffset == -1) {
                break;
            }
            this.decodeRowResult.append((char) charOffset);
            nextStart += 8;
            if ((this.decodeRowResult.length() <= 1 || !arrayContains(STARTEND_ENCODING, ALPHABET[charOffset])) && nextStart < this.counterLength) {
            }
        }
        int trailingWhitespace = this.counters[nextStart - 1];
        int lastPatternSize = 0;
        for (i = -8; i < -1; i++) {
            lastPatternSize += this.counters[nextStart + i];
        }
        if (nextStart >= this.counterLength || trailingWhitespace >= lastPatternSize / 2) {
            validatePattern(startOffset);
            for (i = 0; i < this.decodeRowResult.length(); i++) {
                this.decodeRowResult.setCharAt(i, ALPHABET[this.decodeRowResult.charAt(i)]);
            }
            if (arrayContains(STARTEND_ENCODING, this.decodeRowResult.charAt(0))) {
                if (!arrayContains(STARTEND_ENCODING, this.decodeRowResult.charAt(this.decodeRowResult.length() - 1))) {
                    throw NotFoundException.getNotFoundInstance();
                } else if (this.decodeRowResult.length() <= MIN_CHARACTER_LENGTH) {
                    throw NotFoundException.getNotFoundInstance();
                } else {
                    if (hints != null) {
                    }
                    this.decodeRowResult.deleteCharAt(this.decodeRowResult.length() - 1);
                    this.decodeRowResult.deleteCharAt(0);
                    int runningCount = 0;
                    for (i = 0; i < startOffset; i++) {
                        runningCount += this.counters[i];
                    }
                    float left = (float) runningCount;
                    for (i = startOffset; i < nextStart - 1; i++) {
                        runningCount += this.counters[i];
                    }
                    float right = (float) runningCount;
                    String stringBuilder = this.decodeRowResult.toString();
                    r15 = new ResultPoint[2];
                    r15[0] = new ResultPoint(left, (float) rowNumber);
                    r15[1] = new ResultPoint(right, (float) rowNumber);
                    return new Result(stringBuilder, null, r15, BarcodeFormat.CODABAR);
                }
            }
            throw NotFoundException.getNotFoundInstance();
        }
        throw NotFoundException.getNotFoundInstance();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void validatePattern(int start) throws NotFoundException {
        int[] sizes = new int[]{0, 0, 0, 0};
        int[] counts = new int[]{0, 0, 0, 0};
        int end = this.decodeRowResult.length() - 1;
        int pos = start;
        int i = 0;
        while (true) {
            int j;
            int pattern = CHARACTER_ENCODINGS[this.decodeRowResult.charAt(i)];
            for (j = 6; j >= 0; j--) {
                int category = (j & 1) + ((pattern & 1) * 2);
                sizes[category] = sizes[category] + this.counters[pos + j];
                counts[category] = counts[category] + 1;
                pattern >>= 1;
            }
            if (i >= end) {
                break;
            }
            pos += 8;
            i++;
        }
        int[] maxes = new int[4];
        int[] mins = new int[4];
        for (i = 0; i < 2; i++) {
            mins[i] = 0;
            mins[i + 2] = (((sizes[i] << 8) / counts[i]) + ((sizes[i + 2] << 8) / counts[i + 2])) >> 1;
            maxes[i] = mins[i + 2];
            maxes[i + 2] = ((sizes[i + 2] * MAX_ACCEPTABLE) + PADDING) / counts[i + 2];
        }
        pos = start;
        i = 0;
        loop3:
        while (true) {
            pattern = CHARACTER_ENCODINGS[this.decodeRowResult.charAt(i)];
            j = 6;
            while (j >= 0) {
                category = (j & 1) + ((pattern & 1) * 2);
                int size = this.counters[pos + j] << 8;
                if (size >= mins[category] && size <= maxes[category]) {
                    pattern >>= 1;
                    j--;
                }
            }
            if (i < end) {
                pos += 8;
                i++;
            } else {
                return;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private void setCounters(BitArray row) throws NotFoundException {
        this.counterLength = 0;
        int i = row.getNextUnset(0);
        int end = row.getSize();
        if (i >= end) {
            throw NotFoundException.getNotFoundInstance();
        }
        boolean isWhite = true;
        int count = 0;
        while (i < end) {
            if ((row.get(i) ^ isWhite) != 0) {
                count++;
            } else {
                counterAppend(count);
                count = 1;
                isWhite = !isWhite;
            }
            i++;
        }
        counterAppend(count);
    }

    private void counterAppend(int e) {
        this.counters[this.counterLength] = e;
        this.counterLength++;
        if (this.counterLength >= this.counters.length) {
            int[] temp = new int[(this.counterLength * 2)];
            System.arraycopy(this.counters, 0, temp, 0, this.counterLength);
            this.counters = temp;
        }
    }

    private int findStartPattern() throws NotFoundException {
        int i = 1;
        while (i < this.counterLength) {
            int charOffset = toNarrowWidePattern(i);
            if (charOffset != -1 && arrayContains(STARTEND_ENCODING, ALPHABET[charOffset])) {
                int patternSize = 0;
                for (int j = i; j < i + 7; j++) {
                    patternSize += this.counters[j];
                }
                if (i == 1 || this.counters[i - 1] >= patternSize / 2) {
                    return i;
                }
            }
            i += 2;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    static boolean arrayContains(char[] array, char key) {
        if (array != null) {
            for (char c : array) {
                if (c == key) {
                    return true;
                }
            }
        }
        return false;
    }

    private int toNarrowWidePattern(int position) {
        int end = position + 7;
        if (end >= this.counterLength) {
            return -1;
        }
        int j;
        int i;
        int[] theCounters = this.counters;
        int maxBar = 0;
        int minBar = Integer.MAX_VALUE;
        for (j = position; j < end; j += 2) {
            int currentCounter = theCounters[j];
            if (currentCounter < minBar) {
                minBar = currentCounter;
            }
            if (currentCounter > maxBar) {
                maxBar = currentCounter;
            }
        }
        int thresholdBar = (minBar + maxBar) / 2;
        int maxSpace = 0;
        int minSpace = Integer.MAX_VALUE;
        for (j = position + 1; j < end; j += 2) {
            currentCounter = theCounters[j];
            if (currentCounter < minSpace) {
                minSpace = currentCounter;
            }
            if (currentCounter > maxSpace) {
                maxSpace = currentCounter;
            }
        }
        int thresholdSpace = (minSpace + maxSpace) / 2;
        int bitmask = AppOpsManagerEx.TYPE_MICROPHONE;
        int pattern = 0;
        for (i = 0; i < 7; i++) {
            bitmask >>= 1;
            if (theCounters[position + i] > ((i & 1) == 0 ? thresholdBar : thresholdSpace)) {
                pattern |= bitmask;
            }
        }
        for (i = 0; i < CHARACTER_ENCODINGS.length; i++) {
            if (CHARACTER_ENCODINGS[i] == pattern) {
                return i;
            }
        }
        return -1;
    }
}
