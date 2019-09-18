package com.android.server.devicepolicy;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;

public class DefaultStorageLocation {
    private static final String INTERNAL = "0";
    private static final String PRIMARYSD = "persist.sys.primarysd";
    private static final String SDCARD = "1";

    public static boolean isSdcard() {
        return SystemProperties.get(PRIMARYSD, "0").equals("1");
    }

    public static boolean isInternal() {
        return SystemProperties.get(PRIMARYSD, "0").equals("0");
    }

    public static void switchVolume(Context context) {
        if (isSdcard()) {
            switchVolume(context, "0");
        } else {
            switchVolume(context, "1");
        }
    }

    private static void switchVolume(Context context, String value) {
        SystemProperties.set(PRIMARYSD, value);
        Intent reboot = new Intent("android.intent.action.REBOOT");
        reboot.putExtra("android.intent.extra.KEY_CONFIRM", false);
        reboot.setFlags(268435456);
        context.startActivity(reboot);
    }

    public static void switchToInternal(Context context) {
        switchVolume(context, "0");
    }
}
