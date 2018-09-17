package com.huawei.device.connectivitychrlog;

public class ENCScreenStatus extends Cenum {
    public ENCScreenStatus() {
        this.map.put("OFF", Integer.valueOf(0));
        this.map.put("ON_CHECK", Integer.valueOf(1));
        setLength(1);
    }
}
