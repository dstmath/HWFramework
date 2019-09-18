package com.huawei.zxing.qrcode.detector;

import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ResultPointCallback;
import com.huawei.zxing.common.BitMatrix;
import java.util.ArrayList;
import java.util.List;

final class AlignmentPatternFinder {
    private final int[] crossCheckStateCount;
    private final int height;
    private final BitMatrix image;
    private final float moduleSize;
    private final List<AlignmentPattern> possibleCenters = new ArrayList(5);
    private final ResultPointCallback resultPointCallback;
    private final int startX;
    private final int startY;
    private final int width;

    AlignmentPatternFinder(BitMatrix image2, int startX2, int startY2, int width2, int height2, float moduleSize2, ResultPointCallback resultPointCallback2) {
        this.image = image2;
        this.startX = startX2;
        this.startY = startY2;
        this.width = width2;
        this.height = height2;
        this.moduleSize = moduleSize2;
        this.crossCheckStateCount = new int[3];
        this.resultPointCallback = resultPointCallback2;
    }

    /* access modifiers changed from: package-private */
    public AlignmentPattern find() throws NotFoundException {
        int startX2 = this.startX;
        int height2 = this.height;
        int maxJ = this.width + startX2;
        int middleI = this.startY + (height2 >> 1);
        int[] stateCount = new int[3];
        for (int iGen = 0; iGen < height2; iGen++) {
            int i = ((iGen & 1) == 0 ? (iGen + 1) >> 1 : -((iGen + 1) >> 1)) + middleI;
            stateCount[0] = 0;
            stateCount[1] = 0;
            stateCount[2] = 0;
            int j = startX2;
            while (j < maxJ && !this.image.get(j, i)) {
                j++;
            }
            int currentState = 0;
            for (int j2 = j; j2 < maxJ; j2++) {
                if (!this.image.get(j2, i)) {
                    if (currentState == 1) {
                        currentState++;
                    }
                    stateCount[currentState] = stateCount[currentState] + 1;
                } else if (currentState == 1) {
                    stateCount[currentState] = stateCount[currentState] + 1;
                } else if (currentState == 2) {
                    if (foundPatternCross(stateCount)) {
                        AlignmentPattern confirmed = handlePossibleCenter(stateCount, i, j2);
                        if (confirmed != null) {
                            return confirmed;
                        }
                    }
                    stateCount[0] = stateCount[2];
                    stateCount[1] = 1;
                    stateCount[2] = 0;
                    currentState = 1;
                } else {
                    currentState++;
                    stateCount[currentState] = stateCount[currentState] + 1;
                }
            }
            if (foundPatternCross(stateCount)) {
                AlignmentPattern confirmed2 = handlePossibleCenter(stateCount, i, maxJ);
                if (confirmed2 != null) {
                    return confirmed2;
                }
            }
        }
        if (!this.possibleCenters.isEmpty()) {
            return this.possibleCenters.get(0);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static float centerFromEnd(int[] stateCount, int end) {
        return ((float) (end - stateCount[2])) - (((float) stateCount[1]) / 2.0f);
    }

    private boolean foundPatternCross(int[] stateCount) {
        float moduleSize2 = this.moduleSize;
        float maxVariance = moduleSize2 / 2.0f;
        for (int i = 0; i < 3; i++) {
            if (Math.abs(moduleSize2 - ((float) stateCount[i])) >= maxVariance) {
                return false;
            }
        }
        return true;
    }

    private float crossCheckVertical(int startI, int centerJ, int maxCount, int originalStateCountTotal) {
        BitMatrix image2 = this.image;
        int maxI = image2.getHeight();
        int[] stateCount = this.crossCheckStateCount;
        stateCount[0] = 0;
        stateCount[1] = 0;
        stateCount[2] = 0;
        int i = startI;
        while (i >= 0 && image2.get(centerJ, i) && stateCount[1] <= maxCount) {
            stateCount[1] = stateCount[1] + 1;
            i--;
        }
        float f = Float.NaN;
        if (i < 0 || stateCount[1] > maxCount) {
            return Float.NaN;
        }
        while (i >= 0 && !image2.get(centerJ, i) && stateCount[0] <= maxCount) {
            stateCount[0] = stateCount[0] + 1;
            i--;
        }
        if (stateCount[0] > maxCount) {
            return Float.NaN;
        }
        int i2 = startI + 1;
        while (i2 < maxI && image2.get(centerJ, i2) && stateCount[1] <= maxCount) {
            stateCount[1] = stateCount[1] + 1;
            i2++;
        }
        if (i2 == maxI || stateCount[1] > maxCount) {
            return Float.NaN;
        }
        while (i2 < maxI && !image2.get(centerJ, i2) && stateCount[2] <= maxCount) {
            stateCount[2] = stateCount[2] + 1;
            i2++;
        }
        if (stateCount[2] > maxCount || 5 * Math.abs(((stateCount[0] + stateCount[1]) + stateCount[2]) - originalStateCountTotal) >= 2 * originalStateCountTotal) {
            return Float.NaN;
        }
        if (foundPatternCross(stateCount)) {
            f = centerFromEnd(stateCount, i2);
        }
        return f;
    }

    private AlignmentPattern handlePossibleCenter(int[] stateCount, int i, int j) {
        int stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2];
        float centerJ = centerFromEnd(stateCount, j);
        float centerI = crossCheckVertical(i, (int) centerJ, stateCount[1] * 2, stateCountTotal);
        if (!Float.isNaN(centerI)) {
            float estimatedModuleSize = ((float) ((stateCount[0] + stateCount[1]) + stateCount[2])) / 3.0f;
            for (AlignmentPattern center : this.possibleCenters) {
                if (center.aboutEquals(estimatedModuleSize, centerI, centerJ)) {
                    return center.combineEstimate(centerI, centerJ, estimatedModuleSize);
                }
            }
            AlignmentPattern point = new AlignmentPattern(centerJ, centerI, estimatedModuleSize);
            this.possibleCenters.add(point);
            if (this.resultPointCallback != null) {
                this.resultPointCallback.foundPossibleResultPoint(point);
            }
        }
        return null;
    }
}
