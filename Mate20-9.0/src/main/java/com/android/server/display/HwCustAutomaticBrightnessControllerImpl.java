package com.android.server.display;

import android.os.SystemProperties;

public class HwCustAutomaticBrightnessControllerImpl extends HwCustAutomaticBrightnessController {
    private boolean mAvoidScreenBrightnessFlash = SystemProperties.getBoolean("ro.config.avoidScreenFlash", false);
    private int mLowLuxThreshhold = SystemProperties.getInt("ro.config.lowLuxThreshhold", 30);

    /* access modifiers changed from: protected */
    public boolean avoidScreenFlash() {
        return this.mAvoidScreenBrightnessFlash;
    }

    /* access modifiers changed from: protected */
    public int getLowLuxThreshhold() {
        return this.mLowLuxThreshhold;
    }
}
