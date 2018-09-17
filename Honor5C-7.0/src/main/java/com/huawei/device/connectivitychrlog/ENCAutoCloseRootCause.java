package com.huawei.device.connectivitychrlog;

public class ENCAutoCloseRootCause extends Cenum {
    public ENCAutoCloseRootCause() {
        this.map.put("UNKNOWN", Integer.valueOf(0));
        this.map.put("CONNECT_TO_NEW_AP", Integer.valueOf(1));
        this.map.put("CLOSE_BY_USER", Integer.valueOf(2));
        this.map.put("ENTER_NEW_CELL", Integer.valueOf(3));
        setLength(1);
    }
}
