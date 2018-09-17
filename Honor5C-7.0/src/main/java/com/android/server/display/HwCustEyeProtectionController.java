package com.android.server.display;

import android.content.Context;

public class HwCustEyeProtectionController {
    private static final String TAG = "HwEyeProtectionController";
    protected AutomaticBrightnessController mAutomaticBrightnessController;
    protected Context mContext;

    public HwCustEyeProtectionController(Context context, AutomaticBrightnessController automaticBrightnessController) {
        this.mContext = context;
        this.mAutomaticBrightnessController = automaticBrightnessController;
    }

    public void onScreenStateChanged(boolean powerStatus) {
    }
}
