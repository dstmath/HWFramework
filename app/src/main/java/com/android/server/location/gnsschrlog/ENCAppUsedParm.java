package com.android.server.location.gnsschrlog;

public class ENCAppUsedParm extends Cenum {
    public ENCAppUsedParm() {
        this.map.put("GPS", Integer.valueOf(0));
        this.map.put("NETWORK", Integer.valueOf(1));
        setLength(1);
    }
}
