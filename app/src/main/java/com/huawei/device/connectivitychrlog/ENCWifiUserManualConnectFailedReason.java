package com.huawei.device.connectivitychrlog;

public class ENCWifiUserManualConnectFailedReason extends Cenum {
    public ENCWifiUserManualConnectFailedReason() {
        this.map.put("CHR_WIFI_DRV_ERROR_CONNECT_CMD", Integer.valueOf(1));
        this.map.put("CHR_WIFI_DRV_ERROR_AUTH_REJECTED", Integer.valueOf(2));
        this.map.put("CHR_WIFI_DRV_ERROR_ASSOC_REJECTED", Integer.valueOf(3));
        setLength(1);
    }
}
