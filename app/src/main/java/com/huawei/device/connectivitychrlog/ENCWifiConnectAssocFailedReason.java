package com.huawei.device.connectivitychrlog;

public class ENCWifiConnectAssocFailedReason extends Cenum {
    public ENCWifiConnectAssocFailedReason() {
        this.map.put("CHR_WIFI_DRV_ERROR_ASSOC_TIMEOUT", Integer.valueOf(1));
        setLength(1);
    }
}
