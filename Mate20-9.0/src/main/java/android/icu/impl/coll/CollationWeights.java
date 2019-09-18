package android.icu.impl.coll;

import java.util.Arrays;

public final class CollationWeights {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private int[] maxBytes = new int[5];
    private int middleLength;
    private int[] minBytes = new int[5];
    private int rangeCount;
    private int rangeIndex;
    private WeightRange[] ranges = new WeightRange[7];

    private static final class WeightRange implements Comparable<WeightRange> {
        int count;
        long end;
        int length;
        long start;

        private WeightRange() {
        }

        public int compareTo(WeightRange other) {
            long l = this.start;
            long r = other.start;
            if (l < r) {
                return -1;
            }
            if (l > r) {
                return 1;
            }
            return 0;
        }
    }

    public void initForPrimary(boolean compressible) {
        this.middleLength = 1;
        this.minBytes[1] = 3;
        this.maxBytes[1] = 255;
        if (compressible) {
            this.minBytes[2] = 4;
            this.maxBytes[2] = 254;
        } else {
            this.minBytes[2] = 2;
            this.maxBytes[2] = 255;
        }
        this.minBytes[3] = 2;
        this.maxBytes[3] = 255;
        this.minBytes[4] = 2;
        this.maxBytes[4] = 255;
    }

    public void initForSecondary() {
        this.middleLength = 3;
        this.minBytes[1] = 0;
        this.maxBytes[1] = 0;
        this.minBytes[2] = 0;
        this.maxBytes[2] = 0;
        this.minBytes[3] = 2;
        this.maxBytes[3] = 255;
        this.minBytes[4] = 2;
        this.maxBytes[4] = 255;
    }

    public void initForTertiary() {
        this.middleLength = 3;
        this.minBytes[1] = 0;
        this.maxBytes[1] = 0;
        this.minBytes[2] = 0;
        this.maxBytes[2] = 0;
        this.minBytes[3] = 2;
        this.maxBytes[3] = 63;
        this.minBytes[4] = 2;
        this.maxBytes[4] = 63;
    }

    public boolean allocWeights(long lowerLimit, long upperLimit, int n) {
        if (!getWeightRanges(lowerLimit, upperLimit)) {
            return false;
        }
        while (true) {
            int minLength = this.ranges[0].length;
            if (allocWeightsInShortRanges(n, minLength)) {
                break;
            } else if (minLength == 4) {
                return false;
            } else {
                if (allocWeightsInMinLengthRanges(n, minLength)) {
                    break;
                }
                int i = 0;
                while (i < this.rangeCount && this.ranges[i].length == minLength) {
                    lengthenRange(this.ranges[i]);
                    i++;
                }
            }
        }
        this.rangeIndex = 0;
        if (this.rangeCount < this.ranges.length) {
            this.ranges[this.rangeCount] = null;
        }
        return true;
    }

    public long nextWeight() {
        if (this.rangeIndex >= this.rangeCount) {
            return 4294967295L;
        }
        WeightRange range = this.ranges[this.rangeIndex];
        long weight = range.start;
        int i = range.count - 1;
        range.count = i;
        if (i == 0) {
            this.rangeIndex++;
        } else {
            range.start = incWeight(weight, range.length);
        }
        return weight;
    }

    public static int lengthOfWeight(long weight) {
        if ((16777215 & weight) == 0) {
            return 1;
        }
        if ((65535 & weight) == 0) {
            return 2;
        }
        if ((255 & weight) == 0) {
            return 3;
        }
        return 4;
    }

    private static int getWeightTrail(long weight, int length) {
        return ((int) (weight >> (8 * (4 - length)))) & 255;
    }

    private static long setWeightTrail(long weight, int length, int trail) {
        int length2 = 8 * (4 - length);
        return ((CollationRootElements.PRIMARY_SENTINEL << length2) & weight) | (((long) trail) << length2);
    }

    private static int getWeightByte(long weight, int idx) {
        return getWeightTrail(weight, idx);
    }

    private static long setWeightByte(long weight, int idx, int b) {
        long mask;
        int idx2 = idx * 8;
        if (idx2 < 32) {
            mask = 4294967295 >> idx2;
        } else {
            mask = 0;
        }
        int idx3 = 32 - idx2;
        return (weight & (mask | (CollationRootElements.PRIMARY_SENTINEL << idx3))) | (((long) b) << idx3);
    }

    private static long truncateWeight(long weight, int length) {
        return (4294967295 << (8 * (4 - length))) & weight;
    }

    private static long incWeightTrail(long weight, int length) {
        return (1 << (8 * (4 - length))) + weight;
    }

    private static long decWeightTrail(long weight, int length) {
        return weight - (1 << (8 * (4 - length)));
    }

    private int countBytes(int idx) {
        return (this.maxBytes[idx] - this.minBytes[idx]) + 1;
    }

    private long incWeight(long weight, int length) {
        while (true) {
            int b = getWeightByte(weight, length);
            if (b < this.maxBytes[length]) {
                return setWeightByte(weight, length, b + 1);
            }
            weight = setWeightByte(weight, length, this.minBytes[length]);
            length--;
        }
    }

    private long incWeightByOffset(long weight, int length, int offset) {
        while (true) {
            int offset2 = offset + getWeightByte(weight, length);
            if (offset2 <= this.maxBytes[length]) {
                return setWeightByte(weight, length, offset2);
            }
            int offset3 = offset2 - this.minBytes[length];
            weight = setWeightByte(weight, length, this.minBytes[length] + (offset3 % countBytes(length)));
            offset = offset3 / countBytes(length);
            length--;
        }
    }

    private void lengthenRange(WeightRange range) {
        int length = range.length + 1;
        range.start = setWeightTrail(range.start, length, this.minBytes[length]);
        range.end = setWeightTrail(range.end, length, this.maxBytes[length]);
        range.count *= countBytes(length);
        range.length = length;
    }

    private boolean getWeightRanges(long lowerLimit, long upperLimit) {
        WeightRange[] lower;
        boolean z;
        Object obj;
        int upperLength;
        int lowerLength;
        long weight = upperLimit;
        int lowerLength2 = lengthOfWeight(lowerLimit);
        int upperLength2 = lengthOfWeight(upperLimit);
        if (lowerLimit >= weight) {
            return false;
        }
        if (lowerLength2 < upperLength2 && lowerLimit == truncateWeight(weight, lowerLength2)) {
            return false;
        }
        WeightRange[] lower2 = new WeightRange[5];
        Object obj2 = null;
        WeightRange middle = new WeightRange();
        WeightRange[] upper = new WeightRange[5];
        long weight2 = lowerLimit;
        int length = lowerLength2;
        while (length > this.middleLength) {
            int trail = getWeightTrail(weight2, length);
            if (trail < this.maxBytes[length]) {
                lower2[length] = new WeightRange();
                lower2[length].start = incWeightTrail(weight2, length);
                lowerLength = lowerLength2;
                upperLength = upperLength2;
                lower2[length].end = setWeightTrail(weight2, length, this.maxBytes[length]);
                lower2[length].length = length;
                lower2[length].count = this.maxBytes[length] - trail;
            } else {
                lowerLength = lowerLength2;
                upperLength = upperLength2;
            }
            weight2 = truncateWeight(weight2, length - 1);
            length--;
            lowerLength2 = lowerLength;
            upperLength2 = upperLength;
        }
        int upperLength3 = upperLength2;
        if (weight2 < 4278190080L) {
            middle.start = incWeightTrail(weight2, this.middleLength);
        } else {
            middle.start = 4294967295L;
        }
        long weight3 = weight;
        for (int length2 = upperLength3; length2 > this.middleLength; length2--) {
            int trail2 = getWeightTrail(weight3, length2);
            if (trail2 > this.minBytes[length2]) {
                upper[length2] = new WeightRange();
                upper[length2].start = setWeightTrail(weight3, length2, this.minBytes[length2]);
                upper[length2].end = decWeightTrail(weight3, length2);
                upper[length2].length = length2;
                upper[length2].count = trail2 - this.minBytes[length2];
            }
            weight3 = truncateWeight(weight3, length2 - 1);
        }
        middle.end = decWeightTrail(weight3, this.middleLength);
        middle.length = this.middleLength;
        if (middle.end < middle.start) {
            int length3 = 4;
            while (true) {
                if (length3 <= this.middleLength) {
                    lower = lower2;
                    break;
                }
                if (lower2[length3] == null || upper[length3] == null || lower2[length3].count <= 0 || upper[length3].count <= 0) {
                    lower = lower2;
                    obj = obj2;
                } else {
                    long lowerEnd = lower2[length3].end;
                    long upperStart = upper[length3].start;
                    boolean merged = false;
                    if (lowerEnd > upperStart) {
                        lower = lower2;
                        lower2[length3].end = upper[length3].end;
                        lower[length3].count = (getWeightTrail(lower[length3].end, length3) - getWeightTrail(lower[length3].start, length3)) + 1;
                        merged = true;
                    } else {
                        lower = lower2;
                        if (lowerEnd != upperStart && incWeight(lowerEnd, length3) == upperStart) {
                            lower[length3].end = upper[length3].end;
                            lower[length3].count += upper[length3].count;
                            merged = true;
                        }
                    }
                    if (merged) {
                        upper[length3].count = 0;
                        while (true) {
                            length3--;
                            if (length3 <= this.middleLength) {
                                break;
                            }
                            upper[length3] = null;
                            lower[length3] = null;
                        }
                    } else {
                        obj = null;
                    }
                }
                length3--;
                obj2 = obj;
                lower2 = lower;
                long j = upperLimit;
            }
        } else {
            middle.count = ((int) ((middle.end - middle.start) >> (8 * (4 - this.middleLength)))) + 1;
            lower = lower2;
        }
        this.rangeCount = 0;
        if (middle.count > 0) {
            this.ranges[0] = middle;
            z = true;
            this.rangeCount = 1;
        } else {
            z = true;
        }
        for (int length4 = this.middleLength + (z ? 1 : 0); length4 <= 4; length4++) {
            if (upper[length4] != null && upper[length4].count > 0) {
                WeightRange[] weightRangeArr = this.ranges;
                int i = this.rangeCount;
                this.rangeCount = i + 1;
                weightRangeArr[i] = upper[length4];
            }
            if (lower[length4] != null && lower[length4].count > 0) {
                WeightRange[] weightRangeArr2 = this.ranges;
                int i2 = this.rangeCount;
                this.rangeCount = i2 + 1;
                weightRangeArr2[i2] = lower[length4];
            }
        }
        if (this.rangeCount <= 0) {
            z = false;
        }
        return z;
    }

    private boolean allocWeightsInShortRanges(int n, int minLength) {
        int n2 = n;
        int i = 0;
        while (i < this.rangeCount && this.ranges[i].length <= minLength + 1) {
            if (n2 <= this.ranges[i].count) {
                if (this.ranges[i].length > minLength) {
                    this.ranges[i].count = n2;
                }
                this.rangeCount = i + 1;
                if (this.rangeCount > 1) {
                    Arrays.sort(this.ranges, 0, this.rangeCount);
                }
                return true;
            }
            n2 -= this.ranges[i].count;
            i++;
        }
        return false;
    }

    private boolean allocWeightsInMinLengthRanges(int n, int minLength) {
        boolean z;
        int i = n;
        int i2 = minLength;
        int count = 0;
        int minLengthRangeCount = 0;
        while (minLengthRangeCount < this.rangeCount && this.ranges[minLengthRangeCount].length == i2) {
            count += this.ranges[minLengthRangeCount].count;
            minLengthRangeCount++;
        }
        int nextCountBytes = countBytes(i2 + 1);
        if (i > count * nextCountBytes) {
            return false;
        }
        long start = this.ranges[0].start;
        long end = this.ranges[0].end;
        long start2 = start;
        for (int i3 = 1; i3 < minLengthRangeCount; i3++) {
            if (this.ranges[i3].start < start2) {
                start2 = this.ranges[i3].start;
            }
            if (this.ranges[i3].end > end) {
                end = this.ranges[i3].end;
            }
        }
        int count2 = (i - count) / (nextCountBytes - 1);
        int count1 = count - count2;
        if (count2 == 0 || (count2 * nextCountBytes) + count1 < i) {
            count2++;
            count1--;
        }
        this.ranges[0].start = start2;
        if (count1 == 0) {
            this.ranges[0].end = end;
            this.ranges[0].count = count;
            lengthenRange(this.ranges[0]);
            this.rangeCount = 1;
            long j = end;
            z = true;
        } else {
            long end2 = end;
            this.ranges[0].end = incWeightByOffset(start2, i2, count1 - 1);
            this.ranges[0].count = count1;
            z = true;
            if (this.ranges[1] == null) {
                this.ranges[1] = new WeightRange();
            }
            this.ranges[1].start = incWeight(this.ranges[0].end, i2);
            this.ranges[1].end = end2;
            this.ranges[1].length = i2;
            this.ranges[1].count = count2;
            lengthenRange(this.ranges[1]);
            this.rangeCount = 2;
        }
        return z;
    }
}
