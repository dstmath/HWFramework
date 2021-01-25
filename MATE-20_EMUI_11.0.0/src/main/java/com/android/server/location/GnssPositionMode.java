package com.android.server.location;

import java.util.Arrays;

public class GnssPositionMode {
    private final boolean lowPowerMode;
    private final int minInterval;
    private final int mode;
    private final int preferredAccuracy;
    private final int preferredTime;
    private final int recurrence;

    public GnssPositionMode(int mode2, int recurrence2, int minInterval2, int preferredAccuracy2, int preferredTime2, boolean lowPowerMode2) {
        this.mode = mode2;
        this.recurrence = recurrence2;
        this.minInterval = minInterval2;
        this.preferredAccuracy = preferredAccuracy2;
        this.preferredTime = preferredTime2;
        this.lowPowerMode = lowPowerMode2;
    }

    public boolean equals(Object other) {
        if (!(other instanceof GnssPositionMode)) {
            return false;
        }
        GnssPositionMode that = (GnssPositionMode) other;
        if (this.mode == that.mode && this.recurrence == that.recurrence && this.minInterval == that.minInterval && this.preferredAccuracy == that.preferredAccuracy && this.preferredTime == that.preferredTime && this.lowPowerMode == that.lowPowerMode && getClass() == that.getClass()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Integer.valueOf(this.mode), Integer.valueOf(this.recurrence), Integer.valueOf(this.minInterval), Integer.valueOf(this.preferredAccuracy), Integer.valueOf(this.preferredTime), Boolean.valueOf(this.lowPowerMode), getClass()});
    }
}
