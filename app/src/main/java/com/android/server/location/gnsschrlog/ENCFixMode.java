package com.android.server.location.gnsschrlog;

public class ENCFixMode extends Cenum {
    public ENCFixMode() {
        this.map.put("COLD_START", Integer.valueOf(0));
        this.map.put("WARM_START", Integer.valueOf(1));
        this.map.put("HOT_START", Integer.valueOf(2));
        setLength(1);
    }
}
