package com.huawei.zxing.oned.rss;

import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.oned.OneDReader;

public abstract class AbstractRSSReader extends OneDReader {
    private static final int MAX_AVG_VARIANCE = 51;
    private static final float MAX_FINDER_PATTERN_RATIO = 0.89285713f;
    private static final int MAX_INDIVIDUAL_VARIANCE = 115;
    private static final float MIN_FINDER_PATTERN_RATIO = 0.7916667f;
    private final int[] dataCharacterCounters = new int[8];
    private final int[] decodeFinderCounters = new int[4];
    private final int[] evenCounts;
    private final float[] evenRoundingErrors = new float[4];
    private final int[] oddCounts;
    private final float[] oddRoundingErrors = new float[4];

    protected AbstractRSSReader() {
        int[] iArr = this.dataCharacterCounters;
        this.oddCounts = new int[(iArr.length / 2)];
        this.evenCounts = new int[(iArr.length / 2)];
    }

    /* access modifiers changed from: protected */
    public final int[] getDecodeFinderCounters() {
        return this.decodeFinderCounters;
    }

    /* access modifiers changed from: protected */
    public final int[] getDataCharacterCounters() {
        return this.dataCharacterCounters;
    }

    /* access modifiers changed from: protected */
    public final float[] getOddRoundingErrors() {
        return this.oddRoundingErrors;
    }

    /* access modifiers changed from: protected */
    public final float[] getEvenRoundingErrors() {
        return this.evenRoundingErrors;
    }

    /* access modifiers changed from: protected */
    public final int[] getOddCounts() {
        return this.oddCounts;
    }

    /* access modifiers changed from: protected */
    public final int[] getEvenCounts() {
        return this.evenCounts;
    }

    protected static int parseFinderValue(int[] counters, int[][] finderPatterns) throws NotFoundException {
        for (int value = 0; value < finderPatterns.length; value++) {
            if (patternMatchVariance(counters, finderPatterns[value], MAX_INDIVIDUAL_VARIANCE) < 51) {
                return value;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected static int count(int[] array) {
        int count = 0;
        for (int a : array) {
            count += a;
        }
        return count;
    }

    protected static void increment(int[] array, float[] errors) {
        int index = 0;
        float biggestError = errors[0];
        for (int i = 1; i < array.length; i++) {
            if (errors[i] > biggestError) {
                biggestError = errors[i];
                index = i;
            }
        }
        array[index] = array[index] + 1;
    }

    protected static void decrement(int[] array, float[] errors) {
        int index = 0;
        float biggestError = errors[0];
        for (int i = 1; i < array.length; i++) {
            if (errors[i] < biggestError) {
                biggestError = errors[i];
                index = i;
            }
        }
        array[index] = array[index] - 1;
    }

    protected static boolean isFinderPattern(int[] counters) {
        int firstTwoSum = counters[0] + counters[1];
        float ratio = ((float) firstTwoSum) / ((float) ((counters[2] + firstTwoSum) + counters[3]));
        if (ratio < MIN_FINDER_PATTERN_RATIO || ratio > MAX_FINDER_PATTERN_RATIO) {
            return false;
        }
        int maxCounter = Integer.MIN_VALUE;
        int minCounter = Integer.MAX_VALUE;
        for (int counter : counters) {
            if (counter > maxCounter) {
                maxCounter = counter;
            }
            if (counter < minCounter) {
                minCounter = counter;
            }
        }
        return maxCounter < minCounter * 10;
    }
}
