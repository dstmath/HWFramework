package com.huawei.wifi2;

import android.util.wifi.HwHiLog;

public class HwWifi2ClientModeImplConst {
    public static final int BASE = 131072;
    public static final int CMD_ACCEPT_UNVALIDATED = 131225;
    public static final int CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF = 131281;
    public static final int CMD_ASSOCIATED_BSSID = 131219;
    public static final int CMD_BLUETOOTH_ADAPTER_STATE_CHANGE = 131103;
    public static final int CMD_BOOT_COMPLETED = 131206;
    public static final int CMD_CONFIG_ND_OFFLOAD = 131276;
    public static final int CMD_DIAGS_CONNECT_TIMEOUT = 131324;
    public static final int CMD_DISCONNECT = 131145;
    public static final int CMD_DISCONNECTING_WATCHDOG_TIMER = 131168;
    public static final int CMD_ENABLE_NETWORK = 131126;
    public static final int CMD_ENABLE_RSSI_POLL = 131154;
    public static final int CMD_ENABLE_TDLS = 131164;
    public static final int CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER = 131238;
    public static final int CMD_INITIALIZE = 131207;
    public static final int CMD_INSTALL_PACKET_FILTER = 131274;
    public static final int CMD_IPV4_PROVISIONING_FAILURE = 131273;
    public static final int CMD_IPV4_PROVISIONING_SUCCESS = 131272;
    public static final int CMD_IP_CONFIGURATION_LOST = 131211;
    public static final int CMD_IP_CONFIGURATION_SUCCESSFUL = 131210;
    public static final int CMD_IP_REACHABILITY_LOST = 131221;
    public static final int CMD_MAX_MSG_NUM = 131330;
    public static final int CMD_NETWORK_STATUS = 131220;
    public static final int CMD_ONESHOT_RSSI_POLL = 131156;
    public static final int CMD_POST_DHCP_ACTION = 131329;
    public static final int CMD_PRE_DHCP_ACTION = 131327;
    public static final int CMD_PRE_DHCP_ACTION_COMPLETE = 131328;
    public static final int CMD_READ_PACKET_FILTER = 131280;
    public static final int CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF = 131282;
    public static final int CMD_RESET_SUPPLICANT_STATE = 131183;
    public static final int CMD_ROAM_WATCHDOG_TIMER = 131166;
    public static final int CMD_RSSI_POLL = 131155;
    public static final int CMD_RSSI_THRESHOLD_BREACHED = 131236;
    public static final int CMD_SCREEN_STATE_CHANGED = 131167;
    public static final int CMD_SET_FALLBACK_PACKET_FILTERING = 131275;
    public static final int CMD_SET_HIGH_PERF_MODE = 131149;
    public static final int CMD_SET_OPERATIONAL_MODE = 131144;
    public static final int CMD_SET_SUSPEND_OPT_ENABLED = 131158;
    public static final int CMD_START_CONNECT = 131215;
    public static final int CMD_START_IP_PACKET_OFFLOAD = 131232;
    public static final int CMD_START_ROAM = 131217;
    public static final int CMD_START_RSSI_MONITORING_OFFLOAD = 131234;
    public static final int CMD_START_SUBSCRIPTION_PROVISIONING = 131326;
    public static final int CMD_STOP_IP_PACKET_OFFLOAD = 131233;
    public static final int CMD_STOP_RSSI_MONITORING_OFFLOAD = 131235;
    public static final int CMD_TARGET_BSSID = 131213;
    public static final int CMD_TRY_CACHED_IP = 131330;
    public static final int CMD_UNWANTED_NETWORK = 131216;
    public static final int CMD_UPDATE_LINKPROPERTIES = 131212;
    public static final int CONNECT_MODE = 1;
    public static final int DISABLED_MODE = 4;
    public static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;
    public static final int FAILURE = -1;
    public static final int IPCLIENT_TIMEOUT_MS = 10000;
    public static final int NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN = 2;
    public static final int NETWORK_STATUS_UNWANTED_DISCONNECT = 0;
    public static final int NETWORK_STATUS_UNWANTED_VALIDATION_FAILED = 1;
    public static final int ROAM_GUARD_TIMER_MSEC = 15000;
    public static final int SUCCESS = 1;
    public static final String SUPPLICANT_BSSID_ANY = "any";
    public static final int SUSPEND_DUE_TO_DHCP = 1;
    public static final int SUSPEND_DUE_TO_HIGH_PERF = 2;
    public static final int SUSPEND_DUE_TO_SCREEN = 4;
    private static final String TAG = "HwWifi2ClientModeImplConst";
    public static final String WIFI2_ICON_INFO = "wifi2IconInfo";
    public static final int WIFI2_NONE_ICON = 0;
    public static final int WIFI2_NORMAL_ICON = 1;
    public static final int WIFI2_WIFI6_ICON = 2;
    public static final int WIFI2_WIFI6_PLUS_ICON = 3;

    public static String messageNumToString(int type) {
        switch (type) {
            case CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /* 131103 */:
                return "CMD_BLUETOOTH_ADAPTER_STATE_CHANGE";
            case CMD_ENABLE_NETWORK /* 131126 */:
                return "CMD_ENABLE_NETWORK";
            case CMD_SET_OPERATIONAL_MODE /* 131144 */:
                return "CMD_SET_OPERATIONAL_MODE";
            case CMD_DISCONNECT /* 131145 */:
                return "CMD_DISCONNECT";
            case CMD_SET_HIGH_PERF_MODE /* 131149 */:
                return "CMD_SET_HIGH_PERF_MODE";
            case CMD_ENABLE_RSSI_POLL /* 131154 */:
                return "CMD_ENABLE_RSSI_POLL";
            case CMD_RSSI_POLL /* 131155 */:
                return "CMD_RSSI_POLL";
            case CMD_ONESHOT_RSSI_POLL /* 131156 */:
                return "CMD_ONESHOT_RSSI_POLL";
            case CMD_SET_SUSPEND_OPT_ENABLED /* 131158 */:
                return "CMD_SET_SUSPEND_OPT_ENABLED";
            case CMD_ROAM_WATCHDOG_TIMER /* 131166 */:
                return "CMD_ROAM_WATCHDOG_TIMER";
            case CMD_SCREEN_STATE_CHANGED /* 131167 */:
                return "CMD_SCREEN_STATE_CHANGED";
            case CMD_DISCONNECTING_WATCHDOG_TIMER /* 131168 */:
                return "CMD_DISCONNECTING_WATCHDOG_TIMER";
            case CMD_RESET_SUPPLICANT_STATE /* 131183 */:
                return "CMD_RESET_SUPPLICANT_STATE";
            case CMD_BOOT_COMPLETED /* 131206 */:
                return "CMD_BOOT_COMPLETED";
            case CMD_INITIALIZE /* 131207 */:
                return "CMD_INITIALIZE";
            case CMD_IP_CONFIGURATION_SUCCESSFUL /* 131210 */:
                return "CMD_IP_CONFIGURATION_SUCCESSFUL";
            case CMD_IP_CONFIGURATION_LOST /* 131211 */:
                return "CMD_IP_CONFIGURATION_LOST";
            case CMD_UPDATE_LINKPROPERTIES /* 131212 */:
                return "CMD_UPDATE_LINKPROPERTIES";
            case CMD_TARGET_BSSID /* 131213 */:
                return "CMD_TARGET_BSSID";
            case CMD_START_CONNECT /* 131215 */:
                return "CMD_START_CONNECT";
            case CMD_UNWANTED_NETWORK /* 131216 */:
                return "CMD_UNWANTED_NETWORK";
            case CMD_START_ROAM /* 131217 */:
                return "CMD_START_ROAM";
            case CMD_ASSOCIATED_BSSID /* 131219 */:
                return "CMD_ASSOCIATED_BSSID";
            case CMD_NETWORK_STATUS /* 131220 */:
                return "CMD_NETWORK_STATUS";
            case CMD_IP_REACHABILITY_LOST /* 131221 */:
                return "CMD_IP_REACHABILITY_LOST";
            case CMD_ACCEPT_UNVALIDATED /* 131225 */:
                return "CMD_ACCEPT_UNVALIDATED";
            case CMD_START_IP_PACKET_OFFLOAD /* 131232 */:
                return "CMD_START_IP_PACKET_OFFLOAD";
            case CMD_STOP_IP_PACKET_OFFLOAD /* 131233 */:
                return "CMD_STOP_IP_PACKET_OFFLOAD";
            case CMD_START_RSSI_MONITORING_OFFLOAD /* 131234 */:
                return "CMD_START_RSSI_MONITORING_OFFLOAD";
            case CMD_STOP_RSSI_MONITORING_OFFLOAD /* 131235 */:
                return "CMD_STOP_RSSI_MONITORING_OFFLOAD";
            case CMD_RSSI_THRESHOLD_BREACHED /* 131236 */:
                return "CMD_RSSI_THRESHOLD_BREACHED";
            case CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER /* 131238 */:
                return "CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER";
            case CMD_IPV4_PROVISIONING_SUCCESS /* 131272 */:
                return "CMD_IPV4_PROVISIONING_SUCCESS";
            case CMD_IPV4_PROVISIONING_FAILURE /* 131273 */:
                return "CMD_IPV4_PROVISIONING_FAILURE";
            case CMD_INSTALL_PACKET_FILTER /* 131274 */:
                return "CMD_INSTALL_PACKET_FILTER";
            case CMD_SET_FALLBACK_PACKET_FILTERING /* 131275 */:
                return "CMD_SET_FALLBACK_PACKET_FILTERING";
            case CMD_CONFIG_ND_OFFLOAD /* 131276 */:
                return "CMD_CONFIG_ND_OFFLOAD";
            case CMD_PRE_DHCP_ACTION /* 131327 */:
                return "CMD_PRE_DHCP_ACTION";
            case CMD_PRE_DHCP_ACTION_COMPLETE /* 131328 */:
                return "CMD_PRE_DHCP_ACTION_COMPLETE";
            case CMD_POST_DHCP_ACTION /* 131329 */:
                return "CMD_POST_DHCP_ACTION";
            case HwWifi2Monitor.SLAVE_WIFI_SUP_CONNECTION_EVENT /* 147457 */:
                return "SLAVE_WIFI_SUP_CONNECTION_EVENT";
            case HwWifi2Monitor.SLAVE_WIFI_SUP_DISCONNECTION_EVENT /* 147458 */:
                return "SLAVE_WIFI_SUP_DISCONNECTION_EVENT";
            case HwWifi2Monitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                return "NETWORK_CONNECTION_EVENT";
            case HwWifi2Monitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                return "NETWORK_DISCONNECTION_EVENT";
            case HwWifi2Monitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                return "SUPPLICANT_STATE_CHANGE_EVENT";
            case HwWifi2Monitor.AUTHENTICATION_FAILURE_EVENT /* 147463 */:
                return "AUTHENTICATION_FAILURE_EVENT";
            case HwWifi2Monitor.WAPI_AUTHENTICATION_FAILURE_EVENT /* 147474 */:
                return "WAPI_AUTHENTICATION_FAILURE_EVENT";
            case HwWifi2Monitor.WAPI_CERTIFICATION_FAILURE_EVENT /* 147475 */:
                return "WAPI_CERTIFICATION_FAILURE_EVENT";
            case HwWifi2Monitor.ASSOCIATION_REJECTION_EVENT /* 147499 */:
                return "ASSOCIATION_REJECTION_EVENT";
            case HwWifi2Monitor.VOWIFI_DETECT_IRQ_STR_EVENT /* 147520 */:
                return "VOWIFI_DETECT_IRQ_STR_EVENT";
            case HwWifi2Monitor.WPS_START_OKC_EVENT /* 147656 */:
                return "WPS_START_OKC_EVENT";
            case HwWifi2Monitor.WPA3_CONNECT_FAIL_EVENT /* 147666 */:
                return "WPA3_CONNECT_FAIL_EVENT";
            case HwWifi2Monitor.ANT_CORE_ROB_EVNET /* 147757 */:
                return "ANT_CORE_ROB_EVNET";
            case HwWifi2Monitor.EAP_ERRORCODE_REPORT_EVENT /* 147956 */:
                return "EAP_ERRORCODE_REPORT_EVENT";
            case 151553:
                return "CONNECT_NETWORK";
            case 151556:
                return "FORGET_NETWORK";
            case 151559:
                return "SAVE_NETWORK";
            case 151572:
                return "RSSI_PKTCNT_FETCH";
            default:
                HwHiLog.i(TAG, false, "UNKNOWN_MSG num = %{public}d", new Object[]{Integer.valueOf(type)});
                return "UNKNOWN_MSG";
        }
    }

    public static String connectModeMsgToString(int connectMode) {
        if (connectMode == 1) {
            return "CONNECT_MODE";
        }
        if (connectMode != 4) {
            return "UNKNOWN_MSG";
        }
        return "DISABLED_MODE";
    }
}
