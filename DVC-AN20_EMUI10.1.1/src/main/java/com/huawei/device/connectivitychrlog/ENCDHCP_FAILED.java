package com.huawei.device.connectivitychrlog;

public class ENCDHCP_FAILED extends Cenum {
    public ENCDHCP_FAILED() {
        this.map.put("IFC_ENABLE", 0);
        this.map.put("IFC_DISABLE", 1);
        this.map.put("IFC_RESET_CONNECTIONS", 2);
        this.map.put("DHCPCD_TO_START", 3);
        this.map.put("DHCP_TO_FINISH", 4);
        this.map.put("PROPERTY_WAS_NOT_SET", 5);
        this.map.put("RESULT_WAS", 6);
        this.map.put("DHCP_RENEW_TO_FINISH", 7);
        this.map.put("RENEW_RESULT_PROPERTY_WAS_NOT_SET", 8);
        this.map.put("RENEW_RESULT_WAS", 9);
        this.map.put("CLIENT_CONFIG_IP_FAILED", 10);
        this.map.put("CLIENT_INIT_FAILED", 11);
        this.map.put("STOP_ERROR_WLAN", 12);
        this.map.put("STOP_ERROR_P2P", 13);
        this.map.put("CHR_WIFI_DRV_ERROR_DHCP_TX_FAIL", 14);
        setLength(1);
    }
}
