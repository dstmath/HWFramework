package com.android.server.wifi.wifipro;

import android.net.wifi.ScanResult;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiStateMachine;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WifiproUtils {
    public static final String ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND = "com.huawei.wifipro.action.ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND";
    public static final String ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE = "com.huawei.wifipro.action.ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE";
    public static final String ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND = "com.huawei.wifipro.action.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND";
    public static final String ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND = "com.huawei.wifipro.action.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND";
    public static final String ACTION_NOTIFY_PORTAL_OUT_OF_RANGE = "com.huawei.wifipro.action.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE";
    public static final String ACTION_NOTIFY_SAVED_PORTAL_FOUND = "com.huawei.wifipro.action.ACTION_NOTIFY_SAVED_PORTAL_FOUND";
    public static final String ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY = "com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY";
    public static final int AUTOMATIC_CONNECT_AP = 135676;
    public static final int AUTO_EVALUATE_BACK = 0;
    public static final int AUTO_EVALUATE_SETTINGS = 1;
    public static final int CMD_WIFIPRO_CONNECTED_TO_VERIFY_STATE = 135669;
    public static final int CMD_WIFIPRO_STATE_BASE = 135668;
    public static final int CMD_WIFIPRO_VERIFY_TO_CONNECTED_STATE = 135670;
    public static final String CODE_EXCLUSIVE = "code_exclusive";
    public static final String CODE_EXCLUSIVE_DATE_1 = "code_exclusive_date_1";
    public static final String CODE_EXCLUSIVE_DATE_2 = "code_exclusive_date_2";
    public static final String CODE_EXCLUSIVE_DATE_3 = "code_exclusive_date_3";
    public static final String CODE_NECESSARY = "code_necessary";
    public static final boolean DBG = true;
    public static final boolean IS_DUALBAND_ENABLE = true;
    public static final int MANUAL_CONNECT_AP = 135675;
    public static final int MANUAL_EVALUATE = 2;
    public static final int NET_INET_QOS_BSSID_CHG_WHILE_CHKING = -102;
    public static final int NET_INET_QOS_LEVEL_0_NOT_AVAILABLE = 0;
    public static final int NET_INET_QOS_LEVEL_1_VERY_POOR = 1;
    public static final int NET_INET_QOS_LEVEL_2_POOR = 2;
    public static final int NET_INET_QOS_LEVEL_3_GOOD = 3;
    public static final int NET_INET_QOS_LEVEL_4_BETTER = 4;
    public static final int NET_INET_QOS_LEVEL_5_BEST = 5;
    public static final int NET_INET_QOS_LEVEL_6_PORTAL = 6;
    public static final int NET_INET_QOS_LEVEL_7_TIMEOUT = 7;
    public static final int NET_INET_QOS_LEVEL_NEG_1_NO_INET = -1;
    public static final int NET_INET_QOS_LEVEL_NEG_2_MAYBE_POOR = -2;
    public static final int NET_INET_QOS_LEVEL_UNKNOWN = -101;
    public static final String PERMISSION_RECV_WIFI_CONNECTED_CONCURRENTLY = "com.huawei.wifipro.permission.RECV.WIFI_CONNECTED_CONCURRENTLY";
    public static final int REQUEST_POOR_RSSI_INET_CHECK = -104;
    public static final int REQUEST_WIFI_INET_CHECK = -103;
    public static final String SMS_BODY_OPT = "sms_body_opt";
    public static final String SMS_NUM_BEGIN = "sms_num_begin";
    public static final String SMS_NUM_LEN = "sms_num_min_len";
    public static final String TAG = "WiFi_PRO";
    public static final int WIFIPRO_START_VERIFY_WITH_DATA_LINK = 135672;
    public static final int WIFIPRO_START_VERIFY_WITH_NOT_DATA_LINK = 135671;
    public static final int WIFIPRO_STOP_VERIFY_WITH_DATA_LINK = 135674;
    public static final int WIFIPRO_STOP_VERIFY_WITH_NOT_DATA_LINK = 135673;
    public static final int WIFI_BACKGROUND_AP_SCORE = 1;
    public static final int WIFI_BACKGROUND_IDLE = 0;
    public static final int WIFI_BACKGROUND_INTERNET_RECOVERY_CHECKING = 3;
    public static final int WIFI_BACKGROUND_PORTAL_CHECKING = 2;
    public static final int WIFI_CONNECT_WITH_DATA_LINK = 1;
    public static final int WIFI_VERIFY_NO_DATA_LINK = 2;

    public static String formatTime(long time) {
        if (time == 0) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
    }

    public static List<ScanResult> getScanResultsFromWsm() {
        List<ScanResult> scanResults = new ArrayList();
        WifiStateMachine wsm = WifiInjector.getInstance().getWifiStateMachine();
        if (wsm != null) {
            return wsm.syncGetScanResultsList();
        }
        return scanResults;
    }
}
