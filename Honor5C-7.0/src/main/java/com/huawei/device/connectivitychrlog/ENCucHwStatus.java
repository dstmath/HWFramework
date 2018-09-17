package com.huawei.device.connectivitychrlog;

public class ENCucHwStatus extends Cenum {
    public ENCucHwStatus() {
        this.map.put("READY", Integer.valueOf(0));
        this.map.put("TIMEOUT", Integer.valueOf(1));
        setLength(1);
    }
}
