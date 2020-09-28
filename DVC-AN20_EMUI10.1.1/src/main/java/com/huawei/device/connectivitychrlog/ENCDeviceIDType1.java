package com.huawei.device.connectivitychrlog;

public class ENCDeviceIDType1 extends Cenum {
    public ENCDeviceIDType1() {
        this.map.put("IMEI", 1);
        this.map.put("MEID", 2);
        setLength(1);
    }
}
