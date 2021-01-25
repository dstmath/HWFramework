package com.android.server.location;

public class InjectTimeRecord {
    private long injectTime;
    private int uncertainty;

    public long getInjectTime() {
        return this.injectTime;
    }

    public void setInjectTime(long injectTime2) {
        this.injectTime = injectTime2;
    }

    public int getUncertainty() {
        return this.uncertainty;
    }

    public void setUncertainty(int uncertainty2) {
        this.uncertainty = uncertainty2;
    }
}
