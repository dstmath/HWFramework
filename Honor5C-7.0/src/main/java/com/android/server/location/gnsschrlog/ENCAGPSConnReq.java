package com.android.server.location.gnsschrlog;

public class ENCAGPSConnReq extends Cenum {
    public ENCAGPSConnReq() {
        this.map.put("SUPL_VALUE", Integer.valueOf(0));
        this.map.put("WIFI_VALUE", Integer.valueOf(1));
        this.map.put("C2K_VALUE", Integer.valueOf(2));
        this.map.put("WWAN_ANY_VALUE", Integer.valueOf(3));
        this.map.put("ANY_VALUE", Integer.valueOf(4));
        setLength(1);
    }
}
