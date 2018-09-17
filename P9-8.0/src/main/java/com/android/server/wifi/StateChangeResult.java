package com.android.server.wifi;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiSsid;
import com.android.server.wifi.util.StringUtil;

public class StateChangeResult {
    String BSSID;
    int networkId;
    SupplicantState state;
    WifiSsid wifiSsid;

    StateChangeResult(int networkId, WifiSsid wifiSsid, String BSSID, SupplicantState state) {
        this.state = state;
        this.wifiSsid = wifiSsid;
        this.BSSID = BSSID;
        this.networkId = networkId;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" SSID: ").append(this.wifiSsid.toString());
        sb.append(" BSSID: ").append(StringUtil.safeDisplayBssid(this.BSSID));
        sb.append(" nid: ").append(this.networkId);
        sb.append(" state: ").append(this.state);
        return sb.toString();
    }
}
