package com.android.server.location.gnsschrlog;

public class ENCScreen_Orientation extends Cenum {
    public ENCScreen_Orientation() {
        this.map.put("ORIENTATION_UNKNOWN", Integer.valueOf(0));
        this.map.put("ORIENTATION_PORTRAIT", Integer.valueOf(1));
        this.map.put("ORIENTATION_LANDSCAPE", Integer.valueOf(2));
        setLength(1);
    }
}
