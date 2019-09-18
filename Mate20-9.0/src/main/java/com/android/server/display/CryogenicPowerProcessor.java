package com.android.server.display;

import android.os.Bundle;
import android.os.Handler;
import android.os.HwBrightnessProcessor;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;

public class CryogenicPowerProcessor extends HwBrightnessProcessor {
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static String TAG = "CryogenicPowerProcessor";
    private HwNormalizedAutomaticBrightnessController mAutomaticBrightnessController;
    private final HwBrightnessXmlLoader.Data mData;
    private Handler mHandler;
    private Object mLockHandle;
    private int mMaxBrightness = 0;
    private Runnable mMaxBrightnessEffectivenessCheckingRunnable;
    private long mUpdateTime = 0;

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public CryogenicPowerProcessor(AutomaticBrightnessController controller) {
        this.mAutomaticBrightnessController = (HwNormalizedAutomaticBrightnessController) controller;
        this.mData = HwBrightnessXmlLoader.getData();
        this.mLockHandle = new Object();
        this.mHandler = new Handler();
        this.mMaxBrightnessEffectivenessCheckingRunnable = new Runnable() {
            public void run() {
                CryogenicPowerProcessor.this.handleMaxBrightnessEffectivenessChecking();
            }
        };
        if (this.mAutomaticBrightnessController != null) {
            this.mAutomaticBrightnessController.registerCryogenicProcessor(this);
        }
    }

    public void onScreenOff() {
        synchronized (this.mLockHandle) {
            if (this.mMaxBrightness > 0) {
                this.mUpdateTime = SystemClock.elapsedRealtime();
                if (HWFLOW) {
                    String str = TAG;
                    Slog.d(str, "onScreenOff() mUpdateTime=" + this.mUpdateTime);
                }
                if (!queueMaxBrightnessEffectivenessChecking(this.mData.cryogenicMaxBrightnessTimeOut)) {
                    Slog.e(TAG, "Failed to call queueMaxBrightnessEffectivenessChecking()");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleMaxBrightnessEffectivenessChecking() {
        if (this.mAutomaticBrightnessController == null) {
            Slog.e(TAG, "mAutomaticBrightnessController=null");
            return;
        }
        if (!this.mAutomaticBrightnessController.getScreenStatus()) {
            synchronized (this.mLockHandle) {
                long time = SystemClock.elapsedRealtime() - this.mUpdateTime;
                if (time > this.mData.cryogenicMaxBrightnessTimeOut) {
                    String str = TAG;
                    Slog.i(str, "The time of max brightness updating from cryogenic is over " + time + "ms, and the value becomes invalid!");
                    this.mAutomaticBrightnessController.setMaxBrightnessFromCryogenic(0);
                } else {
                    queueMaxBrightnessEffectivenessChecking(this.mData.cryogenicMaxBrightnessTimeOut - time);
                }
            }
        }
    }

    private boolean queueMaxBrightnessEffectivenessChecking(long delayMillis) {
        if (HWFLOW) {
            String str = TAG;
            Slog.d(str, "queueMaxBrightnessEffectivenessChecking() delay=" + delayMillis + "ms");
        }
        this.mHandler.removeCallbacks(this.mMaxBrightnessEffectivenessCheckingRunnable);
        return this.mHandler.postDelayed(this.mMaxBrightnessEffectivenessCheckingRunnable, delayMillis);
    }

    private boolean checkMaxBrightnessEffectiveness(int maxBrightness) {
        boolean ret;
        synchronized (this.mLockHandle) {
            this.mUpdateTime = SystemClock.elapsedRealtime();
            if (HWFLOW) {
                String str = TAG;
                Slog.d(str, "checkMaxBrightnessEffectiveness() mUpdateTime=" + this.mUpdateTime);
            }
            if (!this.mAutomaticBrightnessController.getScreenStatus()) {
                ret = queueMaxBrightnessEffectivenessChecking(this.mData.cryogenicMaxBrightnessTimeOut);
            } else {
                ret = true;
            }
            if (this.mMaxBrightness != maxBrightness) {
                this.mAutomaticBrightnessController.setMaxBrightnessFromCryogenic(maxBrightness);
            }
        }
        return ret;
    }

    public boolean setData(Bundle data, int[] retValue) {
        int i = -1;
        retValue[0] = -1;
        if (!this.mData.cryogenicEnable) {
            Slog.w(TAG, "Cryogenic is disable!");
            return true;
        } else if (this.mAutomaticBrightnessController == null) {
            Slog.e(TAG, "mAutomaticBrightnessController=null");
            return true;
        } else if (data == null || retValue.length <= 0) {
            Slog.e(TAG, "setData() invalid input: data=" + data + ",retValue.length=" + retValue.length);
            return true;
        } else {
            int maxBrightness = data.getInt("MaxBrightness", 0);
            if (maxBrightness < 0 || maxBrightness > 255) {
                Slog.e(TAG, "setData() invalid input: maxBrightness=" + maxBrightness);
                return true;
            }
            if (checkMaxBrightnessEffectiveness(maxBrightness)) {
                i = 0;
            }
            retValue[0] = i;
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Cryogenic set maxBrightness=");
            sb.append(maxBrightness);
            sb.append(retValue[0] == 0 ? " success" : " failed!");
            Slog.i(str, sb.toString());
            return true;
        }
    }
}
