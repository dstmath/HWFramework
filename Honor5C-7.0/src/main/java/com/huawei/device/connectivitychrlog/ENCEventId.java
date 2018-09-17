package com.huawei.device.connectivitychrlog;

import com.huawei.connectivitylog.ConnectivityLogManager;

public class ENCEventId extends Cenum {
    public ENCEventId() {
        this.map.put("WIFI_CONNECT_AUTH_FAILED", Integer.valueOf(82));
        this.map.put("WIFI_CONNECT_ASSOC_FAILED", Integer.valueOf(83));
        this.map.put("WIFI_CONNECT_DHCP_FAILED", Integer.valueOf(84));
        this.map.put("WIFI_ABNORMAL_DISCONNECT", Integer.valueOf(85));
        this.map.put("WIFI_SCAN_FAILED", Integer.valueOf(86));
        this.map.put("WIFI_ACCESS_INTERNET_FAILED", Integer.valueOf(87));
        this.map.put("WIFI_OPEN_CLOSE_FAILED", Integer.valueOf(88));
        this.map.put("WIFI_STATUS_CHANGEDBY_APK", Integer.valueOf(98));
        this.map.put("WIFI_WIFIPRO_UPLOAD", Integer.valueOf(100));
        this.map.put("WIFI_USER_CONNECT", Integer.valueOf(ConnectivityLogManager.WIFI_USER_CONNECT));
        this.map.put("WIFI_ACCESS_WEB_SLOWLY", Integer.valueOf(ConnectivityLogManager.WIFI_ACCESS_WEB_SLOWLY));
        this.map.put("WIFI_POOR_LEVEL", Integer.valueOf(ConnectivityLogManager.WIFI_POOR_LEVEL));
        this.map.put("WIFI_STABILITY_STAT", Integer.valueOf(ConnectivityLogManager.WIFI_STABILITY_STAT));
        this.map.put("WIFI_STABILITY_SSIDSTAT", Integer.valueOf(HwRippleForegroundImpl.RADIUS_DEF));
        this.map.put("WIFI_WORKAROUND_STAT", Integer.valueOf(ConnectivityLogManager.WIFI_WORKAROUND_STAT));
        this.map.put("WIFI_PORTAL_SAMPLES_COLLECTE", Integer.valueOf(ConnectivityLogManager.WIFI_PORTAL_SAMPLES_COLLECTE));
        this.map.put("WIFI_WIFIPRO_STATISTICS_EVENT", Integer.valueOf(ConnectivityLogManager.WIFI_WIFIPRO_STATISTICS_EVENT));
        this.map.put("WIFI_WIFIPRO_EXCEPTION_EVENT", Integer.valueOf(ConnectivityLogManager.WIFI_WIFIPRO_EXCEPTION_EVENT));
        this.map.put("WIFI_PORTAL_AUTH_MSG_COLLECTE", Integer.valueOf(ConnectivityLogManager.WIFI_PORTAL_AUTH_MSG_COLLECTE));
        this.map.put("WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT", Integer.valueOf(ConnectivityLogManager.WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT));
        this.map.put("WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT", Integer.valueOf(ConnectivityLogManager.WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT));
        this.map.put("WIFI_REPEATER_OPEN_OR_CLOSE_FAILED", Integer.valueOf(ConnectivityLogManager.WIFI_REPEATER_OPEN_OR_CLOSE_FAILED));
        this.map.put("WIFI_HAL_DRIVER_DEVICE_EXCEPTION", Integer.valueOf(ConnectivityLogManager.WIFI_HAL_DRIVER_DEVICE_EXCEPTION));
        this.map.put("WIFI_ANTS_SWITCH_FAILED", Integer.valueOf(ConnectivityLogManager.WIFI_ANTS_SWITCH_FAILED));
        this.map.put("WIFI_AP_INFO_COLLECT", Integer.valueOf(ConnectivityLogManager.WIFI_AP_INFO_COLLECT));
        this.map.put("WIFI_CONNECT_EVENT", Integer.valueOf(ConnectivityLogManager.WIFI_CONNECT_EVENT));
        setLength(1);
    }
}
