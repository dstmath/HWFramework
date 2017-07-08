package com.huawei.device.connectivitychrlog;

public class ENCAC_FailType extends Cenum {
    public ENCAC_FailType() {
        this.map.put("UNKNOWN", Integer.valueOf(0));
        this.map.put("REFUSE", Integer.valueOf(1));
        this.map.put("RESET", Integer.valueOf(2));
        setLength(1);
    }
}
