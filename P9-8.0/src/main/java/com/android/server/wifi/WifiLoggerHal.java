package com.android.server.wifi;

public class WifiLoggerHal {
    public static final byte FRAME_TYPE_80211_MGMT = (byte) 2;
    public static final byte FRAME_TYPE_ETHERNET_II = (byte) 1;
    public static final byte FRAME_TYPE_UNKNOWN = (byte) 0;
    public static final int MAX_FATE_LOG_LEN = 32;
    public static final byte RX_PKT_FATE_DRV_DROP_FILTER = (byte) 7;
    public static final byte RX_PKT_FATE_DRV_DROP_INVALID = (byte) 8;
    public static final byte RX_PKT_FATE_DRV_DROP_NOBUFS = (byte) 9;
    public static final byte RX_PKT_FATE_DRV_DROP_OTHER = (byte) 10;
    public static final byte RX_PKT_FATE_DRV_QUEUED = (byte) 6;
    public static final byte RX_PKT_FATE_FW_DROP_FILTER = (byte) 2;
    public static final byte RX_PKT_FATE_FW_DROP_INVALID = (byte) 3;
    public static final byte RX_PKT_FATE_FW_DROP_NOBUFS = (byte) 4;
    public static final byte RX_PKT_FATE_FW_DROP_OTHER = (byte) 5;
    public static final byte RX_PKT_FATE_FW_QUEUED = (byte) 1;
    public static final byte RX_PKT_FATE_SUCCESS = (byte) 0;
    public static final byte TX_PKT_FATE_ACKED = (byte) 0;
    public static final byte TX_PKT_FATE_DRV_DROP_INVALID = (byte) 7;
    public static final byte TX_PKT_FATE_DRV_DROP_NOBUFS = (byte) 8;
    public static final byte TX_PKT_FATE_DRV_DROP_OTHER = (byte) 9;
    public static final byte TX_PKT_FATE_DRV_QUEUED = (byte) 6;
    public static final byte TX_PKT_FATE_FW_DROP_INVALID = (byte) 3;
    public static final byte TX_PKT_FATE_FW_DROP_NOBUFS = (byte) 4;
    public static final byte TX_PKT_FATE_FW_DROP_OTHER = (byte) 5;
    public static final byte TX_PKT_FATE_FW_QUEUED = (byte) 2;
    public static final byte TX_PKT_FATE_SENT = (byte) 1;
    public static final byte WIFI_ALERT_REASON_MAX = (byte) 64;
    public static final byte WIFI_ALERT_REASON_MIN = (byte) 0;
    public static final byte WIFI_ALERT_REASON_RESERVED = (byte) 0;
}
