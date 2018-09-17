package com.huawei.device.connectivitychrlog;

public class ENCFailedDesc extends Cenum {
    public ENCFailedDesc() {
        this.map.put("CONNECT_IN_PROGRESS", Integer.valueOf(0));
        this.map.put("AUTH_FAILURE", Integer.valueOf(1));
        this.map.put("BLACKLISTED_BY_AUTO_JOIN", Integer.valueOf(2));
        this.map.put("LOW_VISIBILITY", Integer.valueOf(3));
        this.map.put("NO_INTERNET_ACCESS", Integer.valueOf(4));
        this.map.put("PORTAL_NETWORK", Integer.valueOf(5));
        this.map.put("BLACKLISTED_BY_CONTROLLER", Integer.valueOf(6));
        this.map.put("NO_CANDIDATE_FOUND", Integer.valueOf(7));
        this.map.put("DISALLOW_TO_HANDOVER", Integer.valueOf(8));
        setLength(1);
    }
}
