package com.android.server.wifi.wifipro;

public class WifiProApInfoRecord {
    private static final String TAG = "WifiProApInfoRecord";
    public static final int WIFI_SECURITY_TYPE_UNKNOWN = -1;
    public static final String WP_DEAULT_STR = "_NULL_DEF_STR";
    public String apBSSID;
    public String apSSID;
    public int apSecurityType;
    public long connectStartTimeSave;
    public long firstConnectTime;
    public int highSpdFreq;
    public boolean isEnterpriseAP = false;
    public long judgeHomeAPTime;
    public int lanDataSize;
    public long lastConnectTime;
    public int lastRecordDay;
    public int lastRecordHour;
    public int lastRecordMin;
    public long lastRecordRealTime = 0;
    public int lastRecordSec;
    public int totalUseTime;
    public int totalUseTimeAtNight;
    public int totalUseTimeAtWeekend;

    public WifiProApInfoRecord(String bssid, String ssid, int secType) {
        resetAllParameters();
        if (bssid != null) {
            this.apBSSID = bssid;
        }
        if (ssid != null) {
            this.apSSID = ssid;
        }
        this.apSecurityType = secType;
    }

    public WifiProApInfoRecord(WifiProApInfoRecord src) {
        resetAllParameters();
        if (src != null) {
            this.apBSSID = src.apBSSID;
            this.apSSID = src.apSSID;
            this.apSecurityType = src.apSecurityType;
            this.lanDataSize = src.lanDataSize;
            this.highSpdFreq = src.highSpdFreq;
            this.firstConnectTime = src.firstConnectTime;
            this.lastConnectTime = src.lastConnectTime;
            this.totalUseTime = src.totalUseTime;
            this.totalUseTimeAtNight = src.totalUseTimeAtNight;
            this.totalUseTimeAtWeekend = src.totalUseTimeAtWeekend;
            this.connectStartTimeSave = src.connectStartTimeSave;
            this.isEnterpriseAP = src.isEnterpriseAP;
        }
    }

    private void resetAllParameters() {
        this.apBSSID = WP_DEAULT_STR;
        this.apSSID = WP_DEAULT_STR;
        this.apSecurityType = -1;
        this.lanDataSize = 0;
        this.highSpdFreq = 0;
        this.firstConnectTime = 0;
        this.lastConnectTime = 0;
        this.totalUseTime = 0;
        this.totalUseTimeAtNight = 0;
        this.totalUseTimeAtWeekend = 0;
        this.judgeHomeAPTime = 0;
        this.connectStartTimeSave = 0;
        this.isEnterpriseAP = false;
    }

    public String toString() {
        return "apBSSID: *** , apSSID:" + this.apSSID + ", apSecurityType:" + this.apSecurityType + ", firstConnectTime:" + this.firstConnectTime + ", lastConnectTime:" + this.lastConnectTime + ", lanDataSize:" + this.lanDataSize + ", highSpdFreq:" + this.highSpdFreq + ", totalUseTime:" + this.totalUseTime + ", totalUseTimeAtNight:" + this.totalUseTimeAtNight + ", totalUseTimeAtWeekend:" + this.totalUseTimeAtWeekend + ", judgeHomeAPTime:" + this.judgeHomeAPTime;
    }
}
