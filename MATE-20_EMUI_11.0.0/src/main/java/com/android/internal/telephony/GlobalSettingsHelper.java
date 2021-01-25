package com.android.internal.telephony;

import android.content.Context;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

public class GlobalSettingsHelper {
    public static int getInt(Context context, String settingName, int subId, int defaultValue) {
        return Settings.Global.getInt(context.getContentResolver(), getSettingName(context, settingName, subId), defaultValue);
    }

    public static boolean getBoolean(Context context, String settingName, int subId, boolean defaultValue) {
        return Settings.Global.getInt(context.getContentResolver(), getSettingName(context, settingName, subId), defaultValue ? 1 : 0) == 1;
    }

    public static boolean getBoolean(Context context, String settingName, int subId) throws Settings.SettingNotFoundException {
        return Settings.Global.getInt(context.getContentResolver(), getSettingName(context, settingName, subId)) == 1;
    }

    public static boolean setInt(Context context, String settingName, int subId, int value) {
        boolean needChange;
        String settingName2 = getSettingName(context, settingName, subId);
        try {
            needChange = Settings.Global.getInt(context.getContentResolver(), settingName2) != value;
        } catch (Settings.SettingNotFoundException e) {
            needChange = true;
        }
        if (needChange) {
            Settings.Global.putInt(context.getContentResolver(), settingName2, value);
        }
        return needChange;
    }

    public static boolean setBoolean(Context context, String settingName, int subId, boolean value) {
        return setInt(context, settingName, subId, value ? 1 : 0);
    }

    private static String getSettingName(Context context, String settingName, int subId) {
        if (TelephonyManager.from(context).getSimCount() <= 1 || !SubscriptionManager.isValidSubscriptionId(subId)) {
            return settingName;
        }
        return settingName + subId;
    }
}
