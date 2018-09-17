package com.android.server.location;

public class InjectTimeRecord {
    private long injectTime;
    private int uncertainty;

    public long getInjectTime() {
        return this.injectTime;
    }

    public void setInjectTime(long injectTime) {
        this.injectTime = injectTime;
    }

    public int getUncertainty() {
        return this.uncertainty;
    }

    public void setUncertainty(int uncertainty) {
        this.uncertainty = uncertainty;
    }
}
