package com.huawei.systemmanager.common;

public class HwSystemManagerSettings {

    public static final class Global {
        public static final String MOBILE_DATA = "mobile_data";
        public static final int ZEN_MODE_ALARMS = 3;
        public static final String ZEN_MODE_CONFIG_ETAG = "zen_mode_config_etag";
        public static final int ZEN_MODE_IMPORTANT_INTERRUPTIONS = 1;
        public static final int ZEN_MODE_NO_INTERRUPTIONS = 2;
    }

    public static final class Secure {
        public static final String LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS = "lock_screen_allow_private_notifications";
        public static final String LOCK_SCREEN_SHOW_NOTIFICATIONS = "lock_screen_show_notifications";
        public static final String SMS_DEFAULT_APPLICATION = "sms_default_application";
    }

    public static final class System {
        public static final String SCREEN_AUTO_BRIGHTNESS = "screen_auto_brightness";
        public static final String SCREEN_AUTO_BRIGHTNESS_ADJ = "screen_auto_brightness_adj";
    }
}
