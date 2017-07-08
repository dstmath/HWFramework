package com.huawei.device.connectivitychrlog;

public class ENCRATType extends Cenum {
    public ENCRATType() {
        this.map.put("UNKOOWN", Integer.valueOf(0));
        this.map.put("RAT_2G", Integer.valueOf(1));
        this.map.put("RAT_3G_TDS", Integer.valueOf(2));
        this.map.put("RAT_3G_CDMA", Integer.valueOf(3));
        this.map.put("RAT_3G_UMTS", Integer.valueOf(4));
        this.map.put("RAT_4G", Integer.valueOf(5));
        setLength(1);
    }
}
