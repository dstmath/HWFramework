package com.android.server.display;

import android.util.Log;

public class ManualBrightnessController {
    private static final boolean DEBUG;
    private static final String TAG = "ManualBrightnessController";
    protected final ManualBrightnessCallbacks mCallbacks;

    public interface ManualBrightnessCallbacks {
        void updateManualBrightnessForLux();
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public ManualBrightnessController(ManualBrightnessCallbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void updateBrightnesCallbacks() {
        this.mCallbacks.updateManualBrightnessForLux();
    }

    public int getMaxBrightnessForSeekbar() {
        return 255;
    }

    public void updateManualBrightness(int brightness) {
    }

    public int getManualBrightness() {
        return 255;
    }

    public void updatePowerState(int state, boolean enable) {
    }

    public boolean getManualModeAnimationEnable() {
        return false;
    }

    public boolean getManualModeEnable() {
        return false;
    }

    public boolean getManualPowerSavingAnimationEnable() {
        return false;
    }

    public void setManualPowerSavingAnimationEnable(boolean manualPowerSavingAnimationEnable) {
    }

    public boolean getManualThermalModeEnable() {
        return false;
    }

    public boolean getManualThermalModeAnimationEnable() {
        return false;
    }

    public void setManualThermalModeAnimationEnable(boolean thermalModeAnimationEnable) {
    }

    public void setMaxBrightnessFromThermal(int brightness) {
    }
}
