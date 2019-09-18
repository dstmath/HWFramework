package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import com.android.server.wifi.util.ScanResultUtil;
import java.util.Objects;

public class ScanResultMatchInfo {
    public static final int NETWORK_TYPE_CERT = 4;
    public static final int NETWORK_TYPE_EAP = 3;
    public static final int NETWORK_TYPE_OPEN = 0;
    public static final int NETWORK_TYPE_PSK = 2;
    public static final int NETWORK_TYPE_WEP = 1;
    public String networkSsid;
    public int networkType;

    public static ScanResultMatchInfo fromWifiConfiguration(WifiConfiguration config) {
        ScanResultMatchInfo info = new ScanResultMatchInfo();
        info.networkSsid = config.SSID;
        if (WifiConfigurationUtil.isConfigForPskNetwork(config)) {
            info.networkType = 2;
        } else if (WifiConfigurationUtil.isConfigForEapNetwork(config)) {
            info.networkType = 3;
        } else if (WifiConfigurationUtil.isConfigForWepNetwork(config)) {
            info.networkType = 1;
        } else if (WifiConfigurationUtil.isConfigForCertNetwork(config)) {
            info.networkType = 4;
        } else if (WifiConfigurationUtil.isConfigForOpenNetwork(config)) {
            info.networkType = 0;
        } else {
            throw new IllegalArgumentException("Invalid WifiConfiguration: " + config);
        }
        return info;
    }

    public static ScanResultMatchInfo fromScanResult(ScanResult scanResult) {
        ScanResultMatchInfo info = new ScanResultMatchInfo();
        info.networkSsid = ScanResultUtil.createQuotedSSID(scanResult.SSID);
        if (ScanResultUtil.isScanResultForPskNetwork(scanResult)) {
            info.networkType = 2;
        } else if (ScanResultUtil.isScanResultForEapNetwork(scanResult)) {
            info.networkType = 3;
        } else if (ScanResultUtil.isScanResultForWepNetwork(scanResult)) {
            info.networkType = 1;
        } else if (ScanResultUtil.isScanResultForCertNetwork(scanResult)) {
            info.networkType = 4;
        } else if (ScanResultUtil.isScanResultForOpenNetwork(scanResult)) {
            info.networkType = 0;
        } else {
            throw new IllegalArgumentException("Invalid ScanResult: " + scanResult);
        }
        return info;
    }

    public boolean equals(Object otherObj) {
        boolean z = true;
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof ScanResultMatchInfo)) {
            return false;
        }
        ScanResultMatchInfo other = (ScanResultMatchInfo) otherObj;
        if (!Objects.equals(this.networkSsid, other.networkSsid) || this.networkType != other.networkType) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.networkSsid, Integer.valueOf(this.networkType)});
    }

    public String toString() {
        return "ScanResultMatchInfo: " + this.networkSsid + ", type: " + this.networkType;
    }
}
