package com.android.server;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.hardware.IConsumerIrService;
import android.hdm.HwDeviceManager;
import android.media.AudioSystem;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Slog;
import android.widget.Toast;
import huawei.android.security.IHwBehaviorCollectManager;

public class ConsumerIrService extends IConsumerIrService.Stub {
    private static final int HNADLER_DELAY_TIME = 100;
    private static final int MAX_XMIT_TIME = 2000000;
    private static final String TAG = "ConsumerIrService";
    /* access modifiers changed from: private */
    public final Context mContext;
    private final Object mHalLock = new Object();
    private boolean mHasNativeHal = false;
    private String mParameter = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    private final PowerManager.WakeLock mWakeLock;

    private static native int[] halGetCarrierFrequencies();

    private static native boolean halOpen();

    private static native int halTransmit(int i, int[] iArr);

    ConsumerIrService(Context context) {
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG);
        this.mWakeLock.setReferenceCounted(true);
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.consumerir")) {
            this.mHasNativeHal = halOpen();
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
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONSUMERIR_TRANSMIT);
        if (this.mContext.checkCallingOrSelfPermission("android.permission.TRANSMIT_IR") == 0) {
            long totalXmitTime = 0;
            int length = pattern.length;
            int i = 0;
            while (i < length) {
                int slice = pattern[i];
                if (slice > 0) {
                    totalXmitTime += (long) slice;
                    i++;
                } else {
                    throw new IllegalArgumentException("Non-positive IR slice");
                }
            }
            if (totalXmitTime <= 2000000) {
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
                return;
            }
            throw new IllegalArgumentException("IR pattern too long");
        }
        throw new SecurityException("Requires TRANSMIT_IR permission");
    }

    public int[] getCarrierFrequencies() {
        int[] halGetCarrierFrequencies;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.TRANSMIT_IR") == 0) {
            throwIfNoIrEmitter();
            synchronized (this.mHalLock) {
                halGetCarrierFrequencies = halGetCarrierFrequencies();
            }
            return halGetCarrierFrequencies;
        }
        throw new SecurityException("Requires TRANSMIT_IR permission");
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
