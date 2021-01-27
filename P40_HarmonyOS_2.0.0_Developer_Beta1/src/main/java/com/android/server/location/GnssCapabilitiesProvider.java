package com.android.server.location;

import android.location.GnssCapabilities;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;

public class GnssCapabilitiesProvider {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final long GNSS_CAPABILITIES_SUB_HAL_MEASUREMENT_CORRECTIONS = 480;
    private static final long GNSS_CAPABILITIES_TOP_HAL = 31;
    private static final String TAG = "GnssCapabilitiesProvider";
    @GuardedBy({"this"})
    private long mGnssCapabilities;

    public long getGnssCapabilities() {
        long j;
        synchronized (this) {
            j = this.mGnssCapabilities;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    public void setTopHalCapabilities(int topHalCapabilities) {
        long gnssCapabilities = 0;
        if (hasCapability(topHalCapabilities, 256)) {
            gnssCapabilities = 0 | 1;
        }
        if (hasCapability(topHalCapabilities, 512)) {
            gnssCapabilities |= 2;
        }
        if (hasCapability(topHalCapabilities, 32)) {
            gnssCapabilities |= 4;
        }
        if (hasCapability(topHalCapabilities, 64)) {
            gnssCapabilities |= 8;
        }
        if (hasCapability(topHalCapabilities, 128)) {
            gnssCapabilities |= 16;
        }
        synchronized (this) {
            this.mGnssCapabilities &= -32;
            this.mGnssCapabilities |= gnssCapabilities;
            if (DEBUG) {
                Log.d(TAG, "setTopHalCapabilities, mGnssCapabilities=0x" + Long.toHexString(this.mGnssCapabilities) + ", " + GnssCapabilities.of(this.mGnssCapabilities));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setSubHalMeasurementCorrectionsCapabilities(int measurementCorrectionsCapabilities) {
        long gnssCapabilities = 32;
        if (hasCapability(measurementCorrectionsCapabilities, 1)) {
            gnssCapabilities = 32 | 64;
        }
        if (hasCapability(measurementCorrectionsCapabilities, 2)) {
            gnssCapabilities |= 128;
        }
        if (hasCapability(measurementCorrectionsCapabilities, 4)) {
            gnssCapabilities |= 256;
        }
        synchronized (this) {
            this.mGnssCapabilities &= -481;
            this.mGnssCapabilities |= gnssCapabilities;
            if (DEBUG) {
                Log.d(TAG, "setSubHalMeasurementCorrectionsCapabilities, mGnssCapabilities=0x" + Long.toHexString(this.mGnssCapabilities) + ", " + GnssCapabilities.of(this.mGnssCapabilities));
            }
        }
    }

    private static boolean hasCapability(int halCapabilities, int capability) {
        return (halCapabilities & capability) != 0;
    }
}
