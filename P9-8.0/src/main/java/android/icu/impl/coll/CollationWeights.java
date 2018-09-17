package android.icu.impl.coll;

import java.util.Arrays;

public final class CollationWeights {
    static final /* synthetic */ boolean -assertionsDisabled = (CollationWeights.class.desiredAssertionStatus() ^ 1);
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

        /* synthetic */ WeightRange(WeightRange -this0) {
            this();
        }

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
            if (!allocWeightsInShortRanges(n, minLength)) {
                if (minLength != 4) {
                    if (allocWeightsInMinLengthRanges(n, minLength)) {
                        break;
                    }
                    for (int i = 0; this.ranges[i].length == minLength; i++) {
                        lengthenRange(this.ranges[i]);
                    }
                } else {
                    return false;
                }
            }
            break;
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
            if (!-assertionsDisabled && range.start > range.end) {
                throw new AssertionError();
            }
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
        return ((int) (weight >> ((4 - length) * 8))) & 255;
    }

    private static long setWeightTrail(long weight, int length, int trail) {
        length = (4 - length) * 8;
        return ((CollationRootElements.PRIMARY_SENTINEL << length) & weight) | (((long) trail) << length);
    }

    private static int getWeightByte(long weight, int idx) {
        return getWeightTrail(weight, idx);
    }

    private static long setWeightByte(long weight, int idx, int b) {
        long mask;
        idx *= 8;
        if (idx < 32) {
            mask = 4294967295L >> idx;
        } else {
            mask = 0;
        }
        idx = 32 - idx;
        return (weight & (mask | (CollationRootElements.PRIMARY_SENTINEL << idx))) | (((long) b) << idx);
    }

    private static long truncateWeight(long weight, int length) {
        return (4294967295L << ((4 - length) * 8)) & weight;
    }

    private static long incWeightTrail(long weight, int length) {
        return (1 << ((4 - length) * 8)) + weight;
    }

    private static long decWeightTrail(long weight, int length) {
        return weight - (1 << ((4 - length) * 8));
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
            if (!-assertionsDisabled && length <= 0) {
                throw new AssertionError();
            }
        }
    }

    private long incWeightByOffset(long weight, int length, int offset) {
        while (true) {
            offset += getWeightByte(weight, length);
            if (offset <= this.maxBytes[length]) {
                return setWeightByte(weight, length, offset);
            }
            offset -= this.minBytes[length];
            weight = setWeightByte(weight, length, this.minBytes[length] + (offset % countBytes(length)));
            offset /= countBytes(length);
            length--;
            if (!-assertionsDisabled && length <= 0) {
                throw new AssertionError();
            }
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
        if (!-assertionsDisabled && lowerLimit == 0) {
            throw new AssertionError();
        } else if (-assertionsDisabled || upperLimit != 0) {
            int lowerLength = lengthOfWeight(lowerLimit);
            int upperLength = lengthOfWeight(upperLimit);
            if (!-assertionsDisabled && lowerLength < this.middleLength) {
                throw new AssertionError();
            } else if (lowerLimit >= upperLimit) {
                return false;
            } else {
                if (lowerLength < upperLength && lowerLimit == truncateWeight(upperLimit, lowerLength)) {
                    return false;
                }
                int length;
                int trail;
                WeightRange[] lower = new WeightRange[5];
                WeightRange middle = new WeightRange(null);
                WeightRange[] upper = new WeightRange[5];
                long weight = lowerLimit;
                for (length = lowerLength; length > this.middleLength; length--) {
                    trail = getWeightTrail(weight, length);
                    if (trail < this.maxBytes[length]) {
                        lower[length] = new WeightRange();
                        lower[length].start = incWeightTrail(weight, length);
                        lower[length].end = setWeightTrail(weight, length, this.maxBytes[length]);
                        lower[length].length = length;
                        lower[length].count = this.maxBytes[length] - trail;
                    }
                    weight = truncateWeight(weight, length - 1);
                }
                if (weight < 4278190080L) {
                    middle.start = incWeightTrail(weight, this.middleLength);
                } else {
                    middle.start = 4294967295L;
                }
                weight = upperLimit;
                for (length = upperLength; length > this.middleLength; length--) {
                    trail = getWeightTrail(weight, length);
                    if (trail > this.minBytes[length]) {
                        upper[length] = new WeightRange();
                        upper[length].start = setWeightTrail(weight, length, this.minBytes[length]);
                        upper[length].end = decWeightTrail(weight, length);
                        upper[length].length = length;
                        upper[length].count = trail - this.minBytes[length];
                    }
                    weight = truncateWeight(weight, length - 1);
                }
                middle.end = decWeightTrail(weight, this.middleLength);
                middle.length = this.middleLength;
                if (middle.end >= middle.start) {
                    middle.count = ((int) ((middle.end - middle.start) >> ((4 - this.middleLength) * 8))) + 1;
                } else {
                    length = 4;
                    while (length > this.middleLength) {
                        if (lower[length] != null && upper[length] != null && lower[length].count > 0 && upper[length].count > 0) {
                            long lowerEnd = lower[length].end;
                            long upperStart = upper[length].start;
                            boolean merged = false;
                            if (lowerEnd > upperStart) {
                                if (-assertionsDisabled || truncateWeight(lowerEnd, length - 1) == truncateWeight(upperStart, length - 1)) {
                                    lower[length].end = upper[length].end;
                                    lower[length].count = (getWeightTrail(lower[length].end, length) - getWeightTrail(lower[length].start, length)) + 1;
                                    merged = true;
                                } else {
                                    throw new AssertionError();
                                }
                            } else if (lowerEnd == upperStart) {
                                if (!-assertionsDisabled && this.minBytes[length] >= this.maxBytes[length]) {
                                    throw new AssertionError();
                                }
                            } else if (incWeight(lowerEnd, length) == upperStart) {
                                lower[length].end = upper[length].end;
                                WeightRange weightRange = lower[length];
                                weightRange.count += upper[length].count;
                                merged = true;
                            }
                            if (merged) {
                                upper[length].count = 0;
                                while (true) {
                                    length--;
                                    if (length <= this.middleLength) {
                                        break;
                                    }
                                    upper[length] = null;
                                    lower[length] = null;
                                }
                            }
                        }
                        length--;
                    }
                }
                this.rangeCount = 0;
                if (middle.count > 0) {
                    this.ranges[0] = middle;
                    this.rangeCount = 1;
                }
                length = this.middleLength + 1;
                while (length <= 4) {
                    WeightRange[] weightRangeArr;
                    int i;
                    if (upper[length] != null && upper[length].count > 0) {
                        weightRangeArr = this.ranges;
                        i = this.rangeCount;
                        this.rangeCount = i + 1;
                        weightRangeArr[i] = upper[length];
                    }
                    if (lower[length] != null && lower[length].count > 0) {
                        weightRangeArr = this.ranges;
                        i = this.rangeCount;
                        this.rangeCount = i + 1;
                        weightRangeArr[i] = lower[length];
                    }
                    length++;
                }
                return this.rangeCount > 0;
            }
        } else {
            throw new AssertionError();
        }
    }

    private boolean allocWeightsInShortRanges(int n, int minLength) {
        int i = 0;
        while (i < this.rangeCount && this.ranges[i].length <= minLength + 1) {
            if (n <= this.ranges[i].count) {
                if (this.ranges[i].length > minLength) {
                    this.ranges[i].count = n;
                }
                this.rangeCount = i + 1;
                if (this.rangeCount > 1) {
                    Arrays.sort(this.ranges, 0, this.rangeCount);
                }
                return true;
            }
            n -= this.ranges[i].count;
            i++;
        }
        return false;
    }

    private boolean allocWeightsInMinLengthRanges(int n, int minLength) {
        int count = 0;
        int minLengthRangeCount = 0;
        while (minLengthRangeCount < this.rangeCount && this.ranges[minLengthRangeCount].length == minLength) {
            count += this.ranges[minLengthRangeCount].count;
            minLengthRangeCount++;
        }
        int nextCountBytes = countBytes(minLength + 1);
        if (n > count * nextCountBytes) {
            return false;
        }
        long start = this.ranges[0].start;
        long end = this.ranges[0].end;
        for (int i = 1; i < minLengthRangeCount; i++) {
            if (this.ranges[i].start < start) {
                start = this.ranges[i].start;
            }
            if (this.ranges[i].end > end) {
                end = this.ranges[i].end;
            }
        }
        int count2 = (n - count) / (nextCountBytes - 1);
        int count1 = count - count2;
        if (count2 == 0 || (count2 * nextCountBytes) + count1 < n) {
            count2++;
            count1--;
            if (!-assertionsDisabled && (count2 * nextCountBytes) + count1 < n) {
                throw new AssertionError();
            }
        }
        this.ranges[0].start = start;
        if (count1 == 0) {
            this.ranges[0].end = end;
            this.ranges[0].count = count;
            lengthenRange(this.ranges[0]);
            this.rangeCount = 1;
        } else {
            this.ranges[0].end = incWeightByOffset(start, minLength, count1 - 1);
            this.ranges[0].count = count1;
            if (this.ranges[1] == null) {
                this.ranges[1] = new WeightRange();
            }
            this.ranges[1].start = incWeight(this.ranges[0].end, minLength);
            this.ranges[1].end = end;
            this.ranges[1].length = minLength;
            this.ranges[1].count = count2;
            lengthenRange(this.ranges[1]);
            this.rangeCount = 2;
        }
        return true;
    }
}
