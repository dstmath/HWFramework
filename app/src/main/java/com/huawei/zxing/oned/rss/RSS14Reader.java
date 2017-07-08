package com.huawei.zxing.oned.rss;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.ResultPointCallback;
import com.huawei.zxing.common.BitArray;
import com.huawei.zxing.oned.OneDReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class RSS14Reader extends AbstractRSSReader {
    private static final int[][] FINDER_PATTERNS = null;
    private static final int[] INSIDE_GSUM = null;
    private static final int[] INSIDE_ODD_TOTAL_SUBSET = null;
    private static final int[] INSIDE_ODD_WIDEST = null;
    private static final int[] OUTSIDE_EVEN_TOTAL_SUBSET = null;
    private static final int[] OUTSIDE_GSUM = null;
    private static final int[] OUTSIDE_ODD_WIDEST = null;
    private final List<Pair> possibleLeftPairs;
    private final List<Pair> possibleRightPairs;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.oned.rss.RSS14Reader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.oned.rss.RSS14Reader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.oned.rss.RSS14Reader.<clinit>():void");
    }

    public RSS14Reader() {
        this.possibleLeftPairs = new ArrayList();
        this.possibleRightPairs = new ArrayList();
    }

    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException {
        addOrTally(this.possibleLeftPairs, decodePair(row, false, rowNumber, hints));
        row.reverse();
        addOrTally(this.possibleRightPairs, decodePair(row, true, rowNumber, hints));
        row.reverse();
        int lefSize = this.possibleLeftPairs.size();
        for (int i = 0; i < lefSize; i++) {
            Pair left = (Pair) this.possibleLeftPairs.get(i);
            if (left.getCount() > 1) {
                int rightSize = this.possibleRightPairs.size();
                for (int j = 0; j < rightSize; j++) {
                    Pair right = (Pair) this.possibleRightPairs.get(j);
                    if (right.getCount() > 1 && checkChecksum(left, right)) {
                        return constructResult(left, right);
                    }
                }
                continue;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static void addOrTally(Collection<Pair> possiblePairs, Pair pair) {
        if (pair != null) {
            boolean found = false;
            for (Pair other : possiblePairs) {
                if (other.getValue() == pair.getValue()) {
                    other.incrementCount();
                    found = true;
                    break;
                }
            }
            if (!found) {
                possiblePairs.add(pair);
            }
        }
    }

    public void reset() {
        this.possibleLeftPairs.clear();
        this.possibleRightPairs.clear();
    }

    private static Result constructResult(Pair leftPair, Pair rightPair) {
        int i;
        String text = String.valueOf((((long) leftPair.getValue()) * 4537077) + ((long) rightPair.getValue()));
        StringBuilder buffer = new StringBuilder(14);
        for (i = 13 - text.length(); i > 0; i--) {
            buffer.append('0');
        }
        buffer.append(text);
        int checkDigit = 0;
        for (i = 0; i < 13; i++) {
            int digit = buffer.charAt(i) - 48;
            if ((i & 1) == 0) {
                digit *= 3;
            }
            checkDigit += digit;
        }
        checkDigit = 10 - (checkDigit % 10);
        if (checkDigit == 10) {
            checkDigit = 0;
        }
        buffer.append(checkDigit);
        ResultPoint[] leftPoints = leftPair.getFinderPattern().getResultPoints();
        ResultPoint[] rightPoints = rightPair.getFinderPattern().getResultPoints();
        return new Result(String.valueOf(buffer.toString()), null, new ResultPoint[]{leftPoints[0], leftPoints[1], rightPoints[0], rightPoints[1]}, BarcodeFormat.RSS_14);
    }

    private static boolean checkChecksum(Pair leftPair, Pair rightPair) {
        int checkValue = (leftPair.getChecksumPortion() + (rightPair.getChecksumPortion() * 16)) % 79;
        int targetCheckValue = (leftPair.getFinderPattern().getValue() * 9) + rightPair.getFinderPattern().getValue();
        if (targetCheckValue > 72) {
            targetCheckValue--;
        }
        if (targetCheckValue > 8) {
            targetCheckValue--;
        }
        return checkValue == targetCheckValue;
    }

    private Pair decodePair(BitArray row, boolean right, int rowNumber, Map<DecodeHintType, ?> hints) {
        try {
            ResultPointCallback resultPointCallback;
            int[] startEnd = findFinderPattern(row, 0, right);
            FinderPattern pattern = parseFoundFinderPattern(row, rowNumber, right, startEnd);
            if (hints == null) {
                resultPointCallback = null;
            } else {
                resultPointCallback = (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
            }
            if (resultPointCallback != null) {
                float center = ((float) (startEnd[0] + startEnd[1])) / 2.0f;
                if (right) {
                    center = ((float) (row.getSize() - 1)) - center;
                }
                resultPointCallback.foundPossibleResultPoint(new ResultPoint(center, (float) rowNumber));
            }
            DataCharacter outside = decodeDataCharacter(row, pattern, true);
            DataCharacter inside = decodeDataCharacter(row, pattern, false);
            return new Pair((outside.getValue() * 1597) + inside.getValue(), outside.getChecksumPortion() + (inside.getChecksumPortion() * 4), pattern);
        } catch (NotFoundException e) {
            return null;
        }
    }

    private DataCharacter decodeDataCharacter(BitArray row, FinderPattern pattern, boolean outsideChar) throws NotFoundException {
        int i;
        int[] counters = getDataCharacterCounters();
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        counters[4] = 0;
        counters[5] = 0;
        counters[6] = 0;
        counters[7] = 0;
        if (outsideChar) {
            OneDReader.recordPatternInReverse(row, pattern.getStartEnd()[0], counters);
        } else {
            OneDReader.recordPattern(row, pattern.getStartEnd()[1] + 1, counters);
            i = 0;
            for (int j = counters.length - 1; i < j; j--) {
                int temp = counters[i];
                counters[i] = counters[j];
                counters[j] = temp;
                i++;
            }
        }
        int numModules = outsideChar ? 16 : 15;
        float elementWidth = ((float) AbstractRSSReader.count(counters)) / ((float) numModules);
        int[] oddCounts = getOddCounts();
        int[] evenCounts = getEvenCounts();
        float[] oddRoundingErrors = getOddRoundingErrors();
        float[] evenRoundingErrors = getEvenRoundingErrors();
        i = 0;
        while (true) {
            int length = counters.length;
            if (i >= r0) {
                break;
            }
            float value = ((float) counters[i]) / elementWidth;
            int count = (int) (0.5f + value);
            if (count < 1) {
                count = 1;
            } else if (count > 8) {
                count = 8;
            }
            int offset = i >> 1;
            if ((i & 1) == 0) {
                oddCounts[offset] = count;
                oddRoundingErrors[offset] = value - ((float) count);
            } else {
                evenCounts[offset] = count;
                evenRoundingErrors[offset] = value - ((float) count);
            }
            i++;
        }
        adjustOddEvenCounts(outsideChar, numModules);
        int oddSum = 0;
        int oddChecksumPortion = 0;
        for (i = oddCounts.length - 1; i >= 0; i--) {
            oddChecksumPortion = (oddChecksumPortion * 9) + oddCounts[i];
            oddSum += oddCounts[i];
        }
        int evenChecksumPortion = 0;
        int evenSum = 0;
        for (i = evenCounts.length - 1; i >= 0; i--) {
            evenChecksumPortion = (evenChecksumPortion * 9) + evenCounts[i];
            evenSum += evenCounts[i];
        }
        int checksumPortion = oddChecksumPortion + (evenChecksumPortion * 3);
        int group;
        int oddWidest;
        if (outsideChar) {
            if ((oddSum & 1) != 0 || oddSum > 12 || oddSum < 4) {
                throw NotFoundException.getNotFoundInstance();
            }
            group = (12 - oddSum) / 2;
            oddWidest = OUTSIDE_ODD_WIDEST[group];
            int evenWidest = 9 - oddWidest;
            return new DataCharacter(((RSSUtils.getRSSvalue(oddCounts, oddWidest, false) * OUTSIDE_EVEN_TOTAL_SUBSET[group]) + RSSUtils.getRSSvalue(evenCounts, evenWidest, true)) + OUTSIDE_GSUM[group], checksumPortion);
        } else if ((evenSum & 1) != 0 || evenSum > 10 || evenSum < 4) {
            throw NotFoundException.getNotFoundInstance();
        } else {
            group = (10 - evenSum) / 2;
            oddWidest = INSIDE_ODD_WIDEST[group];
            return new DataCharacter(((RSSUtils.getRSSvalue(evenCounts, 9 - oddWidest, false) * INSIDE_ODD_TOTAL_SUBSET[group]) + RSSUtils.getRSSvalue(oddCounts, oddWidest, true)) + INSIDE_GSUM[group], checksumPortion);
        }
    }

    private int[] findFinderPattern(BitArray row, int rowOffset, boolean rightFinderPattern) throws NotFoundException {
        int[] counters = getDecodeFinderCounters();
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int width = row.getSize();
        int i = false;
        while (rowOffset < width) {
            if (row.get(rowOffset)) {
                i = false;
            } else {
                boolean isWhite = true;
            }
            if (rightFinderPattern == i) {
                break;
            }
            rowOffset++;
        }
        int counterPosition = 0;
        int patternStart = rowOffset;
        for (int x = rowOffset; x < width; x++) {
            if ((row.get(x) ^ i) != 0) {
                counters[counterPosition] = counters[counterPosition] + 1;
            } else {
                if (counterPosition != 3) {
                    counterPosition++;
                } else if (AbstractRSSReader.isFinderPattern(counters)) {
                    return new int[]{patternStart, x};
                } else {
                    patternStart += counters[0] + counters[1];
                    counters[0] = counters[2];
                    counters[1] = counters[3];
                    counters[2] = 0;
                    counters[3] = 0;
                    counterPosition--;
                }
                counters[counterPosition] = 1;
                i = i != 0 ? 0 : 1;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private FinderPattern parseFoundFinderPattern(BitArray row, int rowNumber, boolean right, int[] startEnd) throws NotFoundException {
        boolean firstIsBlack = row.get(startEnd[0]);
        int firstElementStart = startEnd[0] - 1;
        while (firstElementStart >= 0 && (row.get(firstElementStart) ^ firstIsBlack) != 0) {
            firstElementStart--;
        }
        firstElementStart++;
        int firstCounter = startEnd[0] - firstElementStart;
        int[] counters = getDecodeFinderCounters();
        System.arraycopy(counters, 0, counters, 1, counters.length - 1);
        counters[0] = firstCounter;
        int value = AbstractRSSReader.parseFinderValue(counters, FINDER_PATTERNS);
        int start = firstElementStart;
        int end = startEnd[1];
        if (right) {
            start = (row.getSize() - 1) - firstElementStart;
            end = (row.getSize() - 1) - end;
        }
        return new FinderPattern(value, new int[]{firstElementStart, startEnd[1]}, start, end, rowNumber);
    }

    private void adjustOddEvenCounts(boolean outsideChar, int numModules) throws NotFoundException {
        int oddSum = AbstractRSSReader.count(getOddCounts());
        int evenSum = AbstractRSSReader.count(getEvenCounts());
        int mismatch = (oddSum + evenSum) - numModules;
        boolean oddParityBad = (oddSum & 1) == (outsideChar ? 1 : 0);
        boolean evenParityBad = (evenSum & 1) == 1;
        boolean incrementOdd = false;
        boolean z = false;
        boolean incrementEven = false;
        boolean decrementEven = false;
        if (outsideChar) {
            if (oddSum > 12) {
                z = true;
            } else if (oddSum < 4) {
                incrementOdd = true;
            }
            if (evenSum > 12) {
                decrementEven = true;
            } else if (evenSum < 4) {
                incrementEven = true;
            }
        } else {
            if (oddSum > 11) {
                z = true;
            } else if (oddSum < 5) {
                incrementOdd = true;
            }
            if (evenSum > 10) {
                decrementEven = true;
            } else if (evenSum < 4) {
                incrementEven = true;
            }
        }
        if (mismatch == 1) {
            if (oddParityBad) {
                if (evenParityBad) {
                    throw NotFoundException.getNotFoundInstance();
                }
                z = true;
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
                z = true;
                incrementEven = true;
            }
        } else if (evenParityBad) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (incrementOdd) {
            if (z) {
                throw NotFoundException.getNotFoundInstance();
            }
            AbstractRSSReader.increment(getOddCounts(), getOddRoundingErrors());
        }
        if (z) {
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
