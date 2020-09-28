package com.huawei.dmsdp.devicevirtualization;

import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.HwLog;

public class VirtualVibrator {
    private static final String TAG = "VirtualVibrator";
    private static final Object VIBRATOR_LOCK = new Object();
    private static DMSDPAdapter mDMSDPAdapter = null;
    private String mDeviceId;
    private int mVibrateId;

    VirtualVibrator(int vibrateId, String deviceId) {
        this.mVibrateId = vibrateId;
        this.mDeviceId = deviceId;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public int getVibrateId() {
        return this.mVibrateId;
    }

    /* access modifiers changed from: package-private */
    public void setVibrateId(int vibrateId) {
        this.mVibrateId = vibrateId;
    }

    /* access modifiers changed from: package-private */
    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public void vibrate(long milliseconds) {
        synchronized (VIBRATOR_LOCK) {
            if (mDMSDPAdapter != null) {
                HwLog.d(TAG, "vibrate");
                mDMSDPAdapter.vibrate(getDeviceId(), getVibrateId(), milliseconds);
            }
        }
    }

    public void vibrate(long[] pattern, int repeat) {
        synchronized (VIBRATOR_LOCK) {
            if (mDMSDPAdapter != null) {
                if (pattern != null) {
                    HwLog.d(TAG, "vibrateRepeat");
                    mDMSDPAdapter.vibrateRepeat(getDeviceId(), getVibrateId(), pattern, repeat);
                }
            }
        }
    }

    public void cancel() {
        synchronized (VIBRATOR_LOCK) {
            if (mDMSDPAdapter != null) {
                mDMSDPAdapter.vibrateCancel(getDeviceId(), getVibrateId());
            }
        }
    }

    protected static void onConnect(VirtualService dmsdpService) {
        synchronized (VIBRATOR_LOCK) {
            if (dmsdpService != null) {
                mDMSDPAdapter = dmsdpService.getDMSDPAdapter();
                if (mDMSDPAdapter == null) {
                    HwLog.e(TAG, "dmsdpAdapter is null when register dmsdpListener");
                }
            }
        }
    }

    protected static void onDisConnect() {
        synchronized (VIBRATOR_LOCK) {
            mDMSDPAdapter = null;
        }
    }
}
