package com.android.server;

import android.content.Context;
import android.hardware.IConsumerIrService.Stub;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Slog;

public class ConsumerIrService extends Stub {
    private static final int MAX_XMIT_TIME = 2000000;
    private static final String TAG = "ConsumerIrService";
    private final Context mContext;
    private final Object mHalLock;
    private final long mNativeHal;
    private final WakeLock mWakeLock;

    private static native int[] halGetCarrierFrequencies(long j);

    private static native int[] halLearnIR(long j, int i);

    private static native long halOpen();

    private static native int halTransmit(long j, int i, int[] iArr);

    private static native void halcancelLearn(long j);

    ConsumerIrService(Context context) {
        this.mHalLock = new Object();
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG);
        this.mWakeLock.setReferenceCounted(true);
        this.mNativeHal = halOpen();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.consumerir")) {
            if (this.mNativeHal == 0) {
                throw new RuntimeException("FEATURE_CONSUMER_IR present, but no IR HAL loaded!");
            }
        } else if (this.mNativeHal != 0) {
            throw new RuntimeException("IR HAL present, but FEATURE_CONSUMER_IR is not set!");
        }
    }

    public boolean hasIrEmitter() {
        return this.mNativeHal != 0;
    }

    private void throwIfNoIrEmitter() {
        if (this.mNativeHal == 0) {
            throw new UnsupportedOperationException("IR emitter not available");
        }
    }

    public void transmit(String packageName, int carrierFrequency, int[] pattern) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.TRANSMIT_IR") != 0) {
            throw new SecurityException("Requires TRANSMIT_IR permission");
        }
        long totalXmitTime = 0;
        for (int slice : pattern) {
            if (slice <= 0) {
                throw new IllegalArgumentException("Non-positive IR slice");
            }
            totalXmitTime += (long) slice;
        }
        if (totalXmitTime > 2000000) {
            throw new IllegalArgumentException("IR pattern too long");
        }
        throwIfNoIrEmitter();
        synchronized (this.mHalLock) {
            int err = halTransmit(this.mNativeHal, carrierFrequency, pattern);
            if (err < 0) {
                Slog.e(TAG, "Error transmitting: " + err);
            }
        }
    }

    public int[] learnIR(int timeout) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.TRANSMIT_IR") != 0) {
            throw new SecurityException("Requires TRANSMIT_IR permission");
        }
        int[] halLearnIR;
        throwIfNoIrEmitter();
        Slog.e(TAG, "learnIR I .");
        synchronized (this.mHalLock) {
            Slog.e(TAG, "learnIR II ");
            halLearnIR = halLearnIR(this.mNativeHal, timeout);
        }
        return halLearnIR;
    }

    public void cancelLearn() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.TRANSMIT_IR") != 0) {
            throw new SecurityException("Requires TRANSMIT_IR permission");
        }
        throwIfNoIrEmitter();
        synchronized (this.mHalLock) {
            Slog.e(TAG, "cancelLearn in java server I..");
            halcancelLearn(this.mNativeHal);
        }
    }

    public int[] getCarrierFrequencies() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.TRANSMIT_IR") != 0) {
            throw new SecurityException("Requires TRANSMIT_IR permission");
        }
        int[] halGetCarrierFrequencies;
        throwIfNoIrEmitter();
        synchronized (this.mHalLock) {
            halGetCarrierFrequencies = halGetCarrierFrequencies(this.mNativeHal);
        }
        return halGetCarrierFrequencies;
    }
}
