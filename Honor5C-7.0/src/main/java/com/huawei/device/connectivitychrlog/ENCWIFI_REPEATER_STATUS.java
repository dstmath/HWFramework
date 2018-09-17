package com.huawei.device.connectivitychrlog;

public class ENCWIFI_REPEATER_STATUS extends Cenum {
    public ENCWIFI_REPEATER_STATUS() {
        this.map.put("CLOSED", Integer.valueOf(0));
        this.map.put("OPENED", Integer.valueOf(1));
        this.map.put("WORKING", Integer.valueOf(2));
        setLength(1);
    }
}
