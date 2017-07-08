package com.android.server.wifi.wifipro;

import android.common.HwFrameworkFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

public class WiFiProScoreInfo implements Comparable {
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

    public WiFiProScoreInfo(ScanResult scanResult) {
        if (scanResult != null) {
            this.rssi = scanResult.level;
            this.ssid = "\"" + scanResult.SSID + "\"";
            this.lastUpdateTime = System.currentTimeMillis();
            this.networkQosLevel = REASON_ABANDON;
            this.networkSecurity = -1;
            this.internetAccessType = REASON_ABANDON;
            this.networkQosScore = REASON_ABANDON;
            this.evaluated = false;
            this.invalid = false;
            this.is5GHz = scanResult.is5GHz();
            this.failCounter = REASON_ABANDON;
            this.probeMode = -1;
        }
    }

    public WiFiProScoreInfo(WifiInfo wifiInfo) {
        if (wifiInfo != null) {
            this.rssi = wifiInfo.getRssi();
            this.lastUpdateTime = System.currentTimeMillis();
            this.ssid = wifiInfo.getSSID();
            this.networkQosLevel = REASON_ABANDON;
            this.networkSecurity = -1;
            this.internetAccessType = REASON_ABANDON;
            this.networkQosScore = REASON_ABANDON;
            this.trusted = true;
            this.evaluated = false;
            this.invalid = false;
            this.is5GHz = wifiInfo.is5GHz();
            this.failCounter = REASON_ABANDON;
            this.probeMode = -1;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int calculateWiFiLevel(WiFiProScoreInfo properties) {
        return (properties == null || properties.abandon || REASON_NOINTERNET == properties.internetAccessType || REASON_INTERNET_CHECK_TIMEOUT == properties.internetAccessType) ? REASON_ABANDON : REASON_ABANDON;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int calculateWiFiScore(WiFiProScoreInfo properties) {
        if (properties == null || properties.abandon || !properties.invalid || REASON_NOINTERNET == properties.internetAccessType || REASON_INTERNET_CHECK_TIMEOUT == properties.internetAccessType) {
            return REASON_ABANDON;
        }
        int newSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(properties.rssi);
        int boost_5G = REASON_ABANDON;
        if (properties.is5GHz) {
            boost_5G = 10;
        }
        int boost_trusted = REASON_ABANDON;
        if (properties.trusted) {
            boost_trusted = REASON_PORTAL;
        }
        return ((newSignalLevel * 10) + boost_5G) + boost_trusted;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int calculateTestWiFiLevel(WiFiProScoreInfo properties) {
        if (properties == null || properties.abandon || REASON_NOINTERNET == properties.internetAccessType || REASON_INTERNET_CHECK_TIMEOUT == properties.internetAccessType) {
            return REASON_ABANDON;
        }
        int newSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(properties.rssi);
        int boost_5G = REASON_ABANDON;
        if (properties.is5GHz) {
            boost_5G = REASON_PORTAL;
        }
        int level = newSignalLevel + boost_5G;
        if (level > REASON_NOINTERNET) {
            return REASON_INTERNET_CHECK_TIMEOUT;
        }
        if (level == REASON_NOINTERNET) {
            return REASON_NOINTERNET;
        }
        return REASON_PORTAL;
    }

    public String dump() {
        StringBuffer sb = new StringBuffer();
        sb.append("networkQosLevel: ").append(this.networkQosLevel).append(", internetAccessType: ").append(this.internetAccessType).append(", lastScoreTime: ").append(WifiproUtils.formatTime(this.lastScoreTime)).append(", rssi: ").append(this.rssi).append(", ssid: ").append(this.ssid).append(", is5GHz: ").append(this.is5GHz);
        return sb.toString();
    }

    public int getRssi() {
        return this.rssi;
    }

    public int hashCode() {
        return Integer.valueOf(this.rssi).hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj != null && getClass() == obj.getClass() && ((WiFiProScoreInfo) obj).getRssi() == this.rssi;
    }

    public int compareTo(Object obj) {
        return Integer.valueOf(this.rssi).compareTo(Integer.valueOf(((WiFiProScoreInfo) obj).getRssi()));
    }
}
