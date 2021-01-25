package com.android.server.wifi;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiSsid;
import com.android.server.wifi.hwUtil.StringUtilEx;

public class StateChangeResult {
    String BSSID;
    int networkId;
    SupplicantState state;
    WifiSsid wifiSsid;

    StateChangeResult(int networkId2, WifiSsid wifiSsid2, String BSSID2, SupplicantState state2) {
        this.state = state2;
        this.wifiSsid = wifiSsid2;
        this.BSSID = BSSID2;
        this.networkId = networkId2;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" SSID: ");
        sb.append(StringUtilEx.safeDisplaySsid(this.wifiSsid.toString()));
        sb.append(" BSSID: ");
        sb.append(StringUtilEx.safeDisplayBssid(this.BSSID));
        sb.append(" nid: ");
        sb.append(this.networkId);
        sb.append(" state: ");
        sb.append(this.state);
        return sb.toString();
    }
}
