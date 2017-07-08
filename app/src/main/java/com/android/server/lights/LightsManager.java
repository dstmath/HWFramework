package com.android.server.lights;

public abstract class LightsManager {
    public static final int LIGHT_ID_AMBIENTLIGHT = 12;
    public static final int LIGHT_ID_ATTENTION = 5;
    public static final int LIGHT_ID_AUTOCUSTOMBACKLIGHT = 9;
    public static final int LIGHT_ID_BACKLIGHT = 0;
    public static final int LIGHT_ID_BACKLIGHT_10000 = 11;
    public static final int LIGHT_ID_BATTERY = 3;
    public static final int LIGHT_ID_BLUETOOTH = 6;
    public static final int LIGHT_ID_BUTTONS = 2;
    public static final int LIGHT_ID_COUNT = 14;
    public static final int LIGHT_ID_KEYBOARD = 1;
    public static final int LIGHT_ID_MANUALCUSTOMBACKLIGHT = 10;
    public static final int LIGHT_ID_NOTIFICATIONS = 4;
    public static final int LIGHT_ID_SMARTBACKLIGHT = 8;
    public static final int LIGHT_ID_SRE = 13;
    public static final int LIGHT_ID_WIFI = 7;

    public abstract Light getLight(int i);
}
