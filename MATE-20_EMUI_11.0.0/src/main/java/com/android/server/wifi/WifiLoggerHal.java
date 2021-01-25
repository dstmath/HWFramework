package com.android.server.wifi;

public class WifiLoggerHal {
    public static final byte FRAME_TYPE_80211_MGMT = 2;
    public static final byte FRAME_TYPE_ETHERNET_II = 1;
    public static final byte FRAME_TYPE_UNKNOWN = 0;
    public static final int MAX_FATE_LOG_LEN = 32;
    public static final byte RX_PKT_FATE_DRV_DROP_FILTER = 7;
    public static final byte RX_PKT_FATE_DRV_DROP_INVALID = 8;
    public static final byte RX_PKT_FATE_DRV_DROP_NOBUFS = 9;
    public static final byte RX_PKT_FATE_DRV_DROP_OTHER = 10;
    public static final byte RX_PKT_FATE_DRV_QUEUED = 6;
    public static final byte RX_PKT_FATE_FW_DROP_FILTER = 2;
    public static final byte RX_PKT_FATE_FW_DROP_INVALID = 3;
    public static final byte RX_PKT_FATE_FW_DROP_NOBUFS = 4;
    public static final byte RX_PKT_FATE_FW_DROP_OTHER = 5;
    public static final byte RX_PKT_FATE_FW_QUEUED = 1;
    public static final byte RX_PKT_FATE_SUCCESS = 0;
    public static final byte TX_PKT_FATE_ACKED = 0;
    public static final byte TX_PKT_FATE_DRV_DROP_INVALID = 7;
    public static final byte TX_PKT_FATE_DRV_DROP_NOBUFS = 8;
    public static final byte TX_PKT_FATE_DRV_DROP_OTHER = 9;
    public static final byte TX_PKT_FATE_DRV_QUEUED = 6;
    public static final byte TX_PKT_FATE_FW_DROP_INVALID = 3;
    public static final byte TX_PKT_FATE_FW_DROP_NOBUFS = 4;
    public static final byte TX_PKT_FATE_FW_DROP_OTHER = 5;
    public static final byte TX_PKT_FATE_FW_QUEUED = 2;
    public static final byte TX_PKT_FATE_SENT = 1;
    public static final int WIFI_ALERT_REASON_MAX = 1024;
    public static final int WIFI_ALERT_REASON_MIN = 0;
    public static final int WIFI_ALERT_REASON_RESERVED = 0;
}
