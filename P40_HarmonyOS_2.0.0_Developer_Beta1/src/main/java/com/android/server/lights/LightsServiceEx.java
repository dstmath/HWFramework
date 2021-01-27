package com.android.server.lights;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class LightsServiceEx {
    private static final String TAG = "LightsServiceEx";
    private LightsServiceBridge mBridge = null;
    private boolean mIsHighPrecision = false;

    public LightsServiceEx(Context context) {
    }

    public void setLightsService(LightsServiceBridge bridge) {
        this.mBridge = bridge;
    }

    public LightsService getLightsService() {
        return this.mBridge;
    }

    public void refreshFramesNative() {
        if (this.mBridge != null) {
            LightsServiceBridge.refreshFramesNative();
        }
    }

    public void setLightNative(int light, int color, int mode, int onMS, int offMS, int brightnessMode) {
        if (this.mBridge != null) {
            LightsServiceBridge.setLight_native(light, color, mode, onMS, offMS, brightnessMode);
        }
    }

    public int getLcdBrightness() {
        LightsServiceBridge lightsServiceBridge = this.mBridge;
        if (lightsServiceBridge != null) {
            return lightsServiceBridge.mLcdBrightness;
        }
        Log.e(TAG, "getLcdBrightness LightsService is null ");
        return -1;
    }

    public boolean getWriteAutoBrightnessDbEnable() {
        LightsServiceBridge lightsServiceBridge = this.mBridge;
        if (lightsServiceBridge != null) {
            return lightsServiceBridge.mWriteAutoBrightnessDbEnable;
        }
        Log.e(TAG, "getWriteAutoBrightnessDbEnable LightsService is null ");
        return false;
    }

    public void setIsHighPrecision(boolean isHighPrecision) {
        this.mIsHighPrecision = isHighPrecision;
    }

    public boolean getIsHighPrecision() {
        return this.mIsHighPrecision;
    }

    public void onBootPhase(int phase) {
    }

    /* access modifiers changed from: protected */
    public int getNormalizedMaxBrightness() {
        return 255;
    }

    /* access modifiers changed from: protected */
    public int mapIntoRealBacklightLevel(int level) {
        return level;
    }

    public void setMirrorLinkBrightnessStatusInternal(boolean isStatus) {
    }

    /* access modifiers changed from: protected */
    public boolean shouldIgnoreSetBrightness(int brightness, int brightnessMode) {
        return false;
    }

    public void sendSmartBackLightWithRefreshFramesImpl(int id, int enable, int level, int value, int frames, boolean isSetAfterRefresh, int enable2, int value2) {
    }

    /* access modifiers changed from: protected */
    public void sendUpdateaAutoBrightnessDbMsg() {
    }

    /* access modifiers changed from: protected */
    public void updateBrightnessMode(boolean isAutoBrightnessEnabled) {
    }

    /* access modifiers changed from: protected */
    public void updateCurrentUserId(int userId) {
    }

    public int getDeviceActualBrightnessLevelImpl() {
        return 0;
    }

    public int getDeviceActualBrightnessNitImpl() {
        return 0;
    }

    public int getDeviceStandardBrightnessNitImpl() {
        return 0;
    }

    public boolean setHwBrightnessDataImpl(String name, Bundle data, int[] result) {
        return false;
    }

    public boolean getHwBrightnessDataImpl(String name, Bundle data, int[] result) {
        return false;
    }

    public boolean isLightsBypassed() {
        return false;
    }
}
