package com.huawei.android.biometric;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.HwBinderEx;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class TouchscreenEx {
    private static final String TAG = "TouchscreenEx";
    private ITouchscreen mTouchscreen;

    public void setTouchscreen(ITouchscreen touchscreen) {
        this.mTouchscreen = touchscreen;
    }

    public int hwSetFeatureConfig(int flag, String cmd) {
        try {
            if (this.mTouchscreen != null) {
                return this.mTouchscreen.hwSetFeatureConfig(flag, cmd);
            }
            return 0;
        } catch (RemoteException e) {
            Log.w(TAG, "hwSetFeatureConfig");
            return 0;
        }
    }

    public void linkToDeath(HwBinderEx.DeathRecipientEx deathRecipient, int deathCookie) {
        try {
            if (this.mTouchscreen != null) {
                this.mTouchscreen.linkToDeath(deathRecipient.getDeathRecipient(), (long) deathCookie);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "linkToDeath");
        }
    }
}
