package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;

public class HwBrightnessBatteryDetection extends BroadcastReceiver {
    private static final int BATTERY_INIT_LEVEL = -1;
    private static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MAXDEFAULTBRIGHTNESS = 255;
    private static final int POWER_SAVE_MODE_BRIGHTNESS_RATIO = SystemProperties.getInt("ro.powersavemode.backlight_ratio", 56);
    private static final int POWER_SAVE_MODE_DEFAULT_BRIGHTNESS_RATIO = 56;
    private static final String TAG = "HwBrightnessBatteryDetection";
    private static int sLastBatteryLevel = -1;
    private boolean mBatteryModeStatus = false;
    private Callbacks mCallbacks;
    private final Context mContext;
    private final HwBrightnessXmlLoader.Data mData;
    private int mLowBatteryMaxBrightness = 255;

    public interface Callbacks {
        void updateBrightnessFromBattery(int i);

        void updateBrightnessRatioFromBattery(int i);
    }

    public HwBrightnessBatteryDetection(Callbacks callbacks, Context context) {
        this.mCallbacks = callbacks;
        this.mContext = context;
        this.mData = HwBrightnessXmlLoader.getData();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.setPriority(1000);
        this.mContext.registerReceiver(this, filter);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Slog.e(TAG, "Invalid input parameter!");
        } else if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
            int batteryLevel = intent.getIntExtra("level", 0);
            if (this.mData.powerSavingModeBatteryLowLevelEnable) {
                updatePowerSavingModeBrightnessRatio(batteryLevel);
            }
            if (this.mData.batteryModeEnable) {
                boolean curBatteryStatus = false;
                if (batteryLevel <= this.mData.batteryLowLevelTh) {
                    this.mLowBatteryMaxBrightness = this.mData.batteryLowLevelMaxBrightness;
                    curBatteryStatus = true;
                } else {
                    this.mLowBatteryMaxBrightness = 255;
                }
                if (DEBUG) {
                    Slog.d(TAG, "batteryLevel =" + batteryLevel + ", curBatteryStatus=" + curBatteryStatus + ", BatteryModeStatus =" + this.mBatteryModeStatus);
                }
                if (curBatteryStatus != this.mBatteryModeStatus) {
                    this.mCallbacks.updateBrightnessFromBattery(this.mLowBatteryMaxBrightness);
                    this.mBatteryModeStatus = curBatteryStatus;
                }
            }
        }
    }

    private void updatePowerSavingModeBrightnessRatio(int batteryLevel) {
        int brightnessRatio = POWER_SAVE_MODE_BRIGHTNESS_RATIO;
        if (batteryLevel <= this.mData.powerSavingModeBatteryLowLevelThreshold) {
            brightnessRatio = this.mData.powerSavingModeBatteryLowLevelBrightnessRatio;
        }
        if (sLastBatteryLevel == -1 || ((batteryLevel <= this.mData.powerSavingModeBatteryLowLevelThreshold && sLastBatteryLevel > this.mData.powerSavingModeBatteryLowLevelThreshold) || (batteryLevel > this.mData.powerSavingModeBatteryLowLevelThreshold && sLastBatteryLevel <= this.mData.powerSavingModeBatteryLowLevelThreshold))) {
            if (DEBUG) {
                Slog.i(TAG, "updateBrightnessRatioFromBattery mLastLevel= " + sLastBatteryLevel + ",batteryLevel= " + batteryLevel + ",batteryLowLevelTh=" + this.mData.powerSavingModeBatteryLowLevelThreshold + ",brightnessRatio = " + brightnessRatio);
            }
            this.mCallbacks.updateBrightnessRatioFromBattery(brightnessRatio);
        }
        sLastBatteryLevel = batteryLevel;
    }
}
