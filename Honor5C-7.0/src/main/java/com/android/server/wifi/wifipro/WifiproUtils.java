package com.android.server.wifi.wifipro;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WifiproUtils {
    public static final String ACTION_NETWOR_PROPERTY_NOTIFICATION = "com.huawei.wifi.action.NETWOR_PROPERTY_NOTIFICATION";
    public static final String ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND = "com.huawei.wifipro.action.ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND";
    public static final String ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE = "com.huawei.wifipro.action.ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE";
    public static final String ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND = "com.huawei.wifipro.action.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND";
    public static final String ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND = "com.huawei.wifipro.action.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND";
    public static final String ACTION_NOTIFY_PORTAL_OUT_OF_RANGE = "com.huawei.wifipro.action.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE";
    public static final String ACTION_NOTIFY_SAVED_PORTAL_FOUND = "com.huawei.wifipro.action.ACTION_NOTIFY_SAVED_PORTAL_FOUND";
    public static final String ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY = "com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY";
    public static final String ACTION_REQUEST_TCP_RX_COUNTER = "com.huawei.wifi.action.ACTION_REQUEST_TCP_RX_COUNTER";
    public static final String ACTION_RESPONSE_TCP_RX_COUNTER = "com.huawei.wifi.action.ACTION_RESPONSE_TCP_RX_COUNTER";
    public static final String ACTION_UPDATE_CONFIG_HISTORY = "com.huawei.wifipro.ACTION_UPDATE_CONFIG_HISTORY";
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
    public static final String EXTRA_FLAG_NETWOR_PROPERTY = "wifi_network_property";
    public static final String EXTRA_FLAG_NEW_WIFI_CONFIG = "new_wifi_config";
    public static final String EXTRA_FLAG_NW_QOS_LEVEL = "network_qos_level";
    public static final String EXTRA_FLAG_NW_QOS_MONITOR_ENABLE = "qos_monitor_enable";
    public static final String EXTRA_FLAG_NW_TYPE = "network_type";
    public static final String EXTRA_FLAG_QOS_VERIFY_TYPE = "network_qos_verify_type";
    public static final String EXTRA_FLAG_TCP_RX_COUNTER = "wifipro_tcp_rx_counter";
    public static final String EXTRA_VERIFYING_LINK_STATE_BSSID = "bssid";
    public static final boolean IS_DUALBAND_ENABLE = true;
    public static final int MANUAL_CONNECT_AP = 135675;
    public static final int MANUAL_EVALUATE = 2;
    public static final String NETWORK_CHECKER_RECV_PERMISSION = "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER";
    public static final String NETWORK_DETECTION_RESULT_ACTION = "huawei.network.detection.result_action";
    public static final String NETWORK_DETECTION_START_ACTION = "huawei.network.detection.start_action";
    public static final String NETWORK_QOS_MONITOR_RESULT_ACTION = "huawei.network.monitor.result_action";
    public static final String NETWORK_QOS_MONITOR_START_ACTION = "huawei.network.monitor.start_action";
    public static final String NETWORK_QOS_VERIFY_REQUEST_ACTION = "huawei.nw.qos.verify.rqs_action";
    public static final int NETWORK_TYPE_MOBILE = 0;
    public static final int NETWORK_TYPE_NONE = -1;
    public static final int NETWORK_TYPE_TCP_IP = 101;
    public static final int NETWORK_TYPE_WIFI = 1;
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
    public static final int NET_TCP_QOS_LEVEL_1_POOR = 1;
    public static final int NET_TCP_QOS_LEVEL_2_MAYBE_POOR = 2;
    public static final int NET_TCP_QOS_LEVEL_3_MAYBE_POOR = 3;
    public static final int NET_TCP_QOS_LEVEL_4_GOOD = 4;
    public static final String PERMISSION_RECV_WIFI_CONNECTED_CONCURRENTLY = "com.huawei.wifipro.permission.RECV.WIFI_CONNECTED_CONCURRENTLY";
    public static final int REQUEST_POOR_RSSI_INET_CHECK = -104;
    public static final int REQUEST_WIFI_INET_CHECK = -103;
    public static final String SMS_BODY_OPT = "sms_body_opt";
    public static final String SMS_NUM_BEGIN = "sms_num_begin";
    public static final String SMS_NUM_LEN = "sms_num_min_len";
    public static final String TAG = "WiFi_PRO";
    public static final String WIFIPRO_ENTER_VERIFYING_LINK_STATE_ACTION = "huawei.wifipro.enter.VerifyingLinkState_action";
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
}
