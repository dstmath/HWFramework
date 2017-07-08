package com.huawei.device.connectivitychrlog;

public class ENCDeviceIDType2 extends Cenum {
    public ENCDeviceIDType2() {
        this.map.put("IMEI", Integer.valueOf(1));
        this.map.put("MEID", Integer.valueOf(2));
        setLength(1);
    }
}
