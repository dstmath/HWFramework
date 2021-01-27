package com.huawei.hwwifiproservice;

import android.common.HwFrameworkFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import com.android.server.wifi.hwUtil.StringUtilEx;

public class WiFiProScoreInfo implements Comparable {
    public static final int BAND_2G = 2412;
    public static final int BAND_5G = 5180;
    public static final int EVALUATE_FAIL = 1;
    public static final int LEVEL_COMMON = 2;
    public static final int LEVEL_GOOD = 3;
    public static final int LEVEL_POOR = 1;
    public static final int LEVEL_UNLEVEL = 0;
    public static final int MODE_BACKGROUND = 1;
    public static final int MODE_CONNECTED = 0;
    public static final int NOINTERNET = 2;
    public static final int NORMAL = 4;
    public static final int PORTAL = 3;
    public static final int REASON_ABANDON = 0;
    public static final int REASON_CONN_FAILURE = 5;
    public static final int REASON_CONN_TIMEOUT = 4;
    public static final int REASON_INTERNET_CHECK_TIMEOUT = 3;
    public static final int REASON_NOINTERNET = 2;
    public static final int REASON_PORTAL = 1;
    private static final String TAG = "WiFi_PRO_ScoreInfo";
    public static final int UNKNOWN = 0;
    public boolean abandon;
    public boolean evaluated;
    public int failCounter;
    public int internetAccessType;
    public boolean invalid;
    public boolean is5GHz;
    public long lastScoreTime;
    public long lastUpdateTime;
    public int networkQosLevel;
    public int networkQosScore;
    public int networkSecurity;
    public int probeMode;
    public int rssi;
    public String ssid;
    public boolean trusted;

    public WiFiProScoreInfo() {
    }

    public WiFiProScoreInfo(ScanResult scanResult) {
        if (scanResult != null) {
            this.rssi = scanResult.level;
            this.ssid = "\"" + scanResult.SSID + "\"";
            this.lastUpdateTime = System.currentTimeMillis();
            this.networkQosLevel = 0;
            this.networkSecurity = -1;
            this.internetAccessType = 0;
            this.networkQosScore = 0;
            this.evaluated = false;
            this.invalid = false;
            this.is5GHz = scanResult.is5GHz();
            this.failCounter = 0;
            this.probeMode = -1;
        }
    }

    public WiFiProScoreInfo(WifiInfo wifiInfo) {
        if (wifiInfo != null) {
            this.rssi = wifiInfo.getRssi();
            this.lastUpdateTime = System.currentTimeMillis();
            this.ssid = wifiInfo.getSSID();
            this.networkQosLevel = 0;
            this.networkSecurity = -1;
            this.internetAccessType = 0;
            this.networkQosScore = 0;
            this.trusted = true;
            this.evaluated = false;
            this.invalid = false;
            this.is5GHz = wifiInfo.is5GHz();
            this.failCounter = 0;
            this.probeMode = -1;
        }
    }

    public static int calculateWiFiLevel(WiFiProScoreInfo properties) {
        int i;
        return (properties == null || properties.abandon || 2 == (i = properties.internetAccessType) || 3 == i) ? 0 : 0;
    }

    public static int calculateWiFiScore(WiFiProScoreInfo properties) {
        int i;
        if (properties == null || properties.abandon || !properties.invalid || 2 == (i = properties.internetAccessType) || 3 == i) {
            return 0;
        }
        int newSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(properties.is5GHz ? 5180 : 2412, properties.rssi);
        int boost5G = 0;
        if (properties.is5GHz) {
            boost5G = 10;
        }
        int boostTrusted = 0;
        if (properties.trusted) {
            boostTrusted = 1;
        }
        return (newSignalLevel * 10) + boost5G + boostTrusted;
    }

    public static int calculateTestWiFiLevel(WiFiProScoreInfo properties) {
        int i;
        if (properties == null || properties.abandon || 2 == (i = properties.internetAccessType) || 3 == i) {
            return 0;
        }
        int newSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(properties.is5GHz ? 5180 : 2412, properties.rssi);
        int boost5G = 0;
        if (properties.is5GHz) {
            boost5G = 1;
        }
        int level = newSignalLevel + boost5G;
        if (level > 2) {
            return 3;
        }
        if (level == 2) {
            return 2;
        }
        return 1;
    }

    public String dump() {
        StringBuffer sb = new StringBuffer();
        sb.append("networkQosLevel: ");
        sb.append(this.networkQosLevel);
        sb.append(", internetAccessType: ");
        sb.append(this.internetAccessType);
        sb.append(", lastScoreTime: ");
        sb.append(WifiproUtils.formatTime(this.lastScoreTime));
        sb.append(", rssi: ");
        sb.append(this.rssi);
        sb.append(", ssid: ");
        sb.append(StringUtilEx.safeDisplaySsid(this.ssid));
        sb.append(", is5GHz: ");
        sb.append(this.is5GHz);
        return sb.toString();
    }

    public int getRssi() {
        return this.rssi;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Integer.valueOf(this.rssi).hashCode();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass() && ((WiFiProScoreInfo) obj).getRssi() == this.rssi) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Comparable
    public int compareTo(Object obj) {
        return Integer.valueOf(this.rssi).compareTo(Integer.valueOf(((WiFiProScoreInfo) obj).getRssi()));
    }
}
