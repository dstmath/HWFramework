package com.huawei.device.connectivitychrlog;

public class ENCTriggerReason extends Cenum {
    public ENCTriggerReason() {
        this.map.put("TRIGGER_OTHER", Integer.valueOf(0));
        this.map.put("TRIGGER_CONNECT_OTHER_AP", Integer.valueOf(1));
        this.map.put("TRIGGER_USER_FORGET_SSID", Integer.valueOf(2));
        this.map.put("TRIGGER_WIFI_OFF", Integer.valueOf(3));
        this.map.put("TRIGGER_DHCP_SUCCESS", Integer.valueOf(4));
        this.map.put("TRIGGER_COUNT_BEYOND_THRESHOLD", Integer.valueOf(5));
        this.map.put("TRIGGER_APK_CONNECT", Integer.valueOf(6));
        this.map.put("TRIGGER_DISABLE_NETWORK", Integer.valueOf(7));
        setLength(1);
    }
}
