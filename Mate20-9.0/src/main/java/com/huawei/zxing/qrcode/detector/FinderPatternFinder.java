package com.huawei.zxing.qrcode.detector;

import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.ResultPointCallback;
import com.huawei.zxing.common.BitMatrix;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FinderPatternFinder {
    private static final int CENTER_QUORUM = 2;
    private static final int INTEGER_MATH_SHIFT = 8;
    protected static final int MAX_MODULES = 57;
    protected static final int MIN_SKIP = 3;
    private final int[] crossCheckStateCount;
    private boolean hasSkipped;
    private final BitMatrix image;
    private final List<FinderPattern> possibleCenters;
    private final ResultPointCallback resultPointCallback;

    private static final class CenterComparator implements Comparator<FinderPattern>, Serializable {
        private final float average;

        private CenterComparator(float f) {
            this.average = f;
        }

        public int compare(FinderPattern center1, FinderPattern center2) {
            if (center2.getCount() != center1.getCount()) {
                return center2.getCount() - center1.getCount();
            }
            float dA = Math.abs(center2.getEstimatedModuleSize() - this.average);
            float dB = Math.abs(center1.getEstimatedModuleSize() - this.average);
            return dA < dB ? 1 : dA == dB ? 0 : -1;
        }
    }

    private static final class FurthestFromAverageComparator implements Comparator<FinderPattern>, Serializable {
        private final float average;

        private FurthestFromAverageComparator(float f) {
            this.average = f;
        }

        public int compare(FinderPattern center1, FinderPattern center2) {
            float dA = Math.abs(center2.getEstimatedModuleSize() - this.average);
            float dB = Math.abs(center1.getEstimatedModuleSize() - this.average);
            if (dA < dB) {
                return -1;
            }
            return dA == dB ? 0 : 1;
        }
    }

    public FinderPatternFinder(BitMatrix image2) {
        this(image2, null);
    }

    public FinderPatternFinder(BitMatrix image2, ResultPointCallback resultPointCallback2) {
        this.image = image2;
        this.possibleCenters = new ArrayList();
        this.crossCheckStateCount = new int[5];
        this.resultPointCallback = resultPointCallback2;
    }

    /* access modifiers changed from: protected */
    public final BitMatrix getImage() {
        return this.image;
    }

    /* access modifiers changed from: protected */
    public final List<FinderPattern> getPossibleCenters() {
        return this.possibleCenters;
    }

    /* access modifiers changed from: package-private */
    public final FinderPatternInfo find(Map<DecodeHintType, ?> hints) throws NotFoundException {
        int iSkip;
        char c;
        int i;
        int currentState;
        Map<DecodeHintType, ?> map = hints;
        int i2 = 1;
        boolean tryHarder = map != null && map.containsKey(DecodeHintType.TRY_HARDER);
        int maxI = this.image.getHeight();
        int maxJ = this.image.getWidth();
        char c2 = 3;
        int iSkip2 = (3 * maxI) / 228;
        if (iSkip2 < 3 || tryHarder) {
            iSkip2 = 3;
        }
        boolean done = false;
        int[] stateCount = new int[5];
        int i3 = iSkip - 1;
        while (i3 < maxI && !done) {
            stateCount[0] = 0;
            stateCount[i2] = 0;
            stateCount[2] = 0;
            stateCount[c2] = 0;
            int i4 = 4;
            stateCount[4] = 0;
            int currentState2 = 0;
            boolean done2 = done;
            int iSkip3 = iSkip;
            int j = 0;
            while (j < maxJ) {
                if (this.image.get(j, i3)) {
                    if ((currentState2 & 1) == i2) {
                        currentState2++;
                    }
                    stateCount[currentState2] = stateCount[currentState2] + i2;
                    i = i2;
                    currentState = i4;
                    c = 3;
                } else if ((currentState2 & 1) != 0) {
                    i = i2;
                    currentState = i4;
                    c = 3;
                    stateCount[currentState2] = stateCount[currentState2] + 1;
                } else if (currentState2 != i4) {
                    i = i2;
                    currentState = i4;
                    c = 3;
                    currentState2++;
                    stateCount[currentState2] = stateCount[currentState2] + 1;
                } else if (foundPatternCross(stateCount)) {
                    if (handlePossibleCenter(stateCount, i3, j)) {
                        iSkip3 = 2;
                        if (this.hasSkipped) {
                            done2 = haveMultiplyConfirmedCenters();
                        } else {
                            int rowSkip = findRowSkip();
                            if (rowSkip > stateCount[2]) {
                                i3 += (rowSkip - stateCount[2]) - 2;
                                j = maxJ - 1;
                            }
                        }
                        stateCount[0] = 0;
                        stateCount[1] = 0;
                        stateCount[2] = 0;
                        c = 3;
                        stateCount[3] = 0;
                        stateCount[4] = 0;
                        currentState2 = 0;
                        currentState = 4;
                    } else {
                        int i5 = i4;
                        c = 3;
                        stateCount[0] = stateCount[2];
                        stateCount[1] = stateCount[3];
                        stateCount[2] = stateCount[i5];
                        stateCount[3] = 1;
                        stateCount[i5] = 0;
                        currentState2 = 3;
                        currentState = 4;
                    }
                    i = 1;
                } else {
                    c = 3;
                    stateCount[0] = stateCount[2];
                    i = 1;
                    stateCount[1] = stateCount[3];
                    currentState = 4;
                    stateCount[2] = stateCount[4];
                    stateCount[3] = 1;
                    stateCount[4] = 0;
                    currentState2 = 3;
                }
                j += i;
                char c3 = c;
                i4 = currentState;
                i2 = i;
                c2 = c3;
            }
            char c4 = c2;
            int i6 = i2;
            if (!foundPatternCross(stateCount) || !handlePossibleCenter(stateCount, i3, maxJ)) {
                iSkip = iSkip3;
            } else {
                iSkip = stateCount[0];
                if (this.hasSkipped != 0) {
                    done = haveMultiplyConfirmedCenters();
                    i3 += iSkip;
                    i2 = i6;
                    c2 = c4;
                }
            }
            done = done2;
            i3 += iSkip;
            i2 = i6;
            c2 = c4;
        }
        FinderPattern[] patternInfo = selectBestPatterns();
        ResultPoint.orderBestPatterns(patternInfo);
        return new FinderPatternInfo(patternInfo);
    }

    private static float centerFromEnd(int[] stateCount, int end) {
        return ((float) ((end - stateCount[4]) - stateCount[3])) - (((float) stateCount[2]) / 2.0f);
    }

    protected static boolean foundPatternCross(int[] stateCount) {
        boolean z = false;
        int totalModuleSize = 0;
        for (int i = 0; i < 5; i++) {
            int count = stateCount[i];
            if (count == 0) {
                return false;
            }
            totalModuleSize += count;
        }
        if (totalModuleSize < 7) {
            return false;
        }
        int moduleSize = (totalModuleSize << 8) / 7;
        int maxVariance = moduleSize / 2;
        if (Math.abs(moduleSize - (stateCount[0] << 8)) < maxVariance && Math.abs(moduleSize - (stateCount[1] << 8)) < maxVariance && Math.abs((3 * moduleSize) - (stateCount[2] << 8)) < 3 * maxVariance && Math.abs(moduleSize - (stateCount[3] << 8)) < maxVariance && Math.abs(moduleSize - (stateCount[4] << 8)) < maxVariance) {
            z = true;
        }
        return z;
    }

    private int[] getCrossCheckStateCount() {
        this.crossCheckStateCount[0] = 0;
        this.crossCheckStateCount[1] = 0;
        this.crossCheckStateCount[2] = 0;
        this.crossCheckStateCount[3] = 0;
        this.crossCheckStateCount[4] = 0;
        return this.crossCheckStateCount;
    }

    private float crossCheckVertical(int startI, int centerJ, int maxCount, int originalStateCountTotal) {
        BitMatrix image2 = this.image;
        int maxI = image2.getHeight();
        int[] stateCount = getCrossCheckStateCount();
        int i = startI;
        while (i >= 0 && image2.get(centerJ, i)) {
            stateCount[2] = stateCount[2] + 1;
            i--;
        }
        float f = Float.NaN;
        if (i < 0) {
            return Float.NaN;
        }
        while (i >= 0 && !image2.get(centerJ, i) && stateCount[1] <= maxCount) {
            stateCount[1] = stateCount[1] + 1;
            i--;
        }
        if (i < 0 || stateCount[1] > maxCount) {
            return Float.NaN;
        }
        while (i >= 0 && image2.get(centerJ, i) && stateCount[0] <= maxCount) {
            stateCount[0] = stateCount[0] + 1;
            i--;
        }
        if (stateCount[0] > maxCount) {
            return Float.NaN;
        }
        int i2 = startI + 1;
        while (i2 < maxI && image2.get(centerJ, i2)) {
            stateCount[2] = stateCount[2] + 1;
            i2++;
        }
        if (i2 == maxI) {
            return Float.NaN;
        }
        while (i2 < maxI && !image2.get(centerJ, i2) && stateCount[3] < maxCount) {
            stateCount[3] = stateCount[3] + 1;
            i2++;
        }
        if (i2 == maxI || stateCount[3] >= maxCount) {
            return Float.NaN;
        }
        while (i2 < maxI && image2.get(centerJ, i2) && stateCount[4] < maxCount) {
            stateCount[4] = stateCount[4] + 1;
            i2++;
        }
        if (stateCount[4] >= maxCount || 5 * Math.abs(((((stateCount[0] + stateCount[1]) + stateCount[2]) + stateCount[3]) + stateCount[4]) - originalStateCountTotal) >= 2 * originalStateCountTotal) {
            return Float.NaN;
        }
        if (foundPatternCross(stateCount)) {
            f = centerFromEnd(stateCount, i2);
        }
        return f;
    }

    private float crossCheckHorizontal(int startJ, int centerI, int maxCount, int originalStateCountTotal) {
        BitMatrix image2 = this.image;
        int maxJ = image2.getWidth();
        int[] stateCount = getCrossCheckStateCount();
        int j = startJ;
        while (j >= 0 && image2.get(j, centerI)) {
            stateCount[2] = stateCount[2] + 1;
            j--;
        }
        float f = Float.NaN;
        if (j < 0) {
            return Float.NaN;
        }
        while (j >= 0 && !image2.get(j, centerI) && stateCount[1] <= maxCount) {
            stateCount[1] = stateCount[1] + 1;
            j--;
        }
        if (j < 0 || stateCount[1] > maxCount) {
            return Float.NaN;
        }
        while (j >= 0 && image2.get(j, centerI) && stateCount[0] <= maxCount) {
            stateCount[0] = stateCount[0] + 1;
            j--;
        }
        if (stateCount[0] > maxCount) {
            return Float.NaN;
        }
        int j2 = startJ + 1;
        while (j2 < maxJ && image2.get(j2, centerI)) {
            stateCount[2] = stateCount[2] + 1;
            j2++;
        }
        if (j2 == maxJ) {
            return Float.NaN;
        }
        while (j2 < maxJ && !image2.get(j2, centerI) && stateCount[3] < maxCount) {
            stateCount[3] = stateCount[3] + 1;
            j2++;
        }
        if (j2 == maxJ || stateCount[3] >= maxCount) {
            return Float.NaN;
        }
        while (j2 < maxJ && image2.get(j2, centerI) && stateCount[4] < maxCount) {
            stateCount[4] = stateCount[4] + 1;
            j2++;
        }
        if (stateCount[4] >= maxCount || 5 * Math.abs(((((stateCount[0] + stateCount[1]) + stateCount[2]) + stateCount[3]) + stateCount[4]) - originalStateCountTotal) >= originalStateCountTotal) {
            return Float.NaN;
        }
        if (foundPatternCross(stateCount)) {
            f = centerFromEnd(stateCount, j2);
        }
        return f;
    }

    /* access modifiers changed from: protected */
    public final boolean handlePossibleCenter(int[] stateCount, int i, int j) {
        int index = 0;
        int stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2] + stateCount[3] + stateCount[4];
        float centerJ = centerFromEnd(stateCount, j);
        float centerI = crossCheckVertical(i, (int) centerJ, stateCount[2], stateCountTotal);
        if (!Float.isNaN(centerI)) {
            float centerJ2 = crossCheckHorizontal((int) centerJ, (int) centerI, stateCount[2], stateCountTotal);
            if (!Float.isNaN(centerJ2)) {
                float estimatedModuleSize = ((float) stateCountTotal) / 7.0f;
                boolean found = false;
                while (true) {
                    if (index >= this.possibleCenters.size()) {
                        break;
                    }
                    FinderPattern center = this.possibleCenters.get(index);
                    if (center.aboutEquals(estimatedModuleSize, centerI, centerJ2)) {
                        this.possibleCenters.set(index, center.combineEstimate(centerI, centerJ2, estimatedModuleSize));
                        found = true;
                        break;
                    }
                    index++;
                }
                if (!found) {
                    FinderPattern point = new FinderPattern(centerJ2, centerI, estimatedModuleSize);
                    this.possibleCenters.add(point);
                    if (this.resultPointCallback != null) {
                        this.resultPointCallback.foundPossibleResultPoint(point);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private int findRowSkip() {
        if (this.possibleCenters.size() <= 1) {
            return 0;
        }
        ResultPoint firstConfirmedCenter = null;
        for (FinderPattern center : this.possibleCenters) {
            if (center.getCount() >= 2) {
                if (firstConfirmedCenter == null) {
                    firstConfirmedCenter = center;
                } else {
                    this.hasSkipped = true;
                    return ((int) (Math.abs(firstConfirmedCenter.getX() - center.getX()) - Math.abs(firstConfirmedCenter.getY() - center.getY()))) / 2;
                }
            }
        }
        return 0;
    }

    private boolean haveMultiplyConfirmedCenters() {
        int confirmedCount = 0;
        float totalModuleSize = 0.0f;
        int max = this.possibleCenters.size();
        for (FinderPattern pattern : this.possibleCenters) {
            if (pattern.getCount() >= 2) {
                confirmedCount++;
                totalModuleSize += pattern.getEstimatedModuleSize();
            }
        }
        boolean z = false;
        if (confirmedCount < 3) {
            return false;
        }
        float average = totalModuleSize / ((float) max);
        float totalDeviation = 0.0f;
        for (FinderPattern pattern2 : this.possibleCenters) {
            totalDeviation += Math.abs(pattern2.getEstimatedModuleSize() - average);
        }
        if (totalDeviation <= 0.05f * totalModuleSize) {
            z = true;
        }
        return z;
    }

    private FinderPattern[] selectBestPatterns() throws NotFoundException {
        int startSize = this.possibleCenters.size();
        if (startSize >= 3) {
            if (startSize > 3) {
                float totalModuleSize = 0.0f;
                float square = 0.0f;
                for (FinderPattern center : this.possibleCenters) {
                    float size = center.getEstimatedModuleSize();
                    totalModuleSize += size;
                    square += size * size;
                }
                float average = totalModuleSize / ((float) startSize);
                Collections.sort(this.possibleCenters, new FurthestFromAverageComparator(average));
                float limit = Math.max(0.2f * average, (float) Math.sqrt((double) ((square / ((float) startSize)) - (average * average))));
                int i = 0;
                while (i < this.possibleCenters.size() && this.possibleCenters.size() > 3) {
                    if (Math.abs(this.possibleCenters.get(i).getEstimatedModuleSize() - average) > limit) {
                        this.possibleCenters.remove(i);
                        i--;
                    }
                    i++;
                }
            }
            if (this.possibleCenters.size() > 3) {
                float totalModuleSize2 = 0.0f;
                for (FinderPattern possibleCenter : this.possibleCenters) {
                    totalModuleSize2 += possibleCenter.getEstimatedModuleSize();
                }
                Collections.sort(this.possibleCenters, new CenterComparator(totalModuleSize2 / ((float) this.possibleCenters.size())));
                this.possibleCenters.subList(3, this.possibleCenters.size()).clear();
            }
            return new FinderPattern[]{this.possibleCenters.get(0), this.possibleCenters.get(1), this.possibleCenters.get(2)};
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
