package ohos.global.icu.impl.coll;

import java.util.Arrays;

public final class CollationWeights {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private int[] maxBytes = new int[5];
    private int middleLength;
    private int[] minBytes = new int[5];
    private int rangeCount;
    private int rangeIndex;
    private WeightRange[] ranges = new WeightRange[7];

    private static long decWeightTrail(long j, int i) {
        return j - (1 << ((4 - i) * 8));
    }

    private static int getWeightTrail(long j, int i) {
        return ((int) (j >> ((4 - i) * 8))) & 255;
    }

    private static long incWeightTrail(long j, int i) {
        return j + (1 << ((4 - i) * 8));
    }

    public static int lengthOfWeight(long j) {
        if ((16777215 & j) == 0) {
            return 1;
        }
        if ((65535 & j) == 0) {
            return 2;
        }
        return (j & 255) == 0 ? 3 : 4;
    }

    private static long setWeightByte(long j, int i, int i2) {
        int i3 = i * 8;
        int i4 = 32 - i3;
        return (j & ((i3 < 32 ? 4294967295 >> i3 : 0) | (CollationRootElements.PRIMARY_SENTINEL << i4))) | (((long) i2) << i4);
    }

    private static long setWeightTrail(long j, int i, int i2) {
        int i3 = (4 - i) * 8;
        return (j & (CollationRootElements.PRIMARY_SENTINEL << i3)) | (((long) i2) << i3);
    }

    private static long truncateWeight(long j, int i) {
        return j & (4294967295 << ((4 - i) * 8));
    }

    public void initForPrimary(boolean z) {
        this.middleLength = 1;
        int[] iArr = this.minBytes;
        iArr[1] = 3;
        int[] iArr2 = this.maxBytes;
        iArr2[1] = 255;
        if (z) {
            iArr[2] = 4;
            iArr2[2] = 254;
        } else {
            iArr[2] = 2;
            iArr2[2] = 255;
        }
        int[] iArr3 = this.minBytes;
        iArr3[3] = 2;
        int[] iArr4 = this.maxBytes;
        iArr4[3] = 255;
        iArr3[4] = 2;
        iArr4[4] = 255;
    }

    public void initForSecondary() {
        this.middleLength = 3;
        int[] iArr = this.minBytes;
        iArr[1] = 0;
        int[] iArr2 = this.maxBytes;
        iArr2[1] = 0;
        iArr[2] = 0;
        iArr2[2] = 0;
        iArr[3] = 2;
        iArr2[3] = 255;
        iArr[4] = 2;
        iArr2[4] = 255;
    }

    public void initForTertiary() {
        this.middleLength = 3;
        int[] iArr = this.minBytes;
        iArr[1] = 0;
        int[] iArr2 = this.maxBytes;
        iArr2[1] = 0;
        iArr[2] = 0;
        iArr2[2] = 0;
        iArr[3] = 2;
        iArr2[3] = 63;
        iArr[4] = 2;
        iArr2[4] = 63;
    }

    public boolean allocWeights(long j, long j2, int i) {
        if (!getWeightRanges(j, j2)) {
            return false;
        }
        while (true) {
            int i2 = this.ranges[0].length;
            if (allocWeightsInShortRanges(i, i2)) {
                break;
            } else if (i2 == 4) {
                return false;
            } else {
                if (allocWeightsInMinLengthRanges(i, i2)) {
                    break;
                }
                int i3 = 0;
                while (i3 < this.rangeCount && this.ranges[i3].length == i2) {
                    lengthenRange(this.ranges[i3]);
                    i3++;
                }
            }
        }
        this.rangeIndex = 0;
        int i4 = this.rangeCount;
        WeightRange[] weightRangeArr = this.ranges;
        if (i4 >= weightRangeArr.length) {
            return true;
        }
        weightRangeArr[i4] = null;
        return true;
    }

    public long nextWeight() {
        int i = this.rangeIndex;
        if (i >= this.rangeCount) {
            return 4294967295L;
        }
        WeightRange weightRange = this.ranges[i];
        long j = weightRange.start;
        int i2 = weightRange.count - 1;
        weightRange.count = i2;
        if (i2 == 0) {
            this.rangeIndex++;
        } else {
            weightRange.start = incWeight(j, weightRange.length);
        }
        return j;
    }

    /* access modifiers changed from: private */
    public static final class WeightRange implements Comparable<WeightRange> {
        int count;
        long end;
        int length;
        long start;

        private WeightRange() {
        }

        public int compareTo(WeightRange weightRange) {
            int i = (this.start > weightRange.start ? 1 : (this.start == weightRange.start ? 0 : -1));
            if (i < 0) {
                return -1;
            }
            return i > 0 ? 1 : 0;
        }
    }

    private static int getWeightByte(long j, int i) {
        return getWeightTrail(j, i);
    }

    private int countBytes(int i) {
        return (this.maxBytes[i] - this.minBytes[i]) + 1;
    }

    private long incWeight(long j, int i) {
        while (true) {
            int weightByte = getWeightByte(j, i);
            if (weightByte < this.maxBytes[i]) {
                return setWeightByte(j, i, weightByte + 1);
            }
            j = setWeightByte(j, i, this.minBytes[i]);
            i--;
        }
    }

    private long incWeightByOffset(long j, int i, int i2) {
        while (true) {
            int weightByte = i2 + getWeightByte(j, i);
            if (weightByte <= this.maxBytes[i]) {
                return setWeightByte(j, i, weightByte);
            }
            int[] iArr = this.minBytes;
            int i3 = weightByte - iArr[i];
            j = setWeightByte(j, i, iArr[i] + (i3 % countBytes(i)));
            i2 = i3 / countBytes(i);
            i--;
        }
    }

    private void lengthenRange(WeightRange weightRange) {
        int i = weightRange.length + 1;
        weightRange.start = setWeightTrail(weightRange.start, i, this.minBytes[i]);
        weightRange.end = setWeightTrail(weightRange.end, i, this.maxBytes[i]);
        weightRange.count *= countBytes(i);
        weightRange.length = i;
    }

    /* JADX WARNING: Removed duplicated region for block: B:80:0x0146 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0155 A[SYNTHETIC] */
    private boolean getWeightRanges(long j, long j2) {
        int i;
        int i2;
        boolean z;
        int lengthOfWeight = lengthOfWeight(j);
        int lengthOfWeight2 = lengthOfWeight(j2);
        if (j >= j2) {
            return false;
        }
        if (lengthOfWeight < lengthOfWeight2 && j == truncateWeight(j2, lengthOfWeight)) {
            return false;
        }
        WeightRange[] weightRangeArr = new WeightRange[5];
        WeightRange weightRange = new WeightRange();
        WeightRange[] weightRangeArr2 = new WeightRange[5];
        while (true) {
            i = this.middleLength;
            if (lengthOfWeight <= i) {
                break;
            }
            int weightTrail = getWeightTrail(j, lengthOfWeight);
            if (weightTrail < this.maxBytes[lengthOfWeight]) {
                weightRangeArr[lengthOfWeight] = new WeightRange();
                weightRangeArr[lengthOfWeight].start = incWeightTrail(j, lengthOfWeight);
                weightRangeArr[lengthOfWeight].end = setWeightTrail(j, lengthOfWeight, this.maxBytes[lengthOfWeight]);
                weightRangeArr[lengthOfWeight].length = lengthOfWeight;
                weightRangeArr[lengthOfWeight].count = this.maxBytes[lengthOfWeight] - weightTrail;
            }
            j = truncateWeight(j, lengthOfWeight - 1);
            lengthOfWeight--;
        }
        if (j < 4278190080L) {
            weightRange.start = incWeightTrail(j, i);
        } else {
            weightRange.start = 4294967295L;
        }
        while (true) {
            i2 = this.middleLength;
            if (lengthOfWeight2 <= i2) {
                break;
            }
            int weightTrail2 = getWeightTrail(j2, lengthOfWeight2);
            if (weightTrail2 > this.minBytes[lengthOfWeight2]) {
                weightRangeArr2[lengthOfWeight2] = new WeightRange();
                weightRangeArr2[lengthOfWeight2].start = setWeightTrail(j2, lengthOfWeight2, this.minBytes[lengthOfWeight2]);
                weightRangeArr2[lengthOfWeight2].end = decWeightTrail(j2, lengthOfWeight2);
                weightRangeArr2[lengthOfWeight2].length = lengthOfWeight2;
                weightRangeArr2[lengthOfWeight2].count = weightTrail2 - this.minBytes[lengthOfWeight2];
            }
            j2 = truncateWeight(j2, lengthOfWeight2 - 1);
            lengthOfWeight2--;
        }
        weightRange.end = decWeightTrail(j2, i2);
        weightRange.length = this.middleLength;
        if (weightRange.end >= weightRange.start) {
            weightRange.count = ((int) ((weightRange.end - weightRange.start) >> ((4 - this.middleLength) * 8))) + 1;
        } else {
            int i3 = 4;
            while (true) {
                if (i3 <= this.middleLength) {
                    break;
                }
                if (weightRangeArr[i3] != null && weightRangeArr2[i3] != null && weightRangeArr[i3].count > 0 && weightRangeArr2[i3].count > 0) {
                    long j3 = weightRangeArr[i3].end;
                    long j4 = weightRangeArr2[i3].start;
                    int i4 = (j3 > j4 ? 1 : (j3 == j4 ? 0 : -1));
                    if (i4 > 0) {
                        weightRangeArr[i3].end = weightRangeArr2[i3].end;
                        weightRangeArr[i3].count = (getWeightTrail(weightRangeArr[i3].end, i3) - getWeightTrail(weightRangeArr[i3].start, i3)) + 1;
                    } else if (i4 != 0 && incWeight(j3, i3) == j4) {
                        weightRangeArr[i3].end = weightRangeArr2[i3].end;
                        weightRangeArr[i3].count += weightRangeArr2[i3].count;
                    } else {
                        z = false;
                        if (!z) {
                            weightRangeArr2[i3].count = 0;
                            while (true) {
                                i3--;
                                if (i3 <= this.middleLength) {
                                    break;
                                }
                                weightRangeArr2[i3] = null;
                                weightRangeArr[i3] = null;
                            }
                        }
                    }
                    z = true;
                    if (!z) {
                    }
                }
                i3--;
            }
        }
        this.rangeCount = 0;
        if (weightRange.count > 0) {
            this.ranges[0] = weightRange;
            this.rangeCount = 1;
        }
        for (int i5 = this.middleLength + 1; i5 <= 4; i5++) {
            if (weightRangeArr2[i5] != null && weightRangeArr2[i5].count > 0) {
                WeightRange[] weightRangeArr3 = this.ranges;
                int i6 = this.rangeCount;
                this.rangeCount = i6 + 1;
                weightRangeArr3[i6] = weightRangeArr2[i5];
            }
            if (weightRangeArr[i5] != null && weightRangeArr[i5].count > 0) {
                WeightRange[] weightRangeArr4 = this.ranges;
                int i7 = this.rangeCount;
                this.rangeCount = i7 + 1;
                weightRangeArr4[i7] = weightRangeArr[i5];
            }
        }
        if (this.rangeCount > 0) {
            return true;
        }
        return false;
    }

    private boolean allocWeightsInShortRanges(int i, int i2) {
        int i3 = i;
        int i4 = 0;
        while (i4 < this.rangeCount && this.ranges[i4].length <= i2 + 1) {
            if (i3 <= this.ranges[i4].count) {
                if (this.ranges[i4].length > i2) {
                    this.ranges[i4].count = i3;
                }
                this.rangeCount = i4 + 1;
                int i5 = this.rangeCount;
                if (i5 > 1) {
                    Arrays.sort(this.ranges, 0, i5);
                }
                return true;
            }
            i3 -= this.ranges[i4].count;
            i4++;
        }
        return false;
    }

    private boolean allocWeightsInMinLengthRanges(int i, int i2) {
        int i3 = 0;
        int i4 = 0;
        while (i3 < this.rangeCount && this.ranges[i3].length == i2) {
            i4 += this.ranges[i3].count;
            i3++;
        }
        int countBytes = countBytes(i2 + 1);
        if (i > i4 * countBytes) {
            return false;
        }
        long j = this.ranges[0].start;
        long j2 = this.ranges[0].end;
        long j3 = j;
        for (int i5 = 1; i5 < i3; i5++) {
            if (this.ranges[i5].start < j3) {
                j3 = this.ranges[i5].start;
            }
            if (this.ranges[i5].end > j2) {
                j2 = this.ranges[i5].end;
            }
        }
        int i6 = (i - i4) / (countBytes - 1);
        int i7 = i4 - i6;
        if (i6 == 0 || (countBytes * i6) + i7 < i) {
            i6++;
            i7--;
        }
        WeightRange[] weightRangeArr = this.ranges;
        weightRangeArr[0].start = j3;
        if (i7 == 0) {
            weightRangeArr[0].end = j2;
            weightRangeArr[0].count = i4;
            lengthenRange(weightRangeArr[0]);
            this.rangeCount = 1;
        } else {
            weightRangeArr[0].end = incWeightByOffset(j3, i2, i7 - 1);
            WeightRange[] weightRangeArr2 = this.ranges;
            weightRangeArr2[0].count = i7;
            if (weightRangeArr2[1] == null) {
                weightRangeArr2[1] = new WeightRange();
            }
            WeightRange[] weightRangeArr3 = this.ranges;
            weightRangeArr3[1].start = incWeight(weightRangeArr3[0].end, i2);
            WeightRange[] weightRangeArr4 = this.ranges;
            weightRangeArr4[1].end = j2;
            weightRangeArr4[1].length = i2;
            weightRangeArr4[1].count = i6;
            lengthenRange(weightRangeArr4[1]);
            this.rangeCount = 2;
        }
        return true;
    }
}
