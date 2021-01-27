package com.android.server.wifi.cast.P2pSharing;

public class Constants {
    public static final int BASE = 1000;
    public static final int CMD_CONFIG_SERVER_REQUEST = 1000;
    public static final int CMD_CONFIG_SERVER_RESP = 1001;
    public static final int CMD_DYNAMIC_PORT_REQUEST = 1004;
    public static final int CMD_DYNAMIC_PORT_RESP = 1005;
    public static final int CMD_NETWORK_UNAVAILABLE = 1006;
    public static final int CMD_RECOVER_SERVER_REQUEST = 1002;
    public static final int CMD_RECOVER_SERVER_RESP = 1003;
    public static final int CMD_SHOW_OPTIMIZE_TOAST = 1007;
    public static final String CMD_TYPE = "CMD_TYPE";
    public static final int CMD_UNKNOWN = 1009;
    public static final String CONFIG_SERVER_RESULT = "CONFIG_SERVER_RESULT";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final int DEVICE_PHONE = 1;
    public static final String DEVICE_TYPE = "DEVICE_TYPE";
    public static final int EVENT_P2P_SHARING_CONFIG_FAILURE = 1;
    public static final int EVENT_P2P_SHARING_CONFIG_SUCCESS = 0;
    public static final int EVENT_P2P_SHARING_FINISH = 2;
    public static final int EVENT_P2P_SHARING_UNAVAILABLE = 3;
    public static final String EXTRA = "EXTRA";
    public static final int REASON_ALREADY_IN_SHARING = 4;
    public static final int REASON_DEFAULT = 0;
    public static final int REASON_NETWORK_UNAVAILABLE = 1;
    public static final int REASON_SOCKET_ERROR = 2;
    public static final int REASON_UNKNOWN = 3;
    public static final int RESULT_FAILURE = 1;
    public static final String RESULT_REASON = "RESULT_REASON";
    public static final int RESULT_SUCCESS = 0;
    public static final String ROOT_TAG = "P2pSharing:";
    public static final int SECURITY_INVALID = -1;
    public static final int SECURITY_OPEN = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_WPA2_PSK = 3;
    public static final int SECURITY_WPA3_PSK = 4;
    public static final int SECURITY_WPA_EAP = 5;
    public static final int SECURITY_WPA_PSK = 2;
    public static final int STATE_INTERNET_AVAILABLE = 1;
    public static final int STATE_INTERNET_UNAVAILABLE = 0;
    public static final String WIFI_SECURITY = "WIFI_SECURITY";

    private Constants() {
    }
}
