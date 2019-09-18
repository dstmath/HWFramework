package com.huawei.android.wifi.p2p;

import com.huawei.android.util.NoExtAPIException;

public class WifiP2pManagerCustExt {
    public static final int WIFI_P2P_OFF = 0;
    public static final int WIFI_P2P_ON = 1;

    @Deprecated
    public boolean setWifiP2pEnabled(int p2pFlag) {
        throw new NoExtAPIException("method not supported.");
    }

    @Deprecated
    public boolean isWifiP2pEnabled() {
        throw new NoExtAPIException("method not supported.");
    }
}
