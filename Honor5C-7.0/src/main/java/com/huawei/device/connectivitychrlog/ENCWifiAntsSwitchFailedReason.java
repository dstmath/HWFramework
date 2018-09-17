package com.huawei.device.connectivitychrlog;

public class ENCWifiAntsSwitchFailedReason extends Cenum {
    public ENCWifiAntsSwitchFailedReason() {
        this.map.put("WIFI_ANTS_SW_FAILED_MODEM_PREEMPTIVE", Integer.valueOf(0));
        this.map.put("WIFI_ANTS_SW_FAILED_DEVICE_EXCEPTION", Integer.valueOf(1));
        this.map.put("WIFI_ANTS_SW_FAILED_ALG_EXCEPTION", Integer.valueOf(2));
        this.map.put("WIFI_ANTS_SW_FAILED_UNKNOWN_ERROR", Integer.valueOf(3));
        setLength(1);
    }
}
