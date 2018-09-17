package com.huawei.device.connectivitychrlog;

public class ENCWifiConnectAuthFailedReason extends Cenum {
    public ENCWifiConnectAuthFailedReason() {
        this.map.put("CHR_WIFI_DRV_ERROR_AUTH_TIMEOUT", Integer.valueOf(1));
        setLength(1);
    }
}
