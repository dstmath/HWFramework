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

final class MultiFinderPatternFinder extends FinderPatternFinder {
    private static final float DIFF_MODSIZE_CUTOFF = 0.5f;
    private static final float DIFF_MODSIZE_CUTOFF_PERCENT = 0.05f;
    private static final FinderPatternInfo[] EMPTY_RESULT_ARRAY = new FinderPatternInfo[0];
    private static final float MAX_MODULE_COUNT_PER_EDGE = 180.0f;
    private static final float MIN_MODULE_COUNT_PER_EDGE = 9.0f;

    private static final class ModuleSizeComparator implements Comparator<FinderPattern>, Serializable {
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
        boolean z2;
        char c;
        int size2;
        List<FinderPattern> possibleCenters2;
        boolean z3;
        boolean z4;
        char c2;
        List<FinderPattern> possibleCenters3 = getPossibleCenters();
        int size3 = possibleCenters3.size();
        int i = 3;
        if (size3 >= 3) {
            char c3 = 2;
            boolean z5 = false;
            boolean z6 = true;
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
                                    z2 = z5;
                                    z = z6;
                                    break;
                                }
                                FinderPattern p3 = possibleCenters3.get(i32);
                                if (p3 != null) {
                                    float vModSize23 = (p2.getEstimatedModuleSize() - p3.getEstimatedModuleSize()) / Math.min(p2.getEstimatedModuleSize(), p3.getEstimatedModuleSize());
                                    if (Math.abs(p2.getEstimatedModuleSize() - p3.getEstimatedModuleSize()) > f && vModSize23 >= f2) {
                                        possibleCenters = possibleCenters3;
                                        size = size3;
                                        c = 2;
                                        z2 = false;
                                        z = true;
                                        break;
                                    }
                                    FinderPattern[] test = new FinderPattern[i];
                                    z4 = false;
                                    test[0] = p1;
                                    z3 = true;
                                    test[1] = p2;
                                    c2 = 2;
                                    test[2] = p3;
                                    ResultPoint.orderBestPatterns(test);
                                    FinderPatternInfo info = new FinderPatternInfo(test);
                                    float dA = ResultPoint.distance(info.getTopLeft(), info.getBottomLeft());
                                    possibleCenters2 = possibleCenters3;
                                    float dC = ResultPoint.distance(info.getTopRight(), info.getBottomLeft());
                                    size2 = size3;
                                    float dB = ResultPoint.distance(info.getTopLeft(), info.getTopRight());
                                    float estimatedModuleCount = (dA + dB) / (p1.getEstimatedModuleSize() * 2.0f);
                                    if (estimatedModuleCount <= MAX_MODULE_COUNT_PER_EDGE && estimatedModuleCount >= MIN_MODULE_COUNT_PER_EDGE) {
                                        FinderPatternInfo finderPatternInfo = info;
                                        float vABBC = Math.abs((dA - dB) / Math.min(dA, dB));
                                        if (vABBC < 0.1f) {
                                            float f3 = dB;
                                            float f4 = vABBC;
                                            float dCpy = (float) Math.sqrt((double) ((dA * dA) + (dB * dB)));
                                            if (Math.abs((dC - dCpy) / Math.min(dC, dCpy)) < 0.1f) {
                                                results.add(test);
                                            }
                                        }
                                    }
                                } else {
                                    possibleCenters2 = possibleCenters3;
                                    size2 = size3;
                                    z4 = z5;
                                    z3 = z6;
                                    c2 = 2;
                                }
                                i32++;
                                c3 = c2;
                                z5 = z4;
                                z6 = z3;
                                possibleCenters3 = possibleCenters2;
                                size3 = size2;
                                i = 3;
                                f = 0.5f;
                                f2 = DIFF_MODSIZE_CUTOFF_PERCENT;
                            }
                        } else {
                            possibleCenters = possibleCenters3;
                            size = size3;
                            c = c3;
                            z2 = z5;
                            z = z6;
                        }
                        i2++;
                        c3 = c;
                        z5 = z2;
                        z6 = z;
                        possibleCenters3 = possibleCenters;
                        size3 = size;
                        i = 3;
                    }
                }
                i1++;
                c3 = c3;
                z5 = z5;
                z6 = z6;
                possibleCenters3 = possibleCenters3;
                size3 = size3;
                i = 3;
            }
            int i4 = size3;
            if (!results.isEmpty()) {
                return (FinderPattern[][]) results.toArray(new FinderPattern[results.size()][]);
            }
            throw NotFoundException.getNotFoundInstance();
        }
        int i5 = size3;
        throw NotFoundException.getNotFoundInstance();
    }

    public FinderPatternInfo[] findMulti(Map<DecodeHintType, ?> hints) throws NotFoundException {
        char c;
        int i;
        Map<DecodeHintType, ?> map = hints;
        boolean tryHarder = map != null && map.containsKey(DecodeHintType.TRY_HARDER);
        BitMatrix image = getImage();
        int maxI = image.getHeight();
        int maxJ = image.getWidth();
        int iSkip = (int) ((((float) maxI) / 228.0f) * 3.0f);
        char c2 = 3;
        if (iSkip < 3 || tryHarder) {
            iSkip = 3;
        }
        int[] stateCount = new int[5];
        int i2 = iSkip - 1;
        while (i2 < maxI) {
            stateCount[0] = 0;
            stateCount[1] = 0;
            stateCount[2] = 0;
            stateCount[c2] = 0;
            stateCount[4] = 0;
            int currentState = 0;
            int j = 0;
            while (j < maxJ) {
                if (image.get(j, i2)) {
                    if ((currentState & 1) == 1) {
                        currentState++;
                    }
                    stateCount[currentState] = stateCount[currentState] + 1;
                    c = 3;
                } else if ((currentState & 1) != 0) {
                    c = 3;
                    stateCount[currentState] = stateCount[currentState] + 1;
                } else if (currentState == 4) {
                    if (!foundPatternCross(stateCount) || !handlePossibleCenter(stateCount, i2, j)) {
                        c = 3;
                        stateCount[0] = stateCount[2];
                        stateCount[1] = stateCount[3];
                        stateCount[2] = stateCount[4];
                        stateCount[3] = 1;
                        stateCount[4] = 0;
                        i = 3;
                    } else {
                        i = 0;
                        stateCount[0] = 0;
                        stateCount[1] = 0;
                        stateCount[2] = 0;
                        c = 3;
                        stateCount[3] = 0;
                        stateCount[4] = 0;
                    }
                    currentState = i;
                } else {
                    c = 3;
                    currentState++;
                    stateCount[currentState] = stateCount[currentState] + 1;
                }
                j++;
                c2 = c;
            }
            char c3 = c2;
            if (foundPatternCross(stateCount)) {
                handlePossibleCenter(stateCount, i2, maxJ);
            }
            i2 += iSkip;
            c2 = c3;
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
