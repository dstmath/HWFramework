package com.android.server.display;

class BackLightCommonData {
    private BrightnessMode mBrightnessMode;
    private int mDiscountBrightness;
    private boolean mIsCommercialVersion;
    private boolean mIsProductEnable;
    private boolean mIsThermalLimited;
    private boolean mIsWindowManagerBrightnessMode;
    private int mSmoothAmbientLight;

    public enum BrightnessMode {
        AUTO,
        MANUAL
    }

    public enum Scene {
        OTHERS,
        GAME,
        VIDEO
    }

    BackLightCommonData() {
    }

    public void setProductEnable(boolean isProductEnable) {
        this.mIsProductEnable = isProductEnable;
    }

    public boolean isProductEnable() {
        return this.mIsProductEnable;
    }

    public void setCommercialVersion(boolean isCommercialVersion) {
        this.mIsCommercialVersion = isCommercialVersion;
    }

    public boolean isCommercialVersion() {
        return this.mIsCommercialVersion;
    }

    public void setWindowManagerBrightnessMode(boolean isWindowManagerBrightnessMode) {
        this.mIsWindowManagerBrightnessMode = isWindowManagerBrightnessMode;
    }

    public boolean isWindowManagerBrightnessMode() {
        return this.mIsWindowManagerBrightnessMode;
    }

    public void setBrightnessMode(BrightnessMode mode) {
        this.mBrightnessMode = mode;
    }

    public BrightnessMode getBrightnessMode() {
        return this.mBrightnessMode;
    }

    public void setSmoothAmbientLight(int value) {
        this.mSmoothAmbientLight = value;
    }

    public int getSmoothAmbientLight() {
        return this.mSmoothAmbientLight;
    }

    public void setThermalLimited(boolean isLimited) {
        this.mIsThermalLimited = isLimited;
    }

    public boolean isThermalLimited() {
        return this.mIsThermalLimited;
    }

    public void setDiscountBrightness(int level) {
        this.mDiscountBrightness = level;
    }

    public int getDiscountBrightness() {
        return this.mDiscountBrightness;
    }
}
