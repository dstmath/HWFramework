package com.huawei.device.connectivitychrlog;

public class ENCWifiDeviceErrorReason extends Cenum {
    public ENCWifiDeviceErrorReason() {
        this.map.put("CHR_WIFI_DEV_ERROR_FEM_FAIL", Integer.valueOf(1));
        this.map.put("CHR_WIFI_DEV_ERROR_32K_CLK", Integer.valueOf(2));
        this.map.put("CHR_WIFI_DEV_ERROR_GPIO", Integer.valueOf(3));
        this.map.put("CHR_WIFI_DEV_ERROR_SDIO_ENUM", Integer.valueOf(4));
        this.map.put("CHR_WIFI_DEV_ERROR_IOMUX", Integer.valueOf(5));
        this.map.put("CHR_WIFI_DEV_ERROR_UART", Integer.valueOf(6));
        setLength(1);
    }
}
