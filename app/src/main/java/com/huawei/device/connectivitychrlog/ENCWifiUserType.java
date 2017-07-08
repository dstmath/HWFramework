package com.huawei.device.connectivitychrlog;

public class ENCWifiUserType extends Cenum {
    public ENCWifiUserType() {
        this.map.put("UNKNOWN", Integer.valueOf(0));
        this.map.put("BETA", Integer.valueOf(1));
        this.map.put("COMMERCIAL", Integer.valueOf(2));
        setLength(1);
    }
}
