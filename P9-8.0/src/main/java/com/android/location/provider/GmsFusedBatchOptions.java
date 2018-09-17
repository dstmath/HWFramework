package com.android.location.provider;

import android.location.FusedBatchOptions;

public class GmsFusedBatchOptions {
    private FusedBatchOptions mOptions = new FusedBatchOptions();

    public static final class BatchFlags {
        public static int CALLBACK_ON_LOCATION_FIX = 2;
        public static int WAKEUP_ON_FIFO_FULL = 1;
    }

    public static final class SourceTechnologies {
        public static int BLUETOOTH = 16;
        public static int CELL = 8;
        public static int GNSS = 1;
        public static int SENSORS = 4;
        public static int WIFI = 2;
    }

    public void setMaxPowerAllocationInMW(double value) {
        this.mOptions.setMaxPowerAllocationInMW(value);
    }

    public double getMaxPowerAllocationInMW() {
        return this.mOptions.getMaxPowerAllocationInMW();
    }

    public void setPeriodInNS(long value) {
        this.mOptions.setPeriodInNS(value);
    }

    public long getPeriodInNS() {
        return this.mOptions.getPeriodInNS();
    }

    public void setSmallestDisplacementMeters(float value) {
        this.mOptions.setSmallestDisplacementMeters(value);
    }

    public float getSmallestDisplacementMeters() {
        return this.mOptions.getSmallestDisplacementMeters();
    }

    public void setSourceToUse(int source) {
        this.mOptions.setSourceToUse(source);
    }

    public void resetSourceToUse(int source) {
        this.mOptions.resetSourceToUse(source);
    }

    public boolean isSourceToUseSet(int source) {
        return this.mOptions.isSourceToUseSet(source);
    }

    public int getSourcesToUse() {
        return this.mOptions.getSourcesToUse();
    }

    public void setFlag(int flag) {
        this.mOptions.setFlag(flag);
    }

    public void resetFlag(int flag) {
        this.mOptions.resetFlag(flag);
    }

    public boolean isFlagSet(int flag) {
        return this.mOptions.isFlagSet(flag);
    }

    public int getFlags() {
        return this.mOptions.getFlags();
    }

    public FusedBatchOptions getParcelableOptions() {
        return this.mOptions;
    }
}
