package com.huawei.device.connectivitychrlog;

public class ENCconnect_type extends Cenum {
    public ENCconnect_type() {
        this.map.put("SCREENON_TO_SUCCESS", Integer.valueOf(0));
        this.map.put("BACK_SERVICEZONE_TO_SUCCESS", Integer.valueOf(1));
        this.map.put("WIFION_TO_SUCCESS", Integer.valueOf(2));
        this.map.put("MANUALCONNECT_TO_SUCCESS", Integer.valueOf(3));
        this.map.put("APKCONNECT_TO_SUCCESS", Integer.valueOf(4));
        setLength(1);
    }
}
