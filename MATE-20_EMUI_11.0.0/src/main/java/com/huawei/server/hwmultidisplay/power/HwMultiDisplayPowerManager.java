package com.huawei.server.hwmultidisplay.power;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.HwPCUtils;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.screenrecorder.activities.SurfaceControlEx;
import com.huawei.server.UiThreadEx;

public class HwMultiDisplayPowerManager {
    private static final Object LOCK = new Object();
    private static final String TAG = "HwMultiDisplayPowerManager";
    private static volatile HwMultiDisplayPowerManager sInstance = null;
    private FoldDisplayListener mFoldDisplayListener;
    private final Runnable mRunner = new Runnable() {
        /* class com.huawei.server.hwmultidisplay.power.HwMultiDisplayPowerManager.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            HwMultiDisplayPowerManager.this.setScreenPowerOff();
        }
    };
    private boolean mScreenPowerOn = true;
    Handler mUIHandler = new Handler(UiThreadEx.getLooper());

    private final class FoldDisplayListener implements HwFoldScreenManagerEx.FoldDisplayModeListener {
        private FoldDisplayListener() {
        }

        public void onScreenDisplayModeChange(int displayMode) {
            boolean isScreenOnFully = HwMultiDisplayPowerManager.this.getScreenPowerOn();
            HwPCUtils.log(HwMultiDisplayPowerManager.TAG, "Fold display mode changed, isScreenOnFully:" + isScreenOnFully);
            if (!isScreenOnFully) {
                HwMultiDisplayPowerManager.this.setScreenPowerInner(true, false);
            }
        }
    }

    private HwMultiDisplayPowerManager() {
        if (HwFoldScreenManagerEx.isInwardFoldDevice() && this.mFoldDisplayListener == null) {
            this.mFoldDisplayListener = new FoldDisplayListener();
            HwFoldScreenManagerEx.registerFoldDisplayMode(this.mFoldDisplayListener);
        }
    }

    public static HwMultiDisplayPowerManager getDefault() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new HwMultiDisplayPowerManager();
                }
            }
        }
        return sInstance;
    }

    public boolean getScreenPowerOn() {
        boolean z;
        synchronized (LOCK) {
            z = this.mScreenPowerOn;
        }
        return z;
    }

    public void setScreenPowerOn(boolean powerOn) {
        synchronized (LOCK) {
            this.mScreenPowerOn = powerOn;
        }
    }

    public void setScreenPowerInner(boolean powerOn, boolean checking) {
        if (HwPCUtils.isInWindowsCastMode()) {
            setScreenPowerForWindowsCast(powerOn, checking);
        } else {
            setScreenPower(powerOn, checking);
        }
    }

    public void setScreenPower(boolean powerOn, boolean checking) {
        synchronized (LOCK) {
            HwPCUtils.log(TAG, "setScreenPower old = " + this.mScreenPowerOn + " new = " + powerOn);
            if (powerOn != this.mScreenPowerOn || !checking) {
                IBinder displayToken = SurfaceControlEx.getInternalDisplayToken();
                if (displayToken == null) {
                    HwPCUtils.log(TAG, "Failed to setScreenPowerInner because internal display is disconnected");
                    return;
                }
                SurfaceControlEx.setDisplayPowerMode(displayToken, powerOn ? 2 : 0);
                this.mScreenPowerOn = powerOn;
            }
        }
    }

    public void setScreenPowerForWindowsCast(boolean powerOn, boolean checking) {
        synchronized (LOCK) {
            HwPCUtils.log(TAG, "setScreenPowerForWindowsCast old = " + this.mScreenPowerOn + " new = " + powerOn);
            if (powerOn != this.mScreenPowerOn || !checking) {
                IBinder displayToken = SurfaceControlEx.getInternalDisplayToken();
                if (displayToken == null) {
                    HwPCUtils.log(TAG, "Failed to setScreenPowerForWindowsCast because internal display is disconnected");
                    return;
                }
                SurfaceControlEx.setDisplayPowerMode(displayToken, powerOn ? 2 : 1);
                this.mScreenPowerOn = powerOn;
                if (!powerOn) {
                    this.mUIHandler.removeCallbacks(this.mRunner);
                    this.mUIHandler.postDelayed(this.mRunner, 10000);
                }
            }
        }
    }

    public void setScreenPowerOff() {
        synchronized (LOCK) {
            if (!this.mScreenPowerOn) {
                IBinder displayToken = SurfaceControlEx.getInternalDisplayToken();
                if (displayToken == null) {
                    HwPCUtils.log(TAG, "Failed to setScreenPowerOff because internal display is disconnected");
                    return;
                }
                SurfaceControlEx.setDisplayPowerMode(displayToken, 0);
            }
        }
    }

    public void lockScreenWhenDisconnected(Context context) {
        if (!getScreenPowerOn() && context != null) {
            HwPCUtils.log(TAG, "Lock phone screen when PC displayer is disconnected.");
            PowerManagerEx.goToSleep((PowerManager) context.getSystemService("power"), SystemClock.uptimeMillis(), 5, 0);
        }
    }
}
