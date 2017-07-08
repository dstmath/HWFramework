package com.huawei.device.connectivitychrlog;

public class ENCOPEN_FAILED extends Cenum {
    public ENCOPEN_FAILED() {
        this.map.put("DIRVER_FAILED", Integer.valueOf(0));
        this.map.put("FIRMWARE_FAILED", Integer.valueOf(1));
        this.map.put("START_SUPPLICANT_FAILED", Integer.valueOf(2));
        this.map.put("CONNECT_SUPPLICANT_FAILED", Integer.valueOf(3));
        this.map.put("SUPPLICANT_CONNECT_LOST", Integer.valueOf(4));
        this.map.put("TIMEOUT", Integer.valueOf(5));
        this.map.put("CHR_WIFI_DRV_ERROR_INSMOD_KO", Integer.valueOf(6));
        this.map.put("CHR_WIFI_DRV_ERROR_SYS_VERSION", Integer.valueOf(7));
        this.map.put("CHR_WIFI_DRV_ERROR_POWER_ON", Integer.valueOf(8));
        this.map.put("CHR_WIFI_DRV_ERROR_CUSTOM_CALL", Integer.valueOf(9));
        this.map.put("CHR_WIFI_HAL_ERROR_CONFIG_READ", Integer.valueOf(10));
        this.map.put("CHR_WIFI_HAL_ERROR_AP_CONFIG_SET_FAT", Integer.valueOf(11));
        this.map.put("CHR_WIFI_HAL_ERROR_SOCKET_FAIL", Integer.valueOf(12));
        this.map.put("CHR_PLAT_DRV_ERROR_FIRMWARE_DOWN", Integer.valueOf(13));
        this.map.put("CHR_PLAT_DRV_ERROR_SDIO_INIT", Integer.valueOf(14));
        this.map.put("CHR_PLAT_DRV_ERROR_OPEN_UART", Integer.valueOf(15));
        this.map.put("CHR_PLAT_DRV_ERROR_BCPU_BOOTUP", Integer.valueOf(16));
        this.map.put("CHR_PLAT_DRV_ERROR_OPEN_WCPU", Integer.valueOf(17));
        this.map.put("CHR_PLAT_DRV_ERROR_WCPU_BOOTUP", Integer.valueOf(18));
        this.map.put("CHR_PLAT_DRV_ERROR_OPEN_THREAD", Integer.valueOf(19));
        this.map.put("CHR_PLAT_DRV_ERROR_CFG_UART", Integer.valueOf(20));
        this.map.put("CHR_PLAT_DRV_ERROR_OPEN_BCPU", Integer.valueOf(21));
        this.map.put("START_HAL_FAILED", Integer.valueOf(22));
        this.map.put("WIFI_SETTING_CLOSED_AND_SERVICE_OPENED", Integer.valueOf(23));
        this.map.put("WIFI_SETTING_OPENED_AND_SERVICE_CLOSED", Integer.valueOf(24));
        this.map.put("NOT_OPEN_FAIL", Integer.valueOf(25));
        setLength(1);
    }
}
