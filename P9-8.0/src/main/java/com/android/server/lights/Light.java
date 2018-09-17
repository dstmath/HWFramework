package com.android.server.lights;

public abstract class Light {
    public static final int BRIGHTNESS_MODE_LOW_PERSISTENCE = 2;
    public static final int BRIGHTNESS_MODE_SENSOR = 1;
    public static final int BRIGHTNESS_MODE_USER = 0;
    public static final int LIGHT_FLASH_HARDWARE = 2;
    public static final int LIGHT_FLASH_NONE = 0;
    public static final int LIGHT_FLASH_TIMED = 1;

    public abstract void configBrightnessRange(int i, int i2, int i3);

    public abstract int getCurrentBrightness();

    public abstract int getDeviceActualBrightnessLevel();

    public abstract int getDeviceActualBrightnessNit();

    public abstract int getDeviceStandardBrightnessNit();

    public abstract int getMaxBrightnessFromKernel();

    public abstract boolean isHighPrecision();

    public abstract void pulse();

    public abstract void pulse(int i, int i2);

    public abstract void sendAmbientLight(int i);

    public abstract void sendCustomBackLight(int i);

    public abstract void sendSREWithRefreshFrames(int i, int i2, int i3, int i4, boolean z, int i5, int i6);

    public abstract void sendSmartBackLight(int i, int i2, int i3);

    public abstract void sendSmartBackLightWithRefreshFrames(int i, int i2, int i3, int i4, boolean z, int i5, int i6);

    public abstract void setBrightness(int i);

    public abstract void setBrightness(int i, int i2);

    public abstract void setColor(int i);

    public abstract void setFlashing(int i, int i2, int i3, int i4);

    public abstract void setLcdRatio(int i, boolean z);

    public abstract void setMirrorLinkBrightness(int i);

    public abstract void setMirrorLinkBrightnessStatus(boolean z);

    public abstract void setVrMode(boolean z);

    public abstract void turnOff();

    public abstract void updateBrightnessAdjustMode(boolean z);

    public abstract void updateUserId(int i);

    public abstract void writeAutoBrightnessDbEnable(boolean z);
}
