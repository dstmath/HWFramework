package com.huawei.hwwifiproservice;

import android.content.Context;
import android.os.Bundle;
import com.android.server.wifi.wifipro.HwWifiProServiceProxy;

public class WifiProManagerEx {
    public static final int INTERFACE_CALL_HWPORTALEXCEPTIONMANAGER_INIT = 29;
    public static final int INTERFACE_CALL_HWPORTALEXCEPTIONMANAGER_NOTIFY_DISCONNETCTED = 42;
    public static final int INTERFACE_CALL_SAVEDNETWORKEVALUATOR_NOTIFY_PORTAL_CHANGED = 43;
    public static final int INTERFACE_CHECK_WIFI_DEFAULT_ROUTE = 65;
    public static final int INTERFACE_DEAUTH_ROAMING_BSSID = 59;
    public static final int INTERFACE_DEL_STATIC_ARP = 58;
    public static final int INTERFACE_DO_GATWAY_ARP_TEST = 75;
    public static final int INTERFACE_DO_GRATUITOUSARP = 67;
    public static final int INTERFACE_DO_SLOW_ARP_TEST = 76;
    public static final int INTERFACE_GET_AP_VENDOR_INFO = 1;
    public static final int INTERFACE_GET_AVAILABLE_CHANNELS = 13;
    public static final int INTERFACE_GET_CAST_OPT_INFO = 87;
    public static final int INTERFACE_GET_DHCP_RESULTS = 46;
    public static final int INTERFACE_GET_GATEWAY_ARP_RESULT = 68;
    public static final int INTERFACE_GET_GW_ADDR = 70;
    public static final int INTERFACE_GET_HISTORY_SCAN_RESULTS = 86;
    public static final int INTERFACE_GET_NETWORK_FOR_TYPE_WIFI = 14;
    public static final int INTERFACE_GET_RSSI_PACKET_COUNT_INFO = 80;
    public static final int INTERFACE_GET_SCAN_RESULTS = 74;
    public static final int INTERFACE_GET_SCN_RESULTS_FROM_WSM = 12;
    public static final int INTERFACE_GET_WIFI6_WITHOUT_HTC_ARP_RESULT = 84;
    public static final int INTERFACE_GET_WIFI6_WITH_HTC_ARP_RESULT = 83;
    public static final int INTERFACE_GET_WIFISTATEMACHINE_MESSENGER = 78;
    public static final int INTERFACE_GET_WIFI_INFO = 52;
    public static final int INTERFACE_GET_WIFI_OTA_INFO = 85;
    public static final int INTERFACE_GET_WIFI_PREFERENCE_FROM_HIDATA = 25;
    public static final int INTERFACE_HANDLE_CONNECTION_STATE_CHANGED = 77;
    public static final int INTERFACE_HANDLE_INVALID_IPADDR = 49;
    public static final int INTERFACE_HANDLE_NO_INTERNET_IP = 45;
    public static final int INTERFACE_INCR_ACCESS_WEB_RECORD = 32;
    public static final int INTERFACE_IS_BSSID_DISABLED = 30;
    public static final int INTERFACE_IS_FULL_SCREEN = 9;
    public static final int INTERFACE_IS_HILINK_UNCONFIG_ROUTER = 34;
    public static final int INTERFACE_IS_HWARBITRATIONMANAGER_NOT_NULL = 24;
    public static final int INTERFACE_IS_IN_GAME_ADN_NEED_DISC = 27;
    public static final int INTERFACE_IS_REACHABLEBY_ICMP = 17;
    public static final int INTERFACE_IS_SCAN_AND_MANUAL_CONNECT_MODE = 18;
    public static final int INTERFACE_IS_WIFIPRO_EVALUATING_AP = 44;
    public static final int INTERFACE_IS_WIFI_RESTRICTED = 11;
    public static final int INTERFACE_MULTI_GATEWAY = 71;
    public static final int INTERFACE_NOTIFY_HWWIFIPROSERVICE_ACCOMPLISHED = 0;
    public static final int INTERFACE_NOTIFY_PORTAL_AUTHEN_STATUS = 41;
    public static final int INTERFACE_NOTIFY_PORTAL_CONNECTED_INFO = 5;
    public static final int INTERFACE_NOTIFY_SELF_ENGINE_RESET_COMPLETE = 62;
    public static final int INTERFACE_NOTIFY_SELF_ENGINE_STATE_END = 72;
    public static final int INTERFACE_NOTIFY_SELF_ENGINE_STATE_START = 69;
    public static final int INTERFACE_PIGN_GATWAY = 66;
    public static final int INTERFACE_PORTAL_NOTIFY_CHANGED = 33;
    public static final int INTERFACE_QUERY_11VROAMING_NETWORK = 26;
    public static final int INTERFACE_QUERY_BQE_RTT_RESULT = 7;
    public static final int INTERFACE_READ_TCP_STAT_LINES = 79;
    public static final int INTERFACE_REQUEST_REASSOC_LINK = 50;
    public static final int INTERFACE_REQUEST_RENEW_DHCP = 48;
    public static final int INTERFACE_REQUEST_RESET_WIFI = 51;
    public static final int INTERFACE_REQUEST_UPDATE_DNS_SERVERS = 54;
    public static final int INTERFACE_REQUEST_USE_STATIC_IPCONFIG = 53;
    public static final int INTERFACE_REQUEST_WIFI_SOFT_SWITCH = 21;
    public static final int INTERFACE_RESET_IPCONFIG_STATUS = 47;
    public static final int INTERFACE_RESET_WLAN_RTT = 6;
    public static final int INTERFACE_SEND_MESSAGE_TO_WIFISTATEMACHINE = 28;
    public static final int INTERFACE_SEND_QOE_CMD = 8;
    public static final int INTERFACE_SET_LAA_ENABLED = 31;
    public static final int INTERFACE_SET_STATIC_ARP = 57;
    public static final int INTERFACE_SET_WIFI_BACKGROUND_REASON = 55;
    public static final int INTERFACE_START_CONNECT_TO_USER_SELECT_NETWORK = 22;
    public static final int INTERFACE_START_CUSTOMIZED_SCAN = 10;
    public static final int INTERFACE_START_PROXY_SCAN = 23;
    public static final int INTERFACE_START_ROAM_TO_NETWORK = 20;
    public static final int INTERFACE_START_SCAN = 73;
    public static final int INTERFACE_START_WIFI2WIFI_REQUEST = 19;
    public static final int INTERFACE_UPDATE_ACCESS_WEB_EXCEPTION = 60;
    public static final int INTERFACE_UPDATE_AP_VENDOR_INFO = 3;
    public static final int INTERFACE_UPDATE_ARP_SUMMERY = 63;
    public static final int INTERFACE_UPDATE_CONNECT_TYPE = 15;
    public static final int INTERFACE_UPDATE_EVALUATE_SCAN_RESULT = 82;
    public static final int INTERFACE_UPDATE_VPN_STATE_CHANGED = 36;
    public static final int INTERFACE_UPDATE_WIFI_CONNECTION_MODE = 35;
    public static final int INTERFACE_UPDATE_WIFI_EXCEPTION = 4;
    public static final int INTERFACE_UPDATE_WIFI_SWITCH_TIME_STAMP = 16;
    public static final int INTERFACE_UPFATE_SC_CHR_COUNT = 61;
    public static final int INTERFACE_UPLOAD_DFT_EVENT = 2;
    public static final String SERVICE_NAME = "WIFIPRO_SERVICE";
    private static boolean isInitialized = false;
    private static HwWifiProServiceProxy sHwWifiProServiceProxy;

    public static synchronized void init(Context context) {
        synchronized (WifiProManagerEx.class) {
            if (context != null) {
                if (!isInitialized) {
                    sHwWifiProServiceProxy = HwWifiProServiceProxy.createHwWifiProServiceProxy(context);
                    isInitialized = true;
                }
            }
        }
    }

    public static synchronized Bundle ctrlHwWifiNetwork(String pkgName, int interfaceId, Bundle data) {
        Bundle result;
        synchronized (WifiProManagerEx.class) {
            result = null;
            if (isInitialized) {
                result = sHwWifiProServiceProxy.ctrlHwWifiNetwork(pkgName, interfaceId, data);
            }
        }
        return result;
    }
}
