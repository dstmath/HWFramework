package com.huawei.device.connectivitychrlog;

public class ENCAutoOpenRootCause extends Cenum {
    public ENCAutoOpenRootCause() {
        this.map.put("UNKNOWN", Integer.valueOf(0));
        this.map.put("PASSWORD_FAILURE", Integer.valueOf(1));
        this.map.put("DHCP_FAILURE", Integer.valueOf(2));
        this.map.put("SERVER_FULL", Integer.valueOf(3));
        this.map.put("ASSOCIATION_REJECT", Integer.valueOf(4));
        this.map.put("DNS_FAILURE", Integer.valueOf(5));
        setLength(1);
    }
}
