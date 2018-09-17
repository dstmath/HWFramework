package com.android.server.display;

import android.content.Context;

public class HwEyeProtectionController {
    private static final String TAG = "HwEyeProtectionController";
    protected HwNormalizedAutomaticBrightnessController mAutomaticBrightnessController;

    public HwEyeProtectionController(Context context, HwNormalizedAutomaticBrightnessController automaticBrightnessController) {
        this.mAutomaticBrightnessController = automaticBrightnessController;
    }

    public void onScreenStateChanged(boolean powerStatus) {
    }
}
