package com.huawei.zxing.oned.rss.expanded;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitArray;
import com.huawei.zxing.oned.OneDReader;
import com.huawei.zxing.oned.rss.AbstractRSSReader;
import com.huawei.zxing.oned.rss.DataCharacter;
import com.huawei.zxing.oned.rss.FinderPattern;
import com.huawei.zxing.oned.rss.expanded.decoders.AbstractExpandedDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class RSSExpandedReader extends AbstractRSSReader {
    private static final int[] EVEN_TOTAL_SUBSET = null;
    private static final int[][] FINDER_PATTERNS = null;
    private static final int[][] FINDER_PATTERN_SEQUENCES = null;
    private static final int FINDER_PAT_A = 0;
    private static final int FINDER_PAT_B = 1;
    private static final int FINDER_PAT_C = 2;
    private static final int FINDER_PAT_D = 3;
    private static final int FINDER_PAT_E = 4;
    private static final int FINDER_PAT_F = 5;
    private static final int[] GSUM = null;
    private static final int MAX_PAIRS = 11;
    private static final int[] SYMBOL_WIDEST = null;
    private static final int[][] WEIGHTS = null;
    private final List<ExpandedPair> pairs;
    private final List<ExpandedRow> rows;
    private final int[] startEnd;
    private boolean startFromEven;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.oned.rss.expanded.RSSExpandedReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.oned.rss.expanded.RSSExpandedReader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.oned.rss.expanded.RSSExpandedReader.<clinit>():void");
    }

    public RSSExpandedReader() {
        this.pairs = new ArrayList(MAX_PAIRS);
        this.rows = new ArrayList();
        this.startEnd = new int[FINDER_PAT_C];
        this.startFromEven = false;
    }

    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> map) throws NotFoundException, FormatException {
        this.pairs.clear();
        this.startFromEven = false;
        try {
            return constructResult(decodeRow2pairs(rowNumber, row));
        } catch (NotFoundException e) {
            this.pairs.clear();
            this.startFromEven = true;
            return constructResult(decodeRow2pairs(rowNumber, row));
        }
    }

    public void reset() {
        this.pairs.clear();
        this.rows.clear();
    }

    List<ExpandedPair> decodeRow2pairs(int rowNumber, BitArray row) throws NotFoundException {
        while (true) {
            try {
                this.pairs.add(retrieveNextPair(row, this.pairs, rowNumber));
            } catch (NotFoundException nfe) {
                if (this.pairs.isEmpty()) {
                    throw nfe;
                } else if (checkChecksum()) {
                    return this.pairs;
                } else {
                    boolean tryStackedDecode = !this.rows.isEmpty();
                    storeRow(rowNumber, false);
                    if (tryStackedDecode) {
                        List<ExpandedPair> ps = checkRows(false);
                        if (ps != null) {
                            return ps;
                        }
                        ps = checkRows(true);
                        if (ps != null) {
                            return ps;
                        }
                    }
                    throw NotFoundException.getNotFoundInstance();
                }
            }
        }
    }

    private List<ExpandedPair> checkRows(boolean reverse) {
        if (this.rows.size() > 25) {
            this.rows.clear();
            return null;
        }
        this.pairs.clear();
        if (reverse) {
            Collections.reverse(this.rows);
        }
        List<ExpandedPair> ps = null;
        try {
            ps = checkRows(new ArrayList(), FINDER_PAT_A);
        } catch (NotFoundException e) {
        }
        if (reverse) {
            Collections.reverse(this.rows);
        }
        return ps;
    }

    private List<ExpandedPair> checkRows(List<ExpandedRow> collectedRows, int currentRow) throws NotFoundException {
        for (int i = currentRow; i < this.rows.size(); i += FINDER_PAT_B) {
            ExpandedRow row = (ExpandedRow) this.rows.get(i);
            this.pairs.clear();
            int size = collectedRows.size();
            for (int j = FINDER_PAT_A; j < size; j += FINDER_PAT_B) {
                this.pairs.addAll(((ExpandedRow) collectedRows.get(j)).getPairs());
            }
            this.pairs.addAll(row.getPairs());
            if (isValidSequence(this.pairs)) {
                if (checkChecksum()) {
                    return this.pairs;
                }
                List<ExpandedRow> rs = new ArrayList();
                rs.addAll(collectedRows);
                rs.add(row);
                try {
                    return checkRows(rs, i + FINDER_PAT_B);
                } catch (NotFoundException e) {
                }
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static boolean isValidSequence(List<ExpandedPair> pairs) {
        int[][] iArr = FINDER_PATTERN_SEQUENCES;
        int length = iArr.length;
        for (int i = FINDER_PAT_A; i < length; i += FINDER_PAT_B) {
            int[] sequence = iArr[i];
            if (pairs.size() <= sequence.length) {
                boolean stop = true;
                for (int j = FINDER_PAT_A; j < pairs.size(); j += FINDER_PAT_B) {
                    if (((ExpandedPair) pairs.get(j)).getFinderPattern().getValue() != sequence[j]) {
                        stop = false;
                        break;
                    }
                }
                if (stop) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void storeRow(int rowNumber, boolean wasReversed) {
        int insertPos = FINDER_PAT_A;
        boolean prevIsSame = false;
        boolean nextIsSame = false;
        while (insertPos < this.rows.size()) {
            ExpandedRow erow = (ExpandedRow) this.rows.get(insertPos);
            if (erow.getRowNumber() > rowNumber) {
                nextIsSame = erow.isEquivalent(this.pairs);
                break;
            } else {
                prevIsSame = erow.isEquivalent(this.pairs);
                insertPos += FINDER_PAT_B;
            }
        }
        if (!nextIsSame && !r3 && !isPartialRow(this.pairs, this.rows)) {
            this.rows.add(insertPos, new ExpandedRow(this.pairs, rowNumber, wasReversed));
            removePartialRows(this.pairs, this.rows);
        }
    }

    private static void removePartialRows(List<ExpandedPair> pairs, List<ExpandedRow> rows) {
        Iterator<ExpandedRow> iterator = rows.iterator();
        while (iterator.hasNext()) {
            ExpandedRow r = (ExpandedRow) iterator.next();
            if (r.getPairs().size() != pairs.size()) {
                boolean allFound = true;
                for (ExpandedPair p : r.getPairs()) {
                    boolean found = false;
                    for (ExpandedPair pp : pairs) {
                        if (p.equals(pp)) {
                            found = true;
                            break;
                            continue;
                        }
                    }
                    if (!found) {
                        allFound = false;
                        break;
                    }
                }
                if (allFound) {
                    iterator.remove();
                }
            }
        }
    }

    private static boolean isPartialRow(Iterable<ExpandedPair> pairs, Iterable<ExpandedRow> rows) {
        for (ExpandedRow r : rows) {
            boolean allFound = true;
            for (ExpandedPair p : pairs) {
                boolean found = false;
                for (ExpandedPair pp : r.getPairs()) {
                    if (p.equals(pp)) {
                        found = true;
                        break;
                        continue;
                    }
                }
                if (!found) {
                    allFound = false;
                    break;
                    continue;
                }
            }
            if (allFound) {
                return true;
            }
        }
        return false;
    }

    List<ExpandedRow> getRows() {
        return this.rows;
    }

    static Result constructResult(List<ExpandedPair> pairs) throws NotFoundException, FormatException {
        String resultingString = AbstractExpandedDecoder.createDecoder(BitArrayBuilder.buildBitArray(pairs)).parseInformation();
        ResultPoint[] firstPoints = ((ExpandedPair) pairs.get(FINDER_PAT_A)).getFinderPattern().getResultPoints();
        ResultPoint[] lastPoints = ((ExpandedPair) pairs.get(pairs.size() - 1)).getFinderPattern().getResultPoints();
        ResultPoint[] resultPointArr = new ResultPoint[FINDER_PAT_E];
        resultPointArr[FINDER_PAT_A] = firstPoints[FINDER_PAT_A];
        resultPointArr[FINDER_PAT_B] = firstPoints[FINDER_PAT_B];
        resultPointArr[FINDER_PAT_C] = lastPoints[FINDER_PAT_A];
        resultPointArr[FINDER_PAT_D] = lastPoints[FINDER_PAT_B];
        return new Result(resultingString, null, resultPointArr, BarcodeFormat.RSS_EXPANDED);
    }

    private boolean checkChecksum() {
        boolean z = false;
        ExpandedPair firstPair = (ExpandedPair) this.pairs.get(FINDER_PAT_A);
        DataCharacter checkCharacter = firstPair.getLeftChar();
        DataCharacter firstCharacter = firstPair.getRightChar();
        if (firstCharacter == null) {
            return false;
        }
        int checksum = firstCharacter.getChecksumPortion();
        int s = FINDER_PAT_C;
        for (int i = FINDER_PAT_B; i < this.pairs.size(); i += FINDER_PAT_B) {
            ExpandedPair currentPair = (ExpandedPair) this.pairs.get(i);
            checksum += currentPair.getLeftChar().getChecksumPortion();
            s += FINDER_PAT_B;
            DataCharacter currentRightChar = currentPair.getRightChar();
            if (currentRightChar != null) {
                checksum += currentRightChar.getChecksumPortion();
                s += FINDER_PAT_B;
            }
        }
        if (((s - 4) * 211) + (checksum % 211) == checkCharacter.getValue()) {
            z = true;
        }
        return z;
    }

    private static int getNextSecondBar(BitArray row, int initialPos) {
        if (row.get(initialPos)) {
            return row.getNextSet(row.getNextUnset(initialPos));
        }
        return row.getNextUnset(row.getNextSet(initialPos));
    }

    ExpandedPair retrieveNextPair(BitArray row, List<ExpandedPair> previousPairs, int rowNumber) throws NotFoundException {
        FinderPattern pattern;
        boolean isOddPattern = previousPairs.size() % FINDER_PAT_C == 0;
        if (this.startFromEven) {
            isOddPattern = !isOddPattern;
        }
        boolean keepFinding = true;
        int forcedOffset = -1;
        do {
            findNextPair(row, previousPairs, forcedOffset);
            pattern = parseFoundFinderPattern(row, rowNumber, isOddPattern);
            if (pattern == null) {
                forcedOffset = getNextSecondBar(row, this.startEnd[FINDER_PAT_A]);
                continue;
            } else {
                keepFinding = false;
                continue;
            }
        } while (keepFinding);
        DataCharacter leftChar = decodeDataCharacter(row, pattern, isOddPattern, true);
        if (previousPairs.isEmpty() || !((ExpandedPair) previousPairs.get(previousPairs.size() - 1)).mustBeLast()) {
            DataCharacter decodeDataCharacter;
            try {
                decodeDataCharacter = decodeDataCharacter(row, pattern, isOddPattern, false);
            } catch (NotFoundException e) {
                decodeDataCharacter = null;
            }
            return new ExpandedPair(leftChar, decodeDataCharacter, pattern, true);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private void findNextPair(BitArray row, List<ExpandedPair> previousPairs, int forcedOffset) throws NotFoundException {
        int rowOffset;
        int[] counters = getDecodeFinderCounters();
        counters[FINDER_PAT_A] = FINDER_PAT_A;
        counters[FINDER_PAT_B] = FINDER_PAT_A;
        counters[FINDER_PAT_C] = FINDER_PAT_A;
        counters[FINDER_PAT_D] = FINDER_PAT_A;
        int width = row.getSize();
        if (forcedOffset >= 0) {
            rowOffset = forcedOffset;
        } else if (previousPairs.isEmpty()) {
            rowOffset = FINDER_PAT_A;
        } else {
            rowOffset = ((ExpandedPair) previousPairs.get(previousPairs.size() - 1)).getFinderPattern().getStartEnd()[FINDER_PAT_B];
        }
        boolean searchingEvenPair = previousPairs.size() % FINDER_PAT_C != 0;
        if (this.startFromEven) {
            searchingEvenPair = !searchingEvenPair;
        }
        int i = false;
        while (rowOffset < width) {
            i = !row.get(rowOffset);
            if (i == false) {
                break;
            }
            rowOffset += FINDER_PAT_B;
        }
        int counterPosition = FINDER_PAT_A;
        int patternStart = rowOffset;
        for (int x = rowOffset; x < width; x += FINDER_PAT_B) {
            if ((row.get(x) ^ i) != 0) {
                counters[counterPosition] = counters[counterPosition] + FINDER_PAT_B;
            } else {
                if (counterPosition == FINDER_PAT_D) {
                    if (searchingEvenPair) {
                        reverseCounters(counters);
                    }
                    if (AbstractRSSReader.isFinderPattern(counters)) {
                        this.startEnd[FINDER_PAT_A] = patternStart;
                        this.startEnd[FINDER_PAT_B] = x;
                        return;
                    }
                    if (searchingEvenPair) {
                        reverseCounters(counters);
                    }
                    patternStart += counters[FINDER_PAT_A] + counters[FINDER_PAT_B];
                    counters[FINDER_PAT_A] = counters[FINDER_PAT_C];
                    counters[FINDER_PAT_B] = counters[FINDER_PAT_D];
                    counters[FINDER_PAT_C] = FINDER_PAT_A;
                    counters[FINDER_PAT_D] = FINDER_PAT_A;
                    counterPosition--;
                } else {
                    counterPosition += FINDER_PAT_B;
                }
                counters[counterPosition] = FINDER_PAT_B;
                i = i != 0 ? FINDER_PAT_A : FINDER_PAT_B;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static void reverseCounters(int[] counters) {
        int length = counters.length;
        for (int i = FINDER_PAT_A; i < length / FINDER_PAT_C; i += FINDER_PAT_B) {
            int tmp = counters[i];
            counters[i] = counters[(length - i) - 1];
            counters[(length - i) - 1] = tmp;
        }
    }

    private FinderPattern parseFoundFinderPattern(BitArray row, int rowNumber, boolean oddPattern) {
        int firstCounter;
        int start;
        int end;
        if (oddPattern) {
            int firstElementStart = this.startEnd[FINDER_PAT_A] - 1;
            while (firstElementStart >= 0 && !row.get(firstElementStart)) {
                firstElementStart--;
            }
            firstElementStart += FINDER_PAT_B;
            firstCounter = this.startEnd[FINDER_PAT_A] - firstElementStart;
            start = firstElementStart;
            end = this.startEnd[FINDER_PAT_B];
        } else {
            start = this.startEnd[FINDER_PAT_A];
            end = row.getNextUnset(this.startEnd[FINDER_PAT_B] + FINDER_PAT_B);
            firstCounter = end - this.startEnd[FINDER_PAT_B];
        }
        int[] counters = getDecodeFinderCounters();
        System.arraycopy(counters, FINDER_PAT_A, counters, FINDER_PAT_B, counters.length - 1);
        counters[FINDER_PAT_A] = firstCounter;
        try {
            int value = AbstractRSSReader.parseFinderValue(counters, FINDER_PATTERNS);
            int[] iArr = new int[FINDER_PAT_C];
            iArr[FINDER_PAT_A] = start;
            iArr[FINDER_PAT_B] = end;
            return new FinderPattern(value, iArr, start, end, rowNumber);
        } catch (NotFoundException e) {
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    DataCharacter decodeDataCharacter(BitArray row, FinderPattern pattern, boolean isOddPattern, boolean leftChar) throws NotFoundException {
        int i;
        int[] counters = getDataCharacterCounters();
        counters[FINDER_PAT_A] = FINDER_PAT_A;
        counters[FINDER_PAT_B] = FINDER_PAT_A;
        counters[FINDER_PAT_C] = FINDER_PAT_A;
        counters[FINDER_PAT_D] = FINDER_PAT_A;
        counters[FINDER_PAT_E] = FINDER_PAT_A;
        counters[FINDER_PAT_F] = FINDER_PAT_A;
        counters[6] = FINDER_PAT_A;
        counters[7] = FINDER_PAT_A;
        if (leftChar) {
            OneDReader.recordPatternInReverse(row, pattern.getStartEnd()[FINDER_PAT_A], counters);
        } else {
            OneDReader.recordPattern(row, pattern.getStartEnd()[FINDER_PAT_B], counters);
            i = FINDER_PAT_A;
            for (int j = counters.length - 1; i < j; j--) {
                int temp = counters[i];
                counters[i] = counters[j];
                counters[j] = temp;
                i += FINDER_PAT_B;
            }
        }
        float elementWidth = ((float) AbstractRSSReader.count(counters)) / 17.0f;
        float expectedElementWidth = ((float) (pattern.getStartEnd()[FINDER_PAT_B] - pattern.getStartEnd()[FINDER_PAT_A])) / 15.0f;
        if (Math.abs(elementWidth - expectedElementWidth) / expectedElementWidth > 0.3f) {
            throw NotFoundException.getNotFoundInstance();
        }
        int[] oddCounts = getOddCounts();
        int[] evenCounts = getEvenCounts();
        float[] oddRoundingErrors = getOddRoundingErrors();
        float[] evenRoundingErrors = getEvenRoundingErrors();
        i = FINDER_PAT_A;
        while (true) {
            int length = counters.length;
            if (i >= r0) {
                break;
            }
            float value = (((float) counters[i]) * 1.0f) / elementWidth;
            int count = (int) (0.5f + value);
            if (count < FINDER_PAT_B) {
                if (value < 0.3f) {
                    break;
                }
                count = FINDER_PAT_B;
            } else if (count > 8) {
                if (value > 8.7f) {
                    break;
                }
                count = 8;
            }
            int offset = i >> FINDER_PAT_B;
            if ((i & FINDER_PAT_B) == 0) {
                oddCounts[offset] = count;
                oddRoundingErrors[offset] = value - ((float) count);
            } else {
                evenCounts[offset] = count;
                evenRoundingErrors[offset] = value - ((float) count);
            }
            i += FINDER_PAT_B;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static boolean isNotA1left(FinderPattern pattern, boolean isOddPattern, boolean leftChar) {
        return (pattern.getValue() == 0 && isOddPattern && leftChar) ? false : true;
    }

    private void adjustOddEvenCounts(int numModules) throws NotFoundException {
        int oddSum = AbstractRSSReader.count(getOddCounts());
        int evenSum = AbstractRSSReader.count(getEvenCounts());
        int mismatch = (oddSum + evenSum) - numModules;
        boolean oddParityBad = (oddSum & FINDER_PAT_B) == FINDER_PAT_B;
        boolean evenParityBad = (evenSum & FINDER_PAT_B) == 0;
        boolean incrementOdd = false;
        boolean decrementOdd = false;
        if (oddSum > 13) {
            decrementOdd = true;
        } else if (oddSum < FINDER_PAT_E) {
            incrementOdd = true;
        }
        boolean incrementEven = false;
        boolean decrementEven = false;
        if (evenSum > 13) {
            decrementEven = true;
        } else if (evenSum < FINDER_PAT_E) {
            incrementEven = true;
        }
        if (mismatch == FINDER_PAT_B) {
            if (oddParityBad) {
                if (evenParityBad) {
                    throw NotFoundException.getNotFoundInstance();
                }
                decrementOdd = true;
            } else if (evenParityBad) {
                decrementEven = true;
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        } else if (mismatch == -1) {
            if (oddParityBad) {
                if (evenParityBad) {
                    throw NotFoundException.getNotFoundInstance();
                }
                incrementOdd = true;
            } else if (evenParityBad) {
                incrementEven = true;
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        } else if (mismatch != 0) {
            throw NotFoundException.getNotFoundInstance();
        } else if (oddParityBad) {
            if (!evenParityBad) {
                throw NotFoundException.getNotFoundInstance();
            } else if (oddSum < evenSum) {
                incrementOdd = true;
                decrementEven = true;
            } else {
                decrementOdd = true;
                incrementEven = true;
            }
        } else if (evenParityBad) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (incrementOdd) {
            if (decrementOdd) {
                throw NotFoundException.getNotFoundInstance();
            }
            AbstractRSSReader.increment(getOddCounts(), getOddRoundingErrors());
        }
        if (decrementOdd) {
            AbstractRSSReader.decrement(getOddCounts(), getOddRoundingErrors());
        }
        if (incrementEven) {
            if (decrementEven) {
                throw NotFoundException.getNotFoundInstance();
            }
            AbstractRSSReader.increment(getEvenCounts(), getOddRoundingErrors());
        }
        if (decrementEven) {
            AbstractRSSReader.decrement(getEvenCounts(), getEvenRoundingErrors());
        }
    }
}
