package com.huawei.wifi2;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiSsid;
import com.android.server.wifi.hwUtil.StringUtilEx;

public class StateChangeResult {
    String bssid;
    int networkId;
    SupplicantState state;
    WifiSsid wifiSsid;

    StateChangeResult(int networkId2, WifiSsid wifiSsid2, String bssid2, SupplicantState state2) {
        this.state = state2;
        this.wifiSsid = wifiSsid2;
        this.bssid = bssid2;
        this.networkId = networkId2;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" SSID: ");
        sb.append(StringUtilEx.safeDisplaySsid(this.wifiSsid.toString()));
        sb.append(" BSSID: ");
        sb.append(StringUtilEx.safeDisplayBssid(this.bssid));
        sb.append(" nid: ");
        sb.append(this.networkId);
        sb.append(" state: ");
        sb.append(this.state);
        return sb.toString();
    }
}
