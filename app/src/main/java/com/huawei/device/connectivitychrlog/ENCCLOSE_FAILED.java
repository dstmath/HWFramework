package com.huawei.device.connectivitychrlog;

public class ENCCLOSE_FAILED extends Cenum {
    public ENCCLOSE_FAILED() {
        this.map.put("CLOSE_SUPPLICANT_CONNECT_FAILED", Integer.valueOf(0));
        this.map.put("CLOSE_SUPPLICANT_FAILED", Integer.valueOf(1));
        this.map.put("TIMEOUT", Integer.valueOf(2));
        this.map.put("CHR_WIFI_DRV_ERROR_POWER_OFF", Integer.valueOf(3));
        this.map.put("CHR_PLAT_DRV_ERROR_CLOSE_BCPU", Integer.valueOf(4));
        this.map.put("CHR_PLAT_DRV_ERROR_CLOSE_WCPU", Integer.valueOf(5));
        this.map.put("CHR_PLAT_DRV_ERROR_CLOSE_THREAD", Integer.valueOf(6));
        this.map.put("WIFI_SETTING_OPENED_AND_SERVICE_CLOSED", Integer.valueOf(7));
        this.map.put("NOT_CLOSE_FAIL", Integer.valueOf(8));
        setLength(1);
    }
}
