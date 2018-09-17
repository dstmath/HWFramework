package android.vkey;

import android.content.Context;
import android.provider.Settings.System;

public class SettingsHelper {
    private static final int TOUCH_PLUS_ON = 1;

    public static boolean isTouchPlusOn(Context ctx) {
        return 1 == System.getInt(ctx.getContentResolver(), "hw_membrane_touch_enabled", 0) && 1 == System.getInt(ctx.getContentResolver(), "hw_membrane_touch_navbar_enabled", 0);
    }
}
