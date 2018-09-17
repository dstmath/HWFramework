package com.android.server.location.gnsschrlog;

public class ENCPosMethod extends Cenum {
    public ENCPosMethod() {
        this.map.put("GPS_POSITION_MODE_STANDALONE", Integer.valueOf(0));
        this.map.put("GPS_POSITION_MODE_MS_BASED", Integer.valueOf(1));
        this.map.put("GPS_POSITION_MODE_MS_ASSISTED", Integer.valueOf(2));
        setLength(1);
    }
}
