package com.android.server.location;

import android.location.GnssMeasurementCorrections;
import android.os.Handler;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;

public class GnssMeasurementCorrectionsProvider {
    static final int CAPABILITY_EXCESS_PATH_LENGTH = 2;
    static final int CAPABILITY_LOS_SATS = 1;
    static final int CAPABILITY_REFLECTING_PLANE = 4;
    private static final int INVALID_CAPABILITIES = Integer.MIN_VALUE;
    private static final String TAG = "GnssMeasurementCorrectionsProvider";
    private volatile int mCapabilities;
    private final Handler mHandler;
    private final GnssMeasurementCorrectionsProviderNative mNative;

    /* access modifiers changed from: private */
    public static native boolean native_inject_gnss_measurement_corrections(GnssMeasurementCorrections gnssMeasurementCorrections);

    /* access modifiers changed from: private */
    public static native boolean native_is_measurement_corrections_supported();

    GnssMeasurementCorrectionsProvider(Handler handler) {
        this(handler, new GnssMeasurementCorrectionsProviderNative());
    }

    @VisibleForTesting
    GnssMeasurementCorrectionsProvider(Handler handler, GnssMeasurementCorrectionsProviderNative aNative) {
        this.mCapabilities = Integer.MIN_VALUE;
        this.mHandler = handler;
        this.mNative = aNative;
    }

    public boolean isAvailableInPlatform() {
        return this.mNative.isMeasurementCorrectionsSupported();
    }

    public void injectGnssMeasurementCorrections(GnssMeasurementCorrections measurementCorrections) {
        if (!isCapabilitiesReceived()) {
            Log.w(TAG, "Failed to inject GNSS measurement corrections. Capabilities not received yet.");
        } else {
            this.mHandler.post(new Runnable(measurementCorrections) {
                /* class com.android.server.location.$$Lambda$GnssMeasurementCorrectionsProvider$VUSA1ROgV90b6YMNVx53Jh7SSc8 */
                private final /* synthetic */ GnssMeasurementCorrections f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    GnssMeasurementCorrectionsProvider.this.lambda$injectGnssMeasurementCorrections$0$GnssMeasurementCorrectionsProvider(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$injectGnssMeasurementCorrections$0$GnssMeasurementCorrectionsProvider(GnssMeasurementCorrections measurementCorrections) {
        if (!this.mNative.injectGnssMeasurementCorrections(measurementCorrections)) {
            Log.e(TAG, "Failure in injecting GNSS corrections.");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onCapabilitiesUpdated(int capabilities) {
        if (hasCapability(capabilities, 1) || hasCapability(capabilities, 2)) {
            this.mCapabilities = capabilities;
            return true;
        }
        Log.e(TAG, "Failed to set capabilities. Received capabilities 0x" + Integer.toHexString(capabilities) + " does not contain the mandatory LOS_SATS or the EXCESS_PATH_LENGTH capability.");
        return false;
    }

    /* access modifiers changed from: package-private */
    public int getCapabilities() {
        return this.mCapabilities;
    }

    /* access modifiers changed from: package-private */
    public String toStringCapabilities() {
        int capabilities = getCapabilities();
        StringBuilder s = new StringBuilder();
        s.append("mCapabilities=0x");
        s.append(Integer.toHexString(capabilities));
        s.append(" ( ");
        if (hasCapability(capabilities, 1)) {
            s.append("LOS_SATS ");
        }
        if (hasCapability(capabilities, 2)) {
            s.append("EXCESS_PATH_LENGTH ");
        }
        if (hasCapability(capabilities, 4)) {
            s.append("REFLECTING_PLANE ");
        }
        s.append(")");
        return s.toString();
    }

    private static boolean hasCapability(int halCapabilities, int capability) {
        return (halCapabilities & capability) != 0;
    }

    private boolean isCapabilitiesReceived() {
        return this.mCapabilities != Integer.MIN_VALUE;
    }

    @VisibleForTesting
    static class GnssMeasurementCorrectionsProviderNative {
        GnssMeasurementCorrectionsProviderNative() {
        }

        public boolean isMeasurementCorrectionsSupported() {
            return GnssMeasurementCorrectionsProvider.native_is_measurement_corrections_supported();
        }

        public boolean injectGnssMeasurementCorrections(GnssMeasurementCorrections measurementCorrections) {
            return GnssMeasurementCorrectionsProvider.native_inject_gnss_measurement_corrections(measurementCorrections);
        }
    }
}
