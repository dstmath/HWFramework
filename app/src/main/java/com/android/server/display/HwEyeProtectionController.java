package com.android.server.display;

import android.content.Context;

public class HwEyeProtectionController {
    private static final String TAG = "HwEyeProtectionController";
    protected HwNormalizedAutomaticBrightnessController mAutomaticBrightnessController;
    protected Context mContext;

    public HwEyeProtectionController(Context context, HwNormalizedAutomaticBrightnessController automaticBrightnessController) {
        this.mContext = context;
        this.mAutomaticBrightnessController = automaticBrightnessController;
    }

    public void onScreenStateChanged(boolean powerStatus) {
    }
}
