package com.android.server.lights;

public class LightEx {
    public static final int LIGHT_FLASH_TIMED = 1;
    private Light mLight = null;

    public void setLight(Light light) {
        this.mLight = light;
    }

    public void turnOff() {
        Light light = this.mLight;
        if (light != null) {
            light.turnOff();
        }
    }

    public void setFlashing(int color, int mode, int onMS, int offMS) {
        Light light = this.mLight;
        if (light != null) {
            light.setFlashing(color, mode, onMS, offMS);
        }
    }

    public void setColor(int color) {
        Light light = this.mLight;
        if (light != null) {
            light.setColor(color);
        }
    }

    public void setMirrorLinkBrightness(int brightness) {
        Light light = this.mLight;
        if (light != null) {
            light.setMirrorLinkBrightness(brightness);
        }
    }

    public void setMirrorLinkBrightnessStatus(boolean isStatus) {
        Light light = this.mLight;
        if (light != null) {
            light.setMirrorLinkBrightnessStatus(isStatus);
        }
    }
}
