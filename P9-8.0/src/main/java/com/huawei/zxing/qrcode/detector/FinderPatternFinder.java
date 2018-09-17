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

        /* synthetic */ CenterComparator(float f, CenterComparator -this1) {
            this(f);
        }

        private CenterComparator(float f) {
            this.average = f;
        }

        public int compare(FinderPattern center1, FinderPattern center2) {
            if (center2.getCount() != center1.getCount()) {
                return center2.getCount() - center1.getCount();
            }
            float dA = Math.abs(center2.getEstimatedModuleSize() - this.average);
            float dB = Math.abs(center1.getEstimatedModuleSize() - this.average);
            int i = dA < dB ? 1 : dA == dB ? 0 : -1;
            return i;
        }
    }

    private static final class FurthestFromAverageComparator implements Comparator<FinderPattern>, Serializable {
        private final float average;

        /* synthetic */ FurthestFromAverageComparator(float f, FurthestFromAverageComparator -this1) {
            this(f);
        }

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

    public FinderPatternFinder(BitMatrix image) {
        this(image, null);
    }

    public FinderPatternFinder(BitMatrix image, ResultPointCallback resultPointCallback) {
        this.image = image;
        this.possibleCenters = new ArrayList();
        this.crossCheckStateCount = new int[5];
        this.resultPointCallback = resultPointCallback;
    }

    protected final BitMatrix getImage() {
        return this.image;
    }

    protected final List<FinderPattern> getPossibleCenters() {
        return this.possibleCenters;
    }

    final FinderPatternInfo find(Map<DecodeHintType, ?> hints) throws NotFoundException {
        boolean tryHarder = hints != null ? hints.containsKey(DecodeHintType.TRY_HARDER) : false;
        int maxI = this.image.getHeight();
        int maxJ = this.image.getWidth();
        int iSkip = (maxI * 3) / 228;
        if (iSkip < 3 || tryHarder) {
            iSkip = 3;
        }
        int done = 0;
        int[] stateCount = new int[5];
        int i = iSkip - 1;
        while (i < maxI && (done ^ 1) != 0) {
            stateCount[0] = 0;
            stateCount[1] = 0;
            stateCount[2] = 0;
            stateCount[3] = 0;
            stateCount[4] = 0;
            int currentState = 0;
            int j = 0;
            while (j < maxJ) {
                if (this.image.get(j, i)) {
                    if ((currentState & 1) == 1) {
                        currentState++;
                    }
                    stateCount[currentState] = stateCount[currentState] + 1;
                } else if ((currentState & 1) != 0) {
                    stateCount[currentState] = stateCount[currentState] + 1;
                } else if (currentState != 4) {
                    currentState++;
                    stateCount[currentState] = stateCount[currentState] + 1;
                } else if (!foundPatternCross(stateCount)) {
                    stateCount[0] = stateCount[2];
                    stateCount[1] = stateCount[3];
                    stateCount[2] = stateCount[4];
                    stateCount[3] = 1;
                    stateCount[4] = 0;
                    currentState = 3;
                } else if (handlePossibleCenter(stateCount, i, j)) {
                    iSkip = 2;
                    if (this.hasSkipped) {
                        done = haveMultiplyConfirmedCenters();
                    } else {
                        int rowSkip = findRowSkip();
                        if (rowSkip > stateCount[2]) {
                            i += (rowSkip - stateCount[2]) - 2;
                            j = maxJ - 1;
                        }
                    }
                    currentState = 0;
                    stateCount[0] = 0;
                    stateCount[1] = 0;
                    stateCount[2] = 0;
                    stateCount[3] = 0;
                    stateCount[4] = 0;
                } else {
                    stateCount[0] = stateCount[2];
                    stateCount[1] = stateCount[3];
                    stateCount[2] = stateCount[4];
                    stateCount[3] = 1;
                    stateCount[4] = 0;
                    currentState = 3;
                }
                j++;
            }
            if (foundPatternCross(stateCount) && handlePossibleCenter(stateCount, i, maxJ)) {
                iSkip = stateCount[0];
                if (this.hasSkipped) {
                    done = haveMultiplyConfirmedCenters();
                }
            }
            i += iSkip;
        }
        FinderPattern[] patternInfo = selectBestPatterns();
        ResultPoint.orderBestPatterns(patternInfo);
        return new FinderPatternInfo(patternInfo);
    }

    private static float centerFromEnd(int[] stateCount, int end) {
        return ((float) ((end - stateCount[4]) - stateCount[3])) - (((float) stateCount[2]) / 2.0f);
    }

    protected static boolean foundPatternCross(int[] stateCount) {
        boolean z = true;
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
        if (Math.abs(moduleSize - (stateCount[0] << 8)) >= maxVariance || Math.abs(moduleSize - (stateCount[1] << 8)) >= maxVariance || Math.abs((moduleSize * 3) - (stateCount[2] << 8)) >= maxVariance * 3 || Math.abs(moduleSize - (stateCount[3] << 8)) >= maxVariance) {
            z = false;
        } else if (Math.abs(moduleSize - (stateCount[4] << 8)) >= maxVariance) {
            z = false;
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
        BitMatrix image = this.image;
        int maxI = image.getHeight();
        int[] stateCount = getCrossCheckStateCount();
        int i = startI;
        while (i >= 0 && image.get(centerJ, i)) {
            stateCount[2] = stateCount[2] + 1;
            i--;
        }
        if (i < 0) {
            return Float.NaN;
        }
        while (i >= 0 && (image.get(centerJ, i) ^ 1) != 0 && stateCount[1] <= maxCount) {
            stateCount[1] = stateCount[1] + 1;
            i--;
        }
        if (i < 0 || stateCount[1] > maxCount) {
            return Float.NaN;
        }
        while (i >= 0 && image.get(centerJ, i) && stateCount[0] <= maxCount) {
            stateCount[0] = stateCount[0] + 1;
            i--;
        }
        if (stateCount[0] > maxCount) {
            return Float.NaN;
        }
        i = startI + 1;
        while (i < maxI && image.get(centerJ, i)) {
            stateCount[2] = stateCount[2] + 1;
            i++;
        }
        if (i == maxI) {
            return Float.NaN;
        }
        while (i < maxI && (image.get(centerJ, i) ^ 1) != 0 && stateCount[3] < maxCount) {
            stateCount[3] = stateCount[3] + 1;
            i++;
        }
        if (i == maxI || stateCount[3] >= maxCount) {
            return Float.NaN;
        }
        while (i < maxI && image.get(centerJ, i) && stateCount[4] < maxCount) {
            stateCount[4] = stateCount[4] + 1;
            i++;
        }
        if (stateCount[4] >= maxCount) {
            return Float.NaN;
        }
        if (Math.abs(((((stateCount[0] + stateCount[1]) + stateCount[2]) + stateCount[3]) + stateCount[4]) - originalStateCountTotal) * 5 >= originalStateCountTotal * 2) {
            return Float.NaN;
        }
        return foundPatternCross(stateCount) ? centerFromEnd(stateCount, i) : Float.NaN;
    }

    private float crossCheckHorizontal(int startJ, int centerI, int maxCount, int originalStateCountTotal) {
        BitMatrix image = this.image;
        int maxJ = image.getWidth();
        int[] stateCount = getCrossCheckStateCount();
        int j = startJ;
        while (j >= 0 && image.get(j, centerI)) {
            stateCount[2] = stateCount[2] + 1;
            j--;
        }
        if (j < 0) {
            return Float.NaN;
        }
        while (j >= 0 && (image.get(j, centerI) ^ 1) != 0 && stateCount[1] <= maxCount) {
            stateCount[1] = stateCount[1] + 1;
            j--;
        }
        if (j < 0 || stateCount[1] > maxCount) {
            return Float.NaN;
        }
        while (j >= 0 && image.get(j, centerI) && stateCount[0] <= maxCount) {
            stateCount[0] = stateCount[0] + 1;
            j--;
        }
        if (stateCount[0] > maxCount) {
            return Float.NaN;
        }
        j = startJ + 1;
        while (j < maxJ && image.get(j, centerI)) {
            stateCount[2] = stateCount[2] + 1;
            j++;
        }
        if (j == maxJ) {
            return Float.NaN;
        }
        while (j < maxJ && (image.get(j, centerI) ^ 1) != 0 && stateCount[3] < maxCount) {
            stateCount[3] = stateCount[3] + 1;
            j++;
        }
        if (j == maxJ || stateCount[3] >= maxCount) {
            return Float.NaN;
        }
        while (j < maxJ && image.get(j, centerI) && stateCount[4] < maxCount) {
            stateCount[4] = stateCount[4] + 1;
            j++;
        }
        if (stateCount[4] >= maxCount) {
            return Float.NaN;
        }
        if (Math.abs(((((stateCount[0] + stateCount[1]) + stateCount[2]) + stateCount[3]) + stateCount[4]) - originalStateCountTotal) * 5 >= originalStateCountTotal) {
            return Float.NaN;
        }
        return foundPatternCross(stateCount) ? centerFromEnd(stateCount, j) : Float.NaN;
    }

    protected final boolean handlePossibleCenter(int[] stateCount, int i, int j) {
        int stateCountTotal = (((stateCount[0] + stateCount[1]) + stateCount[2]) + stateCount[3]) + stateCount[4];
        float centerJ = centerFromEnd(stateCount, j);
        float centerI = crossCheckVertical(i, (int) centerJ, stateCount[2], stateCountTotal);
        if (!Float.isNaN(centerI)) {
            centerJ = crossCheckHorizontal((int) centerJ, (int) centerI, stateCount[2], stateCountTotal);
            if (!Float.isNaN(centerJ)) {
                float estimatedModuleSize = ((float) stateCountTotal) / 7.0f;
                boolean found = false;
                for (int index = 0; index < this.possibleCenters.size(); index++) {
                    FinderPattern center = (FinderPattern) this.possibleCenters.get(index);
                    if (center.aboutEquals(estimatedModuleSize, centerI, centerJ)) {
                        this.possibleCenters.set(index, center.combineEstimate(centerI, centerJ, estimatedModuleSize));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    FinderPattern point = new FinderPattern(centerJ, centerI, estimatedModuleSize);
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
        for (ResultPoint center : this.possibleCenters) {
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
        boolean z = false;
        int confirmedCount = 0;
        float totalModuleSize = 0.0f;
        int max = this.possibleCenters.size();
        for (FinderPattern pattern : this.possibleCenters) {
            if (pattern.getCount() >= 2) {
                confirmedCount++;
                totalModuleSize += pattern.getEstimatedModuleSize();
            }
        }
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
        if (startSize < 3) {
            throw NotFoundException.getNotFoundInstance();
        }
        float totalModuleSize;
        if (startSize > 3) {
            totalModuleSize = 0.0f;
            float square = 0.0f;
            for (FinderPattern center : this.possibleCenters) {
                float size = center.getEstimatedModuleSize();
                totalModuleSize += size;
                square += size * size;
            }
            float average = totalModuleSize / ((float) startSize);
            float stdDev = (float) Math.sqrt((double) ((square / ((float) startSize)) - (average * average)));
            Collections.sort(this.possibleCenters, new FurthestFromAverageComparator(average, null));
            float limit = Math.max(0.2f * average, stdDev);
            int i = 0;
            while (i < this.possibleCenters.size() && this.possibleCenters.size() > 3) {
                if (Math.abs(((FinderPattern) this.possibleCenters.get(i)).getEstimatedModuleSize() - average) > limit) {
                    this.possibleCenters.remove(i);
                    i--;
                }
                i++;
            }
        }
        if (this.possibleCenters.size() > 3) {
            totalModuleSize = 0.0f;
            for (FinderPattern possibleCenter : this.possibleCenters) {
                totalModuleSize += possibleCenter.getEstimatedModuleSize();
            }
            Collections.sort(this.possibleCenters, new CenterComparator(totalModuleSize / ((float) this.possibleCenters.size()), null));
            this.possibleCenters.subList(3, this.possibleCenters.size()).clear();
        }
        return new FinderPattern[]{(FinderPattern) this.possibleCenters.get(0), (FinderPattern) this.possibleCenters.get(1), (FinderPattern) this.possibleCenters.get(2)};
    }
}
