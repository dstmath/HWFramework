package com.huawei.device.connectivitychrlog;

public class ENCWifiProSubEvent extends Cenum {
    public ENCWifiProSubEvent() {
        this.map.put("UNKNOWN", Integer.valueOf(0));
        this.map.put("ROVE_OUT_PARAMETER", Integer.valueOf(1));
        this.map.put("PORTALAP_IN_WHITE", Integer.valueOf(2));
        this.map.put("WHITE_MORETHAN_500", Integer.valueOf(3));
        this.map.put("CANT_CONNECT_FOR_LONG", Integer.valueOf(4));
        this.map.put("AUTO_CLOSE_TERMINATION", Integer.valueOf(5));
        this.map.put("SWITCH_PINGPONG", Integer.valueOf(6));
        this.map.put("BG_FAILED_CNT", Integer.valueOf(7));
        this.map.put("BG_NOT_INET_ACTIVE_IOK", Integer.valueOf(8));
        this.map.put("BG_INET_OK_ACTIVE_NOT_OK", Integer.valueOf(9));
        this.map.put("BG_USER_SEL_AP_FISHING_CNT", Integer.valueOf(10));
        this.map.put("BG_CONNT_TIMEOUT_CNT", Integer.valueOf(11));
        this.map.put("BG_DNS_FAIL_CNT", Integer.valueOf(12));
        this.map.put("BG_DHCP_FAIL_CNT", Integer.valueOf(13));
        this.map.put("BG_AUTH_FAIL_CNT", Integer.valueOf(14));
        this.map.put("BG_ASSOC_REJECT_CNT", Integer.valueOf(15));
        this.map.put("NOT_OPEN_AP_REDIRECT", Integer.valueOf(16));
        this.map.put("ACTIVE_CHECK_FAIL", Integer.valueOf(17));
        this.map.put("HOME_AP_INFO", Integer.valueOf(18));
        this.map.put("ENTERPRISE_AP_INFO", Integer.valueOf(19));
        this.map.put("BG_AC_RS_DIFF", Integer.valueOf(20));
        this.map.put("BG_CONN_AP_TIME_LEN", Integer.valueOf(21));
        this.map.put("BG_AC_TIME_LEN", Integer.valueOf(22));
        setLength(1);
    }
}
