package com.android.server.display;

import android.os.Bundle;
import android.os.Handler;
import android.os.HwBrightnessProcessor;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;

public class CryogenicPowerProcessor extends HwBrightnessProcessor {
    private static final int BRIGHTNESS_MAX_VALUE = 255;
    private static final int BRIGHTNESS_MIN_VALUE = 0;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int INVALID_NUM = -1;
    private static final String TAG = "CryogenicPowerProcessor";
    private HwNormalizedAutomaticBrightnessController mAutomaticBrightnessController;
    private final HwBrightnessXmlLoader.Data mData;
    private Handler mHandler = new Handler();
    private final Object mLockHandle = new Object();
    private int mMaxBrightness = 0;
    private Runnable mMaxBrightnessEffectivenessCheckingRunnable;
    private long mUpdateTime = 0;

    public CryogenicPowerProcessor(AutomaticBrightnessController controller) {
        if (controller instanceof HwNormalizedAutomaticBrightnessController) {
            this.mAutomaticBrightnessController = (HwNormalizedAutomaticBrightnessController) controller;
        } else {
            this.mAutomaticBrightnessController = null;
        }
        this.mData = HwBrightnessXmlLoader.getData();
        this.mMaxBrightnessEffectivenessCheckingRunnable = new Runnable() {
            /* class com.android.server.display.CryogenicPowerProcessor.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                CryogenicPowerProcessor.this.handleMaxBrightnessEffectivenessChecking();
            }
        };
        HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController = this.mAutomaticBrightnessController;
        if (hwNormalizedAutomaticBrightnessController != null) {
            hwNormalizedAutomaticBrightnessController.registerCryogenicProcessor(this);
        }
    }

    public void onScreenOff() {
        synchronized (this.mLockHandle) {
            if (this.mMaxBrightness > 0) {
                this.mUpdateTime = SystemClock.elapsedRealtime();
                if (HWFLOW) {
                    Slog.d(TAG, "onScreenOff() mUpdateTime=" + this.mUpdateTime);
                }
                if (!queueMaxBrightnessEffectivenessChecking(this.mData.cryogenicMaxBrightnessTimeOut)) {
                    Slog.e(TAG, "Failed to call queueMaxBrightnessEffectivenessChecking()");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMaxBrightnessEffectivenessChecking() {
        HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController = this.mAutomaticBrightnessController;
        if (hwNormalizedAutomaticBrightnessController == null) {
            Slog.e(TAG, "mAutomaticBrightnessController=null");
        } else if (!hwNormalizedAutomaticBrightnessController.getScreenStatus()) {
            synchronized (this.mLockHandle) {
                long time = SystemClock.elapsedRealtime() - this.mUpdateTime;
                if (time > this.mData.cryogenicMaxBrightnessTimeOut) {
                    Slog.i(TAG, "The time of max brightness updating from cryogenic is over " + time + "ms, and the value becomes invalid!");
                    this.mAutomaticBrightnessController.setMaxBrightnessFromCryogenic(0);
                } else {
                    queueMaxBrightnessEffectivenessChecking(this.mData.cryogenicMaxBrightnessTimeOut - time);
                }
            }
        }
    }

    private boolean queueMaxBrightnessEffectivenessChecking(long delayMillis) {
        if (HWFLOW) {
            Slog.d(TAG, "queueMaxBrightnessEffectivenessChecking() delay=" + delayMillis + "ms");
        }
        this.mHandler.removeCallbacks(this.mMaxBrightnessEffectivenessCheckingRunnable);
        return this.mHandler.postDelayed(this.mMaxBrightnessEffectivenessCheckingRunnable, delayMillis);
    }

    private boolean checkMaxBrightnessEffectiveness(int maxBrightness) {
        boolean isSuccess;
        synchronized (this.mLockHandle) {
            this.mUpdateTime = SystemClock.elapsedRealtime();
            if (HWFLOW) {
                Slog.d(TAG, "checkMaxBrightnessEffectiveness() mUpdateTime=" + this.mUpdateTime);
            }
            isSuccess = !this.mAutomaticBrightnessController.getScreenStatus() ? queueMaxBrightnessEffectivenessChecking(this.mData.cryogenicMaxBrightnessTimeOut) : true;
            if (this.mMaxBrightness != maxBrightness) {
                this.mAutomaticBrightnessController.setMaxBrightnessFromCryogenic(maxBrightness);
            }
        }
        return isSuccess;
    }

    public boolean setData(Bundle data, int[] retValue) {
        if (data == null || retValue == null || retValue.length <= 0) {
            Slog.e(TAG, "setData() invalid input: data=" + data + ",retValue=" + retValue);
            return true;
        }
        int i = -1;
        retValue[0] = -1;
        if (!this.mData.cryogenicEnable) {
            Slog.w(TAG, "Cryogenic is disable!");
            return true;
        } else if (this.mAutomaticBrightnessController == null) {
            Slog.e(TAG, "mAutomaticBrightnessController=null");
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
            StringBuilder sb = new StringBuilder();
            sb.append("Cryogenic set maxBrightness=");
            sb.append(maxBrightness);
            sb.append(retValue[0] == 0 ? " success" : " failed!");
            Slog.i(TAG, sb.toString());
            return true;
        }
    }
}
