package com.android.server.location.gnsschrlog;

public class ENCNetworkStatus extends Cenum {
    public ENCNetworkStatus() {
        this.map.put("NETWORK_TYPE_UNKNOWN", Integer.valueOf(0));
        this.map.put("NETWORK_TYPE_GPRS", Integer.valueOf(1));
        this.map.put("NETWORK_TYPE_EDGE", Integer.valueOf(2));
        this.map.put("NETWORK_TYPE_UMTS", Integer.valueOf(3));
        this.map.put("NETWORK_TYPE_CDMA", Integer.valueOf(4));
        this.map.put("NETWORK_TYPE_EVDO_0", Integer.valueOf(5));
        this.map.put("NETWORK_TYPE_EVDO_A", Integer.valueOf(6));
        this.map.put("NETWORK_TYPE_1xRTT", Integer.valueOf(7));
        this.map.put("NETWORK_TYPE_HSDPA", Integer.valueOf(8));
        this.map.put("NETWORK_TYPE_HSUPA", Integer.valueOf(9));
        this.map.put("NETWORK_TYPE_HSPA", Integer.valueOf(10));
        this.map.put("NETWORK_TYPE_IDEN", Integer.valueOf(11));
        this.map.put("NETWORK_TYPE_EVDO_B", Integer.valueOf(12));
        this.map.put("NETWORK_TYPE_LTE", Integer.valueOf(13));
        this.map.put("NETWORK_TYPE_EHRPD", Integer.valueOf(14));
        this.map.put("NETWORK_TYPE_HSPAP", Integer.valueOf(15));
        this.map.put("NETWORK_TYPE_GSM", Integer.valueOf(16));
        this.map.put("NETWORK_TYPE_TD_SCDMA", Integer.valueOf(17));
        this.map.put("NETWORK_TYPE_IWLAN", Integer.valueOf(18));
        this.map.put("TYPE_WIFI", Integer.valueOf(100));
        setLength(1);
    }
}
