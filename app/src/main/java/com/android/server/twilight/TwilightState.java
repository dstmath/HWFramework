package com.android.server.twilight;

import java.text.DateFormat;

public class TwilightState {
    private final float mAmount;
    private final boolean mIsNight;

    TwilightState(boolean isNight, float amount) {
        this.mIsNight = isNight;
        this.mAmount = amount;
    }

    public boolean isNight() {
        return this.mIsNight;
    }

    public float getAmount() {
        return this.mAmount;
    }

    public boolean equals(Object o) {
        return o instanceof TwilightState ? equals((TwilightState) o) : false;
    }

    public boolean equals(TwilightState other) {
        if (other != null && this.mIsNight == other.mIsNight && this.mAmount == other.mAmount) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        DateFormat f = DateFormat.getDateTimeInstance();
        return "{TwilightState: isNight=" + this.mIsNight + ", mAmount=" + this.mAmount + "}";
    }
}
