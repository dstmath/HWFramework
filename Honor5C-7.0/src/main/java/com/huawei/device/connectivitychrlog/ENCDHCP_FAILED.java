package com.huawei.device.connectivitychrlog;

public class ENCDHCP_FAILED extends Cenum {
    public ENCDHCP_FAILED() {
        this.map.put("IFC_ENABLE", Integer.valueOf(0));
        this.map.put("IFC_DISABLE", Integer.valueOf(1));
        this.map.put("IFC_RESET_CONNECTIONS", Integer.valueOf(2));
        this.map.put("DHCPCD_TO_START", Integer.valueOf(3));
        this.map.put("DHCP_TO_FINISH", Integer.valueOf(4));
        this.map.put("PROPERTY_WAS_NOT_SET", Integer.valueOf(5));
        this.map.put("RESULT_WAS", Integer.valueOf(6));
        this.map.put("DHCP_RENEW_TO_FINISH", Integer.valueOf(7));
        this.map.put("RENEW_RESULT_PROPERTY_WAS_NOT_SET", Integer.valueOf(8));
        this.map.put("RENEW_RESULT_WAS", Integer.valueOf(9));
        this.map.put("CLIENT_CONFIG_IP_FAILED", Integer.valueOf(10));
        this.map.put("CLIENT_INIT_FAILED", Integer.valueOf(11));
        this.map.put("STOP_ERROR_WLAN", Integer.valueOf(12));
        this.map.put("STOP_ERROR_P2P", Integer.valueOf(13));
        this.map.put("CHR_WIFI_DRV_ERROR_DHCP_TX_FAIL", Integer.valueOf(14));
        setLength(1);
    }
}
