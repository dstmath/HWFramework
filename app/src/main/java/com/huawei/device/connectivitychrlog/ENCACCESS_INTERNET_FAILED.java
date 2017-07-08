package com.huawei.device.connectivitychrlog;

public class ENCACCESS_INTERNET_FAILED extends Cenum {
    public ENCACCESS_INTERNET_FAILED() {
        this.map.put("FIRST_CONNECT_INTERNET_FAILED", Integer.valueOf(1));
        this.map.put("ARP_UNREACHABLE", Integer.valueOf(2));
        this.map.put("DNS_RESOLVE_FAILED", Integer.valueOf(3));
        this.map.put("DUPLICATE_GATEWAY", Integer.valueOf(4));
        this.map.put("OTHER", Integer.valueOf(5));
        this.map.put("CHR_WIFI_DRV_ERROR_RX_NO_BUFFER", Integer.valueOf(6));
        this.map.put("CHR_WIFI_DRV_ERROR_RF_OVERHEAT_EXCEPTION", Integer.valueOf(7));
        this.map.put("ONLY_THE_TX_NO_RX", Integer.valueOf(8));
        this.map.put("DNS_PARSE_FAILED", Integer.valueOf(9));
        this.map.put("ARP_REASSOC_OK", Integer.valueOf(10));
        this.map.put("ERROR_PORTAL", Integer.valueOf(11));
        setLength(1);
    }
}
