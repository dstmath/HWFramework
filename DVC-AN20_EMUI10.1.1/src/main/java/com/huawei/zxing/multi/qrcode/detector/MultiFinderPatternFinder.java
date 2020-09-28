package com.huawei.zxing.multi.qrcode.detector;

import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.ResultPointCallback;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.qrcode.detector.FinderPattern;
import com.huawei.zxing.qrcode.detector.FinderPatternFinder;
import com.huawei.zxing.qrcode.detector.FinderPatternInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
public final class MultiFinderPatternFinder extends FinderPatternFinder {
    private static final float DIFF_MODSIZE_CUTOFF = 0.5f;
    private static final float DIFF_MODSIZE_CUTOFF_PERCENT = 0.05f;
    private static final FinderPatternInfo[] EMPTY_RESULT_ARRAY = new FinderPatternInfo[0];
    private static final float MAX_MODULE_COUNT_PER_EDGE = 180.0f;
    private static final float MIN_MODULE_COUNT_PER_EDGE = 9.0f;

    /* access modifiers changed from: private */
    public static final class ModuleSizeComparator implements Comparator<FinderPattern>, Serializable {
        private ModuleSizeComparator() {
        }

        public int compare(FinderPattern center1, FinderPattern center2) {
            float value = center2.getEstimatedModuleSize() - center1.getEstimatedModuleSize();
            if (((double) value) < 0.0d) {
                return -1;
            }
            return ((double) value) > 0.0d ? 1 : 0;
        }
    }

    MultiFinderPatternFinder(BitMatrix image) {
        super(image);
    }

    MultiFinderPatternFinder(BitMatrix image, ResultPointCallback resultPointCallback) {
        super(image, resultPointCallback);
    }

    private FinderPattern[][] selectMutipleBestPatterns() throws NotFoundException {
        int size;
        List<FinderPattern> possibleCenters;
        boolean z;
        char c;
        int size2;
        List<FinderPattern> possibleCenters2;
        boolean z2;
        char c2;
        List<FinderPattern> possibleCenters3 = getPossibleCenters();
        int size3 = possibleCenters3.size();
        int i = 3;
        if (size3 >= 3) {
            char c3 = 2;
            char c4 = 0;
            boolean z3 = true;
            if (size3 == 3) {
                return new FinderPattern[][]{new FinderPattern[]{possibleCenters3.get(0), possibleCenters3.get(1), possibleCenters3.get(2)}};
            }
            Collections.sort(possibleCenters3, new ModuleSizeComparator());
            List<FinderPattern[]> results = new ArrayList<>();
            int i1 = 0;
            while (i1 < size3 - 2) {
                FinderPattern p1 = possibleCenters3.get(i1);
                if (p1 != null) {
                    int i2 = i1 + 1;
                    while (i2 < size3 - 1) {
                        FinderPattern p2 = possibleCenters3.get(i2);
                        if (p2 != null) {
                            float vModSize12 = (p1.getEstimatedModuleSize() - p2.getEstimatedModuleSize()) / Math.min(p1.getEstimatedModuleSize(), p2.getEstimatedModuleSize());
                            float f = 0.5f;
                            int i3 = (Math.abs(p1.getEstimatedModuleSize() - p2.getEstimatedModuleSize()) > 0.5f ? 1 : (Math.abs(p1.getEstimatedModuleSize() - p2.getEstimatedModuleSize()) == 0.5f ? 0 : -1));
                            float f2 = DIFF_MODSIZE_CUTOFF_PERCENT;
                            if (i3 > 0 && vModSize12 >= DIFF_MODSIZE_CUTOFF_PERCENT) {
                                break;
                            }
                            int i32 = i2 + 1;
                            while (true) {
                                if (i32 >= size3) {
                                    possibleCenters = possibleCenters3;
                                    size = size3;
                                    c = c3;
                                    z = z3;
                                    break;
                                }
                                FinderPattern p3 = possibleCenters3.get(i32);
                                if (p3 != null) {
                                    float vModSize23 = (p2.getEstimatedModuleSize() - p3.getEstimatedModuleSize()) / Math.min(p2.getEstimatedModuleSize(), p3.getEstimatedModuleSize());
                                    if (Math.abs(p2.getEstimatedModuleSize() - p3.getEstimatedModuleSize()) > f && vModSize23 >= f2) {
                                        possibleCenters = possibleCenters3;
                                        size = size3;
                                        c = 2;
                                        z = true;
                                        break;
                                    }
                                    FinderPattern[] test = new FinderPattern[i];
                                    test[c4] = p1;
                                    z2 = true;
                                    test[1] = p2;
                                    c2 = 2;
                                    test[2] = p3;
                                    ResultPoint.orderBestPatterns(test);
                                    FinderPatternInfo info = new FinderPatternInfo(test);
                                    float dA = ResultPoint.distance(info.getTopLeft(), info.getBottomLeft());
                                    float dC = ResultPoint.distance(info.getTopRight(), info.getBottomLeft());
                                    possibleCenters2 = possibleCenters3;
                                    float dB = ResultPoint.distance(info.getTopLeft(), info.getTopRight());
                                    float estimatedModuleCount = (dA + dB) / (p1.getEstimatedModuleSize() * 2.0f);
                                    if (estimatedModuleCount > MAX_MODULE_COUNT_PER_EDGE) {
                                        size2 = size3;
                                    } else if (estimatedModuleCount < MIN_MODULE_COUNT_PER_EDGE) {
                                        size2 = size3;
                                    } else if (Math.abs((dA - dB) / Math.min(dA, dB)) >= 0.1f) {
                                        size2 = size3;
                                    } else {
                                        size2 = size3;
                                        float dCpy = (float) Math.sqrt((double) ((dA * dA) + (dB * dB)));
                                        if (Math.abs((dC - dCpy) / Math.min(dC, dCpy)) < 0.1f) {
                                            results.add(test);
                                        }
                                    }
                                } else {
                                    possibleCenters2 = possibleCenters3;
                                    size2 = size3;
                                    c2 = c3;
                                    z2 = z3;
                                }
                                i32++;
                                c3 = c2;
                                z3 = z2;
                                possibleCenters3 = possibleCenters2;
                                size3 = size2;
                                i = 3;
                                c4 = 0;
                                f = 0.5f;
                                f2 = DIFF_MODSIZE_CUTOFF_PERCENT;
                            }
                        } else {
                            possibleCenters = possibleCenters3;
                            size = size3;
                            c = c3;
                            z = z3;
                        }
                        i2++;
                        c3 = c;
                        z3 = z;
                        possibleCenters3 = possibleCenters;
                        size3 = size;
                        i = 3;
                        c4 = 0;
                    }
                }
                i1++;
                c3 = c3;
                z3 = z3;
                possibleCenters3 = possibleCenters3;
                size3 = size3;
                i = 3;
                c4 = 0;
            }
            if (!results.isEmpty()) {
                return (FinderPattern[][]) results.toArray(new FinderPattern[results.size()][]);
            }
            throw NotFoundException.getNotFoundInstance();
        }
        throw NotFoundException.getNotFoundInstance();
    }

    public FinderPatternInfo[] findMulti(Map<DecodeHintType, ?> hints) throws NotFoundException {
        boolean tryHarder = hints != null && hints.containsKey(DecodeHintType.TRY_HARDER);
        BitMatrix image = getImage();
        int maxI = image.getHeight();
        int maxJ = image.getWidth();
        int iSkip = (int) ((((float) maxI) / 228.0f) * 3.0f);
        int currentState = 3;
        if (iSkip < 3 || tryHarder) {
            iSkip = 3;
        }
        int[] stateCount = new int[5];
        for (int i = iSkip - 1; i < maxI; i += iSkip) {
            stateCount[0] = 0;
            stateCount[1] = 0;
            stateCount[2] = 0;
            stateCount[currentState] = 0;
            stateCount[4] = 0;
            int currentState2 = 0;
            for (int j = 0; j < maxJ; j++) {
                if (image.get(j, i)) {
                    if ((currentState2 & 1) == 1) {
                        currentState2++;
                    }
                    stateCount[currentState2] = stateCount[currentState2] + 1;
                    currentState = 3;
                } else if ((currentState2 & 1) != 0) {
                    currentState = 3;
                    stateCount[currentState2] = stateCount[currentState2] + 1;
                } else if (currentState2 != 4) {
                    currentState = 3;
                    currentState2++;
                    stateCount[currentState2] = stateCount[currentState2] + 1;
                } else if (!foundPatternCross(stateCount) || !handlePossibleCenter(stateCount, i, j)) {
                    stateCount[0] = stateCount[2];
                    currentState = 3;
                    stateCount[1] = stateCount[3];
                    stateCount[2] = stateCount[4];
                    stateCount[3] = 1;
                    stateCount[4] = 0;
                    currentState2 = 3;
                } else {
                    stateCount[0] = 0;
                    stateCount[1] = 0;
                    stateCount[2] = 0;
                    stateCount[3] = 0;
                    stateCount[4] = 0;
                    currentState2 = 0;
                    currentState = 3;
                }
            }
            if (foundPatternCross(stateCount)) {
                handlePossibleCenter(stateCount, i, maxJ);
            }
        }
        FinderPattern[][] patternInfo = selectMutipleBestPatterns();
        List<FinderPatternInfo> result = new ArrayList<>();
        for (FinderPattern[] pattern : patternInfo) {
            ResultPoint.orderBestPatterns(pattern);
            result.add(new FinderPatternInfo(pattern));
        }
        if (result.isEmpty()) {
            return EMPTY_RESULT_ARRAY;
        }
        return (FinderPatternInfo[]) result.toArray(new FinderPatternInfo[result.size()]);
    }
}
