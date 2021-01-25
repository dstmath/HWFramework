package com.huawei.nb.coordinator.breakpoint;

public class Range {
    private boolean allowToSetRange = false;
    private long rangeMax;
    private long rangeMin = 0;

    public boolean isAllowedToSetRange() {
        return this.allowToSetRange;
    }

    public void setAllowToSetRange(boolean z) {
        this.allowToSetRange = z;
    }

    public long getRangeMin() {
        return this.rangeMin;
    }

    public long getRangeMax() {
        return this.rangeMax;
    }

    public void setRange(long j, long j2) {
        this.rangeMin = j;
        this.rangeMax = j2;
    }
}
