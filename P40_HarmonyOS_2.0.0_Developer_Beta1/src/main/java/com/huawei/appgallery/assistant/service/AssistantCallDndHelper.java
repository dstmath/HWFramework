package com.huawei.appgallery.assistant.service;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;

class AssistantCallDndHelper {
    private static final String GAME_DEEP_DND_MODE = "game_deep_nodisturb_mode";
    public static final int MODE_CLOSE_VAL = 2;
    public static final int MODE_OPEN_VAL = 1;
    public static final int MODE_UNSUPPORT_VAL = 0;
    private static final String TAG = "AssistantCallDndHelper";

    AssistantCallDndHelper() {
    }

    public static void notifyGameBackground(Context ctx, int fwkEvent) {
        if ((fwkEvent == 0 || fwkEvent == 3) && isSystemConfigCallDnd()) {
            turnOffCallDnd(ctx);
        }
    }

    private static void turnOffCallDnd(Context ctx) {
        if (Settings.Secure.getInt(ctx.getContentResolver(), GAME_DEEP_DND_MODE, 0) != 0) {
            Settings.Secure.putInt(ctx.getContentResolver(), GAME_DEEP_DND_MODE, 2);
        }
    }

    private static boolean isSystemConfigCallDnd() {
        return SystemProperties.getInt("ro.config.gameassist.nodisturb", 0) == 1;
    }
}
