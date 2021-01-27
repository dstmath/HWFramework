package com.android.location.provider;

import android.location.LocationRequest;

public final class LocationRequestUnbundled {
    public static final int ACCURACY_BLOCK = 102;
    public static final int ACCURACY_CITY = 104;
    public static final int ACCURACY_FINE = 100;
    public static final int POWER_HIGH = 203;
    public static final int POWER_LOW = 201;
    public static final int POWER_NONE = 200;
    private final LocationRequest delegate;

    LocationRequestUnbundled(LocationRequest delegate2) {
        this.delegate = delegate2;
    }

    public long getInterval() {
        return this.delegate.getInterval();
    }

    public long getFastestInterval() {
        return this.delegate.getFastestInterval();
    }

    public int getQuality() {
        return this.delegate.getQuality();
    }

    public float getSmallestDisplacement() {
        return this.delegate.getSmallestDisplacement();
    }

    public boolean isLocationSettingsIgnored() {
        return this.delegate.isLocationSettingsIgnored();
    }

    public String toString() {
        return this.delegate.toString();
    }
}
