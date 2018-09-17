package com.android.server.display;

public class HwCustAutomaticBrightnessController {
    protected boolean avoidScreenFlash() {
        return false;
    }

    protected int getLowLuxThreshhold() {
        return -1;
    }
}
