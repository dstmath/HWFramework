package com.android.server.display;

import android.os.Bundle;

public class Utils {
    public static final String ACTION_SUPER_POWERMODE = "huawei.intent.action.PG_EXTREME_MODE_ENABLE_ACTION";
    public static final float AMBIRNT_LUX_THRESHOLD = 0.2f;
    public static final String COLOR_TEMPERATURE = "color_temperature";
    public static final int COLOR_TEMPERATURE_DEFAULT = 128;
    public static final int COLOR_TEMPERATURE_MAX_VALUE = 255;
    public static final int COLOR_TEMPERATURE_MIN_VALUE = 0;
    public static String COLOR_TEMPERATURE_MODE = null;
    public static int COLOR_TEMPERATURE_MODE_DEFAULT = 0;
    public static int COLOR_TEMPERATURE_MODE_MANUAL = 0;
    public static final String COLOR_TEMPERATURE_RGB = "color_temperature_rgb";
    public static final int DEFAULT_COLOR_TEMPERATURE_DEFAULT_CLOUDY = 127;
    public static final int DEFAULT_COLOR_TEMPERATURE_DEFAULT_INDOOR = 64;
    public static final int DEFAULT_COLOR_TEMPERATURE_DEFAULT_NIGHT = 0;
    public static final int DEFAULT_COLOR_TEMPERATURE_SUNNY = 191;
    public static final int DEFAULT_VALUE_BLUELIGHT_FILTER_REAL = 30;
    public static final int EYEPROTECTION_SCREENOFF_MODE_DEFAULT = 0;
    public static final int EYEPROTECTION_SCREENOFF_MODE_TURN_OFF = 1;
    public static final int EYEPROTECTION_SCREENOFF_MODE_TURN_ON = 2;
    public static final int EYE_DELAYTIME_OFF = 0;
    public static final int EYE_DELAYTIME_ON = 1;
    private static final int EYE_PROTECTIION_MODE = 0;
    public static final int EYE_PROTECTIION_OFF = 0;
    public static final int EYE_PROTECTIION_OFF_FROM_SUPER_POWERMODE = 2;
    public static final int EYE_PROTECTIION_ON = 1;
    public static final int EYE_PROTECTIION_ON_BY_USER = 3;
    public static final int EYE_SCHEDULE_DEFAULT_TTIME = -1;
    public static final int EYE_SCHEDULE_OFF = 0;
    public static final int EYE_SCHEDULE_ON = 1;
    public static final String HW_EYEPROTECTION_CONFIG_FILE = "EyeProtectionConfig.xml";
    public static final String HW_EYEPROTECTION_CONFIG_FILE_NAME = "EyeProtectionConfig";
    public static final String HW_NOTIFICATION_BACKGROUND_INDEX = "huawei.notification.backgroundIndex";
    public static final String HW_NOTIFICATION_CONTENT_ICON = "huawei.notification.contentIcon";
    public static final String HW_NOTIFICATION_REPLACE_ICONID = "huawei.notification.replace.iconId";
    public static final String HW_NOTIFICATION_REPLACE_LOCATION = "huawei.notification.replace.location";
    public static final String KEY_EYES_PROTECTION = "eyes_protection_mode";
    public static final String KEY_EYE_COMFORT_LESSWARM = "eye_comfort_lesswarm";
    public static final String KEY_EYE_COMFORT_MOREWARM = "eye_comfort_morewarm";
    public static final String KEY_EYE_COMFORT_VALID = "eye_comfort_valid";
    public static final String KEY_EYE_SCHEDULE_ENDTIME = "eye_comfort_endtime";
    public static final String KEY_EYE_SCHEDULE_STARTTIME = "eye_comfort_starttime";
    public static final String KEY_EYE_SCHEDULE_SWITCH = "eye_comfort_schedule_switch";
    public static final String KEY_EYE_SET_DELAYTIME_SCHEDULE_KEY = "eye_set_delaytime_schedule_key";
    public static final String KEY_SET_COLOR_TEMP = "user_set_warm";
    public static final int MAXINUM_TEMPERATURE = 255;
    public static final int MODE_BACKLIGHT = 2;
    public static final int MODE_BLUE_LIGHT = 1;
    public static final int MODE_COLOR_TEMPERATURE = 4;
    public static final int MODE_COLOR_TEMP_3_DIMENSION = 1;
    public static final int MODE_DEFAULT = 0;
    public static final int MSG_HANDLE_BOOT_COMPLETE_MESSAGE = 3;
    public static final int MSG_HANDLE_SCREEN_TURN_ON = 8;
    public static final int MSG_HANDLE_SUPER_POWER_MESSAGE = 5;
    public static final int MSG_HANDLE_TIME_AND_TIMEZONE_CHANGE = 6;
    public static final int MSG_HANDLE_TURN_OFF_EYE_PROTECTION_MODE = 7;
    public static final int MSG_HANDLE_USER_SWITCH_MESSAGE = 4;
    public static final int MSG_SET_COLOR_TEMPERATURE = 0;
    public static final int MSG_SET_FILTER_BLUE_LIGHT = 1;
    public static final int MSG_UPDATE_BACKLIGHT = 2;
    public static final int SET_COLOR_TEMP_DEFAULT_VALUE = 0;
    public static final String TAG = "EyeProtectionController Util";
    public static final int VALUE_ANIMATION_MAX_TIMES = 20;
    public static final int VALUE_ANIMATION_MSG_DELAYED = 40;
    public static final int VALUE_BLUELIGHT_FILTER_DEFAULT = 0;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.Utils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.Utils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.Utils.<clinit>():void");
    }

    public static Bundle getNotificationThemeData(int contIconId, int repIconId, int bgIndex, int repLocation) {
        return getNotificationThemeData(null, contIconId, repIconId, bgIndex, repLocation);
    }

    public static Bundle getNotificationThemeData(Bundle bundle, int contIconId, int repIconId, int bgIndex, int repLocation) {
        bundle = new Bundle();
        if (contIconId > 0) {
            bundle.putInt(HW_NOTIFICATION_CONTENT_ICON, contIconId);
        }
        if (repIconId > 0) {
            bundle.putInt(HW_NOTIFICATION_REPLACE_ICONID, repIconId);
        }
        if (bgIndex >= 0) {
            bundle.putInt(HW_NOTIFICATION_BACKGROUND_INDEX, bgIndex);
        }
        if (repLocation > 0) {
            bundle.putInt(HW_NOTIFICATION_REPLACE_LOCATION, repLocation);
        }
        return bundle;
    }

    public static boolean isFunctionExist(int mode) {
        if ((EYE_PROTECTIION_MODE & mode) != 0) {
            return true;
        }
        return false;
    }
}
