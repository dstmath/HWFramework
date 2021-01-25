package com.android.server.wifi.LAA;

import android.content.ContentResolver;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;

public class HwLaaUtils {
    public static final boolean DEBUG_LOG = false;
    public static final String[] DELAY_SENSITIVE_APPS = {"com.whatsapp", "com.facebook.orca", "com.tencent.mm"};
    public static final String EXTRA_LAA_STATE = "laa_state";
    public static final int LAA_CONTROL_CMD_DISABLE = 0;
    public static final int LAA_CONTROL_CMD_ENABLE = 1;
    public static final int LAA_CONTROL_TYPE_CONTENT_AWARE = 5;
    public static final int LAA_CONTROL_TYPE_WIFI_HOTSPOT = 3;
    public static final int LAA_CONTROL_TYPE_WIFI_P2P = 2;
    public static final int LAA_CONTROL_TYPE_WIFI_PLUS = 4;
    public static final int LAA_CONTROL_TYPE_WIFI_STA = 1;
    private static final boolean LAA_PLUS_ENABLE = SystemProperties.getBoolean(LAA_PLUS_PROP, false);
    public static final String LAA_PLUS_PROP = "ro.config.hw_laaplus";
    public static final int LAA_STATE_ACTIVED = 1;
    public static final String LAA_STATE_CHANGED_ACTION = "com.huawei.laa.action.STATE_CHANGE_ACTION";
    public static final int LAA_STATE_DEACTIVATE = 0;
    public static final int LAA_STATE_UNKNOW = -1;
    public static final int MSG_MOBILE_DATA_STATE_CHANGED = 4;
    public static final int MSG_NETWORK_CONNECTION_CHANGED = 3;
    public static final int MSG_PHONE_SERVICE_POWER_ON = 2;
    public static final int MSG_REQUEST_SEND_LAA_CMD = 1;
    public static final int NETWORK_CONNECTED = 1;
    public static final int NETWORK_DISCONNECTED = 2;
    public static final int SENSITIVE_APP_SCORE = 2;
    public static final int SENSITIVE_UPD_PACKETS = 10;
    public static final String TAG = "LAA_";

    public static boolean isLaaPlusEnable() {
        return LAA_PLUS_ENABLE;
    }

    public static boolean matchSensitiveApp(String app) {
        if (TextUtils.isEmpty(app)) {
            return false;
        }
        int length = DELAY_SENSITIVE_APPS.length;
        for (int i = 0; i < length; i++) {
            if (app.equals(DELAY_SENSITIVE_APPS[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean getSettingsGlobalBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.Global.getInt(cr, name, def ? 1 : 0) == 1;
    }

    public static void logD(String tag, boolean isFmtStrPrivate, String info, Object... args) {
        HwHiLog.d(tag, isFmtStrPrivate, info, args);
    }

    public static void logW(String tag, boolean isFmtStrPrivate, String info, Object... args) {
        HwHiLog.w(tag, isFmtStrPrivate, info, args);
    }

    public static void debug_Log(String tag, boolean isFmtStrPrivate, String info, Object... args) {
    }
}
