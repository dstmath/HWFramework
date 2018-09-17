package com.huawei.device.connectivitychrlog;

import com.huawei.connectivitylog.ConnectivityLogManager;

public class ENCABNORMAL_DISCONNECT extends Cenum {
    public ENCABNORMAL_DISCONNECT() {
        this.map.put("UNSPECIFIED", Integer.valueOf(1));
        this.map.put("PREV_AUTH_NOT_VALID", Integer.valueOf(2));
        this.map.put("WLAN_REASON_DEAUTH_LEAVING", Integer.valueOf(3));
        this.map.put("DISASSOC_DUE_TO_INACTIVITY", Integer.valueOf(4));
        this.map.put("DISASSOC_AP_BUSY", Integer.valueOf(5));
        this.map.put("CLASS2_FRAME_FROM_NONAUTH_STA", Integer.valueOf(6));
        this.map.put("CLASS3_FRAME_FROM_NONASSOC_STA", Integer.valueOf(7));
        this.map.put("DISASSOC_STA_HAS_LEFT", Integer.valueOf(8));
        this.map.put("STA_REQ_ASSOC_WITHOUT_AUTH", Integer.valueOf(9));
        this.map.put("PWR_CAPABILITY_NOT_VALID", Integer.valueOf(10));
        this.map.put("SUPPORTED_CHANNEL_NOT_VALID", Integer.valueOf(11));
        this.map.put("INVALID_IE", Integer.valueOf(13));
        this.map.put("MICHAEL_MIC_FAILURE", Integer.valueOf(14));
        this.map.put("FOURWAY_HANDSHAKE_TIMEOUT", Integer.valueOf(15));
        this.map.put("GROUP_KEY_UPDATE_TIMEOUT", Integer.valueOf(16));
        this.map.put("IE_IN_4WAY_DIFFERS", Integer.valueOf(17));
        this.map.put("GROUP_CIPHER_NOT_VALID", Integer.valueOf(18));
        this.map.put("PAIRWISE_CIPHER_NOT_VALID", Integer.valueOf(19));
        this.map.put("AKMP_NOT_VALID", Integer.valueOf(20));
        this.map.put("UNSUPPORTED_RSN_IE_VERSION", Integer.valueOf(21));
        this.map.put("INVALID_RSN_IE_CAPAB", Integer.valueOf(22));
        this.map.put("IEEE_802_1X_AUTH_FAILED", Integer.valueOf(23));
        this.map.put("CIPHER_SUITE_REJECTED", Integer.valueOf(24));
        this.map.put("TDLS_TEARDOWN_UNREACHABLE", Integer.valueOf(25));
        this.map.put("TDLS_TEARDOWN_UNSPECIFIED", Integer.valueOf(26));
        this.map.put("DISASSOC_LOW_ACK", Integer.valueOf(34));
        this.map.put("ABNORMAL_SUPPLICANT_SOCKET", Integer.valueOf(100));
        this.map.put("CHR_WIFI_DRV_ERROR_LINKLOSS", Integer.valueOf(ConnectivityLogManager.WIFI_USER_CONNECT));
        this.map.put("CHR_WIFI_DRV_ERROR_KEEPALIVE_TIMEOUT", Integer.valueOf(ConnectivityLogManager.WIFI_ACCESS_WEB_SLOWLY));
        this.map.put("CHR_WIFI_DRV_ERROR_CHANNEL_CHANGE", Integer.valueOf(ConnectivityLogManager.WIFI_POOR_LEVEL));
        setLength(1);
    }
}
