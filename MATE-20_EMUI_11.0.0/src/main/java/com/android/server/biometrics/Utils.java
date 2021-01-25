package com.android.server.biometrics;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class Utils {
    public static boolean isDebugEnabled(Context context, int targetUserId) {
        if ((Build.IS_ENG || Build.IS_USERDEBUG) && Settings.Secure.getIntForUser(context.getContentResolver(), "biometric_debug_enabled", 0, targetUserId) != 0) {
            return true;
        }
        return false;
    }
}
