package com.huawei.nb.coordinator.breakpoint;

public class Range {
    private boolean allowToSetRange = false;
    private long rangeMax;
    private long rangeMin = 0;

    public boolean isAllowedToSetRange() {
        return this.allowToSetRange;
    }

    public void setAllowToSetRange(boolean allowToSetRange2) {
        this.allowToSetRange = allowToSetRange2;
    }

    public long getRangeMin() {
        return this.rangeMin;
    }

    public void setRangeMin(long rangeMin2) {
        this.rangeMin = rangeMin2;
    }

    public long getRangeMax() {
        return this.rangeMax;
    }

    public void setRangeMax(long rangeMax2) {
        this.rangeMax = rangeMax2;
    }

    public void setRange(long rangeMin2, long rangeMax2) {
        this.rangeMin = rangeMin2;
        this.rangeMax = rangeMax2;
    }
}
