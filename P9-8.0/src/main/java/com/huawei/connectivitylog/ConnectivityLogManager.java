package com.huawei.connectivitylog;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import com.huawei.ncdft.HwWifiDFTConnManager;

public class ConnectivityLogManager extends Handler {
    public static final int CHR_GNSS_HAL_EVENT_EXCEPTION = 202;
    public static final int CHR_GNSS_HAL_EVENT_EXCEPTION_EX = 206;
    public static final int CHR_GNSS_HAL_EVENT_INJECT = 203;
    public static final int CHR_GNSS_HAL_EVENT_INJECT_EX = 207;
    public static final int CHR_GNSS_HAL_EVENT_SYSCALL = 201;
    public static final int CHR_GNSS_HAL_EVENT_SYSCALL_EX = 205;
    public static final int GPS_DAILY_CNT_REPORT = 71;
    private static final int GPS_POS_ERROR_EVENT = 72;
    public static final int GPS_POS_FLOW_ERROR_EVENT = 65;
    public static final int GPS_POS_FLOW_ERROR_EVENT_EX = 68;
    public static final int GPS_POS_TIMEOUT_EVENT = 66;
    public static final int GPS_POS_TIMEOUT_EVENT_EX = 69;
    private static final int GPS_SESSION_EVENT = 73;
    static final String LOG_TAG = "CONNECTIVITY_LOG";
    public static final int NETWK_POS_TIMEOUT_EVENT = 64;
    public static final int NETWK_POS_TIMEOUT_EVENT_EX = 67;
    public static final int WIFI_ABNORMAL_DISCONNECT = 85;
    public static final int WIFI_ABNORMAL_DISCONNECT_EX = 95;
    public static final int WIFI_ABS_ASSOC_FAILED_EVENT = 215;
    public static final int WIFI_ABS_BLACKLIST_EVENT = 217;
    public static final int WIFI_ABS_STATISTICS_EVENT = 216;
    public static final int WIFI_ACCESS_INTERNET_FAILED = 87;
    public static final int WIFI_ACCESS_INTERNET_FAILED_EX = 97;
    public static final int WIFI_ACCESS_WEB_SLOWLY = 102;
    public static final int WIFI_ACCESS_WEB_SLOWLY_EX = 104;
    public static final int WIFI_ANTS_SWITCH_FAILED = 210;
    public static final int WIFI_ANTS_SWITCH_FAILED_EX = 211;
    public static final int WIFI_ANTS_SWITCH_FAILED_PLACEHOLDER = 212;
    public static final int WIFI_AP_INFO_COLLECT = 213;
    public static final int WIFI_CLOSE_FAILED = 81;
    public static final int WIFI_CLOSE_FAILED_EX = 91;
    public static final int WIFI_CONNECT_ASSOC_FAILED = 83;
    public static final int WIFI_CONNECT_ASSOC_FAILED_EX = 93;
    public static final int WIFI_CONNECT_AUTH_FAILED = 82;
    public static final int WIFI_CONNECT_AUTH_FAILED_EX = 92;
    public static final int WIFI_CONNECT_DHCP_FAILED = 84;
    public static final int WIFI_CONNECT_DHCP_FAILED_EX = 94;
    public static final int WIFI_CONNECT_EVENT = 214;
    public static final int WIFI_DEVICE_ERROR = 208;
    public static final int WIFI_DEVICE_ERROR_EX = 209;
    public static final int WIFI_HAL_DRIVER_DEVICE_EXCEPTION = 200;
    public static final int WIFI_HAL_DRIVER_EXCEPTION_EX = 204;
    public static final int WIFI_OPEN_FAILED = 80;
    public static final int WIFI_OPEN_FAILED_EX = 90;
    public static final int WIFI_POOR_LEVEL = 103;
    public static final int WIFI_PORTAL_AUTH_MSG_COLLECTE = 124;
    public static final int WIFI_PORTAL_SAMPLES_COLLECTE = 120;
    public static final int WIFI_REPEATER_OPEN_OR_CLOSE_FAILED = 127;
    public static final int WIFI_SCAN_FAILED = 86;
    public static final int WIFI_SCAN_FAILED_EX = 96;
    public static final int WIFI_STABILITY_STAT = 110;
    public static final int WIFI_STATUS_CHANGEDBY_APK = 98;
    public static final int WIFI_USER_CONNECT = 101;
    public static final int WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT = 126;
    public static final int WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT = 125;
    public static final int WIFI_WIFIPRO_EXCEPTION_EVENT = 122;
    public static final int WIFI_WIFIPRO_STATISTICS_EVENT = 121;
    public static final int WIFI_WORKAROUND_STAT = 113;
    private static ConnectivityLogManager sInstance;

    private ConnectivityLogManager(Looper looper) {
        super(looper);
        Log.d(LOG_TAG, "new ConnectivityLogManager");
    }

    public static synchronized ConnectivityLogManager getInstance() {
        ConnectivityLogManager connectivityLogManager;
        synchronized (ConnectivityLogManager.class) {
            if (sInstance == null) {
                HandlerThread thread = new HandlerThread("ConnectivityLogManager");
                thread.start();
                sInstance = new ConnectivityLogManager(thread.getLooper());
            }
            connectivityLogManager = sInstance;
        }
        return connectivityLogManager;
    }

    public static void init(Context context) {
        Log.d(LOG_TAG, "ConnectivityLogManager context = " + context);
        LogManager.init(context);
        HwWifiDFTConnManager.init(context);
    }
}
