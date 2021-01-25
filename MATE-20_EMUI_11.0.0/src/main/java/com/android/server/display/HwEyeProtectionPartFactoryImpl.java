package com.android.server.display;

import android.content.Context;
import com.huawei.server.DefaultHwBasicPlatformPartFactory;

public class HwEyeProtectionPartFactoryImpl extends DefaultHwBasicPlatformPartFactory {
    public HwEyeProtectionControllerImpl getHwEyeProtectionController(Context context, HwNormalizedAutomaticBrightnessController automaticBrightnessController) {
        return new HwEyeProtectionControllerImpl(context, automaticBrightnessController);
    }
}
