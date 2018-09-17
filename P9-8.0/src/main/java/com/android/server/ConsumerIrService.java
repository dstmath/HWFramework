package com.android.server;

import android.content.Context;
import android.hardware.IConsumerIrService.Stub;
import android.hdm.HwDeviceManager;
import android.media.AudioSystem;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Slog;
import android.widget.Toast;

public class ConsumerIrService extends Stub {
    private static final int HNADLER_DELAY_TIME = 100;
    private static final int MAX_XMIT_TIME = 2000000;
    private static final String TAG = "ConsumerIrService";
    private final Context mContext;
    private final Object mHalLock = new Object();
    private final boolean mHasNativeHal;
    private String mParameter = "";
    private final WakeLock mWakeLock;

    private static native int[] halGetCarrierFrequencies();

    private static native boolean halOpen();

    private static native int halTransmit(int i, int[] iArr);

    ConsumerIrService(Context context) {
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG);
        this.mWakeLock.setReferenceCounted(true);
        this.mHasNativeHal = halOpen();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.consumerir")) {
            if (!this.mHasNativeHal) {
                throw new RuntimeException("FEATURE_CONSUMER_IR present, but no IR HAL loaded!");
            }
        } else if (this.mHasNativeHal) {
            throw new RuntimeException("IR HAL present, but FEATURE_CONSUMER_IR is not set!");
        }
        this.mParameter = AudioSystem.getParameters("audio_capability#irda_support");
    }

    public boolean hasIrEmitter() {
        return this.mHasNativeHal;
    }

    private void throwIfNoIrEmitter() {
        if (!this.mHasNativeHal) {
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
        if (HwDeviceManager.disallowOp(48)) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    Toast.makeText(ConsumerIrService.this.mContext, ConsumerIrService.this.mContext.getResources().getString(33685956), 0).show();
                }
            }, 100);
            return;
        }
        synchronized (this.mHalLock) {
            setStartTransmitParameter();
            int err = halTransmit(carrierFrequency, pattern);
            if (err < 0) {
                Slog.e(TAG, "Error transmitting: " + err);
            }
            setEndTransmitParameter();
        }
    }

    public int[] getCarrierFrequencies() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.TRANSMIT_IR") != 0) {
            throw new SecurityException("Requires TRANSMIT_IR permission");
        }
        int[] halGetCarrierFrequencies;
        throwIfNoIrEmitter();
        synchronized (this.mHalLock) {
            halGetCarrierFrequencies = halGetCarrierFrequencies();
        }
        return halGetCarrierFrequencies;
    }

    private void setStartTransmitParameter() {
        if (this.mParameter != null && this.mParameter.equals("true")) {
            AudioSystem.setParameters("ir_trans=on");
        }
    }

    private void setEndTransmitParameter() {
        if (this.mParameter != null && this.mParameter.equals("true")) {
            AudioSystem.setParameters("ir_trans=off");
        }
    }
}
