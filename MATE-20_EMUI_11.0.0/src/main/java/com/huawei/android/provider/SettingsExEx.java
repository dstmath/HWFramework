package com.huawei.android.provider;

import android.content.ContentResolver;
import android.provider.Settings;

public class SettingsExEx {

    public static final class Secure {
        public static final String SMS_DEFAULT_APPLICATION = "sms_default_application";
    }

    public static final class Global {
        public static final String DB_KEY_UNIFIED_DEVICE_NAME = "unified_device_name";

        public static String getStringForUser(ContentResolver resolver, String name, int userHandle) {
            return Settings.Global.getStringForUser(resolver, name, userHandle);
        }
    }
}
