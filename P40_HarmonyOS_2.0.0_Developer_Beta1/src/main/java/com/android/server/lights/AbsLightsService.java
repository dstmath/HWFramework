package com.android.server.lights;

import android.content.Context;
import android.os.Bundle;
import com.android.server.SystemService;

public abstract class AbsLightsService extends SystemService {
    public static final int DEFAULT_MAX_BRIGHTNESS = 255;
    public static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;

    protected static native void refreshFramesNative();

    public AbsLightsService(Context context) {
        super(context);
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

    /* access modifiers changed from: protected */
    public boolean isLightsBypassed() {
        return false;
    }
}
