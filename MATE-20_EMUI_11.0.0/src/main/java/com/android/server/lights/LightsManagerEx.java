package com.android.server.lights;

import com.android.server.LocalServices;

public class LightsManagerEx {
    public static final int LIGHT_ID_BACKLIGHT = 0;
    public static final int LIGHT_ID_BACKLIGHT_10000 = 260;
    public static final int LIGHT_ID_BATTERY = 3;
    public static final int LIGHT_ID_SMARTBACKLIGHT = 257;
    private LightsManager mLightsManager;

    public LightsManagerEx() {
        this.mLightsManager = null;
        this.mLightsManager = (LightsManager) LocalServices.getService(LightsManager.class);
    }

    public LightEx getLight(int id) {
        Light light = this.mLightsManager.getLight(id);
        LightEx lightEx = new LightEx();
        lightEx.setLight(light);
        return lightEx;
    }
}
