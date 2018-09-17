package com.android.server.lights;

public abstract class LightsManager {
    public static final int LIGHT_ID_AMBIENTLIGHT = 261;
    public static final int LIGHT_ID_ATTENTION = 5;
    public static final int LIGHT_ID_AUTOCUSTOMBACKLIGHT = 258;
    public static final int LIGHT_ID_BACKLIGHT = 0;
    public static final int LIGHT_ID_BACKLIGHT_10000 = 260;
    public static final int LIGHT_ID_BATTERY = 3;
    public static final int LIGHT_ID_BLUETOOTH = 6;
    public static final int LIGHT_ID_BUTTONS = 2;
    public static final int LIGHT_ID_COUNT = 265;
    public static final int LIGHT_ID_KEYBOARD = 1;
    public static final int LIGHT_ID_MANUALCUSTOMBACKLIGHT = 259;
    public static final int LIGHT_ID_NOTIFICATIONS = 4;
    public static final int LIGHT_ID_SMARTBACKLIGHT = 257;
    public static final int LIGHT_ID_SRE = 262;
    public static final int LIGHT_ID_WIFI = 7;

    public abstract Light getLight(int i);
}
