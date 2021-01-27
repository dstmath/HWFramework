package com.android.server.lights;

import android.content.Context;
import android.os.Bundle;
import com.huawei.server.HwBasicPlatformFactory;

public class LightsServiceBridge extends LightsService {
    private static final String TAG = "LightsServiceBridge";
    private LightsServiceEx mLightsServiceEx;

    public LightsServiceBridge(Context context) {
        super(context);
        initLightsService(context);
        this.mIsHighPrecision = this.mLightsServiceEx.getIsHighPrecision();
    }

    private void initLightsService(Context context) {
        this.mLightsServiceEx = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwLightsService(context);
        this.mLightsServiceEx.setLightsService(this);
    }

    public void onBootPhase(int phase) {
        LightsServiceBridge.super.onBootPhase(phase);
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            lightsServiceEx.onBootPhase(phase);
        }
    }

    /* access modifiers changed from: protected */
    public int getNormalizedMaxBrightness() {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            return lightsServiceEx.getNormalizedMaxBrightness();
        }
        return 255;
    }

    /* access modifiers changed from: protected */
    public int mapIntoRealBacklightLevel(int level) {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            return lightsServiceEx.mapIntoRealBacklightLevel(level);
        }
        return level;
    }

    public void setMirrorLinkBrightnessStatusInternal(boolean isStatus) {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            lightsServiceEx.setMirrorLinkBrightnessStatusInternal(isStatus);
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldIgnoreSetBrightness(int brightness, int brightnessMode) {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            return lightsServiceEx.shouldIgnoreSetBrightness(brightness, brightnessMode);
        }
        return false;
    }

    public void sendSmartBackLightWithRefreshFramesImpl(int id, int enable, int level, int value, int frames, boolean isSetAfterRefresh, int enable2, int value2) {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            lightsServiceEx.sendSmartBackLightWithRefreshFramesImpl(id, enable, level, value, frames, isSetAfterRefresh, enable2, value2);
        }
    }

    /* access modifiers changed from: protected */
    public void sendUpdateaAutoBrightnessDbMsg() {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            lightsServiceEx.sendUpdateaAutoBrightnessDbMsg();
        }
    }

    /* access modifiers changed from: protected */
    public void updateBrightnessMode(boolean isAutoBrightnessEnabled) {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            lightsServiceEx.updateBrightnessMode(isAutoBrightnessEnabled);
        }
    }

    /* access modifiers changed from: protected */
    public void updateCurrentUserId(int userId) {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            lightsServiceEx.updateCurrentUserId(userId);
        }
    }

    public int getDeviceActualBrightnessLevelImpl() {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            return lightsServiceEx.getDeviceActualBrightnessLevelImpl();
        }
        return 0;
    }

    public int getDeviceActualBrightnessNitImpl() {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            return lightsServiceEx.getDeviceActualBrightnessNitImpl();
        }
        return 0;
    }

    public int getDeviceStandardBrightnessNitImpl() {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            return lightsServiceEx.getDeviceStandardBrightnessNitImpl();
        }
        return 0;
    }

    public boolean setHwBrightnessDataImpl(String name, Bundle data, int[] result) {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            return lightsServiceEx.setHwBrightnessDataImpl(name, data, result);
        }
        return false;
    }

    public boolean getHwBrightnessDataImpl(String name, Bundle data, int[] result) {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            return lightsServiceEx.getHwBrightnessDataImpl(name, data, result);
        }
        return false;
    }

    public boolean isLightsBypassed() {
        LightsServiceEx lightsServiceEx = this.mLightsServiceEx;
        if (lightsServiceEx != null) {
            return lightsServiceEx.isLightsBypassed();
        }
        return false;
    }
}
