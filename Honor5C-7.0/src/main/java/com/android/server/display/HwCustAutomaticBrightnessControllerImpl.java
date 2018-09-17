package com.android.server.display;

import android.os.SystemProperties;

public class HwCustAutomaticBrightnessControllerImpl extends HwCustAutomaticBrightnessController {
    private boolean mAvoidScreenBrightnessFlash;
    private int mLowLuxThreshhold;

    public HwCustAutomaticBrightnessControllerImpl() {
        this.mLowLuxThreshhold = SystemProperties.getInt("ro.config.lowLuxThreshhold", 30);
        this.mAvoidScreenBrightnessFlash = SystemProperties.getBoolean("ro.config.avoidScreenFlash", false);
    }

    protected boolean avoidScreenFlash() {
        return this.mAvoidScreenBrightnessFlash;
    }

    protected int getLowLuxThreshhold() {
        return this.mLowLuxThreshhold;
    }
}
