package com.huawei.device.connectivitychrlog;

public class ENCSCAN_FAILED extends Cenum {
    public ENCSCAN_FAILED() {
        this.map.put("CONNECTED", Integer.valueOf(1));
        this.map.put("UNCONNECTED", Integer.valueOf(2));
        this.map.put("SCAN_TRIGGER_TIMEOUT", Integer.valueOf(3));
        this.map.put("CHR_WIFI_DRV_ERROR_SCAN_REFUSED", Integer.valueOf(4));
        this.map.put("CHR_WIFI_DRV_ERROR_SCAN_TIMEOUT", Integer.valueOf(5));
        this.map.put("CHR_WIFI_DRV_ERROR_SCAN_ZERO", Integer.valueOf(6));
        this.map.put("CHR_WIFI_HAL_ERROR_SCAN_TIME_OUT", Integer.valueOf(7));
        setLength(1);
    }
}
