package com.huawei.zxing.oned.rss;

import com.huawei.zxing.ResultPoint;

public final class FinderPattern {
    private final ResultPoint[] resultPoints;
    private final int[] startEnd;
    private final int value;

    public FinderPattern(int value2, int[] startEnd2, int start, int end, int rowNumber) {
        this.value = value2;
        this.startEnd = startEnd2;
        this.resultPoints = new ResultPoint[]{new ResultPoint((float) start, (float) rowNumber), new ResultPoint((float) end, (float) rowNumber)};
    }

    public int getValue() {
        return this.value;
    }

    public int[] getStartEnd() {
        return this.startEnd;
    }

    public ResultPoint[] getResultPoints() {
        return this.resultPoints;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof FinderPattern)) {
            return false;
        }
        if (this.value == ((FinderPattern) o).value) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.value;
    }
}
